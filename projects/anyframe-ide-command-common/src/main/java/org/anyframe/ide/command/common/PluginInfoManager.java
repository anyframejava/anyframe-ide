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
import java.io.InputStream;
import java.util.Map;

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.apache.maven.archetype.ArchetypeGenerationRequest;

/**
 * This is a PluginInfoManager class. This interface class is for finding a
 * plugin information
 * 
 * @author SoYon Lim
 */
public interface PluginInfoManager {
	/**
	 * get a latest plugin information which includes name, groupId, artifactId,
	 * version, description, dependencies, etc.
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginName
	 *            plugin name to find
	 * @return plugin detail information without versions
	 */
	PluginInfo getPluginInfo(ArchetypeGenerationRequest request,
			String pluginName) throws Exception;

	/**
	 * get a plugin information which includes name, groupId, artifactId,
	 * version, description, dependencies, etc.
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginName
	 *            plugin name to find
	 * @param pluginVersion
	 *            plugin version to find
	 * @return plugin detail information without versions
	 */
	PluginInfo getPluginInfo(ArchetypeGenerationRequest request,
			String pluginName, String pluginVersion) throws Exception;

	/**
	 * get a plugin information which includes name, groupId, artifactId,
	 * version, etc.
	 * 
	 * @param pluginJar
	 *            plugin binary file
	 * @return plugin detail information without versions
	 */
	PluginInfo getPluginInfo(File pluginJar) throws Exception;

	/**
	 * display a specific plugin information
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginName
	 *            plugin name to be printed
	 */
	void showPluginInfo(ArchetypeGenerationRequest request, String pluginName)
			throws Exception;

	/**
	 * get latest plugins which a specific plugin depends on
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginInfo
	 *            plugin detail information without versions
	 * @return dependent plugins
	 */
	Map<String, String> getDependentPlugins(ArchetypeGenerationRequest request,
			PluginInfo pluginInfo) throws Exception;

	/**
	 * display all updatable plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            folder which includes META-INF/plugin-installed.xml
	 */
	void showUpdatablePlugins(ArchetypeGenerationRequest request, String baseDir)
			throws Exception;

	/**
	 * get all dependent plugin jar files based on installed plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @param pluginInfo
	 *            plugin detail information without versions
	 * @return all dependent plugin binary files
	 */
	Map<String, File> getAllDependentPluginJars(
			ArchetypeGenerationRequest request, String baseDir,
			PluginInfo pluginInfo) throws Exception;

	/**
	 * check whether dependent plugins finally depends on installed essential
	 * plugins
	 * 
	 * @param baseDir
	 *            which includes META-INF/plugin-installed.xml
	 * @param allDependentPluginJars
	 *            all dependent plugin binary files
	 * @return true if dependent plugins finally depends on installed essential,
	 *         else false
	 */
	boolean hasEssentialPluginDependency(String baseDir,
			Map<String, File> allDependentPluginJars) throws Exception;

	/**
	 * get plugins which depends on a specific plugin among installed plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @param pluginInfo
	 *            plugin detail information without versions
	 * @return depended plugins
	 */
	Map<String, String> getDependedPlugins(ArchetypeGenerationRequest request,
			String baseDir, PluginInfo pluginInfo) throws Exception;

	/**
	 * get all plugin information which have 'installed' property value
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @return all plugins
	 */
	Map<String, PluginInfo> getPluginsWithInstallInfo(
			ArchetypeGenerationRequest request, String baseDir)
			throws Exception;

	/**
	 * get a installed plugin information
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @param pluginName
	 *            plugin name to find
	 * @return plugin detail information includes groupId, artifactId, version,
	 *         dependencies, samples, interceptor, etc.
	 */
	PluginInfo getInstalledPluginInfo(ArchetypeGenerationRequest request,
			String baseDir, String pluginName) throws Exception;

	/**
	 * get all installed plugin binary files from local/remote repository
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @return plugin binary files
	 */
	Map<String, File> getInstalledPluginJars(
			ArchetypeGenerationRequest request, String baseDir)
			throws Exception;

	/**
	 * check if a plugin with a specific version is already installed. (in case
	 * of essential plugin, check only plugin name)
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @param pluginName
	 *            plugin name to check
	 * @param pluginVersion
	 *            plugin version to check
	 * @return true if a plugin with a specific version is installed, else false
	 */
	boolean isInstalled(ArchetypeGenerationRequest request, String baseDir,
			String pluginName, String pluginVersion) throws Exception;

	/**
	 * get all installable plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @return all installable plugins
	 */
	Map<String, PluginInfo> getInstallablePlugins(
			ArchetypeGenerationRequest request, String baseDir)
			throws Exception;

	/**
	 * extract plugin resource like pom.xml, plugin.xml, etc. from plugin
	 * library
	 * 
	 * @param resourceName
	 *            file name of plugin resource
	 * @param pluginJar
	 *            plugin binary file
	 * @return inputstream of a specific plugin resource
	 */
	InputStream getPluginResource(String resourceName, File pluginJar)
			throws Exception;

	/**
	 * read plugin resource like pom.xml, plugin.xml, etc. from plugin library
	 * 
	 * @param resourceName
	 *            file name of plugin resource
	 * @param pluginJar
	 *            plugin binary file
	 * @param encoding
	 *            file encoding style
	 * @return file contents
	 */
	String readPluginResource(String resourceName, File pluginJar,
			String encoding) throws Exception;

	/**
	 * get a plugin binary file
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param groupId
	 *            group id of plugin
	 * @param artifactId
	 *            artifact id of plugin
	 * @param version
	 *            version of plugin
	 * @return plugin binary file
	 */
	File getPluginFile(ArchetypeGenerationRequest request, String groupId,
			String artifactId, String version) throws Exception;

	/**
	 * get all installed plugins
	 * 
	 * @param baseDir
	 *            folder which includes META-INF/plugin-installed.xml
	 * @return all installed plugins
	 */
	Map<String, PluginInfo> getInstalledPlugins(String baseDir)
			throws Exception;

	/**
	 * display installed plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            folder which includes META-INF/plugin-installed.xml
	 */
	void showInstalledPlugins(ArchetypeGenerationRequest request, String baseDir)
			throws Exception;
}
