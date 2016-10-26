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
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.appfuse.mojo.HibernateExporterMojo;
import org.appfuse.tool.AppFuseExporter;
import org.appfuse.tool.ArtifactInstaller;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.hibernate.tool.hbm2x.Exporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Generates Java classes from set of annotated POJOs. Use -DdisableInstallation
 * to prevent installation. If using this goal in a "core" module or project,
 * only DAOs and Managers will be created. For "web" modules, the same principle
 * applies.
 * 
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 * @goal gen
 * @phase generate-sources
 * @execute phase="compile"
 * @author modified by SooYeon Park
 */
public class AnyframeGeneratorMojo extends HibernateExporterMojo {
	private static Logger log = LoggerFactory.getLogger(AnyframeGeneratorMojo.class);

	boolean generateCoreOnly;
	boolean generateWebOnly;
	String pojoName;
	String pojoNameLower;

	/**
	 * This is a prompter that can be user within the maven framework.
	 * 
	 * @component
	 */
	Prompter prompter;

	/**
	 * The path where the generated artifacts will be placed. This is
	 * intentionally not set to the default location for maven generated
	 * sources. This is to keep these files out of the eclipse/idea generated
	 * sources directory as the intention is that these files will be copied to
	 * a source directory to be edited and modified and not re generated each
	 * time the plugin is run. If you want to regenerate the files each time you
	 * build the project just set this value to
	 * ${basedir}/target/generated-sources or set the flag on eclipse/idea
	 * plugin to include this file in your project file as a source directory.
	 * 
	 * @parameter expression="${appfuse.destinationDirectory}"
	 *            default-value="${basedir}"
	 * @noinspection UnusedDeclaration
	 */
	private String destinationDirectory;

	/**
	 * The directory containing the source code.
	 * 
	 * @parameter expression="${appfuse.sourceDirectory}"
	 *            default-value="${basedir}/target/appfuse/generated-sources"
	 * @noinspection UnusedDeclaration
	 */
	private String sourceDirectory;

	/**
	 * Allows disabling installation - for tests and end users that don't want
	 * to do a full installation
	 * 
	 * @parameter expression="${appfuse.disableInstallation}"
	 *            default-value="false"
	 */
	private boolean disableInstallation;

	/**
	 * @parameter expression="${appfuse.genericCore}" default-value="true"
	 * @noinspection UnusedDeclaration
	 */
	private boolean genericCore;

	/**
	 * Default constructor.
	 */
	public AnyframeGeneratorMojo() {
		addDefaultComponent("target/appfuse/generated-sources",
				"configuration", false);
		addDefaultComponent("target/appfuse/generated-sources",
				"annotationconfiguration", true);
	}

	// --------------------- Interface ExporterMojo ---------------------

	/**
	 * Returns <b>gen</b>.
	 * 
	 * @return String goal's name
	 */
	public String getName() {
		return "gen";
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// if project is of type "pom", throw an error
		if (getProject().getPackaging().equalsIgnoreCase("pom")) {
			String errorMsg = "This plugin must be run from a jar or war project, please run it from a jar or war project.";
			throw new MojoFailureException(errorMsg);
		}

		pojoName = System.getProperty("entity");

		if (pojoName == null) {
			try {
				pojoName = prompter
						.prompt("What is the name of your pojo (i.e. Person)?");
			} catch (PrompterException pe) {
				log.error("When pojoName is null, prompter has to show message. : "
						+ pe.getMessage());
			}
		}

		if (pojoName == null || "".equals(pojoName.trim())) {
			throw new MojoExecutionException(
					"You must specify an entity name to continue.");
		}

		String daoFramework = getProject().getProperties().getProperty(
				CommonConstants.APP_DAOFRAMEWORK_TYPE);

		// If dao.framework is jpa, change to jpaconfiguration and
		// persistence.xml should be found in classpath.
		// No other configuration is needed.
		if (daoFramework.indexOf("jpa") > -1) {
			getComponentProperties().put("implementation", "jpaconfiguration");
			checkEntityExists();
		}

		// for war projects that have a parent pom, don't reset classpath
		// this is to allow using hibernate.cfg.xml from core module
		if (getProject().getPackaging().equals("war")
				&& getProject().hasParent()) {
			// assume first module in parent project has hibernate.cfg.xml
			String moduleName = (String) getProject().getParent().getModules()
					.get(0);
			String pathToParent = getProject().getOriginalModel().getParent()
					.getRelativePath();
			pathToParent = pathToParent
					.substring(0, pathToParent
							.lastIndexOf(CommonConstants.fileSeparator) + 1);
			getLog().debug("Assuming '"
					+ moduleName
					+ "' has hibernate.cfg.xml in its src/main/resources directory");
			getComponentProperties().put(
					"configurationfile",
					getProject().getBasedir() + "/" + pathToParent + moduleName
							+ "/src/main/resources/hibernate.cfg.xml");
		}

		if (getComponentProperty("configurationfile") == null) {
			getComponentProperties().put("configurationfile",
					"src/main/resources/hibernate.cfg.xml");
		}

		// modified by changje kim 2009.08.07
		// removed the code to add entity in hibernate configuration
		// this function is replaced to addAllEntitiesToHibernateCfgXml() method
		// of AnyframeCodeGenerator class
		super.execute();
	}

	/**
	 * @see org.appfuse.mojo.HibernateExporterMojo#configureExporter(org.hibernate.tool.hbm2x.Exporter)
	 */
	protected Exporter configureExporter(Exporter exp)
			throws MojoExecutionException {
		// Read in AppFuseExporter#configureExporter to decide if a class should
		// be generated or not
		System.setProperty("appfuse.entity", pojoName);

		// add output directory to compile roots
		getProject().addCompileSourceRoot(
				new File(getComponent().getOutputDirectory()).getPath());

		// now set the extra properties for the AppFuseExporter
		AppFuseExporter exporter = (AppFuseExporter) super
				.configureExporter(exp);
		exporter.getProperties().setProperty("ejb3",
				getComponentProperty("ejb3", "true"));
		exporter.getProperties().setProperty("jdk5",
				getComponentProperty("jdk5", "true"));

		if (generateCoreOnly) {
			exporter.getProperties().setProperty("generate-core", "true");
		} else if (generateWebOnly) {
			exporter.getProperties().setProperty("generate-web", "true");
		}

		// AppFuse-specific values
		exporter.getProperties().setProperty("basepackage",
				getProject().getGroupId());
		exporter.getProperties().setProperty("daoframework",
				getProject().getProperties().getProperty(CommonConstants.APP_DAOFRAMEWORK_TYPE));
		exporter.getProperties().setProperty("webframework",
				getProject().getProperties().getProperty(CommonConstants.WEB_FRAMEWORK));
		exporter.getProperties().setProperty("packaging",
				getProject().getPackaging());
		exporter.getProperties().setProperty("genericcore",
				String.valueOf(genericCore));

		if (isFullSource())
			exporter.getProperties().setProperty("appfusepackage",
					getProject().getGroupId());
		else {
			exporter.getProperties().setProperty("appfusepackage",
					"org.appfuse");
		}

		return exporter;
	}

	/**
	 * Executes the plugin in an isolated classloader.
	 * 
	 * @throws MojoExecutionException
	 *             When there is an erro executing the plugin
	 */
	@Override
	protected void doExecute() throws MojoExecutionException {
		super.doExecute();

		if (System.getProperty("disableInstallation") != null) {
			disableInstallation = Boolean.valueOf(System
					.getProperty("disableInstallation"));
		}

		// allow installation to be supressed when testing
		if (!disableInstallation) {
			ArtifactInstaller installer = new ArtifactInstaller(getProject(),
					pojoName, sourceDirectory, destinationDirectory,
					genericCore);
			installer.execute();
		}
	}

	/**
	 * Instantiates a org.appfuse.tool.AppFuseExporter object.
	 * 
	 * @return POJOExporter
	 */
	protected Exporter createExporter() {
		return new AppFuseExporter();
	}

	protected void setGenerateCoreOnly(boolean generateCoreOnly) {
		this.generateCoreOnly = generateCoreOnly;
	}

	protected void setGenerateWebOnly(boolean generateWebOnly) {
		this.generateWebOnly = generateWebOnly;
	}

	private void checkEntityExists() throws MojoFailureException {
		// allow check to be bypassed when -Dentity.check=false
		if (!"false".equals(System.getProperty("entity.check"))) {
			String pathToModelPackage = "src" + CommonConstants.fileSeparator
					+ "main" + CommonConstants.fileSeparator + "java"
					+ CommonConstants.fileSeparator;

			if (getProject().getPackaging().equals("war")
					&& getProject().hasParent()) {
				String moduleName = (String) getProject().getParent()
						.getModules().get(0);
				String pathToParent = getProject().getOriginalModel()
						.getParent().getRelativePath();
				pathToParent = pathToParent.substring(0,
						pathToParent.lastIndexOf('/') + 1);
				pathToParent = pathToParent.replaceAll("/",
						CommonConstants.fileSeparator);
				pathToModelPackage = getProject().getBasedir()
						+ CommonConstants.fileSeparator + pathToParent
						+ moduleName + "/" + pathToModelPackage;
			}

			// refactor to check classpath instead of filesystem
			String groupIdAsPath = getProject().getGroupId().replace(".",
					CommonConstants.fileSeparator);
			File modelPackage = new File(pathToModelPackage + groupIdAsPath
					+ CommonConstants.fileSeparator + "model");
			boolean entityExists = false;

			if (modelPackage.exists()) {
				String[] entities = modelPackage.list();
				for (String entity : entities) {
					getLog().debug("Found '" + entity + "' in model package...");
					if (entity.contains(pojoName + ".java")) {
						entityExists = true;
						break;
					}
				}
			} else {
				getLog().error(
						"The path '" + pathToModelPackage + groupIdAsPath
								+ CommonConstants.fileSeparator
								+ "model' does not exist.");
			}

			if (!entityExists) {
				throw new MojoFailureException("The '" + pojoName
						+ "' entity does not exist in '" + modelPackage + "'.");
			} else {
				// Entity found, make sure it has @Entity annotation
				try {
					File pojoFile = new File(modelPackage
							+ CommonConstants.fileSeparator + pojoName
							+ ".java");
					String entityAsString = FileUtils
							.readFileToString(pojoFile);
					if (!entityAsString.contains("@Entity")) {
						String msg = "Entity '"
								+ pojoName
								+ "' found, but it doesn't contain an @Entity annotation. Please add one.";
						throw new MojoFailureException(msg);
					}
				} catch (IOException io) {
					throw new MojoFailureException("Class '" + pojoName
							+ ".java' not found in '" + modelPackage + "'");
				}
			}
		}
	}

	public class POJOSearcher extends DefaultHandler {
		private String pojoName;
		private boolean foundPojo;
		private String xmlString;

		public POJOSearcher(String xmlString) {
			this.xmlString = xmlString;
		}

		public boolean searchForPojo(String pojoName) {
			this.pojoName = pojoName;
			this.foundPojo = false;

			SAXParserFactory spf = SAXParserFactory.newInstance();
			try {
				SAXParser sp = spf.newSAXParser();
				sp.setProperty(
						"http://xml.org/sax/features/external-parameter-entities",
						false);

				sp.parse(new InputSource(new StringReader(xmlString)), this);
			} catch (Exception ex) {
				log.error("Pojo searching is skipped. The reason is '"
						+ ex.getMessage() + "'.");
			}

			return foundPojo;
		}

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			super.startElement(uri, localName, name, attributes);
			if (name.equals("mapping")) {
				String classValue = attributes.getValue("class");
				if (classValue != null) {
					if (classValue.endsWith(pojoName)) {
						foundPojo = true;
					}
				}
			}
		}
	}
}
