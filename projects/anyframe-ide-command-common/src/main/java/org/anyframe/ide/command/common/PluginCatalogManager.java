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
import java.util.Map;

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.apache.maven.archetype.ArchetypeGenerationRequest;

/**
 * This is a PluginCatalogManager interface class. This class is for finding
 * plugins.
 * 
 * @author Jeryeon Kim
 */
public interface PluginCatalogManager {
	/**
	 * get all plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @return all plugins.
	 * @throws Exception
	 */
	Map<String, PluginInfo> getPlugins(ArchetypeGenerationRequest request)
			throws Exception;

	/**
	 * get essential plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @return essential plugins.
	 * @throws Exception
	 */
	Map<String, PluginInfo> getEssentialPlugins(
			ArchetypeGenerationRequest request) throws Exception;

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
	PluginInfo getPlugin(ArchetypeGenerationRequest request, String pluginName,
			String pluginVersion) throws Exception;

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
	PluginInfo getPlugin(ArchetypeGenerationRequest request, String pluginName)
			throws Exception;

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
	boolean isEssential(ArchetypeGenerationRequest request, String pluginName,
			String pluginVersion) throws Exception;

	/**
	 * display all plugins in plugin-catalog-xxx.xml
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 */
	void showPlugins(ArchetypeGenerationRequest request) throws Exception;

	/**
	 * update plugin-catalog=xxx.xml file in {user.home}/.anyframe/ directory
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param isEssential
	 *            whether the plugin is essential
	 * @param pluginInfo
	 *            information about plugin
	 */
	void updatePluginCatalog(ArchetypeGenerationRequest request,
			boolean isEssential, PluginInfo pluginInfo) throws Exception;

	/**
	 * update plugin-catalog-xxx.xml file in remote repository
	 * 
	 * @param request
	 *            information includes maven repositories in settings.xml
	 * @param baseDir
	 *            target project folder path to install a plugin
	 * @param url
	 *            url for remote repository
	 * @param userName
	 *            user name to connect to remote repository
	 * @param password
	 *            password to connect to remote repository
	 * @param isEssential
	 *            whether the plugin is essential
	 * @param isLatest
	 *            whether version of target plugin is latest
	 * @param pluginInfo
	 *            information about plugin
	 */
	void updatePluginCatalog(ArchetypeGenerationRequest request, File baseDir,
			String url, String userName, String password, boolean isEssential,
			boolean isLatest, PluginInfo pluginInfo) throws Exception;
}
