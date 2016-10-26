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

import java.util.Map;

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.apache.maven.archetype.ArchetypeGenerationRequest;

/**
 * TestCase Name : DefaultPluginInfoManagerTest <br>
 * <br>
 * [Description] : Test for getting plugin information in detail<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : get plugin information (detail information, depended
 * plugins, dependent plugins)</li>
 * <li>#-2 Positive Case : get all plugin information and check 'installed'
 * property value</li>
 * <li>#-3 Positive Case : get all installable plugins</li>
 * </ul>
 */
public class DefaultPluginInfoManagerTest extends AbstractCommandTest {
	DefaultPluginInfoManager pluginInfoManager = null;

	/**
	 * lookup DefaultPluginInfoManager
	 */
	protected void setUp() throws Exception {
		super.setUp();
		pluginInfoManager = (DefaultPluginInfoManager) lookup(DefaultPluginInfoManager.class
				.getName());
	}

	/**
	 * [Flow #-1] Positive Case : get plugin information (detail information,
	 * depended plugins, dependent plugins)
	 */
	public void testGetPluginDetail() throws Exception {
		ArchetypeGenerationRequest request = createRequest("");
		String pluginName = "remoting";
		String pluginVersion = "1.0.0";

		// 1. find a plugin information
		PluginInfo pluginInfo = pluginInfoManager.getPluginInfo(request,
				pluginName, pluginVersion);

		// 2. assert
		assertEquals("fail to convert xml to plugin object - name.",
				"remoting", pluginInfo.getName());
		assertEquals("fail to convert xml to plugin object - groupId.",
				"org.anyframe.plugin", pluginInfo.getGroupId());
		assertEquals("fail to convert xml to plugin object - artifactId.",
				"anyframe-remoting-pi", pluginInfo.getArtifactId());
		assertEquals("fail to convert xml to plugin object - version.",
				"1.0.0", pluginInfo.getVersion());
		assertEquals("fail to convert xml to plugin object - dependencies.",
				"core", pluginInfo.getDependentPlugins().get(0).getName());
		assertEquals("fail to convert xml to plugin object - dependencies.",
				"1.0.0>=*", pluginInfo.getDependentPlugins().get(0)
						.getVersion());

		// 3. find plugin list which depends on a specific plugin
		Map<String, String> dependendedPlugins = pluginInfoManager
				.getDependedPlugins(request, PATH_SAMPLE_PROJECT, pluginInfo);

		// 4. assert
		assertEquals(
				"fail to find plugin list which depends on a specific plugin.",
				0, dependendedPlugins.size());

		// 5. find plugin list which a specific plugin depends on
		Map<String, String> dependentPlugins = pluginInfoManager
				.getDependentPlugins(request, pluginInfo);

		// 6. assert
		assertEquals(
				"fail to find plugin list which a specific plugin depends on.",
				1, dependentPlugins.size());
	}

	/**
	 * [Flow #-2] Positive Case : get all plugin information and check
	 * 'installed' property value
	 */
	public void testGetPluginsWithInstallInfo() throws Exception {
		Map<String, PluginInfo> plugins = pluginInfoManager
				.getPluginsWithInstallInfo(createRequest(""), PATH_SAMPLE_PROJECT);

		assertNotNull("fail to find plugin list.", plugins.get("cxf"));
	}

	/**
	 * [Flow #-3] Positive Case : get all installable plugins
	 */
	public void testGetInstallablePlugins() throws Exception {
		Map<String, PluginInfo> installablePlugins = pluginInfoManager
				.getInstallablePlugins(createRequest(""), PATH_SAMPLE_PROJECT);

		assertEquals("fail to get installable plugins.", 8, installablePlugins
				.size());
	}

	public void testShowPluginInfo() throws Exception {
		pluginInfoManager.showPluginInfo(createRequest(""), "remoting");
	}
}
