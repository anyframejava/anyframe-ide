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
import java.util.zip.ZipFile;

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.plugin.TargetPluginInfo;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.velocity.context.Context;

/**
 * This is an PluginInstaller interface class. This class is for installing a
 * new plugin based on selected plugin name
 * 
 * @author SoYon Lim
 */
public interface PluginInstaller {

	/**
	 * install a new plugin includes dependent plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder path to install a plugin
	 * @param pluginName
	 *            a target plugin name to install
	 * @param pluginVersion
	 *            a target plugin version to install
	 * @param pluginJar
	 *            a target plugin binary file to install
	 * @param encoding
	 *            file encoding style
	 * @param pomHandling
	 *            whether current project is based on maven or ant
	 * @param excludeSrc
	 *            whether plugin resources exclude source codes will be
	 *            installed
	 * @param templateHome
	 *            folder which have freemarker templates
	 * @param inspectionHome
	 *            folder which have inspection resources
	 * @param isCLIMode
	 *            execution mode
	 */
	void install(ArchetypeGenerationRequest request, String baseDir,
			String pluginName, String pluginVersion, File pluginJar,
			String encoding, boolean pomHandling, boolean excludeSrc,
			String templateHome, String inspectionHome, boolean isCLIMode)
			throws Exception;

	/**
	 * merge a archetype resource and copy a merged file to target folder
	 * 
	 * @param context
	 *            velocity context
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginZip
	 *            zip file includes plugin binary file
	 * @param resourceDir
	 *            plugin resources folder
	 * @param packageName
	 *            project's base package name
	 * @param packaged
	 *            whether a plugin resource has package (ex. java)
	 * @param filtered
	 *            whether a plugin resource will be merged with velocity context
	 * @param originalTemplates
	 *            original archetype resources
	 * @param changedTemplates
	 *            changed archetype resources (archetype-resources ->
	 *            plugin-resources)
	 * @param encoding
	 *            file encoding style
	 */
	public void processTemplate(Context context, File targetDir,
			ZipFile pluginZip, String resourceDir, String packageName,
			boolean packaged, boolean filtered, List<String> originalTemplates,
			List<String> changedTemplates, String encoding) throws Exception;

	/**
	 * make a output file
	 * 
	 * @param context
	 *            velocity context
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param resourceDir
	 *            plugin resources folder
	 * @param template
	 *            a plugin resource
	 * @param packaged
	 *            whether a plugin resource has package (ex. java)
	 * @param packageName
	 *            project's base package name
	 * @return output file
	 */
	File getOutput(Context context, File targetDir, String resourceDir,
			String template, boolean packaged, String packageName);

	/**
	 * copy dependent libraries of plugin to target folder
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param projectType
	 *            generated project's style ('web' or 'service')
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginJar
	 *            plugin binary file
	 * @param pomProperties
	 *            properties of pom in current project
	 */
	void processDependencyLibs(ArchetypeGenerationRequest request,
			String projectType, File targetDir, File pluginJar,
			Properties pomProperties);

	/**
	 * initialize velocity runtime configuration
	 */
	void initializeVelocity() throws Exception;

	/**
	 * analyze plugin's dependencies to install plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder path to install a plugin
	 * @param pluginNames
	 *            target plugin names to install
	 * @param pluginVersion
	 *            a target plugin version to install
	 * @param pluginJar
	 *            a target plugin binary file to install
	 * @param pluginInfoFromJar
	 *            plugin information reading from plugin jar
	 * @param visitedPlugins
	 *            plugins to be installed
	 */
	void analyzePluginDependencies(ArchetypeGenerationRequest request,
			String baseDir, String[] pluginNames, String pluginVersion,
			File pluginJar, PluginInfo pluginInfoFromJar,
			Map<String, TargetPluginInfo> visitedPlugins) throws Exception;

	/**
	 * check to proceed install/update plugins
	 * 
	 * @param visitedPlugins
	 *            install/update plugins
	 * @param isCLIMode
	 *            execution mode
	 * @return prompt message
	 */
	String checkInstall(Map<String, TargetPluginInfo> visitedPlugins,
			boolean isCLIMode) throws Exception;

	/**
	 * change classpath entries
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginInfo
	 *            plugin detail information
	 * @param pomHandling
	 *            whether current project is based on maven or ant
	 * @param pomProperties
	 *            properties of pom in current project
	 */
	public void processClasspath(ArchetypeGenerationRequest request,
			File targetDir, PluginInfo pluginInfo, boolean pomHandling,
			Properties pomProperties) throws Exception;
}
