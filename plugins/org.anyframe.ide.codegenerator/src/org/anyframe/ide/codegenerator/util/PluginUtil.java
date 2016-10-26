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
package org.anyframe.ide.codegenerator.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.command.ant.task.container.PluginContainer;
import org.anyframe.ide.command.common.DefaultPluginInstaller;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.plugin.TargetPluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.artifact.manager.WagonManager;

/**
 * This is an PluginUtil class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class PluginUtil {

	private static org.anyframe.ide.command.ant.task.collector.PluginCollector antCollector = null;
	private static org.anyframe.ide.command.maven.mojo.collector.PluginCollector mavenCollector = null;

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	protected PluginUtil() {
		throw new UnsupportedOperationException();
	}

	public static Map<String, PluginInfo> getPluginList(String pjtBuild,
			String anyframeHome, String baseDir) throws Exception {

		boolean isOffline = isOffline(baseDir);

		if (pjtBuild.equals(CommonConstants.PROJECT_BUILD_TYPE_ANT)) {

			antCollector = new org.anyframe.ide.command.ant.task.collector.PluginCollector(
					anyframeHome, baseDir, isOffline);

			PluginContainer container = antCollector.getContainer();
			WagonManager wagonManager = (WagonManager) container
					.lookup(WagonManager.class.getName());
			
			return antCollector.getPlugins(baseDir);
		} else {
			mavenCollector = new org.anyframe.ide.command.maven.mojo.collector.PluginCollector(
					baseDir);

			return mavenCollector.getPlugins(baseDir);
		}
	}

	public static String getLatestArchetypeVersion(String archetypeArtifactId,
			String pjtBuild, String anyframeHome, boolean isOffline)
			throws Exception {

		if (pjtBuild.equals(CommonConstants.PROJECT_BUILD_TYPE_ANT)) {

			antCollector = new org.anyframe.ide.command.ant.task.collector.PluginCollector(
					anyframeHome, null, isOffline);

			PluginContainer container = antCollector.getContainer();
			WagonManager wagonManager = (WagonManager) container
					.lookup(WagonManager.class.getName());
			
			return antCollector.getLatestArchetypeVersion(archetypeArtifactId);
		} else {
			mavenCollector = new org.anyframe.ide.command.maven.mojo.collector.PluginCollector(
					null);

			return mavenCollector
					.getLatestArchetypeVersion(archetypeArtifactId);
		}
	}

	public static List<String> getArchetypeVersions(String groupId,
			String archetypeArtifactId, String pjtBuild, String anyframeHome,
			boolean isOffline) throws Exception {

		if (pjtBuild.equals(CommonConstants.PROJECT_BUILD_TYPE_ANT)) {

			antCollector = new org.anyframe.ide.command.ant.task.collector.PluginCollector(
					anyframeHome, null, isOffline);

			PluginContainer container = antCollector.getContainer();
			WagonManager wagonManager = (WagonManager) container
					.lookup(WagonManager.class.getName());
			
			return antCollector.getArchetypeVersions(groupId,
					archetypeArtifactId);
		} else {
			mavenCollector = new org.anyframe.ide.command.maven.mojo.collector.PluginCollector(
					null);

			return mavenCollector.getArchetypeVersions(groupId,
					archetypeArtifactId);
		}
	}

	public static String checkInstall(String pjtBuild, String anyframeHome,
			String baseDir, String[] pluginNames) throws Exception {

		boolean isOffline = isOffline(baseDir);
		DefaultPluginInstaller installer = null;
		ArchetypeGenerationRequest request = null;
		if (pjtBuild.equals(CommonConstants.PROJECT_BUILD_TYPE_ANT)) {

			antCollector = new org.anyframe.ide.command.ant.task.collector.PluginCollector(
					anyframeHome, baseDir, isOffline);

			PluginContainer container = antCollector.getContainer();
			installer = (DefaultPluginInstaller) container
					.lookup(DefaultPluginInstaller.class.getName());
			request = container.getRequest();
			WagonManager wagonManager = (WagonManager) container
					.lookup(WagonManager.class.getName());
			
		} else {

			mavenCollector = new org.anyframe.ide.command.maven.mojo.collector.PluginCollector(
					baseDir);

			org.anyframe.ide.command.maven.mojo.container.PluginContainer container = mavenCollector
					.getContainer();
			installer = (DefaultPluginInstaller) container
					.lookup(DefaultPluginInstaller.class.getName());
			request = container.getRequest();
		}
		Map<String, TargetPluginInfo> visitedPlugins = new HashMap<String, TargetPluginInfo>();

		installer.analyzePluginDependencies(request, baseDir, pluginNames,
				null, null, null, visitedPlugins);

		return installer.checkInstall(visitedPlugins, false);
	}

	public static Map<String, PluginInfo> getInstalledPluginList(
			String pjtBuild, String anyframeHome, String baseDir)
			throws Exception {

		boolean isOffline = isOffline(baseDir);

		if (pjtBuild.equals(CommonConstants.PROJECT_BUILD_TYPE_ANT)) {

			antCollector = new org.anyframe.ide.command.ant.task.collector.PluginCollector(
					anyframeHome, baseDir, isOffline);
			PluginContainer container = antCollector.getContainer();
			WagonManager wagonManager = (WagonManager) container
					.lookup(WagonManager.class.getName());
			return antCollector.getInstalledPlugins(baseDir);
		} else {

			mavenCollector = new org.anyframe.ide.command.maven.mojo.collector.PluginCollector(
					baseDir);

			return mavenCollector.getInstalledPlugins(baseDir);
		}
	}

	public static Set<String> getPluginNameSet(String pjtBuild,
			String anyframeHome, boolean isOffline) throws Exception {
		if (pjtBuild.equals(CommonConstants.PROJECT_BUILD_TYPE_ANT)) {
			org.anyframe.ide.command.ant.task.collector.PluginCollector collector = new org.anyframe.ide.command.ant.task.collector.PluginCollector(
					anyframeHome, null, isOffline);
			return collector.getPluginNames();
		} else {
			org.anyframe.ide.command.maven.mojo.collector.PluginCollector collector = new org.anyframe.ide.command.maven.mojo.collector.PluginCollector(
					null);
			return collector.getPluginNames();
		}
	}

	private static boolean isOffline(String baseDir) throws Exception {
		// get offline option
		String properties = baseDir + CommonConstants.METAINF
				+ CommonConstants.METADATA_FILE;
		PropertiesIO appProps = new PropertiesIO(properties);
		boolean offlineValue = new Boolean(
				appProps.readValue(CommonConstants.OFFLINE)).booleanValue();

		return offlineValue;
	}
}
