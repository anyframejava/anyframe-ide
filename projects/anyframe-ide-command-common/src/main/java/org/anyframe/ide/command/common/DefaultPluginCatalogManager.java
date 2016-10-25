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
package org.anyframe.ide.command.common;

import java.io.File;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

import org.anyframe.ide.command.common.catalog.LocalPluginCatalogDataSource;
import org.anyframe.ide.command.common.catalog.RemotePluginCatalogDataSource;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.archetype.ArchetypeGenerationRequest;

/**
 * This is a DefaultPluginCatalogManager class. This class is for finding
 * plugins.
 * 
 * @plexus.component 
 *                   role="org.anyframe.ide.command.common.DefaultPluginCatalogManager"
 * @author Jeryeon Kim
 */
public class DefaultPluginCatalogManager implements PluginCatalogManager {

	/**
	 * @plexus.requirement
	 */
	private LocalPluginCatalogDataSource localPluginCatalogDataSource;

	/**
	 * @plexus.requirement
	 */
	private RemotePluginCatalogDataSource remotePluginCatalogDataSource;

	/**
	 * get all plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @return all plugins.
	 * @throws Exception
	 */
	public Map<String, PluginInfo> getPlugins(ArchetypeGenerationRequest request)
			throws Exception {

		Map<String, PluginInfo> plugins = localPluginCatalogDataSource
				.readPluginCatalog(request);

		if (request.getRemoteArtifactRepositories().size() > 0
				&& plugins.isEmpty()) {
			plugins = remotePluginCatalogDataSource.readPluginCatalog(request);
		}

		return plugins;
	}

	/**
	 * get plugin with name and version
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginName
	 *            plugin name
	 * @param pluginVersion
	 *            plugin version
	 * @return information about plugin
	 * @throws Exception
	 */
	public PluginInfo getPlugin(ArchetypeGenerationRequest request,
			String pluginName, String pluginVersion) throws Exception {

		PluginInfo plugin = getPlugin(request, pluginName);

		if (plugin != null) {
			List<String> versions = plugin.getVersions();

			if (versions.contains(pluginVersion)) {
				plugin.setVersion(pluginVersion);
				return plugin;
			}
		}

		return null;
	}

	/**
	 * get plugin with name
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginName
	 *            plugin name
	 * @return information about plugin
	 * @throws Exception
	 */
	public PluginInfo getPlugin(ArchetypeGenerationRequest request,
			String pluginName) throws Exception {

		Map<String, PluginInfo> plugins = getPlugins(request);

		if (plugins.containsKey(pluginName))
			return plugins.get(pluginName);

		return null;
	}

	/**
	 * check if the plugin is essential.
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginName
	 *            plugin name
	 * @param pluginVersion
	 *            plugin version
	 * @return whether the plugin is essential
	 * @throws Exception
	 */
	public boolean isEssential(ArchetypeGenerationRequest request,
			String pluginName, String pluginVersion) throws Exception {

		// Get essential plugins
		Map<String, PluginInfo> plugins = localPluginCatalogDataSource
				.readPluginCatalog(request,
						CommonConstants.PLUGIN_TYPE_ESSENTIAL);

		if (request.getRemoteArtifactRepositories().size() > 0
				&& plugins.isEmpty()) {
			plugins = remotePluginCatalogDataSource.readPluginCatalog(request,
					CommonConstants.PLUGIN_TYPE_ESSENTIAL);
		}

		if (plugins.containsKey(pluginName)) {
			PluginInfo plugin = plugins.get(pluginName);
			String latestVersion = plugin.getLatestVersion();

			List<String> versions = plugin.getVersions();
			if (!StringUtils.isEmpty(latestVersion)
					&& versions.contains(pluginVersion)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * get all archetypes.
	 * 
	 * @param archetypeCatalog
	 *            path which includes archetype-catalog.xml
	 * @return all archetypes
	 * @throws Exception
	 */
	public List<PluginInfo> getArchetypes(String archetypeCatalog)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * get name of plugins using plugin catalog
	 * 
	 * @param request
	 *            information includes maven repository
	 * @return all plugin names
	 * @throws Exception
	 */
	public Map<String, PluginInfo> getPluginNames(
			ArchetypeGenerationRequest request) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * display all plugins in plugin-catalog-xxx.xml
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 */
	public void showPlugins(ArchetypeGenerationRequest request)
			throws Exception {
		Map<String, PluginInfo> optionalPlugins = localPluginCatalogDataSource
				.readPluginCatalog(request,
						CommonConstants.PLUGIN_TYPE_OPTIONAL);

		if (request.getRemoteArtifactRepositories().size() > 0
				&& optionalPlugins.isEmpty()) {
			optionalPlugins = remotePluginCatalogDataSource.readPluginCatalog(
					request, CommonConstants.PLUGIN_TYPE_OPTIONAL);
		}

		Map<String, PluginInfo> customPlugins = localPluginCatalogDataSource
				.readPluginCatalog(request, CommonConstants.PLUGIN_TYPE_CUSTOM);

		if (request.getRemoteArtifactRepositories().size() > 0
				&& customPlugins.isEmpty()) {
			customPlugins = remotePluginCatalogDataSource.readPluginCatalog(
					request, CommonConstants.PLUGIN_TYPE_CUSTOM);
		}

		optionalPlugins.putAll(customPlugins);

		Collection<PluginInfo> optionalPluginValues = optionalPlugins.values();
		printPlugins(optionalPluginValues, false);

		Map<String, PluginInfo> essentialPlugins = localPluginCatalogDataSource
				.readPluginCatalog(request,
						CommonConstants.PLUGIN_TYPE_ESSENTIAL);

		if (request.getRemoteArtifactRepositories().size() > 0
				&& essentialPlugins.isEmpty()) {
			essentialPlugins = remotePluginCatalogDataSource.readPluginCatalog(
					request, CommonConstants.PLUGIN_TYPE_ESSENTIAL);
		}

		Collection<PluginInfo> essentialPluginValues = essentialPlugins
				.values();
		printPlugins(essentialPluginValues, true);
	}

	/**
	 * print essential/optional plugins
	 * 
	 * @param pluginValues
	 *            plugins to printed
	 * @param isEssential
	 *            whether essential plugin is
	 */
	private void printPlugins(Collection<PluginInfo> pluginValues,
			boolean isEssential) {
		if (pluginValues.size() > 0) {
			StringBuffer buffer = new StringBuffer();

			buffer.append((isEssential ? "Essential" : "Optional")
					+ " plugins are listed below: \n");
			buffer
					.append("------------------------------------------------------------------------------ \n");
			Formatter formatter = new Formatter();

			formatter.format(
					CommonConstants.PLUGININFO_NAME_LATEST_DESCRIPTION, "name",
					"latest version", "description");
			buffer.append(formatter.toString() + "\n");
			buffer
					.append("------------------------------------------------------------------------------ \n");

			for (PluginInfo pluginInfo : pluginValues) {
				StringBuilder builder = new StringBuilder();
				Formatter pluginInfoFormatter = new Formatter(builder);

				String pluginName = pluginInfo.getName();
				if (pluginName.length() > 17) {
					pluginName = pluginName.substring(0, 16) + "...";
				}

				String pluginDescription = pluginInfo.getDescription();
				if (pluginDescription.length() > 33) {
					pluginDescription = pluginDescription.substring(0, 32)
							+ "...";
				}

				pluginInfoFormatter.format(
						CommonConstants.PLUGININFO_NAME_LATEST_DESCRIPTION,
						pluginName, pluginInfo.getLatestVersion(),
						pluginDescription);
				buffer.append(pluginInfoFormatter.toString() + "\n");
			}

			System.out.println(buffer.toString());
		}
	}

	/**
	 * select a plugin to install
	 * 
	 * @param request
	 *            information includes maven repository, db settings, plugin
	 * @param interactiveMode
	 *            interactive mode processing install
	 * @param baseDir
	 *            target folder to install
	 * @return information about plugins to be selected
	 * @throws Exception
	 */
	public List<PluginInfo> selectPlugin(ArchetypeGenerationRequest request,
			Boolean interactiveMode, String baseDir) throws Exception {
		// TODO Auto-generated method stub
		return null;
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
			boolean isEssential, PluginInfo pluginInfo) throws Exception {
		localPluginCatalogDataSource.updatePluginCatalog(request, isEssential,
				pluginInfo);
	}

	/**
	 * update plugin-catalog=xxx.xml file in remote repository
	 * 
	 * @param request
	 *            information includes maven repositories in settings.xml
	 * @param url
	 *            url for remote repository
	 * @param userName
	 *            user name to connect to remote repository
	 * @param password
	 *            password to connect to remote repository
	 * @param isEssential
	 *            whether the plugin is essential
	 * @param pluginInfo
	 *            information about plugin
	 */
	public void updatePluginCatalog(ArchetypeGenerationRequest request,
			File baseDir, String url, String userName, String password,
			boolean isEssential, PluginInfo pluginInfo) throws Exception {
		remotePluginCatalogDataSource.updatePluginCatalog(request, baseDir,
				url, userName, password, isEssential, pluginInfo);
	}
}
