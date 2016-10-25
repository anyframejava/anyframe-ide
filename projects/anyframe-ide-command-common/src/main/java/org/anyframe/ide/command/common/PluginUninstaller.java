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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.archetype.ArchetypeGenerationRequest;

/**
 * This is a PluginUninstaller interface class. This class is for uninstalling a
 * plugin based on selected plugin name
 * 
 * @author SoYon Lim
 */
public interface PluginUninstaller {

	/**
	 * uninstall a plugin
	 * 
	 * @param request
	 *            information includes maven repository, db settings, plugin
	 * @param baseDir
	 *            target folder
	 * @param pluginName
	 *            plugin name to be uninstalled
	 * @param excludes
	 *            file list which exclude from removing
	 * @param encoding
	 *            file encoding style
	 * @param pomHandling
	 *            whether to handle pom file
	 * @throws Exception
	 */
	void uninstall(ArchetypeGenerationRequest request, String baseDir,
			String pluginName, String excludes, String encoding,
			boolean pomHandling) throws Exception;

	/**
	 * uninstall a plugin
	 * 
	 * @param request
	 *            information includes maven repository, db settings, plugin
	 * @param baseDir
	 *            target folder
	 * @param pluginName
	 *            plugin name to be uninstalled
	 * @param excludes
	 *            file list which exclude from removing
	 * @param encoding
	 *            file encoding style
	 * @param pomHandling
	 *            whether to handle pom file
	 * @param ignoreDependency
	 *            ignore dependency plugins
	 * @throws Exception
	 */
	void uninstall(ArchetypeGenerationRequest request, String baseDir,
			String pluginName, String excludes, String encoding,
			boolean pomHandling, boolean ignoreDependency) throws Exception;

	/**
	 * update installation information of plugin-installed.xml file
	 * 
	 * @param baseDir
	 *            the path of current project
	 * @param pluginName
	 *            plugin name to be installed
	 * @throws Exception
	 */
	void updateInstallationInfo(String baseDir, String pluginName)
			throws Exception;

	/**
	 * in case of uninstalling using maven, find dependencies to be removed
	 * 
	 * @param baseDir
	 *            the path of current project
	 * @param installedPluginJars
	 *            installed plugin jar files
	 * @param pluginJar
	 *            plugin jar file to be removed
	 * @param backupDir
	 *            directory for backup
	 * @param properties
	 *            properties of pom in current project
	 */
	void processPom(String baseDir, Map<String, File> installedPluginJars,
			File pluginJar, File backupDir, Properties properties)
			throws Exception;

	/**
	 * backup dependent libraries of current plugin
	 * 
	 * @param baseDir
	 *            the path of current project
	 * @param projectType
	 *            generated project's style ('web' or 'service')
	 * @param pluginName
	 *            plugin name
	 * @param installedPluginJars
	 *            jar files of installed plugins
	 * @param removePluginJar
	 *            jar file of plugin to remove
	 * @param properties
	 *            properties of pom in current project
	 * @param backupDir
	 *            directory for backup
	 * @throws Exception
	 */
	void processDependencyLibs(String baseDir, String projectType,
			String pluginName, Map<String, File> installedPluginJars,
			File removePluginJar, Properties properties, File backupDir)
			throws Exception;

	/**
	 * find dependencies to be removed
	 * 
	 * @param installedPluginJars
	 *            installed plugin jar files
	 * @param pluginJar
	 *            plugin jar file to be removed
	 * @param properties
	 *            properties of pom in current project
	 * @return jar file names to be removed
	 * @throws Exception
	 */
	List<String> findRemoveDependencies(Map<String, File> installedPluginJars,
			File pluginJar, Properties properties) throws Exception;

	/**
	 * Updates .classpath file
	 * 
	 * @param installedPluginJars
	 *            installed plugin jar files
	 * @param baseDir
	 *            the path of current project
	 * @throws Exception
	 */
	void processClasspathFile(Map<String, File> installedPluginJars,
			String baseDir) throws Exception;
}
