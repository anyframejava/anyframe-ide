/*   
 * Copyright 2008-2012 the original author or authors.   
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");   
 * you may not use this file except in compliance with the License.   
 * You may obtain a copy of the License at   
 *   
 *      http://www.apache.org/licenses/LICENSE-2.0   
 *   
 * Unless required by applicable law or agreed to in writing, software   
 * distributed under the License is distributed on an "AS IS" BASIS,   
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   
 * See the License for the specific language governing permissions and   
 * limitations under the License.   
 */
package org.anyframe.ide.eclipse.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.eclipse.core.runtime.IStatus;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * This is a HudsonRemoteAPI class.
 * @author Soungmin Joo
 */
public class HudsonRemoteAPI {

    private VelocityEngine velocity;

    private String hudsonURL;

    private Element jobConfigElement;

    public HudsonRemoteAPI() {
        try {
            velocity = new VelocityEngine();
            velocity
                .setProperty("file.resource.loader.class",
                    "org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader");
            velocity.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
            
            velocity.init();

        } catch (Exception e) {
            ExceptionUtil.showException("Fail to make HudsonRemoteAPI.",
                IStatus.ERROR, e);
            velocity = null;
        }
    }

    public void setHudsonURL(String hudsonURL) {
        if (!hudsonURL.endsWith("/")) {
            hudsonURL += "/";
        }
        this.hudsonURL = hudsonURL;
    }

    @SuppressWarnings("unchecked")
    public List<Element> getJobList() throws JDOMException, IOException {

        URL url = new URL(hudsonURL + "api/xml");
        SAXBuilder builder = new SAXBuilder(false);
        Document dom = builder.build(url);

        return dom.getRootElement().getChildren("job");
    }

    public Element getJobDetail(String jobName) throws JDOMException,
            IOException {

        if (!jobName.endsWith("/")) {
            jobName += "/";
        }

        URL url = new URL(hudsonURL + "job/" + jobName + "api/xml");
        SAXBuilder builder = new SAXBuilder(false);

        return builder.build(url).getRootElement();
    }

    public Element getHudsonConfig() throws JDOMException, IOException {

        URL url = new URL(hudsonURL + "anyframe/api?service=getHudsonConfig");
        SAXBuilder builder = new SAXBuilder(false);

        return builder.build(url).getRootElement();
    }

    public void saveHudsonConfig(String antHome, String mavenHome,
            String hudsonLinkInEmail) throws Exception {

        PostMethod method =
            new PostMethod(hudsonURL
                + "anyframe/api?service=saveHudsonConfig&antHome=" + antHome
                + "&mavenHome=" + mavenHome + "&hudsonURL=" + hudsonLinkInEmail);
        try {
            HttpClient httpClient = new HttpClient();
            int statusCode = httpClient.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK
                && statusCode != HttpStatus.SC_MOVED_TEMPORARILY) {
                throw new Exception(method.getResponseBodyAsString());
            }

            sendReloadMessage();

        } catch (Exception e) {
            throw e;

        } finally {
            method.releaseConnection();
        }
    }

    private void sendReloadMessage() throws Exception {
        GetMethod method = new GetMethod(hudsonURL + "reload");
        HttpClient httpClient = new HttpClient();
        httpClient.executeMethod(method);
    }

    public Element getJobConfigXml(String jobName) throws JDOMException,
            IOException {

        URL url =
            new URL(hudsonURL + "anyframe/api?service=getJobConfig&jobName="
                + jobName);
        SAXBuilder builder = new SAXBuilder(false);

        return builder.build(url).getRootElement();
    }

    public void createJob(String jobName, String type, Context context)
            throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, "UTF-8");

        String configXmlTemplate = "template/ctip/build/config.vm";
        
        if(type.equals("onlybuild")){
            configXmlTemplate = "template/ctip/build/onlybuildconfig.vm";
        }else if(!type.equals("build")) {
            configXmlTemplate = "template/ctip/report/config.vm";
        }

        velocity.mergeTemplate(configXmlTemplate, "UTF-8", context, writer);
        writer.flush();
        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());

        PostMethod method =
            new PostMethod(hudsonURL + "createItem?name=" + jobName);
        sendConfigXmlDataToServer(method, bais);
    }

    public void updateJob(String jobName, Context context) throws Exception {
        jobConfigElement = getJobConfigXml(jobName);
        changeXmlElementWithUserInput(context);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, "UTF-8");

        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\r\n");
        outputter.setFormat(format);
        outputter.output(jobConfigElement, writer);
        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());

        PostMethod method =
            new PostMethod(hudsonURL
                + "anyframe/api?service=saveJobConfig&jobName=" + jobName);
        sendConfigXmlDataToServer(method, bais);

        sendReloadMessage();
    }

    private void changeXmlElementWithUserInput(Context context) {
        changeWorkspaceElement((String) context.get("customWorkspace"));
        changeScmElement((String) context.get("scmType"), (String) context
            .get("scmUrl"));
        changeTriggerElement((String) context.get("triggerSchedule"));
        changeChlidProjectElement((String) context.get("childProject"));
    }

    private void changeChlidProjectElement(String childProjectText) {
        Element childProject =
            jobConfigElement.getChild("publishers").getChild(
                "hudson.tasks.BuildTrigger");
        if (childProject == null) {
            if (!childProjectText.equals("")) {
                Element child =
                    createXmlElement("hudson.tasks.BuildTrigger/childProjects",
                        childProjectText);
                jobConfigElement.getChild("publishers").addContent(child);
            }
        } else {
            childProject.getChild("childProjects").setText(childProjectText);
        }
    }

    private void changeTriggerElement(String scheduleText) {
        Element trigger =
            jobConfigElement.getChild("triggers").getChild(
                "hudson.triggers.SCMTrigger");
        if (trigger == null) {
            if (!scheduleText.equals("")) {
                Element child =
                    createXmlElement("hudson.triggers.SCMTrigger/spec",
                        scheduleText);
                jobConfigElement.getChild("triggers").addContent(child);
            }

        } else {
            trigger.getChild("spec").setText(scheduleText);
        }
    }

    private void changeWorkspaceElement(String workspaceTex) {
        if (jobConfigElement.getChild("customWorkspace") == null) {
            Element child = createXmlElement("customWorkspace", workspaceTex);
            jobConfigElement.addContent(child);

        } else {
            jobConfigElement.getChild("customWorkspace").setText(workspaceTex);
        }
    }

    private void changeScmElement(String scmType, String scmUrlText) {
        Element scm = jobConfigElement.getChild("scm");
        scm.removeContent();
        if (scmType.equals("subversion")) {
            scm.setAttribute("class", "hudson.scm.SubversionSCM");
            Element child =
                createXmlElement(
                    "locations/hudson.scm.SubversionSCM_-ModuleLocation/remote",
                    scmUrlText);
            scm.addContent(child);
            scm.addContent(createXmlElement("useUpdate", "true"));
            scm.addContent(createXmlElement("doRevert", "false"));
            scm.addContent(createXmlElement("excludedRegions", ""));
            scm.addContent(createXmlElement("includedRegions", ""));
            scm.addContent(createXmlElement("excludedUsers", ""));
            scm.addContent(createXmlElement("excludedRevprop", ""));
            scm.addContent(createXmlElement("excludedCommitMessages", ""));

        } else if (scmType.equals("cvs")) {
            scm.setAttribute("class", "hudson.scm.CVSSCM");
            scm.addContent(createXmlElement("cvsroot", scmUrlText));
            scm.addContent(createXmlElement("module", ""));
            scm.addContent(createXmlElement("canUseUpdate", "true"));
            scm.addContent(createXmlElement("flatten", "true"));
            scm.addContent(createXmlElement("isTag", "false"));
            scm.addContent(createXmlElement("excludedRegions", ""));

        } else {
            scm.setAttribute("class", "hudson.scm.NullSCM");
        }
    }

    private void sendConfigXmlDataToServer(PostMethod method,
            ByteArrayInputStream bais) throws Exception {

        try {
            method.setRequestHeader("Content-type", "text/xml");
            method.setRequestEntity(new InputStreamRequestEntity(bais, bais
                .available()));

            HttpClient httpClient = new HttpClient();
            int statusCode = httpClient.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception(method.getResponseBodyAsString());
            }

        } catch (Exception e) {
            throw e;

        } finally {
            method.releaseConnection();
        }
    }

    public void executeBuild(String jobName) throws Exception {

        GetMethod method =
            new GetMethod(hudsonURL + "job/" + jobName + "/build");
        try {
            HttpClient httpClient = new HttpClient();
            int statusCode = httpClient.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception(method.getResponseBodyAsString());
            }

        } catch (Exception e) {
            throw e;

        } finally {
            method.releaseConnection();
        }
    }

    public void deleteJob(String jobName) throws Exception {

        PostMethod method =
            new PostMethod(hudsonURL + "job/" + jobName + "/doDelete");
        try {
            HttpClient httpClient = new HttpClient();
            int statusCode = httpClient.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK
                && statusCode != HttpStatus.SC_MOVED_TEMPORARILY) {
                throw new Exception(method.getResponseBodyAsString());
            }

        } catch (Exception e) {
            throw e;

        } finally {
            method.releaseConnection();
        }
    }

    public String getCustomWorkspace(Element jobConfig) {
        if (jobConfig.getChildText("customWorkspace") == null) {
            return "";
        }
        return jobConfig.getChildText("customWorkspace");
    }

    public int getScmTypeMapping(Element jobConfig) {
        try {
            String scmClass =
                jobConfig.getChild("scm").getAttribute("class").getValue();
            if (scmClass.equals("hudson.scm.SubversionSCM")) {
                return 0;
            } else if (scmClass.equals("hudson.scm.CVSSCM")) {
                return 1;
            }

        } catch (Exception e) {
            ExceptionUtil.showException("Fail to get scm type.", IStatus.ERROR,
                e);
        }
        return 2;
    }

    public String getScmURL(Element jobConfig) {
        try {
            String ret = null;
            int type = getScmTypeMapping(jobConfig);
            if (type == 0) {
                ret =
                    jobConfig.getChild("scm").getChild("locations").getChild(
                        "hudson.scm.SubversionSCM_-ModuleLocation")
                        .getChildText("remote");

            } else if (type == 1) {
                ret = jobConfig.getChild("scm").getChildText("cvsroot");
            }
            if (ret == null) {
                ret = "";
            }
            return ret;

        } catch (Exception e) {
            ExceptionUtil.showException("Fail to get scm url.", IStatus.ERROR,
                e);
        }

        return "";
    }

    @SuppressWarnings("unchecked")
    public String getSchedule(Element jobConfig) {
        try {
            List triggers = jobConfig.getChild("triggers").getChildren();
            if (triggers.size() > 0) {
                Element elem = (Element) triggers.get(0);
                if (elem.getChildText("spec") == null) {
                    return "";
                }
                return elem.getChildText("spec");
            }

        } catch (Exception e) {
            ExceptionUtil.showException("Fail to get schedule.", IStatus.ERROR,
                e);
        }
        return "";
    }

    public String getChildProject(Element jobConfig) {
        try {
            if (jobConfig.getChild("publishers").getChild(
                "hudson.tasks.BuildTrigger") == null) {
                return "";
            }
            return jobConfig.getChild("publishers").getChild(
                "hudson.tasks.BuildTrigger").getChildText("childProjects");

        } catch (Exception e) {
            ExceptionUtil.showException("Fail to get child project.",
                IStatus.ERROR, e);
        }

        return "";
    }

    private Element createXmlElement(String path, String value) {
        Element top = null;
        Element parent = null;

        StringTokenizer st = new StringTokenizer(path, "/");
        while (st.hasMoreElements()) {
            String token = (String) st.nextElement();
            Element child = new Element(token);
            if (!st.hasMoreTokens()) {
                child.setText(value);
            }
            if (top == null) {
                top = child;
                parent = child;
            } else {
                parent.addContent(child);
                parent = child;
            }
        }

        return top;
    }
}
