/*   
 * Copyright 2002-2009 the original author or authors.   
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
package org.anyframe.ide.eclipse.hudson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * This is an HudsonGenServlet class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class HudsonGenServlet extends HttpServlet {
	private static final long serialVersionUID = -2156795557555304816L;
	private String hudsonHome;
	private String hudsonJobDir;

	private String antHomeXPath;
	private String mavenHomeXPath;
	private String hudsonUrlXPath;
	
	private File antXmlFile;
	private File mavenXmlFile;
	private File mailerXmlFile;

	/**
	 * Initialize member fields about file path, etc.
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		hudsonHome = config.getInitParameter("hudsonHome");
		hudsonJobDir = config.getInitParameter("hudsonJobDir");

		antHomeXPath   = "//hudson.tasks.Ant_-DescriptorImpl/installations/hudson.tasks.Ant_-AntInstallation/home";
		mavenHomeXPath = "//hudson.tasks.Maven_-DescriptorImpl/installations/hudson.tasks.Maven_-MavenInstallation/home";
		hudsonUrlXPath = "//hudson.tasks.Mailer_-DescriptorImpl/hudsonUrl";
		
		antXmlFile    = new File(hudsonHome + System.getProperty("file.separator") + "hudson.tasks.Ant.xml");
		mavenXmlFile  = new File(hudsonHome + System.getProperty("file.separator") + "hudson.tasks.Maven.xml");
		mailerXmlFile = new File(hudsonHome + System.getProperty("file.separator") + "hudson.tasks.Mailer.xml");
	}
	
	/**
	 * Redirect to doPost() method.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException ,IOException {
		doPost(request, response);
	};
	
	/**
	 * Receive request and process server job.
	 * Read/write hudson config xml file and project config xml file. 
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String service = request.getParameter("service");
		if(service == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		try {
			if(service.equals("getHudsonConfig")) {
				getHudsonConfig(response);
				
			}else if(service.equals("saveHudsonConfig")) {
				saveHudsonConfig(request, response);
				
			}else if(service.equals("getJobConfig")) {
				getJobConfig(request, response);
				
			}else if(service.equals("saveJobConfig")) {
				saveJobConfig(request, response);
				
			}else{
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
	}

	/**
	 * Read hudson.tasks.Ant.xml file and hudson.tasks.Mailer.xml file,
	 * then send the contents of the files.
	 * 
	 * @param response
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void getHudsonConfig(HttpServletResponse response) throws DocumentException, IOException {
		
		Node antHome = getDocument(antXmlFile).selectSingleNode(antHomeXPath);
		String antHomeStr = "";
		if(antHome != null) {
			antHomeStr = antHome.getText();
		}
		Node mavenHome = getDocument(mavenXmlFile).selectSingleNode(mavenHomeXPath);
		Node hudsonUrl = getDocument(mailerXmlFile).selectSingleNode(hudsonUrlXPath);
		String mavenHomeStr = "";
		if(mavenHome != null) {
			mavenHomeStr = mavenHome.getText();
		}
		
		response.setContentType("text/xml");
		String hudsonConfigXml = "<hudsongen><antHome>"+antHomeStr+"</antHome><mavenHome>"+mavenHomeStr+"</mavenHome><hudsonURL>"+hudsonUrl.getText()+"</hudsonURL></hudsongen>";
		response.getOutputStream().write(hudsonConfigXml.getBytes());
		response.getOutputStream().flush();
	}

	/**
	 * Change hudson.tasks.Ant.xml file and hudson.tasks.Mailer.xml file
	 * with parameter value sent from client
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void saveHudsonConfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String antHome = request.getParameter("antHome");
		String mavenHome = request.getParameter("mavenHome");
		String hudsonURL = request.getParameter("hudsonURL");
		
		if(antHome == null || antHome.trim().equals("") || hudsonURL == null || hudsonURL.trim().equals("")) {
			throw new Exception("Invalid request parameter : antHome="+antHome+", hudsonUrl="+hudsonURL);
		}
		
		if(!antHome.equals("KEEP_AS_IS")) {
			Document antDoc = getDocument(antXmlFile);
			if(antDoc.selectSingleNode(antHomeXPath) == null) {
				addElementToDocument(antDoc, antHomeXPath);
				antDoc.selectSingleNode(antHomeXPath).getParent().addElement("name").setText("ANYFRAME_ANT");
			}
			antDoc.selectSingleNode(antHomeXPath).setText(antHome);
			saveXmlFile(antDoc, antXmlFile);
		}
		
		if(!mavenHome.equals("KEEP_AS_IS")) {
			Document mavenDoc = getDocument(mavenXmlFile);
			if(mavenDoc.selectSingleNode(mavenHomeXPath) == null) {
				addElementToDocument(mavenDoc, mavenHomeXPath);
				mavenDoc.selectSingleNode(mavenHomeXPath).setText(mavenHome);
				mavenDoc.selectSingleNode(mavenHomeXPath).getParent().addElement("name").setText("ANYFRAME_MAVEN");
			}
			mavenDoc.selectSingleNode(mavenHomeXPath).setText(mavenHome);
			saveXmlFile(mavenDoc, mavenXmlFile);
		}
		
		Document mailerDoc = getDocument(mailerXmlFile);
		mailerDoc.selectSingleNode(hudsonUrlXPath).setText(hudsonURL);
		saveXmlFile(mailerDoc, mailerXmlFile);
		
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	private void addElementToDocument(Document antDoc, String xPath) throws Exception {
		if(!xPath.startsWith("//")) {
			throw new Exception("Invalid xPath for addElementToDocument.");
		}
		
		Element node = antDoc.getRootElement();
		StringTokenizer st = new StringTokenizer(xPath.substring(2), "/");
		st.nextToken();
		while (st.hasMoreTokens()) {
			String token = (String) st.nextToken();
			Element newNode = node.element(token); 
			if(newNode == null) {
				newNode = node.addElement(token);
			}
			node = newNode;
		}
	}

	/**
	 * Read job config.xml file and send the contents to the client.
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void getJobConfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String jobName = request.getParameter("jobName");
		
		if(jobName == null || jobName.trim().equals("")) {
			throw new Exception("Invalid request parameter : jobName="+jobName);
		}
		
		File jobConfig = new File(hudsonJobDir + System.getProperty("file.separator") + jobName + System.getProperty("file.separator") + "config.xml");
		Document jobConfigDoc = getDocument(jobConfig);
		
		response.setContentType("text/xml");
		XMLWriter writer = new XMLWriter(response.getWriter());
        writer.write(jobConfigDoc);
		
        response.getWriter().flush();
	}

	/**
	 * Change job config.xml file with parameter value sent from client
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void saveJobConfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String jobName = request.getParameter("jobName");
		
		SAXReader reader = new SAXReader();
		Document doc = reader.read(request.getInputStream());
		
		File jobConfig = new File(hudsonJobDir + System.getProperty("file.separator") + jobName + System.getProperty("file.separator") + "config.xml");
		saveXmlFile(doc, jobConfig);
	}

	/**
	 * Save org.dom4j.Document object as File outfile.
	 * 
	 * @param doc : org.dom4j.Document object to save
	 * @param outfile : File object indicating target file
	 * @throws Exception
	 */
	private void saveXmlFile(Document doc, File outfile) throws Exception {
		FileWriter fw = null;
		try {
			fw = new FileWriter(outfile);
			XMLWriter writer = new XMLWriter(fw);
	        writer.write(doc);
	        
		} catch (Exception e) {
			throw e;
			
		} finally {
			if(fw != null) try { fw.close(); } catch (Exception ex) { ex.printStackTrace(); }
		}
	}

	/**
	 * Read xmlFileName file and make org.dom4j.Document object, then return.
	 * 
	 * @param xmlFileName : File object represent file location to read
	 * @return : org.dom4j.Document object
	 * @throws DocumentException
	 */
	private Document getDocument(File xmlFileName) throws DocumentException {
		SAXReader reader = new SAXReader();
		return reader.read(xmlFileName);
	}
	
}
