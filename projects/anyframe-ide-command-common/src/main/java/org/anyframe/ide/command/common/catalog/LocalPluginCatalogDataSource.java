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
package org.anyframe.ide.command.common.catalog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyframe.ide.command.common.CommandException;
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
	 * read all plugin catalog files in {user.home}/.anyframe/ directory.
	 * 
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

		// Get custom plugins
		plugins.putAll(readPluginCatalog(request,
				CommonConstants.PLUGIN_TYPE_CUSTOM));

		return plugins;
	}

	/**
	 * read a plugin catalog file in {user.home}/.anyframe/ directory.
	 * 
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
		// System.out.println("#### BASE DIR : " + baseDir);

		try {
			if (catalogFile.exists()) {
				Map<String, PluginInfo> plugins = (Map<String, PluginInfo>) FileUtil
						.getObjectFromXML(catalogFile);

				return plugins;

			} else if (CommonConstants.PROJECT_BUILD_TYPE_ANT.equals(getProjectBuildType(baseDir))) {
				// if ant type and not exist plugin-catalog file at '.anyframe'
				ArtifactRepository localArtifactRepository = request
						.getLocalRepository();
				String localRepoUrl = localArtifactRepository.getUrl();

				localRepoUrl = localRepoUrl.substring("file://".length());
				File catalogFileAtRepo = new File(localRepoUrl, catalogFile
						.getName());

				if (catalogFileAtRepo.exists()) {
					Map<String, PluginInfo> plugins = (Map<String, PluginInfo>) FileUtil
							.getObjectFromXML(catalogFileAtRepo);

					downloadPluginCatalogFile(catalogFile.getName(), plugins);

					FileUtil.deleteFile(catalogFileAtRepo);

					return plugins;
				}
			}

			return new HashMap<String, PluginInfo>();

		} catch (Exception e) {
			e.printStackTrace();
			getLogger().warn(
					"Reading a plugin catalog file from local [location="
							+ catalogFile.getAbsolutePath()
							+ "] is skipped. The reason is a '"
							+ e.getMessage() + "'.");
			return new HashMap<String, PluginInfo>();
		}
	}

	/**
	 * update plugin-catalog=xxx.xml file in {user.home}/.anyframe/ directory
	 * 
	 * @param isEssential
	 *            whether the plugin is essential
	 * @param pluginInfo
	 *            information about plugin
	 */
	public void updatePluginCatalog(ArchetypeGenerationRequest request,
			boolean isEssential, PluginInfo pluginInfo) {
		Map<String, PluginInfo> plugins = new HashMap<String, PluginInfo>();
		File catalogFile = null;
		if (isEssential) {
			catalogFile = getPluginCatalogFile(1);
			plugins = readPluginCatalog(request, 1);
		} else {
			catalogFile = getPluginCatalogFile(2);
			plugins = readPluginCatalog(request, 2);
		}

		String pluginName = pluginInfo.getName();
		String pluginVersion = pluginInfo.getVersion();

		boolean isUpdated = false;

		if (plugins.containsKey(pluginName)) {
			PluginInfo targetPluginInfo = plugins.get(pluginName);

			if (!targetPluginInfo.getLatestVersion().equals(pluginVersion)) {
				targetPluginInfo.setLatestVersion(pluginVersion);
				isUpdated = true;
			}

			List<String> versions = targetPluginInfo.getVersions();
			if (!versions.contains(pluginVersion)) {
				versions.add(pluginVersion);
				isUpdated = true;
			}
		} else {
			PluginInfo targetPluginInfo = new PluginInfo();
			targetPluginInfo.setName(pluginName);
			targetPluginInfo.setLatestVersion(pluginVersion);
			targetPluginInfo.setDescription(pluginName + " plugin");
			targetPluginInfo.setGroupId(pluginInfo.getGroupId());
			targetPluginInfo.setArtifactId(pluginInfo.getArtifactId());

			List<String> versions = new ArrayList<String>();
			versions.add(pluginVersion);
			targetPluginInfo.setVersions(versions);
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

		switch (pluginType) {
		case CommonConstants.PLUGIN_TYPE_ESSENTIAL:
			catalogFileName = CommonConstants.PLUGIN_CATALOG_ESSENTIAL_FILE;
			break;

		case CommonConstants.PLUGIN_TYPE_OPTIONAL:
			catalogFileName = CommonConstants.PLUGIN_CATALOG_OPTIONAL_FILE;
			break;

		case CommonConstants.PLUGIN_TYPE_CUSTOM:
			catalogFileName = CommonConstants.PLUGIN_CATALOG_CUSTOM_FILE;
			break;
		}

		return new File(System.getProperty("user.home")
				+ CommonConstants.USER_HOME_ANYFRAME, catalogFileName);
	}

	/**
	 * copy temporary file to
	 * {user.home}/.anyframe/plugin-catalog-{essential|optional|custom}.xml
	 * 
	 * @param fileName
	 *            target file name
	 * @param file
	 *            temporary catalog file object
	 * @return local catalog file object
	 * @throws Exception
	 */
	private File downloadPluginCatalogFile(String fileName,
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

			getLogger().info(
					"Download " + pluginCatalogFile.getAbsolutePath()
							+ " from anyframe repository successfully.");
		}

		return pluginCatalogFile;
	}

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

				// System.out.println("### PROJECT BUILD TYPE : "+
				// projectBuildType);
			} catch (Exception e) {
				getLogger().warn(
						"Loading properties from project.mf in " + baseDir
								+ " is skipped. The reason is "
								+ e.getMessage());
			}
		}

		return projectBuildType;
	}
}
