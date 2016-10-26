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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyframe.ide.command.common.plugin.DependentPlugin;
import org.anyframe.ide.command.common.plugin.Exclude;
import org.anyframe.ide.command.common.plugin.Include;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.plugin.PluginInterceptor;
import org.anyframe.ide.command.common.plugin.PluginResource;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.ConfigXmlUtil;
import org.anyframe.ide.command.common.util.FileUtil;
import org.anyframe.ide.command.common.util.ProjectConfig;
import org.anyframe.ide.command.common.util.ValidationUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.model.Model;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * This is a DefaultPluginBuildManager class. This class is for creating a
 * plugin build script file for packaging a plugin.
 * 
 * @plexus.component 
 *                   role="org.anyframe.ide.command.common.DefaultPluginBuildManager"
 * @author Sooyeon Park
 */
public class DefaultPluginBuildManager extends AbstractLogEnabled implements
		PluginBuildManager {

	/** @plexus.requirement */
	DefaultPluginPomManager pluginPomManager;

	/**
	 * @plexus.requirement 
	 *                     role="org.anyframe.ide.command.common.DefaultPluginCatalogManager"
	 */
	PluginCatalogManager pluginCatalogManager;

	/** directory constants */
	private static final String SRC_MAIN_JAVA = "src/main/java";
	private static final String SRC_MAIN_RESOURCES = "src/main/resources";
	private static final String SRC_TEST_JAVA = "src/test/java";
	private static final String SRC_TEST_RESOURCES = "src/test/resources";
	private static final String SRC_MAIN_WEBAPP = "src/main/webapp";

	/**
	 * create plugin build script file
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @throws Exception
	 */
	public void activate(ArchetypeGenerationRequest request, String baseDir)
			throws Exception {
		getLogger().debug(
				DefaultPluginBuildManager.class.getName()
						+ " activate execution start.");
		String pluginName = "[NO PLUGIN NAME]";

		try {
			// 1. check validation about current project
			ProjectConfig projectConfig = checkProject(baseDir);
			Model pjtPom = checkProjectPom(baseDir);
			Map<String, PluginInfo> pluginMap = checkInstalledPlugins(baseDir);

			// 2. validate the plugin name
			pluginName = pjtPom.getName();
			checkPluginName(request, pluginName);

			// 3. create pluginBuildInfo object
			String packageName = projectConfig.getPackageName();
			PluginInfo pluginBuildInfo = getPluginBuildInfo(packageName,
					pjtPom, pluginMap);

			// 4. write pluginBuildInfo object to plugin-build.xml file
			FileUtil.getObjectToXML(pluginBuildInfo, new File(baseDir,
					CommonConstants.PLUGIN_BUILD_FILE));

			System.out.println("The plugin build file for '"
					+ pluginBuildInfo.getName()
					+ "' plugin is created successfully.");

		} catch (Exception e) {
			if (e instanceof CommandException)
				throw e;
			throw new CommandException(
					"Error occurred in creating a plugin build file for '"
							+ pluginName + "' plugin. The reason is a '"
							+ e.getMessage() + "'.");
		}
	}

	/**
	 * remove a plugin build script file
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @throws Exception
	 */
	public void deactivate(String baseDir) throws Exception {
		getLogger().debug(
				DefaultPluginBuildManager.class.getName()
						+ " deactivate execution start.");

		try {
			File buildFile = new File(baseDir,
					CommonConstants.PLUGIN_BUILD_FILE);
			if (buildFile.exists()) {
				FileUtil.deleteFile(new File(baseDir,
						CommonConstants.PLUGIN_BUILD_FILE));
				System.out
						.println("The plugin build file for this project is removed successfully.");
			} else
				System.out
						.println("The plugin build file for this project has already removed.");
		} catch (Exception e) {
			if (e instanceof CommandException)
				throw e;
			throw new CommandException(
					"Error occurred in removing a plugin build file for this project. The reason is a '"
							+ e.getMessage() + "'.");
		}
	}

	/**
	 * check current project information
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @return project information properties
	 * @throws Exception
	 */
	private ProjectConfig checkProject(String baseDir) throws Exception {
		getLogger().debug("Call checkProject() of DefaultPluginPackager");

		// 1. get a project configuration
		String configFile = ConfigXmlUtil.getCommonConfigFile(baseDir);
		ProjectConfig projectConfig = ConfigXmlUtil.getProjectConfig(configFile);
		
		if (StringUtils.isEmpty(projectConfig.getPackageName())) {
			throw new CommandException(
					"Can not find a package.name property value in '"
							+ configFile
							+ "' file. Please check the package name.");
		}

		getLogger().debug(
				"Current plugin sample project directory is a " + baseDir);

		return projectConfig;
	}

	/**
	 * get sample project pom model from project pom.xml
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @return Model object which has a sample project pom.xml content
	 * @throws Exception
	 */
	private Model checkProjectPom(String baseDir) throws Exception {
		getLogger()
				.debug("Call checkProjectPom() of DefaultPluginBuildManager");

		// 1. check a pom.xml file
		File pomXml = new File(baseDir, Constants.ARCHETYPE_POM);
		if (!pomXml.exists())
			throw new CommandException("Can not find a '"
					+ pomXml.getAbsolutePath()
					+ "' file. Please check a location of your project.");

		return this.pluginPomManager.readPom(pomXml);
	}

	/**
	 * get installed plugins information from project plugin-installed.xml
	 * 
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @return all installed plugins information
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private Map<String, PluginInfo> checkInstalledPlugins(String baseDir)
			throws Exception {
		getLogger().debug(
				"Call checkInstalledPlugins() of DefaultPluginBuildManager");

		// 1. check a plugin-installed.xml file
		File pluginInstalledFile = new File(baseDir
				+ CommonConstants.PLUGIN_INSTALLED_FILE_PATH);
		
		if (!pluginInstalledFile.exists())
			throw new CommandException("Can not find a '"
					+ pluginInstalledFile.getAbsolutePath()
					+ "' file. Please check a location of your project.");

		// 2. create all installed plugins map from plugin-installed.xml
		Map<String, PluginInfo> pluginMap = (Map<String, PluginInfo>) FileUtil
				.getObjectFromXML(pluginInstalledFile);

		return pluginMap;
	}

	/**
	 * create default pluginBuildInfo object and return the object
	 * 
	 * @param packageName
	 *            project's base package name
	 * @param pjtPom
	 *            pom object of project
	 * @param pluginMap
	 *            all installed plugins information
	 * @return plugin build information
	 * @throws Exception
	 */
	private PluginInfo getPluginBuildInfo(String packageName, Model pjtPom,
			Map<String, PluginInfo> pluginMap) throws Exception {
		getLogger().debug(
				"Call getPluginBuildInfo() of DefaultPluginBuildManager");

		PluginInfo pluginBuildInfo = new PluginInfo();
		pluginBuildInfo.setName(pjtPom.getName());
		if (StringUtils.isEmpty(pjtPom.getDescription()))
			pluginBuildInfo.setDescription(pjtPom.getName() + " plugin");
		else
			pluginBuildInfo.setDescription(pjtPom.getDescription());
		pluginBuildInfo.setGroupId(pjtPom.getGroupId());
		pluginBuildInfo.setArtifactId(pjtPom.getArtifactId());
		pluginBuildInfo.setVersion(pjtPom.getVersion());

		PluginInterceptor interceptor = new PluginInterceptor();
		interceptor.setClassName("");
		interceptor.setDependencies(null);
		pluginBuildInfo.setInterceptor(interceptor);

		// create dependent plugins object from pluginMap
		List<DependentPlugin> dependentPlugins = new ArrayList<DependentPlugin>();
		Iterator<PluginInfo> itr = pluginMap.values().iterator();
		while (itr.hasNext()) {
			PluginInfo installedPluginInfo = itr.next();
			DependentPlugin dependency = new DependentPlugin();
			dependency.setName(installedPluginInfo.getName());
			dependency.setVersion(installedPluginInfo.getVersion());
			dependentPlugins.add(dependency);
		}
		pluginBuildInfo.setDependentPlugins(dependentPlugins);
		pluginBuildInfo.setSamples("true");
		pluginBuildInfo.setEssential("false");
		pluginBuildInfo.setResources(getPluginResources(packageName,
				pluginBuildInfo.getName()));

		return pluginBuildInfo;
	}

	/**
	 * check whether a plugin name is valid
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginName
	 *            plugin name
	 * @return true if plugin name is valid, else false
	 * @throws Exception
	 */
	private void checkPluginName(ArchetypeGenerationRequest request,
			String pluginName) throws Exception {
		getLogger()
				.debug("Call checkPluginName() of DefaultPluginBuildManager");

		// 1. check existing plugin names
		Map<String, PluginInfo> pluginNames = this.pluginCatalogManager
				.getPlugins(request);
		
		if (pluginNames != null && pluginNames.containsKey(pluginName))
			throw new CommandException("The same plugin name '" + pluginName
					+ "' already exists. Please use another plugin name.");

		// 2. check special characters
		if (!ValidationUtil.isValidPluginName(pluginName))
			throw new CommandException(
					"The plugin name '"
							+ pluginName
							+ "' has special characters besides '.','-'. Please use another plugin name.");
	}

	/**
	 * get plugin resource list which has default plugin
	 * resources(includes/excludes)
	 * 
	 * @return plugin resource list
	 */
	private List<PluginResource> getPluginResources(String packageName,
			String pluginName) {
		getLogger().debug(
				"Call getPluginResources() of DefaultPluginBuildManager");
		List<PluginResource> resources = new ArrayList<PluginResource>();

		// java resource include
		List<String> javaIncludeNames = new ArrayList<String>();
		String javaPackageName = StringUtils.replace(pluginName, "-", "/");
		javaIncludeNames.add("**/" + javaPackageName + "/**/*.java");
		List<Include> javaResourceIncludes = getIncludes(javaIncludeNames);

		// xml resource include
		List<String> xmlIncludeNames = new ArrayList<String>();
		xmlIncludeNames.add("**/*" + pluginName + "*.xml");
		List<Include> xmlResourceIncludes = getIncludes(xmlIncludeNames);

		// webapp resource include
		List<String> webappIncludeNames = new ArrayList<String>();
		webappIncludeNames.add("**/" + pluginName + "/**/*.*");
		List<Include> webappResourceIncludes = getIncludes(webappIncludeNames);

		// webapp resource exclude
		List<Exclude> webappResourceExcludes = new ArrayList<Exclude>();
		Exclude webxml = new Exclude();
		webxml.setName("**/" + CommonConstants.WEB_XML_FILE);
		webxml.setMerged(true);
		webappResourceExcludes.add(webxml);
		Exclude welcomeFile = new Exclude();
		welcomeFile.setName("**/" + CommonConstants.WELCOME_FILE);
		welcomeFile.setMerged(true);
		webappResourceExcludes.add(welcomeFile);

		PluginResource srcMainJavaResource = getPluginResource(SRC_MAIN_JAVA,
				true, true, javaResourceIncludes, null);
		PluginResource srcMainResourcesResource = getPluginResource(
				SRC_MAIN_RESOURCES, true, false, xmlResourceIncludes, null);
		PluginResource srcTestJavaResource = getPluginResource(SRC_TEST_JAVA,
				true, true, javaResourceIncludes, null);
		PluginResource srcTestResourcesResource = getPluginResource(
				SRC_TEST_RESOURCES, true, false, xmlResourceIncludes, null);
		PluginResource srcMainWebappResource = getPluginResource(
				SRC_MAIN_WEBAPP, false, false, webappResourceIncludes,
				webappResourceExcludes);

		resources.add(srcMainJavaResource);
		resources.add(srcMainResourcesResource);
		resources.add(srcTestJavaResource);
		resources.add(srcTestResourcesResource);
		resources.add(srcMainWebappResource);

		return resources;
	}

	/**
	 * get plugin resource with options(directory, filtered, packaged, includes,
	 * excludes)
	 * 
	 * @param dir
	 *            plugin resource folder
	 * @param filtered
	 *            whether a plugin resource will be merged with velocity context
	 * @param packaged
	 *            whether a plugin resource has package (ex. java)
	 * @param includes
	 *            include folders
	 * @param excludes
	 *            exclude folders
	 * @return plugin resource information
	 */
	private PluginResource getPluginResource(String dir, boolean filtered,
			boolean packaged, List<Include> includes, List<Exclude> excludes) {
		getLogger().debug(
				"Call getPluginResource() of DefaultPluginBuildManager");

		PluginResource pluginResource = new PluginResource();
		pluginResource.setDir(dir);
		pluginResource.setFiltered(filtered);
		pluginResource.setPackaged(packaged);
		pluginResource.setIncludes(includes);
		pluginResource.setExcludes(excludes);

		return pluginResource;
	}

	/**
	 * get include folders
	 * 
	 * @param includeNames
	 *            include pattern list
	 * @return include folders
	 */
	private List<Include> getIncludes(List<String> includeNames) {
		getLogger().debug("Call getIncludes() of DefaultPluginBuildManager");

		List<Include> includes = new ArrayList<Include>();
		Iterator<String> itr = includeNames.iterator();
		while (itr.hasNext()) {
			Include include = new Include();
			include.setName(itr.next());
			includes.add(include);
		}
		return includes;
	}

}
