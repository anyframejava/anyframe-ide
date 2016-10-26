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
package org.anyframe.ide.command.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyframe.ide.command.common.plugin.DependentPlugin;
import org.anyframe.ide.command.common.plugin.Exclude;
import org.anyframe.ide.command.common.plugin.Include;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.plugin.PluginInterceptorDependency;
import org.anyframe.ide.command.common.plugin.PluginResource;
import org.anyframe.ide.command.common.plugin.versioning.VersionComparator;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.anyframe.ide.command.common.util.PluginArchiver;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * This is a DefaultPluginPackager class. This class is for packaging a new
 * plugin with plugin resources.
 * 
 * @plexus.component 
 *                   role="org.anyframe.ide.command.common.DefaultPluginPackager"
 * @author Sooyeon Park
 */
public class DefaultPluginPackager extends AbstractLogEnabled implements
		PluginPackager {

	/**
	 * @plexus.requirement 
	 *                     role="org.anyframe.ide.command.common.DefaultPluginInfoManager"
	 */
	PluginInfoManager pluginInfoManager;

	/** @plexus.requirement */
	DefaultPluginPomManager pluginPomManager;

	/** constants */
	private static final String PACKAGE = "package";
	private static final String URL = "http://localhost:8080/";

	/**
	 * package a new plugin
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @throws Exception
	 */
	public void packagePlugin(ArchetypeGenerationRequest request, String baseDir)
			throws Exception {
		getLogger().debug(
				DefaultPluginPackager.class.getName() + " execution start.");
		PluginInfo pluginInfo = null;

		try {
			// 1. check validation about current project
			String tempDir = "target" + CommonConstants.fileSeparator + "temp";
			String metaInfDir = tempDir + CommonConstants.METAINF_ANYFRAME;
			String pluginResourcesDir = tempDir + CommonConstants.fileSeparator
					+ CommonConstants.PLUGIN_RESOURCES;
			PropertiesIO pio = checkProject(baseDir);
			pluginInfo = checkPluginBuildScript(baseDir);
			Map<String, File> allDependentPluginJars = checkPluginDependency(
					request, baseDir, pluginInfo);

			// 2. create target/temp folder in current project
			createTempFolder(baseDir, tempDir, metaInfDir, pluginResourcesDir);

			// 3. generate target/temp/pom.xml
			Model samplePjtPom = getSampleProjectPom(baseDir);
			Model pluginPom = generatePluginPomXML(baseDir, tempDir,
					pluginInfo, samplePjtPom);

			// 4. generate target/temp/plugin-resources/pom.xml
			generatePluginResourcesPomXML(request, baseDir, pluginResourcesDir,
					pluginInfo, samplePjtPom, allDependentPluginJars);

			// 5. copy sample codes into the plugin resources based on build
			// resources in plugin-build.xml
			File targetDir = new File(baseDir, pluginResourcesDir);
			List<String> fileNames = FileUtil.findFiles(baseDir, null, true);

			generatePluginResources(baseDir, targetDir, tempDir, fileNames,
					pluginInfo, pio);

			// 6. generate META-INF/plugin.xml from plugin-build.xml
			generatePluginXML(baseDir, metaInfDir, pluginInfo);

			// 7. generate JAR file from current project
			createArchive(baseDir, tempDir, pluginPom);

			System.out.println("'" + pluginInfo.getName()
					+ "' plugin is packaged successfully.");

		} catch (Exception e) {
			if (e instanceof CommandException)
				throw e;
			if (pluginInfo != null)
				throw new CommandException("Error occurred in packaging a '"
						+ pluginInfo.getName() + "' plugin. The reason is a '"
						+ e.getMessage() + "'.");
			else
				throw new CommandException(
						"Error occurred in packaging a plugin in this project '"
								+ baseDir + "'. The reason is a '"
								+ e.getMessage() + "'.");
		}
	}

	/**
	 * get sample project pom model from project pom.xml
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @return Model object which has a sample project pom.xml content
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private Model getSampleProjectPom(String baseDir) throws IOException,
			XmlPullParserException {
		File pomXml = new File(baseDir, Constants.ARCHETYPE_POM);
		return this.pluginPomManager.readPom(pomXml);
	}

	/**
	 * merge plugin resources and copy merged files to target folder
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @param targetDir
	 *            target project folder to generate plugin resources
	 * @param tempDir
	 *            temp folder path
	 * @param fileNames
	 *            resources in plugin jar file
	 * @param pluginInfo
	 *            plugin information object from plugin-build.xml file
	 * @param pio
	 *            project information properties
	 */
	private void generatePluginResources(String baseDir, File targetDir,
			String tempDir, List<String> fileNames, PluginInfo pluginInfo,
			PropertiesIO pio) throws Exception {
		getLogger().debug(
				"Call generatePluginResources() of DefaultPluginPackager");

		// 1. get package name, project name
		String packageName = pio.readValue(CommonConstants.PACKAGE_NAME);
		String projectName = pio.readValue(CommonConstants.PROJECT_NAME);

		// 2. get resource and generate plugin resources based on resources
		List<PluginResource> pluginResources = pluginInfo.getResources();

		for (PluginResource resource : pluginResources) {
			getLogger().debug(
					"Processing resources in ["
							+ CommonConstants.PLUGIN_BUILD_FILE + "]");
			// 2.1 get file list from current resource
			// set include
			List<Include> includeResources = resource.getIncludes();
			List<String> includes = new ArrayList<String>();
			for (Include include : includeResources) {
				includes.add(include.getName());
			}

			// set exclude
			List<Exclude> excludeResources = resource.getExcludes();
			List<String> excludes = new ArrayList<String>();
			List<String> replaces = new ArrayList<String>();

			for (Exclude exclude : excludeResources) {
				excludes.add(exclude.getName());
				if (exclude.isMerged()) {
					replaces.add(exclude.getName());
				}
			}
			// 2.2 scan resources
			List<String> templates = FileUtil.findFiles(fileNames, baseDir
					+ CommonConstants.fileSeparator + resource.getDir(),
					includes, excludes);

			// 2.3 make directory for copying plugin resource
			getOutput(baseDir, targetDir, resource.getDir(), "",
					resource.isPackaged(), packageName).mkdirs();
			getLogger().debug("Copying resource " + resource);

			// 2.4 merge template and copy files to output directory
			processTemplate(baseDir, targetDir, resource.getDir(),
					pluginInfo.getName(), packageName, projectName,
					resource.isPackaged(), resource.isFiltered(), templates);

			// 2.5 merge file
			if (replaces.size() > 0) {

				List<String> replaceFiles = FileUtil.findFiles(
						fileNames,
						baseDir + CommonConstants.fileSeparator
								+ resource.getDir(), replaces, null);

				processReplace(baseDir, targetDir, resource.getDir(),
						pluginInfo.getName(), packageName, projectName,
						resource.isPackaged(), resource.isFiltered(),
						replaceFiles);
				getLogger().debug("Merged " + replaces.size() + " files");
			}
		}

		// 3. copy classes and resources relative to interceptor
		if (pluginInfo.getInterceptor() != null
				&& !StringUtils.isEmpty(pluginInfo.getInterceptor()
						.getClassName())) {
			String interceptorClassName = pluginInfo.getInterceptor()
					.getClassName();
			String interceptorPackageName = interceptorClassName.substring(0,
					interceptorClassName.lastIndexOf("."));
			String interceptorPackageDir = FileUtil
					.changePackageForDir(interceptorPackageName);
			File srcDir = new File(baseDir + CommonConstants.fileSeparator
					+ "target" + CommonConstants.fileSeparator + "classes",
					interceptorPackageDir);
			File destDir = new File(baseDir + CommonConstants.fileSeparator
					+ tempDir, interceptorPackageDir);

			srcDir.mkdirs();
			destDir.mkdirs();

			File[] interceptorFiles = srcDir.listFiles();
			if (interceptorFiles == null || interceptorFiles.length == 0) {
				throw new CommandException(
						"Can not find resources relative to interceptor class '"
								+ interceptorClassName
								+ "'. Please check the interceptor class name.");
			} else {
				for (int i = 0; i < interceptorFiles.length; i++) {
					if (interceptorFiles[i].isFile()) {
						String fileName = interceptorFiles[i].getName();
						FileUtil.copyFile(new File(srcDir, fileName), new File(
								destDir, fileName));
					}
				}
			}
		}
	}

	/**
	 * merge a plugin resource and copy a merged file to target folder
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @param targetDir
	 *            output folder for templates
	 * @param resourceDir
	 *            resource folder to build plugin resources
	 * @param pluginName
	 *            plugin name
	 * @param packageName
	 *            project's base package name
	 * @param projectName
	 *            project's name
	 * @param packaged
	 *            whether a plugin resource has package (ex. java)
	 * @param filtered
	 *            whether a plugin resource will be merged with velocity context
	 * @param templates
	 *            plugin resources
	 */
	private void processTemplate(String baseDir, File targetDir,
			String resourceDir, String pluginName, String packageName,
			String projectName, boolean packaged, boolean filtered,
			List<String> templates) throws Exception {
		getLogger().debug("Call processTemplate() of DefaultPluginPackager");

		Iterator<String> templateItr = templates.iterator();

		resourceDir = StringUtils.replace(resourceDir, "/",
				CommonConstants.fileSeparator);
		resourceDir = StringUtils.replace(resourceDir, "\\",
				CommonConstants.fileSeparator);

		while (templateItr.hasNext()) {
			String template = templateItr.next();

			template = StringUtils.replaceOnce(template, baseDir
					+ CommonConstants.fileSeparator + resourceDir, "");
			File output = getOutput(baseDir, targetDir, resourceDir, template,
					packaged, packageName);
			output.getParentFile().mkdirs();

			// 1. copy template to output file
			copyFile(baseDir + CommonConstants.fileSeparator + resourceDir,
					template, output);

			// 2. merge velocity template with output file
			if (filtered)
				mergeTemplate(output, packageName, projectName);
		}

	}

	private void processReplace(String baseDir, File targetDir,
			String resourceDir, String pluginName, String packageName,
			String projectName, boolean packaged, boolean filtered,
			List<String> replaceFiles) throws Exception {
		getLogger().debug("Call processReplace() of DefaultPluginPackager");

		resourceDir = StringUtils.replace(resourceDir, "/",
				CommonConstants.fileSeparator);
		resourceDir = StringUtils.replace(resourceDir, "\\",
				CommonConstants.fileSeparator);

		if (replaceFiles.size() > 0) {
			Iterator<String> replaceItr = replaceFiles.iterator();
			while (replaceItr.hasNext()) {

				String replaceFileName = replaceItr.next();

				replaceFileName = StringUtils.replaceOnce(replaceFileName,
						baseDir + CommonConstants.fileSeparator + resourceDir,
						"");
				File output = getOutput(baseDir, targetDir, resourceDir,
						replaceFileName, packaged, packageName);

				output.getParentFile().mkdirs();

				copyFile(baseDir + CommonConstants.fileSeparator + resourceDir,
						replaceFileName, output);

				writeFileForReplaceRegion(pluginName, output);

				if (filtered)
					mergeTemplate(output, packageName, projectName);
			}
		}

	}

	/**
	 * write a file which has the contents between plugin name start tag and end
	 * tag
	 * 
	 * @param pluginName
	 *            plugin name
	 * @param output
	 *            output file
	 * @throws Exception
	 * @throws FileNotFoundException
	 */
	private void writeFileForReplaceRegion(String pluginName, File output)
			throws Exception, FileNotFoundException {
		// 1. find plugin name start, end tags
		Map<String, String> tokenMap = new HashMap<String, String>();

		if (output.getName().endsWith("." + CommonConstants.EXT_JAVA)) {
			tokenMap = FileUtil.findReplaceRegionOfClass(new FileInputStream(
					output), pluginName);
		} else {
			tokenMap = FileUtil.findReplaceRegion(new FileInputStream(output),
					pluginName);

		}
		// 2. get contents between start tag and end tag
		if (tokenMap.size() == 1) {
			// write file only with the contents
			FileUtil.writeStringToFile(output,
					tokenMap.values().toArray()[0].toString(), "UTF-8");
		} else if (tokenMap.size() == 0)
			FileUtil.deleteFile(output);
	}

	/**
	 * generate plugin.xml file using PluginInfo object which is from
	 * plugin-build.xml file
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @param metaInfDir
	 *            temp/META-INF folder
	 * @param pluginInfo
	 *            plugin information object from plugin-build.xml file
	 * @throws Exception
	 */
	private void generatePluginXML(String baseDir, String metaInfDir,
			PluginInfo pluginInfo) throws Exception {
		getLogger().debug("Call generatePluginXML() of DefaultPluginPackager");

		// 1. remove interceptor tag if there is no interceptor class name
		if (pluginInfo.getInterceptor() != null
				&& StringUtils.isEmpty(pluginInfo.getInterceptor()
						.getClassName()))
			pluginInfo.setInterceptor(null);

		// 2. write plugin.xml file from plugin info object
		FileUtil.getObjectToXML(pluginInfo, new File(baseDir
				+ CommonConstants.fileSeparator + metaInfDir,
				CommonConstants.PLUGIN_FILE));
	}

	/**
	 * create temp, temp/META-INF, temp/plugin-resources folder
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @param tempDir
	 *            temp folder path
	 * @param metaInfDir
	 *            temp/META-INF folder path
	 * @param pluginResourcesDir
	 *            temp/plugin-resources folder path
	 * @throws Exception
	 */
	private void createTempFolder(String baseDir, String tempDir,
			String metaInfDir, String pluginResourcesDir) throws Exception {
		getLogger().debug("Call createTempFolder() of DefaultPluginPackager");
		File tempFolder = new File(baseDir, tempDir);

		if (tempFolder.exists())
			FileUtil.deleteDir(tempFolder);

		FileUtil.makeDir(baseDir, metaInfDir);
		FileUtil.makeDir(baseDir, pluginResourcesDir);
	}

	/**
	 * generate pom.xml file in temp folder for new plugin
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @param tempDir
	 *            temp folder path
	 * @param pluginInfo
	 *            plugin information object from plugin-build.xml file
	 * @param samplePjtPom
	 *            Model object which has a sample project pom.xml content
	 * @return Model object which has a pom.xml content
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws Exception
	 */
	private Model generatePluginPomXML(String baseDir, String tempDir,
			PluginInfo pluginInfo, Model samplePjtPom)
			throws FileNotFoundException, IOException, XmlPullParserException,
			Exception {
		getLogger().debug(
				"Call generatePluginPomXML() of DefaultPluginPackager");
		Model pluginPom = new Model();
		if (samplePjtPom.getParent() != null) {
			Parent parent = new Parent();
			parent.setGroupId(samplePjtPom.getParent().getGroupId());
			parent.setArtifactId(samplePjtPom.getParent().getArtifactId());
			parent.setVersion(samplePjtPom.getParent().getVersion());
			pluginPom.setParent(parent);
		}
		pluginPom.setModelVersion(samplePjtPom.getModelVersion());
		pluginPom.setGroupId(samplePjtPom.getGroupId());
		pluginPom.setArtifactId(samplePjtPom.getArtifactId());
		pluginPom.setVersion(samplePjtPom.getVersion());
		pluginPom.setPackaging(CommonConstants.EXT_JAR);

		// get dependencies from plugin-build.xml
		if (pluginInfo.getInterceptor() != null) {
			List<PluginInterceptorDependency> interceptorDependencies = pluginInfo
					.getInterceptor().getDependencies();
			List<Dependency> dependencies = new ArrayList<Dependency>();
			Iterator<PluginInterceptorDependency> itr = interceptorDependencies
					.iterator();
			while (itr.hasNext()) {
				PluginInterceptorDependency interceptorDependency = itr.next();
				Dependency dependency = new Dependency();
				dependency.setGroupId(interceptorDependency.getGroupId());
				dependency.setArtifactId(interceptorDependency.getArtifactId());
				dependency.setVersion(interceptorDependency.getVersion());
				dependencies.add(dependency);
			}
			// set dependencies for pom.xml
			pluginPom.setDependencies(dependencies);
		}

		// write new pom.xml
		File newpomXml = new File(baseDir + CommonConstants.fileSeparator
				+ tempDir, Constants.ARCHETYPE_POM);
		this.pluginPomManager.writePom(pluginPom, newpomXml, newpomXml);

		return pluginPom;
	}

	/**
	 * generate pom.xml file in temp/plugin-resources folder for plugin
	 * dependencies
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @param pluginResourcesDir
	 *            temp/plugin-resources folder path
	 * @param pluginInfo
	 *            plugin information object from plugin-build.xml file
	 * @param samplePjtPom
	 *            Model object which has a sample project pom.xml content
	 * @param dependentPluginJars
	 *            dependent plugin jar files
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void generatePluginResourcesPomXML(
			ArchetypeGenerationRequest request, String baseDir,
			String pluginResourcesDir, PluginInfo pluginInfo,
			Model samplePjtPom, Map<String, File> dependentPluginJars)
			throws FileNotFoundException, IOException, XmlPullParserException,
			Exception {
		getLogger()
				.debug("Call generatePluginResourcesPomXML() of DefaultPluginPackager");
		Model pluginResourcesPom = new Model();
		pluginResourcesPom.setModelVersion(samplePjtPom.getModelVersion());
		pluginResourcesPom.setGroupId(samplePjtPom.getGroupId());
		pluginResourcesPom.setArtifactId(samplePjtPom.getArtifactId());
		pluginResourcesPom.setVersion(samplePjtPom.getVersion());
		pluginResourcesPom.setPackaging(CommonConstants.EXT_JAR);

		// calculate dependencies using dependent plugins information in
		// plugin-build.xml
		List<Dependency> samplePjtDependencies = samplePjtPom.getDependencies();
		Map<String, Dependency> samplePjtDependenciesMap = this.pluginPomManager
				.convertDependencyList(samplePjtDependencies);

		if (!pluginInfo.getName().equals(CommonConstants.CORE_PLUGIN)) {
			List<Dependency> dependentPluginDependencies = new ArrayList<Dependency>();

			// get all dependent plugin dependencies
			Iterator<String> keyItr = dependentPluginJars.keySet().iterator();
			while (keyItr.hasNext()) {
				dependentPluginDependencies
						.addAll(this.pluginPomManager
								.getDependencies(dependentPluginJars.get(keyItr
										.next())));
			}

			// plugin dependencies = samplePjtDependencies -
			// dependentPluginDependencies
			Map<String, Dependency> dependentPluginDependenciesMap = this.pluginPomManager
					.convertDependencyList(dependentPluginDependencies);

			Iterator<String> itr = dependentPluginDependenciesMap.keySet()
					.iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				if (samplePjtDependenciesMap.containsKey(key)) {
					samplePjtDependenciesMap.remove(key);
				}

			}
		}

		// extract interceptor's dependencies with 'interceptor' scope
		if (pluginInfo.getInterceptor() != null) {
			List<PluginInterceptorDependency> interceptorDependencies = pluginInfo
					.getInterceptor().getDependencies();
			for (PluginInterceptorDependency interceptorDependency : interceptorDependencies) {
				String scope = interceptorDependency.getScope();
				if (scope != null
						&& scope.equalsIgnoreCase(CommonConstants.SCOPE_INTERCEPTOR)) {
					samplePjtDependenciesMap.remove(interceptorDependency
							.getGroupId()
							+ ":"
							+ interceptorDependency.getArtifactId());
				}
			}
		}

		// set dependencies for pom.xml
		pluginResourcesPom.setDependencies(this.pluginPomManager
				.convertDependencyMap(samplePjtDependenciesMap));

		// set properties for pom.xml
		if (pluginInfo.getName().equals(CommonConstants.CORE_PLUGIN)
				|| pluginInfo.getName().equals(CommonConstants.SPRING_PLUGIN)) {
			pluginResourcesPom.setProperties(samplePjtPom.getProperties());
		}

		List<Dependency> list = pluginResourcesPom.getDependencies();

		// write new pom.xml
		File newpomXml = new File(baseDir + CommonConstants.fileSeparator
				+ pluginResourcesDir, Constants.ARCHETYPE_POM);
		this.pluginPomManager
				.writePom(pluginResourcesPom, newpomXml, newpomXml);
	}

	/**
	 * check current project information
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @return project information properties
	 * @throws Exception
	 */
	private PropertiesIO checkProject(String baseDir) throws Exception {
		getLogger().debug("Call checkProject() of DefaultPluginPackager");

		// 1. check a project.mf file
		File metadataFile = new File(new File(baseDir)
				+ CommonConstants.METAINF, CommonConstants.METADATA_FILE);
		if (!metadataFile.exists()) {
			throw new CommandException("Can not find a '"
					+ metadataFile.getAbsolutePath()
					+ "' file. Please check a location of your project.");
		}

		PropertiesIO pio = new PropertiesIO(metadataFile.getAbsolutePath());
		getLogger().debug(
				"Current plugin sample project directory is a " + baseDir);

		return pio;
	}

	/**
	 * check plugin build script xml file
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @return PluginInfo object from plugin-build.xml file
	 * @throws Exception
	 */
	private PluginInfo checkPluginBuildScript(String baseDir) throws Exception {
		getLogger().debug(
				"Call checkPluginBuildScript() of DefaultPluginPackager");

		// 1. check plugin-build.xml file
		File pluginBuildXMLFile = new File(baseDir,
				CommonConstants.PLUGIN_BUILD_FILE);

		if (!pluginBuildXMLFile.exists()) {
			throw new CommandException(
					"You need plugin-build.xml file to package your plugin. Please try activate-plugin first.");
		}

		// 2. create PluginInfo object from plugin-build.xml
		PluginInfo pluginInfo = (PluginInfo) FileUtil
				.getObjectFromXML(pluginBuildXMLFile);

		getLogger().debug(
				"Current plugin build script is located in "
						+ pluginBuildXMLFile.getAbsolutePath());

		return pluginInfo;
	}

	/**
	 * check plugin dependency relationship with essential plugin list and
	 * version
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @param pluginInfo
	 *            plugin detail information
	 * @return all dependent plugin binary files
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private Map<String, File> checkPluginDependency(
			ArchetypeGenerationRequest request, String baseDir,
			PluginInfo pluginInfo) throws Exception {
		getLogger().debug(
				"Call checkPluginDependency() of DefaultPluginPackager");

		// 1. get all dependent plugin jars
		Map<String, File> allDependentPluginJars = this.pluginInfoManager
				.getAllDependentPluginJars(request, baseDir, pluginInfo);

		// 2. check a plugin-installed.xml file
		File pluginInstalledFile = new File(baseDir + CommonConstants.METAINF,
				CommonConstants.PLUGIN_INSTALLED_FILE);

		if (!pluginInstalledFile.exists())
			throw new CommandException("Can not find a '"
					+ pluginInstalledFile.getAbsolutePath()
					+ "' file. Please check a location of your project.");

		// 3. check the match between the installed plugin version and dependent
		// plugin version range
		List<DependentPlugin> dependentPlugins = pluginInfo
				.getDependentPlugins();
		Map<String, PluginInfo> pluginMap = (Map<String, PluginInfo>) FileUtil
				.getObjectFromXML(pluginInstalledFile);

		Iterator<DependentPlugin> itr = dependentPlugins.iterator();
		while (itr.hasNext()) {
			DependentPlugin plugin = itr.next();
			if (pluginMap.get(plugin.getName()) == null)
				throw new CommandException(
						"You need to install the dependent '"
								+ plugin.getName() + "' plugin first. The '"
								+ plugin.getName()
								+ "' plugin is not installed yet.");
			if (!VersionComparator.isMatched(plugin.getVersion(), pluginMap
					.get(plugin.getName()).getVersion()))
				throw new CommandException(
						"You should modify the version range of dependent '"
								+ plugin.getName()
								+ "' plugin in plugin-build.xml file. The version of installed '"
								+ plugin.getName()
								+ "' plugin is not matched with the version range.");
		}

		// 4. check the dependency relationship with essential plugins
		boolean hasEssentialPlugins = this.pluginInfoManager
				.hasEssentialPluginDependency(baseDir, allDependentPluginJars);
		if (!hasEssentialPlugins && !pluginInfo.isEssential()) {
			throw new CommandException(
					"You need to have a dependency with essential plugins.(ex. core plugin)");
		}

		return allDependentPluginJars;
	}

	/**
	 * create JAR archive based on temp/pom.xml file
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @param tempDir
	 *            temp folder path
	 * @param pluginPom
	 *            Model object from temp/pom.xml file
	 * @throws Exception
	 */
	private void createArchive(String baseDir, String tempDir, Model pluginPom)
			throws Exception {
		getLogger().debug("Call createArchive() of DefaultPluginPackager");
		String groupId = pluginPom.getGroupId();
		String artifactId = pluginPom.getArtifactId();
		String version = pluginPom.getVersion();
		String finalName = artifactId + "-" + version;
		String outputDirectory = baseDir + CommonConstants.fileSeparator
				+ "target";

		File jarFile = new File(outputDirectory, finalName + ".jar");
		File pomXml = new File(baseDir + CommonConstants.fileSeparator
				+ tempDir, Constants.ARCHETYPE_POM);

		PluginArchiver archiver = new PluginArchiver();
		MavenArchiveConfiguration archive = new MavenArchiveConfiguration();
		archiver.setArchiver(new JarArchiver());
		archiver.setOutputFile(jarFile);
		archive.setForced(false);

		MavenProject project = new MavenProject();
		project.setGroupId(groupId);
		project.setArtifactId(artifactId);
		project.setVersion(version);
		project.setFile(pomXml);

		File contentDirectory = new File(outputDirectory, "temp");
		if (!contentDirectory.exists()) {
			getLogger().warn(
					"JAR will be empty - no content was marked for inclusion.");
		} else {
			archiver.getArchiver().addDirectory(contentDirectory);
		}
		archiver.createArchive(project, archive);
	}

	/**
	 * merge velocity template with output file. ex.) convert package name to
	 * ${package}, convert project name to ${artifactId}
	 * 
	 * @param output
	 *            merged file
	 * @param packageName
	 *            project's base package name
	 * @param projectName
	 *            project name
	 */
	private void mergeTemplate(File output, String packageName,
			String projectName) throws Exception {
		getLogger().debug("Call mergeTemplate() of DefaultPluginPackager");
		try {
			boolean isPretty = false;
			// 1. change package name to velocity template
			FileUtil.replaceFileContent(output, packageName, "${" + PACKAGE
					+ "}", isPretty);
			// 2. specific cases for all files
			mergeSpecificCases(output, projectName, isPretty);
		} catch (Exception e) {
			getLogger().warn(
					"Merging of " + output.getName() + " with template"
							+ " is skipped. The reason is a '" + e.getMessage()
							+ "'.");
		}
	}

	/**
	 * merge template with output file in specific cases
	 * 
	 * @param output
	 *            merged file
	 * @param projectName
	 *            project's name
	 * @param isPretty
	 *            whether output file will be printed with pretty format or not
	 * @throws Exception
	 */
	private void mergeSpecificCases(File output, String projectName,
			boolean isPretty) throws Exception {
		getLogger().debug("Call mergeSpecificCases() of DefaultPluginPackager");

		// 1. get file name
		String fileName = output.getName();

		Map<String, String> replaceMap = new HashMap<String, String>();
		// 2. change web context name to velocity template
		replaceMap.put(URL + projectName + "/", URL + "${artifactId}/");

		// 3. in case of java files, replace sharp character
		if (fileName.endsWith("." + CommonConstants.EXT_JAVA)) {
			// replaceMap.put("#{", "${" + CommonConstants.VELOCITY_SHARP_BRACE
			// + "}");
			// replaceMap.put("#", "${" + CommonConstants.VELOCITY_SHARP + "}");

			String keyword = CommonConstants.VELOCITY_SUPPORT;
			Map<String, String> tokenMap = FileUtil.findReplaceRegionOfClass(
					new FileInputStream(output), keyword);
			Iterator<String> itr = tokenMap.keySet().iterator();

			while (itr.hasNext()) {
				String token = itr.next();

				String value = tokenMap.get(token);
				String replaceValue = replaceEscapeChar(value);

				String startToken = "//" + keyword + "-" + token + "-START";
				String endToken = "//" + keyword + "-" + token + "-END";

				FileUtil.replaceFileContent(output, startToken, endToken,
						startToken + endToken, "#set($" + token + "=\""
								+ replaceValue + "\")${" + token + "}",
						isPretty);
			}

		}

		// 4. replace file content
		Iterator<String> itr = replaceMap.keySet().iterator();
		while (itr.hasNext()) {
			String token = itr.next();
			String value = replaceMap.get(token);

			FileUtil.replaceFileContent(output, token, value, isPretty);
		}

		// 5. in case of xml files which have dynamic sql statements, replace
		// comments
		if (fileName.endsWith("." + CommonConstants.EXT_XML)) {
			// 4.1 find plugin name start, end tags
			String keyword = CommonConstants.VELOCITY_SUPPORT;
			Map<String, String> tokenMap = FileUtil.findReplaceRegion(
					new FileInputStream(output), keyword);
			Iterator<String> itr1 = tokenMap.keySet().iterator();
			while (itr1.hasNext()) {
				String token = itr1.next();

				String value = tokenMap.get(token);
				String replaceValue = replaceEscapeChar(value);

				String startToken = "<!--" + keyword + "-" + token
						+ "-START-->";
				String endToken = "<!--" + keyword + "-" + token + "-END-->";

				FileUtil.replaceFileContent(output, startToken, endToken,
						startToken + endToken, "#set($" + token + "=\""
								+ replaceValue + "\")${" + token + "}",
						isPretty);
			}
		}

		FileWriter writer = new FileWriter(output, true);
		writer.flush();
		writer.close();
	}

	private String replaceEscapeChar(String token) throws Exception {
		token = StringUtils.replace(token, "$", "${esc.d}");
		token = StringUtils.replace(token, "#", "${esc.h}");
		token = StringUtils.replace(token, "\"", "${esc.q}");
		token = StringUtils.replace(token, "'", "${esc.s}");
		token = StringUtils.replace(token, "\\", "${esc.b}");

		return token;
	}

	/**
	 * make a output file
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param resourceDir
	 *            resource folder to build plugin resources
	 * @param template
	 *            a plugin resource
	 * @param packaged
	 *            whether a plugin resource has package (ex. java)
	 * @param packageName
	 *            project's base package name
	 * @return output file
	 */
	private File getOutput(String baseDir, File targetDir, String resourceDir,
			String template, boolean packaged, String packageName) {
		getLogger().debug("Call getOutput() of DefaultPluginPackager");

		if (packaged)
			template = StringUtils.replaceOnce(template,
					FileUtil.changePackageForDir(packageName), "");

		String outputName = resourceDir + CommonConstants.fileSeparator
				+ CommonConstants.fileSeparator + template;

		File output = new File(targetDir, outputName);

		return output;
	}

	/**
	 * copy a template to output file
	 * 
	 * @param resourceDir
	 *            resource folder to build plugin resources
	 * @param template
	 *            a plugin resource to copy
	 * @param output
	 *            a target file
	 */
	private void copyFile(final String resourceDir, final String template,
			final File output) throws Exception {
		File testFile = new File(resourceDir, template);
		FileUtil.copyFile(testFile, output);

	}

}
