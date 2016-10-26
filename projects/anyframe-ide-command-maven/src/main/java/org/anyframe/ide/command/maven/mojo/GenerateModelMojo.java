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
package org.anyframe.ide.command.maven.mojo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.anyframe.ide.command.common.CommandException;
import org.anyframe.ide.command.common.PluginInfoManager;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.anyframe.ide.command.common.util.PrettyPrinter;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.command.maven.mojo.codegen.AnyframeArtifactCollector;
import org.anyframe.ide.command.maven.mojo.codegen.AnyframePOJOExporter;
import org.anyframe.ide.command.maven.mojo.codegen.SourceCodeChecker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.appfuse.mojo.exporter.ModelGeneratorMojo;
import org.codehaus.mojo.hibernate3.configuration.AnnotationComponentConfiguration;
import org.codehaus.mojo.hibernate3.configuration.ComponentConfiguration;
import org.codehaus.mojo.hibernate3.configuration.JDBCComponentConfiguration;
import org.codehaus.mojo.hibernate3.configuration.JPAComponentConfiguration;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.hibernate.HibernateException;
import org.hibernate.tool.hbm2x.Exporter;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.hibernate.tool.hbm2x.XMLPrettyPrinter;
import org.hibernate.util.StringHelper;

/**
 * This is an AnyframeModelGenerator class.
 * 
 * @goal create-model
 * @author Matt Raible
 * @author modified by SooYeon Park
 */
public class GenerateModelMojo extends ModelGeneratorMojo {
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
	 * target table
	 * 
	 * @parameter expression="${table}" default-value="*"
	 */
	private String table = "";

	/**
	 * The project's home.
	 * 
	 * @parameter expression="${projecthome}" default-value="${basedir}"
	 */
	protected String projectHome = "";

	/**
	 * The project's home.
	 * 
	 * @parameter expression="${package}"
	 */
	protected String packageName = "";

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

	private PropertiesIO pio = null;

	// project meta information
	private String basePackage = "";
	private String daoframework = "hibernate";
	private String templateDirectory;
	private String templateType = "default";
	protected String templateHome;

	// db properties
	private String dialect = "";
	private String driverClass = "";
	private String userName = "";
	private String passWord = "";
	private String url = "";
	private String dbType = "";
	private String dbSchema = "";

	// etc.
	private String hibernateCfgFilePath = "";
	private String hibernateRevengFilePath = null;

	private SourceCodeChecker sourceCodeChecker = new SourceCodeChecker();

	// test
	private File hibernateCfgFile = null;

	public GenerateModelMojo() {
		componentConfigurations.add(new AnnotationComponentConfiguration());
		componentConfigurations.add(new JDBCComponentConfiguration());
		componentConfigurations.add(new JPAComponentConfiguration());
	}

	/**
	 * main method for executing GenerateModelMojo. This mojo is executed when
	 * you input 'mvn anyframe:create-model [-options]'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			checkInstalledPlugins(new String[] { CommonConstants.CORE_PLUGIN });
			pio = setProperties();
			if (isCLIMode) {
				sourceCodeChecker.checkExistingModel(this.isCLIMode,
						this.prompter, pio, this.projectHome, this.packageName,
						this.table);
			}
			generateModel();
			executeInstaller();
			
			System.out.println("Domain classes for the given table names are generated successfully.");
		} catch (Exception ex) {
			getLog().error(
					"Fail to execute GenerateModelMojo. The reason is '"
							+ ex.getMessage() + "'.");
			throw new MojoFailureException(null);
		}
	}

	public PropertiesIO setProperties() throws Exception {
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
		this.daoframework = pio
				.readValue(CommonConstants.APP_DAOFRAMEWORK_TYPE);
		this.templateType = pio.readValue(CommonConstants.APP_TEMPLATE_TYPE);
		this.templateHome = pio
				.readValue(CommonConstants.PROJECT_TEMPLATE_HOME);
		this.basePackage = pio.readValue(CommonConstants.PACKAGE_NAME);

		this.dialect = pio.readValue(CommonConstants.DB_DIALECT);
		this.driverClass = pio.readValue(CommonConstants.DB_DRIVER_CLASS);
		this.userName = pio.readValue(CommonConstants.DB_USERNAME);
		this.passWord = pio.readValue(CommonConstants.DB_PASSWORD);
		this.url = pio.readValue(CommonConstants.DB_URL);
		this.dbType = pio.readValue(CommonConstants.DB_TYPE);
		this.dbSchema = pio.readValue(CommonConstants.DB_SCHEMA);
		this.templateDirectory = templateHome + "/" + templateType + "/source/";

		// 3. set maven project
		setMavenProject();

		// 4. set location to be generated codes
		setGenLocation(this.projectHome + "/src/main/java");

		// 5. set table
		setTable();

		return pio;
	}

	private void setMavenProject() {
		// 1. set maven project
		project.setFile(new File(this.projectHome + "/temp"));

		// 2. set maven project's properties
		setProjectProperties(new Properties());

		// 3. set maven component's properties
		Properties componentProperties = new Properties();
		// for test
		if (StringUtils.isEmpty(this.hibernateCfgFilePath)) {
			this.hibernateCfgFilePath = "/src/main/resources/hibernate/hibernate.cfg.xml";
		}

		componentProperties.setProperty("configurationfile",
				this.hibernateCfgFilePath);

		if (StringUtils.isEmpty(this.packageName)) {
			this.packageName = new StringBuilder(
					String.valueOf(this.basePackage)).append(".domain")
					.toString();
		}
		componentProperties.setProperty("packagename", this.packageName);

		// for test
		if (StringUtils.isEmpty(this.hibernateRevengFilePath)) {
			this.hibernateRevengFilePath = "/target/test-classes/hibernate.reveng.xml";
		}
		componentProperties.setProperty("revengfile",
				this.hibernateRevengFilePath);

		componentProperties.setProperty("implementation", "jdbcconfiguration");
		componentProperties.setProperty("outputDirectory",
				this.baseDir.getAbsolutePath()
						+ "/target/appfuse/generated-sources");

		File revengFile = new File(this.projectHome,
				"target/test-classes/hibernate.reveng.xml");
		if (revengFile.exists() && getComponentProperty("revengfile") == null)
			componentProperties.setProperty("revengfile",
					"src/test/resources/hibernate.reveng.xml");
		if (getComponentProperty("revengfile") == null)
			componentProperties.setProperty("revengfile",
					"target/test-classes/hibernate.reveng.xml");

		setComponentProperties(componentProperties);
	}

	public void generateModel() throws Exception {
		File existingConfig = new File(this.projectHome,
				getComponentProperty("revengfile"));

		if (!existingConfig.exists()) {
			java.io.InputStream in = this.getClass().getResourceAsStream(
					"/templates/default/source/model/hibernate.reveng.ftl");
			StringBuffer configFile = new StringBuffer();
			try {
				InputStreamReader isr = new InputStreamReader(in);
				BufferedReader reader = new BufferedReader(isr);
				String line;
				while ((line = reader.readLine()) != null)
					configFile.append(line).append("\n");
				reader.close();
				getLog().debug(
						(new StringBuilder("Writing 'hibernate.reveng.xml' to "))
								.append(existingConfig.getPath()).toString());
				FileUtils.writeStringToFile(existingConfig,
						configFile.toString());
			} catch (IOException io) {
				throw new MojoFailureException(io.getMessage());
			}
		}

		if (this.hibernateCfgFile == null) {
			this.hibernateCfgFile = new File(this.projectHome,
					getComponentProperty("configurationfile"));
		}

		if (this.hibernateCfgFile.exists()) {
			// TODO [CASE] hibernate.cfg.xml contents are different from
		} else {
			try {
				// 1. generate hibernate.cfg.xml
				String hibernateCfgXml = null;
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
						+ this.passWord
						+ "</property>\n"
						+ "<property name=\"hibernate.connection.url\">"
						+ this.url
						+ "</property>\n"
						+ "<!--hibernate jdbc configuration-END-->";

				hibernateCfgXml = hibernateCfgXml.replace(
						"<!--hibernate jdbc configuration here-->",
						replaceString);

				File xmlFile = new File(this.projectHome, getComponentProperty(
						"configurationfile",
						"src/main/resources/hibernate/hibernate.cfg.xml"));

				FileUtils.writeStringToFile(xmlFile, hibernateCfgXml);

				XMLPrettyPrinter.prettyPrintFile(
						PrettyPrinter.getDefaultTidy(), xmlFile, xmlFile, true);

				getLog().info(
						(new StringBuilder(
								"Hibernate.cfg.xml is generated to project"))
								.toString());
			} catch (Exception e) {
				throw new HibernateException(
						"Hibernate.cfg.xml is not found in /src/main/resources/hibernate/.",
						e);
			}
		}

		super.doExecute();
	}

	public void executeInstaller() throws Exception {
		// add new package to spring context-hibernate.xml file
		if (this.daoframework.equals("hibernate")
				|| this.daoframework.equals("query")) {

			String packageLocation = StringHelper.replace(this.packageName,
					".", "/");

			File existingFile = new File(projectHome
					+ "/src/main/resources/spring/context-hibernate.xml");
			try {
				FileUtil.replaceStringXMLFilePretty(existingFile,
						"<!--Add new Packages to scan here-->", "<value>"
								+ packageLocation + "</value>");
				getLog().info(
						"Adding '"
								+ packageLocation
								+ "' packages to spring hibernate xml(context-hibernate.xml)...");
			} catch (Exception e) {
				if (this.daoframework.equals("hibernate")) {
					if (e instanceof FileNotFoundException)
						getLog().warn(
								"The process of adding new packages information in context-hibernate.xml is skipped.\n"
										+ "        context-hibernate.xml is not found in /src/main/resources/spring/.");
					else
						getLog().warn(
								"The process of adding new packages information in context-hibernate.xml is skipped.\n"
										+ "        <!--Add new Packages to scan here--> token is not found in /src/main/resources/spring/context-hibernate.xml.");
				}
			}
		}

	}

	protected Exporter configureExporter(Exporter exp)
			throws MojoExecutionException {
		// add output directory to compile roots
		this.project.addCompileSourceRoot(new File(getComponent()
				.getOutputDirectory()).getPath());

		// now set the extra properties for the POJO
		// Exporter
		POJOExporter exporter = (POJOExporter) super.configureExporter(exp);

		// Add custom template path if specified
		String[] templatePaths;
		if (templateDirectory != null) {
			templatePaths = new String[exporter.getTemplatePaths().length + 1];
			templatePaths[0] = templateDirectory;
			if (exporter.getTemplatePaths().length > 1) {
				for (int i = 1; i < exporter.getTemplatePaths().length; i++) {
					templatePaths[i] = exporter.getTemplatePaths()[i - 1];
				}
			}
		} else {
			templatePaths = exporter.getTemplatePaths();
		}

		exporter.setTemplatePath(templatePaths);
		exporter.setTemplateName("model/Pojo.ftl");
		exporter.getProperties().setProperty("package", this.packageName);
		exporter.getProperties().setProperty("ejb3",
				getComponentProperty("ejb3", "true"));
		exporter.getProperties().setProperty("jdk5",
				getComponentProperty("jdk5", "true"));

		if (isFullSource()) {
			exporter.getProperties().setProperty("appfusepackage",
					this.packageName);
		} else {
			exporter.getProperties().setProperty("appfusepackage",
					"org.anyframe.generic");
		}

		return exporter;
	}

	protected Exporter createExporter() {

		AnyframePOJOExporter exporter = new AnyframePOJOExporter();
		exporter.setPropertiesIO(pio);
		exporter.setArtifactCollector(new AnyframeArtifactCollector());

		return exporter;
	}

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

	private String getHibernateCfgXml() throws Exception {
		InputStream in = new FileInputStream(new File(this.templateDirectory
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
			// throw new MojoFailureException(io.getMessage());
		}
		return configFile.toString();
	}

	private void setGenLocation(String location) {
		addDefaultComponent(location, "configuration", false);
		addDefaultComponent(location, "jdbcconfiguration", true);
		addDefaultComponent(location, "annotationconfiguration", true);
	}

	private void setProjectProperties(Properties properties) {
		Properties mavenProperties = this.project.getProperties();
		Iterator<Object> itr = properties.keySet().iterator();

		while (itr.hasNext()) {
			String key = (String) itr.next();
			mavenProperties.setProperty(key, (String) properties.get(key));
		}
	}

	private void setComponentProperties(Properties properties) {
		Map<String, String> componentProperties = getComponentProperties();

		Iterator<Object> itr = properties.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();

			componentProperties.put(key, (String) properties.get(key));
		}
	}

	private void setTable() throws MojoExecutionException, MojoFailureException {

		getLog().info("Input Table Name is " + this.table);
		if (getComponentProperty("revengfile") == null) {
			getComponentProperties().put("revengfile",
					"target/test-classes/hibernate.reveng.xml");
		}

		File existingConfig = new File(this.projectHome,
				getComponentProperty("revengfile"));
		try {
			InputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(templateHome
						+ "/" + this.templateType
						+ "/source/model/hibernate.reveng.ftl"));
			} catch (Exception e) {
				// this is for test case execution
				in = this.getClass().getResourceAsStream(
						"/templates/default/source/model/hibernate.reveng.ftl");
			}
			// InputStream in =
			// this.getClass().getResourceAsStream("/anyframe/model/hibernate.reveng.ftl");

			StringBuffer configFile = new StringBuffer();
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(isr);
			String line;

			// get schema information from application
			// properties file
			while ((line = reader.readLine()) != null) {

				if (line.contains("<schema-selection match-schema=")) {
					if (this.dbType.equalsIgnoreCase("syabse"))
						continue;
				}
				line = line.replace("${schema}", this.dbSchema);
				configFile.append(line).append("\n");
			}
			reader.close();

			getLog().debug(
					"Writing 'hibernate.reveng.xml' to "
							+ existingConfig.getPath());
			FileUtils.writeStringToFile(existingConfig, configFile.toString());

			// replace string text
			String tableFilterString = "<table-filter match-name=\"${tableName}\"/>";
			if (this.table.equals("*")) {
				tableFilterString = tableFilterString.replace("${tableName}",
						".*");
			} else if (this.table.indexOf(",") == -1) {
				tableFilterString = tableFilterString.replace("${tableName}",
						this.table);
			} else {
				StringTokenizer tokenizer = new StringTokenizer(this.table, ",");
				String tempString = "";
				while (tokenizer.hasMoreTokens()) {
					tempString = tempString
							+ tableFilterString.replace("${tableName}",
									tokenizer.nextToken()) + "\n";
				}
				tableFilterString = tempString;
			}

			getLog().debug(
					"Writing table filter string [" + tableFilterString
							+ "] to 'hibernate.reveng.xml'");

			FileUtil.replaceStringXMLFilePretty(existingConfig,
					"<!--Add table names to generate domain classes-->",
					tableFilterString);

		} catch (IOException io) {
			throw new MojoFailureException(io.getMessage());
		} catch (Exception e) {
			throw new MojoFailureException(e.getMessage());
		}
	}

	public MavenProject getProject() {
		return project;
	}

	// getter, setter for ANT Task
	public void setTable(String table) {
		this.table = table;
	}

	public void setProjectHome(String projectHome) {
		this.projectHome = projectHome;
	}

	public void setMavenProject(MavenProject project) {
		this.project = project;
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

	// for test
	public void setHibernateCfgFilePath(String hibernateCfgFilePath) {
		this.hibernateCfgFilePath = hibernateCfgFilePath;
	}

	public void setHibernateRevengFilePath(String hibernateRevengFilePath) {
		this.hibernateRevengFilePath = hibernateRevengFilePath;
	}

	public void setHibernateCfgFile(File hibernateCfgFile) {
		this.hibernateCfgFile = hibernateCfgFile;
	}

	public void setPluginInfoManager(PluginInfoManager pluginInfoManager) {
		this.pluginInfoManager = pluginInfoManager;
	}
}
