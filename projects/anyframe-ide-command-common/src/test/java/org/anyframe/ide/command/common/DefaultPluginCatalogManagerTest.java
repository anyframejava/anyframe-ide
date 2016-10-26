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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.anyframe.ide.command.common.plugin.PluginInfo;

/**
 * TestCase Name : DefaultPluginCatalogManagerTest <br>
 * <br>
 * [Description] : Test for Component 'PluginCatalogManager'<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : get all plugins</li>
 * <li>#-6 Positive Case : display all plugins in plugin-catalog-xxx.xml.</li>
 * </ul>
 */
public class DefaultPluginCatalogManagerTest extends AbstractCommandTest {

	DefaultPluginCatalogManager pluginCatalogManager = null;

	/**
	 * lookup DefaultPluginCatalogManager
	 */
	protected void setUp() throws Exception {
		super.setUp();
		pluginCatalogManager = (DefaultPluginCatalogManager) lookup(DefaultPluginCatalogManager.class
				.getName());
	}

	/**
	 * [Flow #-2] Positive Case : get plugin with name
	 * 
	 * @throws Exception
	 */
	public void testGetPluginWithName() throws Exception {
		// 1. get plugin with name
		PluginInfo plugin = pluginCatalogManager.getPlugin(createRequest(""),
				"cxf");

		// 2. assert
		assertThat("Fail to get plugin", plugin.getName(), is("cxf"));

	}

	/**
	 * [Flow #-2] Negative Case : get plugin with invalid name
	 * 
	 * @throws Exception
	 */
	public void testGetPluginWithInvalidName() throws Exception {
		// 1. get plugin with name
		PluginInfo plugin = pluginCatalogManager.getPlugin(createRequest(""),
				"bullshit");

		// 2. assert
		assertThat("Fail to get plugin", plugin, nullValue());

	}

	/**
	 * [Flow #-3] Positive Case : get plugin with name and version
	 * 
	 * @throws Exception
	 */
	public void testGetPluginWithNameAndVersion() throws Exception {
		// 1. get plugin with name and version
		PluginInfo plugin = pluginCatalogManager.getPlugin(createRequest(""),
				"cxf-jaxrs", "1.0.0");

		// 2. assert
		assertThat("Fail to get plugin", plugin.getName(), is("cxf-jaxrs"));
	}

	/**
	 * [Flow #-3-1] Negative Case : get plugin with invalid name and version
	 * 
	 * @throws Exception
	 */
	public void testGetPluginWithInvalidNameAndVersion() throws Exception {
		// 1. get plugin with name and version
		PluginInfo plugin = pluginCatalogManager.getPlugin(createRequest(""),
				"cxf-jaxrs", "9.9.9-SNAPSHOT");

		// 2. assert
		assertThat("Fail to get plugin", plugin, nullValue());
	}

	/**
	 * [Flow #-4] Positive Case : get plugins
	 * 
	 * @throws Exception
	 */
	public void testGetPlugins() throws Exception {
		// 1. get all plugins
		Map<String, PluginInfo> plugins = pluginCatalogManager
				.getPlugins(createRequest(""));

		// 2. assert (essential 1, optional 7)
		assertThat("Fail to get plugins", plugins.size(), is(8));
	}

	/**
	 * [Flow #-5] Positive Case : Check if core is essential. And check if cxf
	 * is not essential. And check if plugin with invalid name is not essential.
	 * 
	 * @throws Exception
	 */
	public void testIsEssentialPlugin() throws Exception {
		boolean coreIsEssential = pluginCatalogManager.isEssential(
				createRequest(""), "core", "1.0.0");
		assertThat(coreIsEssential, is(true));

		boolean cxfIsEssential = pluginCatalogManager.isEssential(
				createRequest(""), "cxf", "1.0.0");
		assertThat(cxfIsEssential, not(true));

		boolean bullshitIsEssential = pluginCatalogManager.isEssential(
				createRequest(""), "bullshit", "1.0.0");
		assertThat(bullshitIsEssential, not(true));
	}

	/**
	 * [Flow #-6] Positive Case : display all plugins in plugin-catalog-xxx.xml.
	 */
//	public void testShowPlugins() throws Exception {
//		pluginCatalogManager.showPlugins(createRequest(""));
//	}
}
