/*   
 * Copyright 2008-2011 the original author or authors.   
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.anyframe.ide.command.common.util.AntUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.Project;
import org.hibernate.util.StringHelper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * This is an ArtifactInstaller class to copy generated codes to destination
 * folder.
 * 
 * @author Matt Raible
 * @author modified by SooYeon Park
 */
public class ArtifactInstaller {
	private Log log;
	private HashMap<String, String> mergeFilePath;
	private HashMap<String, String> singleFilePath;
	private HashMap<String, String> singleFilePathForMerge;

	private String templateHome;
	private Project antProject;

	public void setLog(Log log) {
		this.log = log;
	}

	public Project getAntProject() {
		return antProject;
	}

	public void setAntProject(Project antProject) {
		this.antProject = antProject;
	}

	private String pjtName;

	public String getPjtName() {
		return pjtName;
	}

	public void setPjtName(String pjtName) {
		this.pjtName = pjtName;
	}

	private String pojoName;

	public String getPojoName() {
		return pojoName;
	}

	public void setPojoName(String pojoName) {
		this.pojoName = pojoName;
	}

	private String pojoNameLower;

	public String getPojoNameLower() {
		return pojoNameLower;
	}

	public void setPojoNameLower(String pojoNameLower) {
		this.pojoNameLower = pojoNameLower;
	}

	private String modelpackage = "";

	public String getModelpackage() {
		return modelpackage;
	}

	public void setModelpackage(String modelpackage) {
		this.modelpackage = modelpackage;
	}

	private String destinationDirectory;

	public String getDestinationDirectory() {
		return destinationDirectory;
	}

	public void setDestinationDirectory(String destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
	}

	private String sourceDirectory;

	public String getSourceDirectory() {
		return sourceDirectory;
	}

	public void setSourceDirectory(String sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	private MavenProject project;

	public MavenProject getProject() {
		return project;
	}

	private boolean genericCore;

	public boolean isGenericCore() {
		return genericCore;
	}

	// String webSourceDirectory;
	private String webDestinationDirectory;

	public String getWebDestinationDirectory() {
		return webDestinationDirectory;
	}

	private String mainPjtDirectory;

	public String getMainPjtDirectory() {
		return mainPjtDirectory;
	}

	private String domainPjtDirectory;

	public String getDomainPjtDirectory() {
		return domainPjtDirectory;
	}

	public ArtifactInstaller(MavenProject project, String modelpackage,
			String pojoName, String sourceDirectory,
			String destinationDirectory, boolean genericCore,
			String templateHome) throws JDOMException, IOException {
		this.project = project;
		this.pjtName = project.getProperties().getProperty("pjt.name");
		this.pojoName = pojoName;
		this.pojoNameLower = pojoLowerCase(pojoName);
		this.modelpackage = modelpackage;
		this.sourceDirectory = sourceDirectory;
		this.destinationDirectory = destinationDirectory;
		this.genericCore = genericCore;
		this.templateHome = templateHome;
		// this.antProject = AntUtils.createProject();
		this.antProject = new Project();
		antProject.init();
		loadMergeFilePath();
	}

	private void loadMergeFilePath() throws JDOMException, IOException {
		mergeFilePath = new HashMap<String, String>();
		singleFilePath = new HashMap<String, String>();
		singleFilePathForMerge = new HashMap<String, String>();

		SAXBuilder reader = new SAXBuilder();

		String templateType = project.getProperties().getProperty(
				"template.type");

		try {
			Document docDefault = reader.build(new File(templateHome + "/"
					+ templateType + "/source/" + "template.config"));
			singleFilePath.put(
					"dynamic-hibernate.xml",
					getSingleFilePath(docDefault,
							"dao/hibernate/dynamic-hibernate.ftl"));
			singleFilePath
					.put("mapping-query.xml",
							getSingleFilePath(docDefault,
									"dao/query/mapping-query.ftl"));
			singleFilePath.put(
					"mapping-ibatis2.xml",
					getSingleFilePath(docDefault,
							"dao/ibatis2/mapping-ibatis2.ftl"));
			singleFilePath
					.put("mapping-query-miplatform.xml",
							getSingleFilePath(docDefault,
									"dao/query/mapping-query.ftl"));
			singleFilePath.put("grid_list.xml",
					getSingleFilePath(docDefault, "web/mip-grid-list.ftl"));

		} catch (Exception e) {
			log.error("Configuration file path handling is skipped in loadMergeFilePath(). The reason is '"
					+ e.getMessage() + "'.");
		}

		try {
			Document docDefault = reader.build(new File(templateHome + "/"
					+ templateType + "/source/" + "template.config"));
			singleFilePathForMerge.put("sample-data.xml",
					getSingleFilePath(docDefault, "dao/sample-data.ftl"));
			singleFilePathForMerge.put(
					"message-generation.properties",
					getSingleFilePath(docDefault,
							"web/ApplicationResources.ftl"));
			singleFilePathForMerge.put("left-gen.jsp",
					getSingleFilePath(docDefault, "web/menu.ftl"));
			singleFilePathForMerge.put("tilesviews.xml",
					getSingleFilePath(docDefault, "web/tiles-menu.ftl"));
			singleFilePathForMerge.put(
					"mip-query-generation-servlet.xml",
					getSingleFilePath(docDefault,
							"web/spring/controller-beans.ftl"));

		} catch (Exception e) {
			log.error("Configuration file path handling is skipped in loadMergeFilePath(). The reason is '"
					+ e.getMessage() + "'.");
		}

		try {
			Document docDefault = reader.build(new File(templateHome + "/"
					+ templateType + "/source/" + "template.config"));
			mergeFilePath.put("sample-data.xml",
					getMergeFilePath(docDefault, "dao/sample-data.ftl"));
			mergeFilePath
					.put("message-generation.properties",
							getMergeFilePath(docDefault,
									"web/ApplicationResources.ftl"));
			mergeFilePath.put("left-gen.jsp",
					getMergeFilePath(docDefault, "web/menu.ftl"));
			mergeFilePath.put("tilesviews.xml",
					getMergeFilePath(docDefault, "web/tiles-menu.ftl"));

		} catch (Exception e) {
			log.error("Target Configuration file path handling for merging is skipped in loadMergeFilePath(). The reason is '"
					+ e.getMessage() + "'.");
		}

		try {
			Document docMiplatform = reader.build(new File(templateHome + "/"
					+ templateType + "/source/" + "template.config"));
			mergeFilePath.put(
					"mip-query-generation-servlet.xml",
					getMergeFilePath(docMiplatform,
							"web/spring/controller-beans.ftl"));

		} catch (Exception e) {
			log.error("Target Configuration file path handling for merging is skipped in loadMergeFilePath(). The reason is '"
					+ e.getMessage() + "'.");
		}
	}

	private String getSingleFilePath(Document docDefault, String ftlName)
			throws Exception {
		String ret = "";
		List<Element> children = docDefault.getRootElement().getChildren(
				"template");
		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			if (ftlName.equals(child.getChildText("ftl"))) {
				ret = child.getChildText("src");
				if (ret.indexOf("{basepkg-name}") != -1) {
					throw new Exception(
							"Variable {basepkg-name} is not allowed to use. Check tagname <"
									+ child.getName() + ">");
				}
				ret = StringHelper.replace(ret, "{class-name}", pojoName);
				ret = StringHelper.replace(ret, "{class-name-lower}",
						pojoNameLower);
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private String getMergeFilePath(Document docDefault, String ftlName) {
		String ret = "";
		List<Element> children = docDefault.getRootElement().getChildren(
				"template");
		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			if (ftlName.equals(child.getChildText("ftl"))) {
				ret = child.getChildText("mergeSrc");
			}
		}
		return ret;
	}

	public void setMainPjtDirectory(String mainPjtDirectory) {
		this.mainPjtDirectory = mainPjtDirectory;
	}

	public void setDomainPjtDirectory(String domainPjtDirectory) {
		this.domainPjtDirectory = domainPjtDirectory;
	}

	public void setWebDestinationDirectory(String webDestinationDirectory) {
		this.webDestinationDirectory = webDestinationDirectory;
	}

	public void execute() {
		copyGeneratedObjects(this.sourceDirectory, this.destinationDirectory,
				"**/dao/**/*.java");
		copyGeneratedObjects(this.sourceDirectory, this.destinationDirectory,
				"**/service/**/*.java");

		log.info("Installing sample data for DbUnit...");
		installSampleData();
	}

	public void executeConf() {
		// install dao and service if jar
		// (modular/core) or war w/o parent (basic)
		log.info("Installing Spring bean definitions...");

		// only installs if Hibernate is configured as
		// dao.framework
		installHibernateFiles();

		// only installs if Query is configured as
		// dao.framework
		installQueryFiles();

		// only installs if Query is configured as
		// dao.framework
		installIBatis2Files();

		// only add if hibernate is configured as
		// dao.framework
		addEntityToSpringHibernateXml();

		installInternationalizationKeys();
	}

	private void addEntityToSpringHibernateXml() {
		if (project.getProperties()
				.getProperty(CommonConstants.APP_DAOFRAMEWORK_TYPE)
				.equals(CommonConstants.DAO_HIBERNATE)) {
			String className = this.modelpackage + "." + pojoName;

			File existingFile = new File(this.domainPjtDirectory
					+ "/src/main/resources/spring/context-hibernate.xml");

			try {
				FileUtil.replaceStringXMLFilePretty(existingFile,
						"<!--Add new Entities here-->", "<value>" + className
								+ "</value>");

				log.info("Adding '" + pojoName
						+ "' to spring hibernate xml(context-hibernate.xml)...");
			} catch (Exception e) {
				if (project.getProperties()
						.getProperty(CommonConstants.APP_DAOFRAMEWORK_TYPE)
						.equals("hibernate")) {
					// ignore Exception
					if (e instanceof FileNotFoundException)
						log.warn("The process of adding new entities information in context-hibernate.xml is skipped.\n"
								+ "       Reason: context-hibernate.xml is not found in /src/main/resources/spring/.");
					else
						log.warn("The process of adding new entities information in context-hibernate.xml is skipped.\n"
								+ "       Reason: <!--Add new Entities here--> token is not found in /src/main/resources/context-hibernate.xml.");
				}

			}
		}
	}

	public void webExecute() {
		copyGeneratedObjects(this.sourceDirectory,
				this.webDestinationDirectory, "**/web/**/*.java");
	}

	public void webExecuteConf() {
		String webFramework = project.getProperties().getProperty(
				CommonConstants.WEB_FRAMEWORK);
		if ("spring".equalsIgnoreCase(webFramework)) {
			log.info("Installing Spring views and configuring...");
			installSpringControllerBeanDefinitions();
			installSpringViews();
			installMiPlatformViews();
		}

		log.info("Installing menu...");
		installMenu();
		// installTilesMenu();
	}

	/**
	 * This method will copy files from the source directory to the destination
	 * directory based on the pattern.
	 * 
	 * @param inSourceDirectory
	 *            The source directory to copy from.
	 * @param inDestinationDirectory
	 *            The destination directory to copy to.
	 * @param inPattern
	 *            The file pattern to match to locate files to copy.
	 */
	protected void copyGeneratedObjects(final String inSourceDirectory,
			final String inDestinationDirectory, final String inPattern) {
		AntUtil.executeCopyTask(antProject, inSourceDirectory,
				inDestinationDirectory, inPattern, true);
	}

	private String pojoLowerCase(String name) {
		return name.substring(0, 1).toLowerCase() + name.substring(1);
	}

	/**
	 * Add sample-data.xml to project's sample-data.xml
	 */
	private void installSampleData() {

		File existingFile = new File(destinationDirectory
				+ mergeFilePath.get("sample-data.xml"));

		try {
			// 1. clear previous sample data
			String replaceString = "<!--sample data-START-->\n" + "<dataset>\n"
					+ "</dataset>\n" + "<!--sample data-END-->";
			FileUtil.replaceStringXMLFilePretty(existingFile, "sample data",
					"<!--sample data here-->", "<!--sample data here-->\n"
							+ replaceString);

			// 2. add new sample data
			executeLoadFileTask(singleFilePathForMerge.get("sample-data.xml"),
					"sample.data");
			parseXMLFile(existingFile, null, "</dataset>", "sample.data");
		} catch (Exception e) {
			// ignore exception

			if (e instanceof FileNotFoundException)
				log.warn("The process of adding new sample data in sample-data.xml is skipped.\n"
						+ "       Reason: sample-data.xml is not found in /src/test/resources/.");
			else
				log.warn("The process of adding new sample data in sample-data.xml is skipped.\n"
						+ "       Reason: <!--sample data here--> token is not found in /src/test/resources/sample-data.xml.");
		}
	}

	private void installHibernateFiles() {
		if (project.getProperties()
				.getProperty(CommonConstants.APP_DAOFRAMEWORK_TYPE)
				.equals(CommonConstants.DAO_HIBERNATE)) {
			log.info("Installing Hibernate xml...");
			// 1. create dynamic-hibernate-pojo.xml
			// file
			executeLoadFileTask(
					"src/main/resources/hibernate/dynamic-hibernate-"
							+ pojoNameLower + ".xml", "hibernate.dynamic.hql");

			File dynamicHqlDir = new File(this.domainPjtDirectory
					+ "/src/main/resources/hibernate");

			try {
				if (!dynamicHqlDir.exists()) {
					dynamicHqlDir.mkdirs();
				}

				AntUtil.executeCopyTask(
						antProject,
						sourceDirectory + "/"
								+ singleFilePath.get("dynamic-hibernate.xml"),
						this.domainPjtDirectory + "/"
								+ singleFilePath.get("dynamic-hibernate.xml"));
			} catch (Exception e) {
				// ignore Exception
				log.warn("Generating "
						+ singleFilePath.get("dynamic-hibernate.xml")
						+ " is skipped. The reason is '" + e.getMessage()
						+ "'.");
			}

			// 2. add xml file name to
			// context-hibernate.xml file
			File existingFile = new File(this.domainPjtDirectory
					+ "/src/main/resources/spring/context-hibernate.xml");

			try {
				FileUtil.replaceStringXMLFilePretty(existingFile,
						"<!--Add new file name here-->",
						"<value>classpath:hibernate/dynamic-hibernate-"
								+ this.pojoNameLower + ".xml</value>");
			} catch (Exception e) {
				// ignore Exception
				if (e instanceof FileNotFoundException)
					log.warn("The process of adding new dynamic hibernate file name in context-hibernate.xml is skipped.\n"
							+ "       Reason: context-hibernate.xml is not found in /src/main/resources/spring/.");
				else
					log.warn("The process of adding new dynamic hibernate file name in context-hibernate.xml is skipped.\n"
							+ "       Reason: <!--Add new file name here--> token is not found in /src/main/resources/spring/context-hibernate.xml");
			}
		}
	}

	private void installQueryFiles() {
		if (project.getProperties()
				.getProperty(CommonConstants.APP_DAOFRAMEWORK_TYPE)
				.equals(CommonConstants.DAO_QUERY)) {
			if (project.getProperties()
					.getProperty(CommonConstants.TEMPLATE_TYPE)
					.startsWith("miplatform")) {
				log.info("Installing MiPlatform Query xml...");
				// 1. create mapping-query-pojo.xml file
				executeLoadFileTask(
						"src/main/resources/sql/mip-query/mapping-mip-query-"
								+ pojoNameLower + ".xml", "query.mapping.sql");

				File mappingSqlDir = new File(this.domainPjtDirectory
						+ "/src/main/resources/sql/mip-query");

				try {
					if (!mappingSqlDir.exists()) {
						mappingSqlDir.mkdirs();
					}

					AntUtil.executeCopyTask(
							antProject,
							sourceDirectory
									+ "/"
									+ singleFilePath
											.get("mapping-query-miplatform.xml"),
							this.domainPjtDirectory
									+ "/"
									+ singleFilePath
											.get("mapping-query-miplatform.xml"));
				} catch (Exception e) {
					// ignore Exception
					log.warn("Generating "
							+ singleFilePath
									.get("mapping-query-miplatform.xml")
							+ " is skipped. The reason is '" + e.getMessage()
							+ "'.");
				}

				// 2. add xml file name to
				// context-query.xml file
			} else {
				log.info("Installing Query xml...");
				// 1. create mapping-query-pojo.xml file
				executeLoadFileTask(
						"src/main/resources/sql/query/mapping-query-"
								+ pojoNameLower + ".xml", "query.mapping.sql");

				File mappingSqlDir = new File(this.domainPjtDirectory
						+ "/src/main/resources/sql/query");

				try {
					if (!mappingSqlDir.exists()) {
						mappingSqlDir.mkdirs();
					}

					AntUtil.executeCopyTask(
							antProject,
							sourceDirectory + "/"
									+ singleFilePath.get("mapping-query.xml"),
							this.domainPjtDirectory + "/"
									+ singleFilePath.get("mapping-query.xml"));
				} catch (Exception e) {
					// ignore Exception
					log.warn("Generating "
							+ singleFilePath.get("mapping-query.xml")
							+ " is skipped. The reason is '" + e.getMessage()
							+ "'.");
				}
			}
		}
	}

	private void installIBatis2Files() {
		if (project.getProperties()
				.getProperty(CommonConstants.APP_DAOFRAMEWORK_TYPE)
				.equals(CommonConstants.DAO_IBATIS2)) {
			log.info("Installing iBatis2 xml...");
			// 1. create pojo.xml file for ibatis2
			executeLoadFileTask("src/main/resources/sql/ibatis2/" + pojoName
					+ ".xml", "query.mapping.sql");

			File mappingiBatis2SqlDir = new File(this.domainPjtDirectory
					+ "/src/main/resources/sql/ibatis2");

			try {
				if (!mappingiBatis2SqlDir.exists()) {
					mappingiBatis2SqlDir.mkdirs();
				}

				AntUtil.executeCopyTask(
						antProject,
						sourceDirectory + "/"
								+ singleFilePath.get("mapping-ibatis2.xml"),
						this.domainPjtDirectory + "/"
								+ singleFilePath.get("mapping-ibatis2.xml"));
			} catch (Exception e) {
				// ignore Exception
				log.warn("Generating "
						+ singleFilePath.get("mapping-ibatis2.xml")
						+ " is skipped. The reason is '" + e.getMessage()
						+ "'.");
			}

			// 2. add xml file name to SqlMapConfig.xml file
			File existingFile = new File(this.domainPjtDirectory
					+ "/src/main/resources/sql/ibatis2/SqlMapConfig.xml");

			try {
				FileUtil.removeFileContent(existingFile, this.pojoName, "",
						true);

				String replaceString = "<!--" + this.pojoName + "-START-->\n"
						+ "<sqlMap resource=\"sql/ibatis2/mapping-ibatis2-" + this.pojoNameLower
						+ ".xml\"/>\n" + "<!--" + this.pojoName + "-END-->";
				FileUtil.addFileContent(existingFile,
						"<!--Add new file name here-->",
						"<!--Add new file name here-->\n" + replaceString, true);

			} catch (Exception e) {
				// ignore Exception
				if (e instanceof FileNotFoundException)
					log.warn("The process of adding new ibatis2 sql file name in SqlMapConfig.xml is skipped.\n"
							+ "       Reason: SqlMapConfig.xml is not found in /src/main/resources/spring/.");
				else
					log.warn("The process of adding new ibatis2 sql file name in SqlMapConfig.xml is skipped.\n"
							+ "       Reason: <!--Add new file name here--> token is not found in /src/main/resources/sql/ibatis2/SqlMapConfig.xml");
			}
		}
	}

	private void installSpringControllerBeanDefinitions() {

		try {
			if (project.getProperties()
					.getProperty(CommonConstants.TEMPLATE_TYPE)
					.startsWith("miplatform")) {

				executeLoadWebFileTask(
						singleFilePathForMerge
								.get("mip-query-generation-servlet.xml"),
						"dispatcher.servlet");

				String controllerFilePath = this.webDestinationDirectory
						+ mergeFilePath.get("mip-query-generation-servlet.xml");

				File generatedFile = new File(controllerFilePath);

				parseXMLFile(generatedFile, pojoName,
						"<!--Add additional controller beans here-->",
						"dispatcher.servlet");
			}
		} catch (Exception e) {
			// ignore exception
			if (e instanceof FileNotFoundException)
				log.warn("The process of adding additional controller bean in mip-query-generation-servlet.xml is skipped.\n"
						+ "       Reason: mip-query-generation-servlet.xml is not found in /src/main/resources/spring/.");
			else
				log.warn("The process of adding additional controller bean in mip-query-generation-servlet.xml is skipped.\n"
						+ "       Reason: <!--Add additional controller beans here--> token is not found in /src/main/resources/spring/mip-query-generation-servlet.xml");
		}
	}

	// =================== Views ===================

	private void installSpringViews() {

		try {
			if (!project.getProperties()
					.getProperty(CommonConstants.TEMPLATE_TYPE)
					.startsWith("miplatform")) {
				copyGeneratedObjects(this.sourceDirectory,
						this.webDestinationDirectory,
						"src/main/webapp/**/*.jsp");
			}

		} catch (Exception e) {
			// ignore exception
			log.warn("Jsp file generating is skipped. (form.jsp / list.jsp). The reason is '"
					+ e.getMessage() + "'.");
		}
	}

	private void installMiPlatformViews() {
		if (project.getProperties().getProperty(CommonConstants.TEMPLATE_TYPE)
				.startsWith("miplatform")) {
			try {
				AntUtil.executeCopyTask(
						antProject,
						this.sourceDirectory + "/"
								+ singleFilePath.get("grid_list.xml"),
						this.webDestinationDirectory + "/"
								+ singleFilePath.get("grid_list.xml"));
			} catch (Exception e) {
				// ignore Exception
				log.warn("Generating " + singleFilePath.get("grid_list.xml")
						+ " is skipped. The reason is '" + e.getMessage()
						+ "'.");

			}

			File generatedFile = new File(this.webDestinationDirectory
					+ "/src/main/webapp/mip-query/extends/mip_query_sdi.xml");

			try {
				String replaceString = "<!--Miplatform "
						+ this.pojoNameLower
						+ "Service-START-->\n"
						+ "<AppGroup CodePage=\"utf-8\" Language=\"0\" Prefix=\""
						+ this.pojoNameLower
						+ "\" Type=\"form\" Version=\"1.0\">\n"
						+ "<script Baseurl=\"./" + this.pojoNameLower
						+ "/\" ScriptUrl=\"./" + this.pojoNameLower + "/\"/>\n"
						+ "</AppGroup>\n" + "<!--Miplatform "
						+ this.pojoNameLower + "Service-END-->";

				FileUtil.replaceStringXMLFilePretty(generatedFile,
						"Miplatform " + this.pojoNameLower + "Service",
						"<!--new miplatform service group xml here-->",
						"<!--new miplatform service group xml here-->"
								+ replaceString);

			} catch (Exception e) {
				// ignore exception
				if (e instanceof FileNotFoundException)
					log.warn("The process of adding new miplatform service group in mip_query_sdi.xml is skipped.\n"
							+ "       Reason: mip_query_sdi.xml is not found in /src/main/webapp/mip-query/extends/.");
				else
					log.warn("The process of adding new miplatform service group in mip_query_sdi.xml is skipped.\n"
							+ "       Reason: <!--new miplatform service group xml here--> token is not found in /src/main/webapp/mip-query/extends/mip_query_sdi.xml.");
			}
		}
	}

	// =================== End of Views
	// ===================

	private void installMenu() {
		try {
			executeLoadWebFileTask(singleFilePathForMerge.get("left-gen.jsp"),
					"menu.jsp");
			File existingFile = new File(this.webDestinationDirectory
					+ mergeFilePath.get("left-gen.jsp"));

			parseXMLFile(existingFile, pojoName,
					"<!--Add new crud generation menu here-->", "menu.jsp");

		} catch (Exception e) {
			// ignore exception
			if (e instanceof FileNotFoundException)
				log.warn("The process of adding new menu in anyframe.jsp is skipped.\n"
						+ "       Reason: anyframe.jsp is not found in /src/main/webapp/.");
			else
				log.warn("The process of adding new menu in anyframe.jsp is skipped.\n"
						+ "       Reason: <!--Add new menu here--> token is not found in /src/main/webapp/anyframe.jsp.");
		}
	}

	/*
	 * private void installTilesMenu() { try { executeLoadWebFileTask(
	 * singleFilePathForMerge.get("tilesviews.xml"), "tiles-menu.jsp"); File
	 * existingFile = new File(this.webDestinationDirectory +
	 * mergeFilePath.get("tilesviews.xml"));
	 * 
	 * parseXMLFile(existingFile, pojoName,
	 * "<!--Add new tiles definition here-->", "tiles-menu.jsp");
	 * 
	 * } catch (Exception e) { // ignore exception if (e instanceof
	 * FileNotFoundException) log
	 * .warn("The process of adding new menu in tilesviews.xml is skipped.\n" +
	 * "       Reason: left.jsp is not found in /src/main/webapp/WEB-INF/.");
	 * else log
	 * .warn("The process of adding new menu in tilesviews.xml is skipped.\n" +
	 * "       Reason: <!--Add new tiles definition here--> token is not found in /src/main/webapp/WEB-INF/tilesviews.xml."
	 * ); } }
	 */

	private void installInternationalizationKeys() {
		try {
			log.info("Installing i18n messages...");

			executeLoadWebFileTask(
					singleFilePathForMerge.get("message-generation.properties"),
					"i18n.file");
			File existingFile = new File(this.mainPjtDirectory
					+ mergeFilePath.get("message-generation.properties"));

			if (!existingFile.exists())
				throw new FileNotFoundException();

			parsePropertiesFile(existingFile, pojoName);

			AntUtil.executeEchoTask(antProject, existingFile);
		} catch (Exception e) {
			// ignore exception
			if (e instanceof FileNotFoundException)
				log.warn("The process of adding message properties in message-generation.properties is skipped.\n"
						+ "       Reason: message-generation.properties is not found in /src/main/resources/message/.");
			else
				log.warn("The process of adding message properties in message-generation.properties is skipped.");

		}
	}

	/**
	 * This method will create an ANT based LoadFile task based on an infile and
	 * a property name. The property will be loaded with the infile for use
	 * later by the Replace task.
	 * 
	 * @param inFile
	 *            The file to process
	 * @param propName
	 *            the name to assign it to
	 * @return The ANT LoadFile task that loads a property with a file
	 */
	protected void executeLoadFileTask(String inFile, String propName) {
		inFile = sourceDirectory + CommonConstants.fileSeparator + inFile;

		AntUtil.executeLoadFileTask(antProject, this.sourceDirectory, inFile,
				propName);
	}

	/**
	 * This method will create an ANT based LoadFile task based on an infile and
	 * a property name. The property will be loaded with the infile for use
	 * later by the Replace task.
	 * 
	 * @param inFile
	 *            The file to process
	 * @param propName
	 *            the name to assign it to
	 * @return The ANT LoadFile task that loads a property with a file
	 */
	protected void executeLoadWebFileTask(String inFile, String propName) {
		inFile = this.sourceDirectory + CommonConstants.fileSeparator + inFile;

		AntUtil.executeLoadFileTask(antProject, this.sourceDirectory, inFile,
				propName);
	}

	private void parseXMLFile(File existingFile, String beanName,
			String newToken, String fileVariable) throws Exception {

		if (!existingFile.exists())
			throw new FileNotFoundException();

		String nameInComment = beanName;
		if (beanName == null) {
			nameInComment = pojoName;
		}

		try {
			AntUtil.executeReplaceRegExpTask(antProject, existingFile, "<!--"
					+ nameInComment + "-START-->", "<!--" + nameInComment
					+ "-END-->", "", newToken,
					antProject.getProperty(fileVariable));
		} catch (Exception e) {
			log.warn("Replacing comment tag in XML is skipped. The reason is '"
					+ e.getMessage() + "'.");
		}
	}

	/**
	 * This file is the same as the method above, except for different comment
	 * placeholder formats. Yeah, I know, it's ugly.
	 * 
	 * @param existingFile
	 *            file to merge with in project
	 * @param beanName
	 *            name of placeholder string that goes in comment
	 */
	private void parsePropertiesFile(File existingFile, String beanName)
			throws Exception {
		String nameInComment = beanName;
		if (beanName == null) {
			nameInComment = pojoName;
		}

		AntUtil.executeReplaceRegExpTask(antProject, existingFile, "# -- "
				+ nameInComment + "-START", "# -- " + nameInComment + "-END",
				"");
	}

	public Log getLog() {
		if (log == null) {
			log = new SystemStreamLog();
		}

		return log;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public void setGenericCore(boolean genericCore) {
		this.genericCore = genericCore;
	}

}
