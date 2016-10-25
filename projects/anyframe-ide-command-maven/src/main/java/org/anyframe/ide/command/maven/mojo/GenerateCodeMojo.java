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
package org.anyframe.ide.command.maven.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.Entity;

import org.anyframe.ide.command.cli.util.PluginConstants;
import org.anyframe.ide.command.common.CommandException;
import org.anyframe.ide.command.common.PluginInfoManager;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.AntUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.anyframe.ide.command.common.util.PrettyPrinter;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.command.maven.mojo.codegen.AnyframeExporter;
import org.anyframe.ide.command.maven.mojo.codegen.AnyframeGeneratorMojo;
import org.anyframe.ide.command.maven.mojo.codegen.ArtifactInstaller;
import org.anyframe.ide.command.maven.mojo.codegen.SourceCodeChecker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.appfuse.mojo.HibernateExporterMojo;
import org.codehaus.mojo.hibernate3.configuration.AnnotationComponentConfiguration;
import org.codehaus.mojo.hibernate3.configuration.ComponentConfiguration;
import org.codehaus.mojo.hibernate3.configuration.JDBCComponentConfiguration;
import org.codehaus.mojo.hibernate3.configuration.JPAComponentConfiguration;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.hibernate.tool.hbm2x.Exporter;
import org.hibernate.tool.hbm2x.XMLPrettyPrinter;

/**
 * This is an AnyframeCodeGenerator class.
 * 
 * @goal create-crud
 * @author Matt Raible
 * @author extended by SooYeon Park
 */
public class GenerateCodeMojo extends AnyframeGeneratorMojo {
	/** @component role="org.codehaus.plexus.components.interactivity.Prompter" */
	private Prompter prompter;

	/**
	 * @component 
	 *            role="org.anyframe.ide.command.common.DefaultPluginInfoManager"
	 */
	protected PluginInfoManager pluginInfoManager;

	/**
	 * target folder to generate model-class
	 * 
	 * @parameter expression="${basedir}"
	 */
	protected File baseDir;

	/**
	 * The entity's home.
	 * 
	 * @parameter expression="${entity}"
	 * @required
	 */
	private String entity = "";

	/**
	 * The project's home.
	 * 
	 * @parameter expression="${projecthome}" default-value="${basedir}"
	 */
	protected String projectHome = "";

	/**
	 * The generated code's package.
	 * 
	 * @parameter expression="${package}"
	 */
	private String packageName;

	/**
	 * The scope.
	 * 
	 * @parameter expression="${scope}" default-value="all"
	 */
	private String scope;

	/**
	 * <i>Maven Internal</i>: Project to interact with.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 * @noinspection UnusedDeclaration
	 */
	protected MavenProject project;

	/**
	 * execution mode
	 * 
	 * @parameter expression="${isCLIMode}" default-value="true"
	 */
	private boolean isCLIMode;

	private List<ComponentConfiguration> componentConfigurations = new ArrayList<ComponentConfiguration>();

	// project meta information
	/**
	 * project's name
	 * 
	 * @parameter expression="${projectName}" default-value="${project.groupId}"
	 */
	private String projectName;

	private String basePackage = "";
	private String daoframework = "hibernate";
	private String templateType = "";
	protected String templateHome;
	private String projectType = "web";

	// db properties
	private String dialect = "";
	private String driverClass = "";
	private String userName = "";
	private String password = "";
	private String url = "";
	private String dbType = "";
	private String dbSchema = "";

	// etc.
	private String webframework = "spring";
	private String hibernateCfgFilePath = "";
	private String modelpackage = "";
	private String entityClassName = "";
	private String packaging = "jar";
	private boolean genericcore = false;

	private SourceCodeChecker sourceCodeChecker = new SourceCodeChecker();

	// test
	private String outputDirectory = "";
	private String destinationDirectory = "";
	private String webDestinationDirectory = "";
	private String domainPjtDirectory;
	private File hibernateCfgFile = null;

	public GenerateCodeMojo() {
		componentConfigurations.add(new AnnotationComponentConfiguration());
		componentConfigurations.add(new JDBCComponentConfiguration());
		componentConfigurations.add(new JPAComponentConfiguration());
	}

	// The method executing the mojo
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			checkInstalledPlugins(new String[] { CommonConstants.CORE_PLUGIN });
			setProperties();
			if (isCLIMode) {
				sourceCodeChecker.checkExistingCrud(this.isCLIMode,
						this.prompter, this.templateType, this.templateHome,
						this.projectHome, this.basePackage, this.packageName,
						this.entityClassName, this.scope, this.daoframework);
			}
			generateCode();
			executeCodeInstaller();
			executeConfInstaller();
			insertInitialSampleData();
			postExecute();
		} catch (Exception ex) {
			getLog().error("Fail to execute GenerateCodeMojo.");
			throw new MojoFailureException(ex.getMessage());
		}
	}

	private void setProperties() throws Exception {
		// 1. read project.mf
		File metadataFile = new File(baseDir.getAbsolutePath()
				+ CommonConstants.METAINF, CommonConstants.METADATA_FILE);

		if (!metadataFile.exists()) {
			throw new CommandException("Can not find a '"
					+ metadataFile.getAbsolutePath()
					+ "' file. Please check a location of your project.");
		}

		PropertiesIO pio = new PropertiesIO(metadataFile.getAbsolutePath());

		// 2. set member variables from project.mf
		this.projectName = pio.readValue(PluginConstants.PROJECT_NAME);
		this.basePackage = pio.readValue(PluginConstants.PACKAGE_NAME);

		this.dialect = pio.readValue(CommonConstants.DB_DIALECT);
		this.driverClass = pio.readValue(PluginConstants.DB_DRIVER_CLASS);
		this.userName = pio.readValue(PluginConstants.DB_USERNAME);
		this.password = pio.readValue(PluginConstants.DB_PASSWORD);
		this.url = pio.readValue(PluginConstants.DB_URL);
		this.dbType = pio.readValue(PluginConstants.DB_TYPE);
		this.dbSchema = pio.readValue(PluginConstants.DB_SCHEMA);

		this.daoframework = pio
				.readValue(CommonConstants.APP_DAOFRAMEWORK_TYPE);
		this.templateType = pio.readValue(CommonConstants.APP_TEMPLATE_TYPE);

		if (StringUtils.isEmpty(this.templateHome)) {
			this.templateHome = pio
					.readValue(CommonConstants.PROJECT_TEMPLATE_HOME);
		}
		this.projectType = pio.readValue(PluginConstants.PROJECT_TYPE);
		if (this.scope.equalsIgnoreCase("all")) {
			if (this.projectType
					.equalsIgnoreCase(PluginConstants.PROJECT_TYPE_WEB))
				getLog().info(
						"Scope for generation code is automatically set to 'all'. You can select a scope.");
			else if (this.projectType
					.equalsIgnoreCase(PluginConstants.PROJECT_TYPE_SERVICE)) {
				this.scope = PluginConstants.PROJECT_TYPE_SERVICE;
				getLog().info(
						"Scope for generation code is automatically set to 'service'. You cannot use scope 'all' because your project is a 'service' type.");
			}
		}
		if (this.scope.equalsIgnoreCase("web")) {
			if (this.projectType
					.equalsIgnoreCase(PluginConstants.PROJECT_TYPE_SERVICE)) {
				this.scope = PluginConstants.PROJECT_TYPE_SERVICE;
				getLog().info(
						"Scope for generation code is automatically set to 'service'. You cannot use scope 'all' because your project is a 'service' type.");
			}
		}

		if (!this.scope.equals("service"))
			this.packaging = "war";
		int lastDotIndex = this.entity.lastIndexOf(".");
		this.modelpackage = (lastDotIndex != -1) ? entity.substring(0,
				lastDotIndex) : this.basePackage + ".domain";
		this.entityClassName = lastDotIndex != -1 ? entity
				.substring(lastDotIndex + 1) : entity;
		if (StringUtils.isEmpty(this.packageName)) {
			this.packageName = this.basePackage + "."
					+ this.entity.toLowerCase();
		}
		if (StringUtils.isEmpty(this.templateHome)) {
			this.templateHome = baseDir.getParent()
					+ CommonConstants.fileSeparator + "templates";
		}

		// 3. check temporary directory
		checkTemporaryDirectory();

		// 4. set maven project
		setMavenProject();

		// 5. set System properties
		System.setProperty("entity", this.entityClassName);
		System.setProperty("modelpackage", this.modelpackage);
		System.setProperty("type", "pojo");
		System.setProperty("disableInstallation", "true");
		System.setProperty("entity.check", "false");

		// 6. set location to be generated codes
		setGenLocation(this.projectHome + CommonConstants.fileSeparator
				+ ".temp");
	}

	public void generateCode() throws MojoExecutionException,
			MojoFailureException {
		addAllEntitiesToHibernateCfgXml();
		super.execute();
	}

	public void executeCodeInstaller() throws Exception {
		// for test
		if (StringUtils.isEmpty(this.destinationDirectory)) {
			this.destinationDirectory = this.projectHome
					+ CommonConstants.fileSeparator;
		}

		ArtifactInstaller installer = new ArtifactInstaller(project,
				this.modelpackage, this.entityClassName, getOutputDirectory(),
				this.destinationDirectory, this.genericcore, this.templateHome);
		installer.setLog(getLog());
		installer.setDomainPjtDirectory(this.projectHome
				+ CommonConstants.fileSeparator + this.projectName);

		installer.execute();

		// for test
		if (StringUtils.isEmpty(this.webDestinationDirectory)) {
			this.webDestinationDirectory = this.projectHome
					+ CommonConstants.fileSeparator;
		}

		if (!this.scope.equals("service")) {
			installer.setWebDestinationDirectory(this.webDestinationDirectory);
			installer.setMainPjtDirectory(this.projectHome
					+ CommonConstants.fileSeparator);
			installer.webExecute();
		}
	}

	public void executeConfInstaller() throws Exception {
		// for test
		if (StringUtils.isEmpty(this.destinationDirectory)) {
			this.destinationDirectory = this.projectHome;
		}

		if (StringUtils.isEmpty(this.domainPjtDirectory)) {
			this.domainPjtDirectory = this.projectHome;
		}

		ArtifactInstaller installer = new ArtifactInstaller(project,
				this.modelpackage, this.entityClassName, getOutputDirectory(),
				this.destinationDirectory, this.genericcore, this.templateHome);
		installer.setLog(getLog());
		installer.setMainPjtDirectory(this.projectHome);
		installer.setDomainPjtDirectory(this.domainPjtDirectory);

		installer.executeConf();

		// for test
		if (StringUtils.isEmpty(this.webDestinationDirectory)) {
			this.webDestinationDirectory = this.projectHome;
		}

		if (!this.scope.equals("service")) {
			installer.setWebDestinationDirectory(this.webDestinationDirectory);
			installer.webExecuteConf();
		}
	}

	public void insertInitialSampleData() throws Exception {
		File sampleDataFile = new File(this.projectHome,
				"/src/test/resources/sample-data.xml");

		if (sampleDataFile.exists()) {
			try {
				AntUtil.executeDbUnitTask(this.dbType.toLowerCase(), this.url,
						this.driverClass, this.userName, this.password,
						this.dbSchema, sampleDataFile, this.projectHome);
			} catch (Exception e) {
				getLog().warn(
						"Inserting sample data using DBUnit is skipped.: The reason is '"
								+ e.getMessage() + "'.");
			}
		}
	}

	public void postExecute() throws Exception {
		String genLocation = this.projectHome + CommonConstants.fileSeparator
				+ ".temp";
		FileUtil.deleteDir(new File(genLocation));
	}

	/**
	 * Instantiates a org.appfuse.tool.AppFuseExporter object.
	 * 
	 * @return POJOExporter
	 */
	protected Exporter createExporter() {
		AnyframeExporter exporter = new AnyframeExporter();
		exporter.setTemplateType(this.templateType);
		exporter.setTemplateHome(this.templateHome);
		exporter.setPackageName(this.packageName);
		exporter.setProjectType(this.projectType);

		return exporter;
	}

	/**
	 * Returns the ComponentConfiguration for this maven goal.
	 */
	protected ComponentConfiguration getComponentConfiguration(String name)
			throws MojoExecutionException {
		for (Iterator<ComponentConfiguration> it = componentConfigurations
				.iterator(); it.hasNext();) {
			ComponentConfiguration componentConfiguration = it.next();
			if (componentConfiguration.getName().equals(name)) {
				return componentConfiguration;
			}
		}
		throw new MojoExecutionException("Could not get ConfigurationTask.");
	}

	private void setMavenProject() {
		// 1. set maven project
		project.setGroupId(this.basePackage);
		project.setFile(new File(this.projectHome + "/temp"));
		project.setBuild(new Build());

		// for test
		if (StringUtils.isEmpty(this.outputDirectory)) {
			this.outputDirectory = this.projectHome + "/target/classes";
		}
		project.getBuild().setOutputDirectory(this.outputDirectory);
		project.getBuild().setTestOutputDirectory("./test");
		project.setPackaging(this.packaging);
		setPrivateField(HibernateExporterMojo.class, "project", this, project);

		// 2. set maven project's properties
		Properties projectProps = new Properties();
		projectProps.setProperty(CommonConstants.APP_DAOFRAMEWORK_TYPE,
				this.daoframework);
		projectProps.setProperty(CommonConstants.WEB_FRAMEWORK,
				this.webframework);
		projectProps.setProperty("packaging", this.packaging);
		projectProps.setProperty("genericcore",
				new Boolean(this.genericcore).toString());
		projectProps.setProperty("pjt.name", this.projectName);
		projectProps.setProperty("template.type", this.templateType);
		setProjectProperties(projectProps);

		// 3. set maven component's properties
		Properties componentProps = new Properties();

		if (StringUtils.isEmpty(hibernateCfgFilePath)) {
			this.hibernateCfgFilePath = "/src/main/resources/hibernate/hibernate.cfg.xml";
			this.hibernateCfgFile = new File(baseDir, hibernateCfgFilePath);
		} else {
			this.hibernateCfgFile = new File(hibernateCfgFilePath);
			this.hibernateCfgFilePath = "/src/main/resources/hibernate/hibernate.cfg.xml";
		}

		componentProps.setProperty("configurationfile",
				this.hibernateCfgFilePath);
		componentProps.setProperty("uuid",
				new Long(System.currentTimeMillis()).toString());
		componentProps.setProperty("servicepackage", this.packageName);
		componentProps.setProperty("modelpackage", this.modelpackage);

		// for impala test cases
		componentProps.setProperty("modulemain", this.projectName);
		componentProps.setProperty("moduledomain", this.projectName);
		componentProps.setProperty("module", this.projectName);
		componentProps.setProperty("pojoNameLower", entity == null ? ""
				: entity.toLowerCase().charAt(0) + entity.substring(1));
		componentProps.setProperty("templatedirectory", templateHome + "/"
				+ templateType + "/source/");
		if (this.scope.equals("service"))
			componentProps.setProperty("generate-core", "true");

		setComponentProperties(componentProps);
	}

	private void addAllEntitiesToHibernateCfgXml() throws MojoFailureException {
		ClassLoader old = Thread.currentThread().getContextClassLoader();
		try {

			String domainPath = project.getBuild().getOutputDirectory() + "/"
					+ (modelpackage.replace(".", "/"));
			String classFileName = domainPath + "/" + this.entityClassName
					+ ".class";

			URL[] newUrls = new URL[1];
			newUrls[0] = new File(project.getBuild().getOutputDirectory())
					.toURI().toURL();

			Thread.currentThread().setContextClassLoader(
					new URLClassLoader(newUrls, this.getClass()
							.getClassLoader()));

			File classFile = new File(classFileName);
			if (!classFile.exists()) {
				throw new MojoFailureException(this.entityClassName
						+ ".class is not found in " + domainPath + ".");
			}

			File files = new File(domainPath);
			String[] fileList = files.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".class");
				}
			});

			if (fileList != null && fileList.length != 0) {
				String hibernateCfgXml = null;
				if (this.hibernateCfgFile.exists()) {
					FileUtil.removeFileContent(this.hibernateCfgFile,
							"mapping class", "", true);
					FileUtil.addFileContent(this.hibernateCfgFile,
							"</session-factory>",
							"<!--mapping class-START-->\n"
									+ "<!--mapping class-END-->\n"
									+ "</session-factory>\n", true);

					hibernateCfgXml = FileUtils
							.readFileToString(this.hibernateCfgFile);
				} else {
					hibernateCfgXml = getHibernateCfgXml();

					String metadataDialect = "";
					if (this.dbType.equalsIgnoreCase("sybase"))
						metadataDialect = "<property name=\"hibernatetool.metadatadialect\">org.anyframe.ide.command.maven.mojo.codegen.dialect.SybaseMetaDataDialect</property>\n";

					String replaceString = "<!--hibernate jdbc configuration here-->\n"
							+ "<!--hibernate jdbc configuration-START-->\n"
							+ "<property name=\"hibernate.dialect\">"
							+ this.dialect
							+ "</property>\n"
							+ metadataDialect
							+ "<property name=\"hibernate.connection.driver_class\">"
							+ this.driverClass
							+ "</property>\n"
							+ "<property name=\"hibernate.connection.username\">"
							+ this.userName
							+ "</property>\n"
							+ "<property name=\"hibernate.connection.password\">"
							+ this.password
							+ "</property>\n"
							+ "<property name=\"hibernate.connection.url\">"
							+ this.url
							+ "</property>\n"
							+ "<!--hibernate jdbc configuration-END-->";

					hibernateCfgXml = hibernateCfgXml.replace(
							"<!--hibernate jdbc configuration here-->",
							replaceString);

					getLog().info(
							(new StringBuilder(
									"Hibernate.cfg.xml is generated to project"))
									.toString());
				}

				for (int i = 0; i < fileList.length; i++) {
					String pojoName = fileList[i].replace(".class", "");
					String className = pojoName;
					if (modelpackage.length() > 0)
						className = modelpackage + "." + pojoName;

					try {
						Class clazz = classForName(className);

						ClassLoader loader = clazz.getClassLoader();

						if (!hibernateCfgXml.contains("\"" + className + "\"")
								&& clazz.isAnnotationPresent(Entity.class)) {
							hibernateCfgXml = hibernateCfgXml.replace(
									"<!--mapping class-END-->",
									" <mapping class=\"" + className + "\"/>"
											+ "\n <!--mapping class-END-->");
						}
					} catch (ClassNotFoundException ignore) {
					}
				}

				File xmlFile = new File(this.projectHome, getComponentProperty(
						"configurationfile",
						"src/main/resources/hibernate/hibernate.cfg.xml"));
				FileUtils.writeStringToFile(xmlFile, hibernateCfgXml);

				XMLPrettyPrinter.prettyPrintFile(
						PrettyPrinter.getDefaultTidy(), xmlFile, xmlFile, true);

			}

		} catch (IOException io) {
			throw new MojoFailureException(io.getMessage());
		} catch (Exception e) {
			throw new MojoFailureException(
					e,
					"Fail to add All Entities to Hibernate.cfg.xml in /src/main/resources/hibernate/.",
					e.getMessage());
		} finally {
			Thread.currentThread().setContextClassLoader(old);
		}

	}

	public Class classForName(String name) throws ClassNotFoundException {
		try {
			ClassLoader contextClassLoader = Thread.currentThread()
					.getContextClassLoader();
			if (contextClassLoader != null) {
				return contextClassLoader.loadClass(name);
			}
		} catch (Throwable ignore) {
		}
		return Class.forName(name);
	}

	private void checkTemporaryDirectory() {
		String temp = this.projectHome + CommonConstants.fileSeparator
				+ ".temp";
		File tempFolder = new File(temp);
		try {
			if (!tempFolder.exists()) {
				if (tempFolder.mkdir())
					getLog().debug(
							"Temporary directory to generate source codes is created in "
									+ tempFolder.getAbsolutePath());
				else
					getLog().debug(
							"Temporary directory to generate source codes is not created in "
									+ tempFolder.getAbsolutePath());
			} else {
				FileUtil.deleteDir(tempFolder);
			}
		} catch (Exception e) {
			getLog().warn("Check temporary directory : " + e.getMessage());
		}
	}

	/**
	 * Check that the any plugin was installed.
	 * 
	 * @throws Exception
	 */
	protected void checkInstalledPlugins(String[] pluginNames) throws Exception {
		Map<String, PluginInfo> installedPlugins = pluginInfoManager
				.getInstalledPlugins(baseDir.getAbsolutePath());
		if (installedPlugins.size() == 0) {
			throw new CommandException(
					"Can not find any installed plugin information. Please install any plugin at the very first.");
		}

		boolean isInstalled = false;

		StringBuffer uninstallPluginsNames = new StringBuffer();
		for (String pluginName : pluginNames) {
			if (installedPlugins.containsKey(pluginName)) {
				isInstalled = true;
			} else {
				uninstallPluginsNames.append("'" + pluginName + "',");
			}
		}

		if (!isInstalled) {
			throw new CommandException(
					"Can not find installed plugin ["
							+ uninstallPluginsNames.toString()
									.substring(
											0,
											uninstallPluginsNames.toString()
													.length() - 1)
							+ "] information. Please install those plugins at the very first.");
		}
	}

	private void setGenLocation(String location) {
		addDefaultComponent(location, "configuration", false);
		addDefaultComponent(location, "jdbcconfiguration", true);
		addDefaultComponent(location, "annotationconfiguration", true);
	}

	private void setPrivateField(Class<?> clazz, String field, Object object,
			Object value) {
		try {
			Field prompterField = clazz.getDeclaredField(field);
			prompterField.setAccessible(true);
			prompterField.set(object, value);
		} catch (Exception e) {
			getLog().warn(
					"Setting the maven project's properties is skipped.: The reason is a '"
							+ e.getMessage() + "'.");
		}
	}

	private void setComponentProperties(Properties properties) {
		Map<String, String> componentProperties = this.getComponentProperties();

		Iterator<Object> itr = properties.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			componentProperties.put(key, (String) properties.get(key));
		}
	}

	private String getHibernateCfgXml() throws MojoFailureException,
			FileNotFoundException {
		InputStream in = new FileInputStream(new File(
				getComponentProperty("templatedirectory")
						+ "model/hibernate.cfg.ftl"));
		StringBuffer configFile = new StringBuffer();
		try {
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(isr);
			String line;
			while ((line = reader.readLine()) != null) {
				configFile.append(line).append("\n");
			}
			reader.close();
		} catch (IOException io) {
			throw new MojoFailureException(io.getMessage());
		}
		return configFile.toString();
	}

	private String getOutputDirectory() {
		String temp = this.projectHome + CommonConstants.fileSeparator
				+ ".temp";
		File tempFolder = new File(temp);
		try {
			if (!tempFolder.exists()) {
				if (tempFolder.mkdir())
					getLog().debug(
							"Temporary directory to generate source codes is created in "
									+ tempFolder.getAbsolutePath());
				else
					getLog().debug(
							"Temporary directory to generate source codes is not created in "
									+ tempFolder.getAbsolutePath());
			}
		} catch (Exception e) {
			getLog().warn(
					"Output directory is not found. The reason is '"
							+ e.getMessage() + "'.");
		}

		return temp;
	}

	private void setProjectProperties(Properties properties) {
		Properties mavenProperties = this.project.getProperties();
		Iterator<Object> itr = properties.keySet().iterator();

		while (itr.hasNext()) {
			String key = (String) itr.next();
			mavenProperties.setProperty(key, (String) properties.get(key));
		}
	}

	public MavenProject getProject() {
		return project;
	}

	// getter, setter for ANT Task
	public void setEntity(String entity) {
		this.entity = entity;
	}

	public void setMavenProject(MavenProject project) {
		this.project = project;
	}

	public void setProjectHome(String projectHome) {
		this.projectHome = projectHome;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setPrompter(Prompter prompter) {
		this.prompter = prompter;
	}

	public void setCLIMode(boolean isCLIMode) {
		this.isCLIMode = isCLIMode;
	}

	public void setDaoframework(String daoframework) {
		this.daoframework = daoframework;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	// for test
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void setDestinationDirectory(String destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
	}

	public void setWebDestinationDirectory(String webDestinationDirectory) {
		this.webDestinationDirectory = webDestinationDirectory;
	}

	public void setDomainPjtDirectory(String domainPjtDirectory) {
		this.domainPjtDirectory = domainPjtDirectory;
	}

	public void setHibernateCfgFilePath(String hibernateCfgFilePath) {
		this.hibernateCfgFilePath = hibernateCfgFilePath;
	}

	public void setTemplateHome(String templateHome) {
		this.templateHome = templateHome;
	}

	public void setPluginInfoManager(PluginInfoManager pluginInfoManager) {
		this.pluginInfoManager = pluginInfoManager;
	}
}
