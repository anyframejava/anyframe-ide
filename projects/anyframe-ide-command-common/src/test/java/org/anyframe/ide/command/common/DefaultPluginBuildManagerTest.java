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

import org.anyframe.ide.command.common.plugin.DependentPlugin;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.model.Model;

/**
 * TestCase Name : DefaultPluginBuildManagerTest <br>
 * <br>
 * [Description] : Test for component 'PluginBuildManager'<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : create a plugin build file.</li>
 * <li>#-2 Positive Case : remove a plugin build file.</li>
 * <li>#-3 Negative Case : fail to create a plugin build file without a project
 * information(project.mf) file.</li>
 * <li>#-4 Negative Case : fail to create a plugin build file without a
 * installed plugin(plugin-installed.xml) file.</li>
 * <li>#-5 Negative Case : fail to create a plugin build file without a project
 * base package information in project information(project.mf) file.</li>
 * <li>#-6 Negative Case : fail to create a plugin build file with invalid
 * plugin name in pom.xml file.</li>
 * </ul>
 */
public class DefaultPluginBuildManagerTest extends AbstractCommandTest {
	DefaultPluginBuildManager pluginBuildManager = null;
	DefaultPluginPomManager pluginPomManager = null;

	/**
	 * lookup DefaultPluginBuildManager
	 */
	protected void setUp() throws Exception {
		super.setUp();
		pluginBuildManager = (DefaultPluginBuildManager) lookup(DefaultPluginBuildManager.class
				.getName());
		pluginPomManager = (DefaultPluginPomManager) lookup(DefaultPluginPomManager.class
				.getName());
	}

	/**
	 * [Flow #-1] Positive Case : create a plugin build file .
	 */
	public void testCreatePluginBuild() throws Exception {
		// 1. set input arguments for creating a build file
		String projectHome = "./src/test/resources/project/sample-buildmanager";
		String baseDir = new File(projectHome).getAbsolutePath();

		// 2. create a plugin build file
		pluginBuildManager.activate(createRequest(baseDir), baseDir);

		// 3. assert created build file
		File pluginBuildXml = new File(baseDir,
				CommonConstants.PLUGIN_BUILD_FILE);
		assertTrue("fail to create plugin build file.", pluginBuildXml.exists());

		PluginInfo pluginBuildInfo = (PluginInfo) FileUtil
				.getObjectFromXML(pluginBuildXml);

		// 3.1 assert groupId, artifactId, version, name
		File pomFile = new File(baseDir, Constants.ARCHETYPE_POM);
		Model pom = this.pluginPomManager.readPom(pomFile);
		assertEquals(
				"fail to generate the right plugin name of plugin build file - name.",
				pom.getName(), pluginBuildInfo.getName());
		assertEquals(
				"fail to generate the right plugin groupId of plugin build file - groupId.",
				pom.getGroupId(), pluginBuildInfo.getGroupId());
		assertEquals(
				"fail to generate the right plugin artifactId of plugin build file - artifactId.",
				pom.getArtifactId(), pluginBuildInfo.getArtifactId());
		assertEquals(
				"fail to generate the right plugin version of plugin build file - version.",
				pom.getVersion(), pluginBuildInfo.getVersion());

		// 3.2 assert dependent plugins
		assertEquals("fail to get depedent plugins.", 1, pluginBuildInfo
				.getDependentPlugins().size());
		DependentPlugin dependentPlugin = pluginBuildInfo.getDependentPlugins()
				.get(0);
		assertEquals("fail to get depedent plugin name.", "core",
				dependentPlugin.getName());
		assertEquals("fail to get depedent plugin version.", "1.0.0",
				dependentPlugin.getVersion());

		// 3.3 assert samples, interceptor
		assertEquals("fail to get default samples value.", true,
				pluginBuildInfo.hasSamples());
		assertNotNull("fail to get default interceptor value.",
				pluginBuildInfo.getInterceptor());

		// 3.4 assert build filesets, resources
		assertEquals("fail to get default build filesets.", 5, pluginBuildInfo
				.getBuild().getFilesets().size());
		assertEquals("fail to get default resources.", 5, pluginBuildInfo
				.getResources().size());
	}

	/**
	 * [Flow #-2] Positive Case : remove a plugin build file .
	 */
	public void testRemovePluginBuild() throws Exception {
		// 1. set input arguments for creating a build file
		String projectHome = "./src/test/resources/project/sample-buildremove";
		String baseDir = new File(projectHome).getAbsolutePath();

		// 2. remove a plugin build file
		pluginBuildManager.deactivate(baseDir);

		// 3. assert removed build file
		File pluginBuildXml = new File(baseDir,
				CommonConstants.PLUGIN_BUILD_FILE);
		assertFalse("fail to remove plugin build file.",
				pluginBuildXml.exists());
	}

	/**
	 * [Flow #-3] Negative Case : fail to create a plugin build file without a
	 * project information(project.mf) file.
	 */
	public void testCreatePluginBuildWithoutProjectInfo() throws Exception {
		// 1. set input arguments for creating a build file
		String projectHome = "./src/test/resources/project/negative-noprojectinfo";
		String baseDir = new File(projectHome).getAbsolutePath();

		// 2. create a plugin build file
		try {
			pluginBuildManager.activate(createRequest(baseDir), baseDir);
		} catch (CommandException e) {
			File metadataFile = new File(new File(baseDir)
					+ CommonConstants.METAINF, CommonConstants.METADATA_FILE);
			assertEquals("Can not find a '" + metadataFile.getAbsolutePath()
					+ "' file. Please check a location of your project.",
					e.getMessage());
		}
	}

	/**
	 * [Flow #-4] Negative Case : fail to create a plugin build file without a
	 * installed plugin(plugin-installed.xml) file.
	 */
	public void testCreatePluginBuildWithoutInstalledPlugin() throws Exception {
		// 1. set input arguments for creating a build file
		String projectHome = "./src/test/resources/project/negative-noplugininstalled";
		String baseDir = new File(projectHome).getAbsolutePath();

		// 2. create a plugin build file
		try {
			pluginBuildManager.activate(createRequest(baseDir), baseDir);
		} catch (CommandException e) {
			File pluginInstalledFile = new File(baseDir
					+ CommonConstants.METAINF,
					CommonConstants.PLUGIN_INSTALLED_FILE);

			assertEquals(
					"Can not find a '"
							+ pluginInstalledFile.getAbsolutePath()
							+ "' file. Please check a location of your project.",
					e.getMessage());
		}
	}

	/**
	 * [Flow #-5] Negative Case : fail to create a plugin build file without a
	 * project base package information in project information(project.mf) file.
	 */
	public void testCreatePluginBuildWithoutPackageInfo() throws Exception {
		// 1. set input arguments for creating a build file
		String projectHome = "./src/test/resources/project/negative-nopackageinfo";
		String baseDir = new File(projectHome).getAbsolutePath();

		// 2. create a plugin build file
		try {
			pluginBuildManager.activate(createRequest(baseDir), baseDir);
		} catch (CommandException e) {
			File metadataFile = new File(new File(baseDir)
					+ CommonConstants.METAINF, CommonConstants.METADATA_FILE);
			assertEquals("Can not find a package.name property value in '"
					+ metadataFile.getAbsolutePath()
					+ "' file. Please check the package name.", e.getMessage());
		}
	}

	/**
	 * [Flow #-6] Negative Case : fail to create a plugin build file with
	 * invalid plugin name in pom.xml file.
	 */
	public void testCreatePluginBuildWithInvalidPluginName() throws Exception {
		// 1. set input arguments for creating a build file
		String projectHome = "./src/test/resources/project/negative-invalidname";
		String baseDir = new File(projectHome).getAbsolutePath();

		// 2. create a plugin build file
		try {
			pluginBuildManager.activate(createRequest(baseDir), baseDir);
		} catch (CommandException e) {
			assertEquals(
					"The plugin name 'remoting plugin project' has special characters besides '.','-'. Please use another plugin name.",
					e.getMessage());
		}
	}
}
