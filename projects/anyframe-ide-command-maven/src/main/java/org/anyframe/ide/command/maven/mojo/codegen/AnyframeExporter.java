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
package org.anyframe.ide.command.maven.mojo.codegen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.appfuse.tool.AppFuseExporter;
import org.appfuse.tool.StringUtils;
import org.hibernate.tool.hbm2x.Cfg2JavaTool;
import org.hibernate.tool.hbm2x.GenericExporter;
import org.hibernate.tool.hbm2x.TemplateProducer;
import org.hibernate.tool.hbm2x.pojo.POJOClass;
import org.hibernate.util.StringHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;

/**
 * This is an AnyframeExporter class to generate codes based on templates.
 * 
 * @author Matt Raible
 * @author modified by SooYeon Park
 */
public class AnyframeExporter extends AppFuseExporter {

	private List<AnyframeTemplateData> templates = null;
	private AnyframeDBData dbdata = null;
	private String templateType = "default";
	private String packageName = "";
	private String projectType = "web";
	private String templateHome = "";
	private GenericExporter exporter = null;

//	public Cfg2JavaTool getC2j() {
//		return getCfg2JavaTool();
//	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public void setTemplateHome(String templateHome) {
		this.templateHome = templateHome;
	}

	@SuppressWarnings("unchecked")
	public void doStart() {

		// load AnyframeTemplateData
		XStream xstream = new XStream();
		Annotations.configureAliases(xstream, AnyframeTemplateData.class);
		xstream.setMode(XStream.NO_REFERENCES);

		FileInputStream templateConfigFile = null;
		try {
			templateConfigFile = new FileInputStream(templateHome + "/"
					+ templateType + "/source/" + "template.config");
			templates = (java.util.List<AnyframeTemplateData>) xstream
					.fromXML(templateConfigFile);
		} catch (Exception e) {
			log.error("Getting template.config file is skipped. The reason is '"
					+ e.getMessage() + "'.");
		} finally {
			try {
				templateConfigFile.close();
			} catch (IOException e) {
				log.error("Getting template.config file is skipped. The reason is '"
						+ e.getMessage() + "'.");
			}
		}

		String generateCore = getProperties().getProperty("generate-core");
		if (generateCore != null && generateCore.equals("true")) {
			generateCore();
		}

		String generateWeb = getProperties().getProperty("generate-web");
		if (!"true".equals(generateCore) && generateWeb != null
				&& generateWeb.equals("true")) {
			generateWeb();
		}

		if (generateCore == null && generateWeb == null) {
			generateCore();
			generateWeb();
		}
	}

	private void generateCore() {

		String daoFramework = getProperties().getProperty("daoframework");

		for (AnyframeTemplateData template : templates) {

			if (template.getType().equals("service")) {
				if (template.getDao() == null) {
					configureExporter(template.getFtl(), template.getSrc(),
							template.isShare()).start();
				}
				if (template.getDao() != null
						&& daoFramework.equals(template.getDao())) {
					configureExporter(template.getFtl(), template.getSrc(),
							template.isShare()).start();
				}
			}
		}
	}

	private void generateWeb() {
		String packaging = getProperties().getProperty("packaging");
		boolean webProject = packaging != null
				&& packaging.equalsIgnoreCase("war");

		if (!webProject)
			return;

		String webFramework = getProperties().getProperty("webframework");

		for (AnyframeTemplateData template : templates) {
			if (template.getType().equals("web")) {
				if (template.getFramework() != null
						&& template.getFramework().equals(webFramework)) {
					configureExporter(template.getFtl(), template.getSrc(),
							template.isShare()).start();
				} else
					configureExporter(template.getFtl(), template.getSrc(),
							template.isShare()).start();
			}
		}

	}

	private GenericExporter configureExporter(String template, String pattern,
			boolean usePreviousData) {

		// Add custom template path if specified
		String[] templatePaths;
		if (getProperties().getProperty("templatedirectory") != null) {
			templatePaths = new String[getTemplatePaths().length + 1];
			templatePaths[0] = getProperties().getProperty("templatedirectory");
			if (getTemplatePaths().length > 1) {
				for (int i = 1; i < getTemplatePaths().length; i++) {
					templatePaths[i] = getTemplatePaths()[i - 1];
				}
			}
		} else {
			templatePaths = getTemplatePaths();
		}

		GenericExporter exporter = new GenericExporter(getConfiguration(),
				getOutputDirectory()) {

			@Override
			public void start() {
				setTemplateHelper(new AnyframeTemplateHelper());
				setupTemplates();
				setupContext();
				doStart();
				cleanUpContext();
				setTemplateHelper(null);
				getArtifactCollector().formatFiles();
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void exportPOJO(Map additionalContext, POJOClass element) {

				if (element.getShortName().equals(
						System.getProperty("appfuse.entity"))) {
					TemplateProducer producer = new AnyframeTemplateProducer(
							getTemplateHelper(), getArtifactCollector());
					additionalContext.put("pojo", element);
					additionalContext
							.put("clazz", element.getDecoratedObject());
					String filename = resolveFilename(element);
					if (filename.endsWith(".java")
							&& filename.indexOf('$') >= 0) {
						log.warn("Filename for "
								+ getClassNameForFile(element)
								+ " contains a $. Innerclass generation is not supported.");
					}

					producer.produce(additionalContext, getTemplateName(),
							new File(getOutputDirectory(), filename),
							getTemplateName(), element.toString());
				}
			}

			@Override
			protected String resolveFilename(POJOClass element) {
				String filename = super.resolveFilename(element);
				String packageLocation = StringHelper.replace(getProperties()
						.getProperty("package"), ".", "/");
				filename = StringHelper.replace(filename, "{basepkg-name}",
						packageLocation);
				filename = StringHelper.replace(filename, "{class-name-lower}",
						getLowerClassName(element));
				return filename;
			}

			private String getLowerClassName(POJOClass element) {
				String className = getClassNameForFile(element);
				if (className.indexOf(".") != -1) {
					className = className
							.substring(className.lastIndexOf(".") + 1);
				}
				return className.substring(0, 1).toLowerCase()
						+ className.substring(1);
			}
		};
		exporter.setProperties((Properties) getProperties().clone());
		exporter.setTemplatePath(templatePaths);
		exporter.setTemplateName(template);
		exporter.setFilePattern(pattern);
		exporter.setForEach("entity,component");

		exporter.setArtifactCollector(getArtifactCollector());
		exporter.getProperties().put("data", new AnyframeDataHelper());
		exporter.getProperties().put("util", new StringUtils());
		exporter.getProperties().put("package", this.packageName);
		String testConfigXml = "classpath:/spring/**/context-*.xml";
		exporter.getProperties().put("testconfigxml", testConfigXml);

		if (usePreviousData) {
			exporter.getProperties().put("dbdata", this.dbdata);
		} else {
			AnyframeDBData dbdata = new AnyframeDBData(getCfg2JavaTool(),
					getCfg2HbmTool(), exporter);
			exporter.getProperties().put("dbdata", dbdata);
			this.dbdata = dbdata;
		}
		this.exporter = exporter;
		return exporter;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}
	
	public GenericExporter getGenericExporter(){
		return exporter;
	}

}
