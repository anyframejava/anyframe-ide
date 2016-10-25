/*
 * Copyright 2002-2008 the original author or authors.
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
package org.anyframe.ide.command.ant.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipFile;

import org.anyframe.ide.command.common.DefaultPluginCatalogManager;
import org.anyframe.ide.command.common.DefaultPluginInfoManager;
import org.anyframe.ide.command.common.DefaultPluginInstaller;
import org.anyframe.ide.command.common.DefaultPluginPomManager;
import org.anyframe.ide.command.common.PluginCatalogManager;
import org.anyframe.ide.command.common.PluginInfoManager;
import org.anyframe.ide.command.common.PluginInstaller;
import org.anyframe.ide.command.common.catalog.ArchetypeCatalogDataSource;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.FileSet;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.codehaus.plexus.util.StringUtils;

/**
 * This is an GenerateArchetypeTask class. This task is for create a sample
 * project based on anyframe-basic-archetype
 * 
 * @author SoYon Lim
 */
public class GenerateArchetypeTask extends AbstractPluginTask {
	PluginInstaller pluginInstaller;
	PluginInfoManager pluginInfoManager;
	ArchetypeArtifactManager archetypeArtifactManager;
	PluginCatalogManager pluginCatalogManager;
	ArchetypeCatalogDataSource archetypeCatalogDataSource;
	DefaultPluginPomManager pluginPomManager;

	/**
	 * project name for user-defined project
	 */
	private String pjtname = "";

	/**
	 * project version for user-defined project
	 */
	private String pjtversion = "";

	/**
	 * archetype version to install
	 */
	private String archetypeVersion = "";

	/**
	 * archetype groupId
	 */
	private String archetypeGroupId = "";

	/**
	 * archetype artifactId
	 */
	private String archetypeArtifactId = "";

	/**
	 * archetype style
	 */
	private String pjttype = "web";

	/**
	 * offline mode
	 */
	private String offline = "False";

	private boolean offlineMode = false;

	/**
	 * main method for executing custom task
	 */
	public void execute() {
		ClassLoader originalClassLoader = Thread.currentThread()
				.getContextClassLoader();
		try {
			if (isEmpty("offline", getOffline())) {
				setOffline("False");
			}

			this.offlineMode = new Boolean(this.offline.substring(0, 1)
					.toUpperCase() + offline.substring(1).toLowerCase())
					.booleanValue();

			initialize(this.offlineMode);
			lookupComponents();
			doExecute();
		} catch (Exception e) {
			throw new BuildException(e.getMessage());
		} finally {
			// plexus container changes context classloader of current thread,
			// so recover
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

	/**
	 * look up essential components which can install, uninstall, etc.
	 */
	public void lookupComponents() {
		pluginInstaller = (DefaultPluginInstaller) getPluginContainer().lookup(
				DefaultPluginInstaller.class.getName());
		pluginInfoManager = (DefaultPluginInfoManager) getPluginContainer()
				.lookup(DefaultPluginInfoManager.class.getName());
		archetypeArtifactManager = (ArchetypeArtifactManager) getPluginContainer()
				.lookup(ArchetypeArtifactManager.ROLE);
		pluginCatalogManager = (DefaultPluginCatalogManager) getPluginContainer()
				.lookup(DefaultPluginCatalogManager.class.getName());
		archetypeCatalogDataSource = (ArchetypeCatalogDataSource) getPluginContainer()
				.lookup(ArchetypeCatalogDataSource.class.getName());
		pluginPomManager = (DefaultPluginPomManager) getPluginContainer()
				.lookup(DefaultPluginPomManager.class.getName());
	}

	/**
	 * main method for executing GenerateArchetypeTask this task is executed
	 * when you input 'anyframe create-project [-options]'
	 */
	public void doExecute() throws BuildException {
		ClassLoader old = Thread.currentThread().getContextClassLoader();

		try {
			// 1. check validation
			checkValidation();

			// 2. initialize velocity context
			Context context = prepareVelocityContext();

			// 3. find archetype jar file
			File archetypeJar = pluginInfoManager.getPluginFile(getRequest(),
					getArchetypeGroupId(), getArchetypeArtifactId(),
					getArchetypeVersion());
			ClassLoader archetypeJarLoader = archetypeArtifactManager
					.getArchetypeJarLoader(archetypeJar);
			Thread.currentThread().setContextClassLoader(archetypeJarLoader);

			// 4. make a target directory
			File targetDir = new File(getTarget()
					+ CommonConstants.fileSeparator + pjtname);
			targetDir.mkdirs();

			// 5. merge template and copy merged file to output directory
			processTemplates(context, targetDir, archetypeJar);

			// 6. change project.mf
			File metadataFile = new File(new File(getTarget(), getPjtname())
					+ CommonConstants.METAINF, CommonConstants.METADATA_FILE);
			boolean pomHandling = true;
			if (metadataFile.exists()) {
				PropertiesIO pio = new PropertiesIO(
						metadataFile.getAbsolutePath());

				pio.setProperty(CommonConstants.ANYFRAME_HOME,
						getAnyframeHome());
				pio.setProperty(CommonConstants.PROJECT_HOME, getTarget()
						+ CommonConstants.fileSeparator + getPjtname());
				pio.setProperty(CommonConstants.PROJECT_BUILD_TYPE,
						CommonConstants.PROJECT_BUILD_TYPE_ANT);
				pio.setProperty(CommonConstants.OFFLINE, new Boolean(
						this.offlineMode).toString());
				pio.write();

				pomHandling = false;
			}

			// 7. in case of service type project, copy dependent libararies and
			// update .classpath

			Model model = pluginPomManager.readPom(pluginInfoManager
					.getPluginResource("archetype-resources/"
							+ Constants.ARCHETYPE_POM, archetypeJar));
			Properties currentProperties = model.getProperties();

			if (getPjttype().equals(CommonConstants.PROJECT_TYPE_SERVICE)) {
				// 7.1 copy dependent libraries to project.home/lib folder
				pluginInstaller.processDependencyLibs(getRequest(),
						getPjttype(), new File(getTarget(), pjtname),
						archetypeJar, currentProperties);

				// 7.2 change .classpath file
				pluginInstaller.processClasspath(getRequest(), new File(
						getTarget(), pjtname), null, pomHandling, currentProperties);
			}
			// 8. remove pom.xml
			removePom();

		} catch (Exception e) {
			log("Fail to execute GenerateArchetypeTask", e, Project.MSG_ERR);
			throw new BuildException(e.getMessage());
		} finally {
			Thread.currentThread().setContextClassLoader(old);
		}
	}

	/**
	 * 1. check if target location is a valid. If target/META-INF folder
	 * includes plugins.xml, then can't continue to install 2. check input
	 * argument 'pjtname'. If pjtname is null or empty string, set a default
	 * value ('myproject')
	 * 
	 * @throws BuildException
	 *             when there is a install information under the target folder
	 *             or pjtname is null or empty string
	 */
	private void checkValidation() throws Exception {
		File pluginsXML = new File(getTarget() + CommonConstants.fileSeparator
				+ pjtname + CommonConstants.METAINF,
				CommonConstants.PLUGIN_INSTALLED_FILE);

		if (pluginsXML.exists()) {
			throw new BuildException(
					"You already generated a basic or service archetype. Try anyframe -help.");
		}
		setInitialValue();
	}

	/**
	 * set a new velocity context
	 * 
	 * @return velocity context
	 */
	private Context prepareVelocityContext() throws Exception {
		pluginInstaller.initializeVelocity();

		VelocityContext context = new VelocityContext();
		context.put("package", getPackage());
		context.put("artifactId", pjtname);
		context.put("version", pjtversion);

		return context;
	}

	/**
	 * merge plugin resources and copy merged files to target folder
	 * 
	 * @param context
	 *            velocity context
	 * @param targetDir
	 *            target project folder to generate a archetype
	 * @param archetypeJar
	 *            archetype binary file
	 */
	@SuppressWarnings("unchecked")
	private void processTemplates(Context context, File targetDir,
			File archetypeJar) throws Exception {
		// 1. get zipfile from archetype jar file
		ZipFile archetypeZip = archetypeArtifactManager
				.getArchetypeZipFile(archetypeJar);

		// 2. get archetype-descriptor for basic archetype
		ArchetypeDescriptor archetypeDescriptor = archetypeArtifactManager
				.getFileSetArchetypeDescriptor(archetypeJar);

		// 3. get all file names from archetypeFile
		List<String> fileNames = FileUtil.resolveFileNames(archetypeJar);

		List<FileSet> fileSets = archetypeDescriptor.getFileSets();

		for (FileSet fileSet : fileSets) {
			log("Processing filesets in [" + CommonConstants.METAINF_ANYFRAME
					+ CommonConstants.PLUGIN_FILE + "]", Project.MSG_DEBUG);

			// 3.1 scan resources
			List<String> templates = FileUtil.findFiles(
					fileNames,
					Constants.ARCHETYPE_RESOURCES
							+ CommonConstants.fileSeparator
							+ fileSet.getDirectory(), fileSet.getIncludes(),
					fileSet.getExcludes());

			List<String> changedTemplates = new ArrayList<String>();
			for (String template : templates) {
				template = StringUtils.replaceOnce(template,
						Constants.ARCHETYPE_RESOURCES,
						CommonConstants.PLUGIN_RESOURCES);
				changedTemplates.add(template);
			}

			// 3.2 make directory for copying archetype template
			pluginInstaller.getOutput(context, targetDir,
					fileSet.getDirectory(), "", fileSet.isPackaged(),
					(String) context.get("package")).mkdirs();
			log("Copying fileset " + fileSet, Project.MSG_DEBUG);

			// 3.3 merge template and copy files to output directory
			pluginInstaller.processTemplate(context, targetDir, archetypeZip,
					fileSet.getDirectory(), (String) context.get("package"),
					fileSet.isPackaged(), fileSet.isFiltered(), templates,
					changedTemplates, getEncoding());
			log("Copied " + changedTemplates.size() + " files",
					Project.MSG_DEBUG);
		}
	}

	/**
	 * set initial value if input argument is empty string or nullF
	 */
	private void setInitialValue() {
		if (isEmpty("pjtname", getPjtname()))
			setPjtname("myproject");

		if (isEmpty("package", getPackage()))
			setPackage(getPjtname());

		if (isEmpty("pjttype", getPjttype()))
			setPjttype("web");

		if (isEmpty("archetypeGroupId", getArchetypeGroupId()))
			setArchetypeGroupId(CommonConstants.ARCHETYPE_GROUP_ID);

		if (isEmpty("archetypeArtifactId", getArchetypeArtifactId())) {
			if (getPjttype().equals("web"))
				setArchetypeArtifactId(CommonConstants.ARCHETYPE_BASIC_ARTIFACT_ID);
			else
				setArchetypeArtifactId(CommonConstants.ARCHETYPE_SERVICE_ARTIFACT_ID);
		}
		this.pjtversion = "1.0-SNAPSHOT";

		if (isEmpty("archetypeVersion", getArchetypeVersion()))
			try {
				setArchetypeVersion(archetypeCatalogDataSource
						.getLatestArchetypeVersion(getRequest(),
								getArchetypeArtifactId(), "ant",
								getAnyframeHome()));
			} catch (Exception e) {
				log("Error setting initial value : " + e.getMessage(),
						Project.MSG_ERR);
				throw new BuildException(
						"You failed to get an archetype verion. Please check the archetype version.");
			}
	}

	/**
	 * change classpath entries
	 * 
	 * @param archetypeJar
	 *            archetype binary file
	 */
	public void processClasspath(File archetypeJar) throws Exception {
		List<Dependency> dependencyList = pluginPomManager
				.getDependencies(archetypeJar);

		List<String> classentries = new ArrayList<String>();
		StringBuffer dependencies = new StringBuffer();

		for (Dependency dependencyInfo : dependencyList) {
			dependencies.append("<classpathentry kind=\"lib\" path=\"" + "lib/"
					+ dependencyInfo.getArtifactId() + "-"
					+ dependencyInfo.getVersion() + ".jar\"/> \n");

			classentries.add(dependencyInfo.getGroupId() + ","
					+ dependencyInfo.getArtifactId() + ","
					+ dependencyInfo.getVersion());
		}

		File classpathFile = new File(getTarget()
				+ CommonConstants.fileSeparator + pjtname, ".classpath");

		FileUtil.replaceFileContent(
				classpathFile,
				"<!--Add new classpathentry here-->",
				"</classpath>",
				"<!--Add new classpathentry here-->\n</classpath>",
				"<!--Add new classpathentry here-->",
				"<!--Add new classpathentry here-->\n"
						+ dependencies.toString());
	}

	/**
	 * merge pom file of archetype with temporary pom file
	 */
	private void removePom() throws Exception {
		// 1. make a pom_foundation.xml file into target/temp folder
		File temporaryPomFile = new File(getTarget()
				+ CommonConstants.fileSeparator + pjtname, "pom.xml");

		// 2. remove pom file
		FileUtil.deleteFile(temporaryPomFile);
	}

	/*********************************************************************/
	/************ getter, setter *****************************************/
	/*********************************************************************/
	public String getPjtname() {
		return pjtname;
	}

	public void setPjtname(String pjtname) {
		this.pjtname = pjtname;
	}

	public String getArchetypeGroupId() {
		return archetypeGroupId;
	}

	public void setArchetypeGroupId(String archetypeGroupId) {
		this.archetypeGroupId = archetypeGroupId;
	}

	public String getArchetypeArtifactId() {
		return archetypeArtifactId;
	}

	public void setArchetypeArtifactId(String archetypeArtifactId) {
		this.archetypeArtifactId = archetypeArtifactId;
	}

	public String getArchetypeVersion() {
		return archetypeVersion;
	}

	public void setArchetypeVersion(String archetypeVersion) {
		this.archetypeVersion = archetypeVersion;
	}

	public String getPjttype() {
		return pjttype;
	}

	public void setPjttype(String pjttype) {
		this.pjttype = pjttype;
	}

	public String getOffline() {
		return offline;
	}

	public void setOffline(String offline) {
		this.offline = offline;
	}

	// for test
	public PluginInfoManager getPluginInfoManager() {
		return pluginInfoManager;
	}
}
