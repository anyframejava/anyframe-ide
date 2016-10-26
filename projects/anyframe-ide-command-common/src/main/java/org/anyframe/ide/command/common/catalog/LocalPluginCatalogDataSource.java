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
package org.anyframe.ide.command.common.catalog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * This is an LocalPluginCatalogDataSource class. This class is for reading a
 * plugin catalog files in {user.home}/.anyframe/ directory.
 * 
 * @plexus.component 
 *                   role="org.anyframe.ide.command.common.catalog.LocalPluginCatalogDataSource"
 * @author Jeryeon Kim
 */
public class LocalPluginCatalogDataSource extends AbstractLogEnabled {

	/**
	 * Read all plugin catalog files in '{user.home}/.anyframe' directory.
	 * 
	 * @param request
	 *            information includes maven repositories in settings.xml
	 * @return plugins defined all plugin catalog files (essential, optional,
	 *         custom).
	 */
	public Map<String, PluginInfo> readPluginCatalog(
			ArchetypeGenerationRequest request) {

		// Get essential plugins
		Map<String, PluginInfo> plugins = readPluginCatalog(request,
				CommonConstants.PLUGIN_TYPE_ESSENTIAL);

		// Get optional plugins
		plugins.putAll(readPluginCatalog(request,
				CommonConstants.PLUGIN_TYPE_OPTIONAL));

		return plugins;
	}

	/**
	 * Read a plugin catalog file in '{user.home}/.anyframe' directory.
	 * 
	 * @param request
	 *            information includes maven repositories in settings.xml
	 * @param pluginType
	 *            type of plugin. (CommonConstants.PLUGIN_TYPE_ESSENTIAL,
	 *            CommonConstants.PLUGIN_TYPE_OPTIONAL,
	 *            CommonConstants.PLUGIN_TYPE_CUSTOM)
	 * @return plugins in a specified plugin catalog file
	 */
	@SuppressWarnings("unchecked")
	public Map<String, PluginInfo> readPluginCatalog(
			ArchetypeGenerationRequest request, int pluginType) {

		File catalogFile = getPluginCatalogFile(pluginType);

		// get project build type
		String baseDir = request.getOutputDirectory();

		try {
			if (!catalogFile.exists()
					&& CommonConstants.PROJECT_BUILD_TYPE_ANT
							.equals(getProjectBuildType(baseDir))) {
				// if ant type and not exist plugin-catalog file at '.anyframe'
				handleTheFirstDownload(request,
						CommonConstants.PLUGIN_TYPE_ESSENTIAL);
				handleTheFirstDownload(request,
						CommonConstants.PLUGIN_TYPE_OPTIONAL);
			}

			if (catalogFile.exists()) {
				Map<String, PluginInfo> plugins = (Map<String, PluginInfo>) FileUtil
						.getObjectFromXML(catalogFile);
				return plugins;
			}

			return new HashMap<String, PluginInfo>();

		} catch (Exception e) {
			getLogger().warn(
					"Reading a plugin catalog file from local [location="
							+ catalogFile.getAbsolutePath()
							+ "] is skipped. The reason is a '"
							+ e.getMessage() + "'.");
			return new HashMap<String, PluginInfo>();
		}
	}

	@SuppressWarnings("unchecked")
	private void handleTheFirstDownload(ArchetypeGenerationRequest request,
			int pluginType) throws Exception {
		File catalogFile = getPluginCatalogFile(pluginType);
		ArtifactRepository localArtifactRepository = request
				.getLocalRepository();
		String localRepoUrl = localArtifactRepository.getUrl();

		localRepoUrl = localRepoUrl.substring("file://".length());
		File catalogFileAtRepo = new File(localRepoUrl, catalogFile.getName());

		if (catalogFileAtRepo.exists()) {
			// if plugin-catalog-xxx.xml file exist at '[anyframeHome]/repo'
			Map<String, PluginInfo> plugins = (Map<String, PluginInfo>) FileUtil
					.getObjectFromXML(catalogFileAtRepo);

			makeLocalPluginCatalogFile(catalogFile.getName(), plugins);

			FileUtil.deleteFile(catalogFileAtRepo);
		}
	}

	/**
	 * Update plugin information to plugin-catalog-xxx.xml file in
	 * '{user.home}/.anyframe' directory
	 * 
	 * @param request
	 *            information includes maven repositories in settings.xml
	 * @param isEssential
	 *            whether the plugin is essential
	 * @param pluginInfo
	 *            information about plugin
	 */
	public void updatePluginCatalog(ArchetypeGenerationRequest request,
			boolean isEssential, PluginInfo pluginInfo) {

		Map<String, PluginInfo> plugins = new HashMap<String, PluginInfo>();
		int pluginType = (isEssential) ? CommonConstants.PLUGIN_TYPE_ESSENTIAL
				: CommonConstants.PLUGIN_TYPE_OPTIONAL;

		File catalogFile = getPluginCatalogFile(pluginType);
		plugins = readPluginCatalog(request, pluginType);

		String pluginName = pluginInfo.getName();
		String pluginVersion = pluginInfo.getVersion();
		String pluginDescription = pluginInfo.getDescription();

		boolean isUpdated = false;

		if (plugins.containsKey(pluginName)) {
			PluginInfo targetPluginInfo = plugins.get(pluginName);

			if (!targetPluginInfo.getLatestVersion().equals(pluginVersion)) {
				targetPluginInfo.setLatestVersion(pluginVersion);
				isUpdated = true;
			}

			if (!targetPluginInfo.getDescription().equals(pluginDescription)) {
				targetPluginInfo.setDescription(pluginDescription);
				isUpdated = true;
			}

			List<String> versions = targetPluginInfo.getVersions();
			if (!versions.contains(pluginVersion)) {
				versions.add(pluginVersion);
				isUpdated = true;
			}
		} else {
			PluginInfo targetPluginInfo = makePluginInfo(pluginInfo);
			plugins.put(pluginName, targetPluginInfo);

			isUpdated = true;
		}

		try {
			if (isUpdated) {
				FileUtil.getObjectToXML(plugins, catalogFile);
			}
		} catch (Exception e) {
			getLogger().warn(
					"Updating " + catalogFile.getAbsolutePath()
							+ " is skipped. The reason is " + e.getMessage());
		}
	}

	/**
	 * get plugin catalog file by plugin type
	 * 
	 * @param pluginType
	 *            type of plugin. (CommonConstants.PLUGIN_TYPE_ESSENTIAL,
	 *            CommonConstants.PLUGIN_TYPE_OPTIONAL,
	 *            CommonConstants.PLUGIN_TYPE_CUSTOM)
	 * @return plugin catalog file
	 */
	private File getPluginCatalogFile(int pluginType) {
		String catalogFileName = CommonConstants.PLUGIN_CATALOG_ESSENTIAL_FILE;

		if (CommonConstants.PLUGIN_TYPE_OPTIONAL == pluginType)
			catalogFileName = CommonConstants.PLUGIN_CATALOG_OPTIONAL_FILE;

		return new File(System.getProperty("user.home")
				+ CommonConstants.USER_HOME_ANYFRAME, catalogFileName);
	}

	/**
	 * Make plugin catalog file to
	 * {user.home}/.anyframe/plugin-catalog-{essential|optional|custom}.xml
	 * using plugins in Map
	 * 
	 * @param fileName
	 *            target file name
	 * @param plugins
	 *            plugin informations
	 * @return local catalog file object
	 * @throws Exception
	 */
	private File makeLocalPluginCatalogFile(String fileName,
			Map<String, PluginInfo> plugins) throws Exception {

		File userHomeAnyframeDir = new File(System.getProperty("user.home")
				+ CommonConstants.USER_HOME_ANYFRAME);
		if (!userHomeAnyframeDir.exists())
			userHomeAnyframeDir.mkdir();

		File pluginCatalogFile = new File(
				userHomeAnyframeDir.getAbsoluteFile(), fileName);

		if (pluginCatalogFile.exists())
			FileUtil.deleteFile(pluginCatalogFile);

		if (!plugins.isEmpty()) {

			FileUtil.getObjectToXML(plugins, pluginCatalogFile);

			getLogger().debug(
					"Download " + pluginCatalogFile.getAbsolutePath()
							+ " from anyframe repository successfully.");
		}

		return pluginCatalogFile;
	}

	/**
	 * Get current project build type from project.mf file
	 * 
	 * @param baseDir
	 *            the path of current project
	 * @return build type of current project
	 */
	private String getProjectBuildType(String baseDir) {
		String projectBuildType = "";
		File metadataFile = new File(new File(baseDir)
				+ CommonConstants.METAINF, CommonConstants.METADATA_FILE);

		if (metadataFile.exists()) {
			try {
				PropertiesIO pio = new PropertiesIO(metadataFile
						.getAbsolutePath());

				projectBuildType = pio
						.readValue(CommonConstants.PROJECT_BUILD_TYPE);

			} catch (Exception e) {
				getLogger().warn(
						"Loading properties from project.mf in " + baseDir
								+ " is skipped. The reason is "
								+ e.getMessage());
			}
		}

		return projectBuildType;
	}

	/**
	 * Make PluginInfo object with full information
	 * 
	 * @param pluginInfo
	 *            PluginInfo object with basic information
	 * @return PluginInfo object with full information
	 */
	private PluginInfo makePluginInfo(PluginInfo pluginInfo) {
		PluginInfo targetPluginInfo = new PluginInfo();
		targetPluginInfo.setName(pluginInfo.getName());
		targetPluginInfo.setLatestVersion(pluginInfo.getVersion());
		targetPluginInfo.setDescription(pluginInfo.getDescription());
		targetPluginInfo.setGroupId(pluginInfo.getGroupId());
		targetPluginInfo.setArtifactId(pluginInfo.getArtifactId());

		List<String> versions = new ArrayList<String>();
		versions.add(pluginInfo.getVersion());
		targetPluginInfo.setVersions(versions);

		return targetPluginInfo;
	}
}
