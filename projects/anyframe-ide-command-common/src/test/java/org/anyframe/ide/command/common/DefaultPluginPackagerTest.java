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

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.model.Model;

/**
 * TestCase Name : DefaultPluginPackagerTest <br>
 * <br>
 * [Description] : Test for component 'PluginPackager'<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : package a remoting plugin and check the generated
 * resources.</li>
 * <li>#-2 Negative Case : fail to package a remoting plugin without a
 * dependency with core plugin.</li>
 * <li>#-3 Negative Case : fail to package a remoting plugin without a plugin
 * build script(plugin-build.xml) file.</li>
 * <li>#-4 Negative Case : fail to package a remoting plugin without a project
 * information(project.mf) file.</li>
 * <li>#-5 Negative Case : fail to package a remoting plugin with mismatched
 * dependent plugin version.</li>
 * </ul>
 */
public class DefaultPluginPackagerTest extends AbstractCommandTest {
	DefaultPluginPackager pluginPackager = null;
	DefaultPluginPomManager pluginPomManager = null;

	/**
	 * lookup DefaultPluginPackager
	 */
	protected void setUp() throws Exception {
		super.setUp();
		pluginPackager = (DefaultPluginPackager) lookup(DefaultPluginPackager.class
				.getName());
		pluginPomManager = (DefaultPluginPomManager) lookup(DefaultPluginPomManager.class
				.getName());
	}

	/**
	 * [Flow #-1] Positive Case : package a remoting plugin and check the
	 * generated resources.
	 */
	public void testPackagePlugin() throws Exception {
		// 1. set input arguments for packaging a remoting plugin
		//String projectHome = "./src/test/resources/project/sample";
		String projectHome = PATH_SAMPLE_PROJECT;
		String baseDir = new File(projectHome).getAbsolutePath();
		String tempDir = "target" + CommonConstants.fileSeparator + "temp";
		String metaInfDir = tempDir + CommonConstants.METAINF_ANYFRAME;
		String pluginResourcesDir = tempDir + CommonConstants.fileSeparator
				+ CommonConstants.PLUGIN_RESOURCES;

		// 2. package a sample plugin
		pluginPackager.packagePlugin(createRequest(""), baseDir);

		// 3. assert generated resources
		File pluginBuildXml = new File(baseDir,
				CommonConstants.PLUGIN_BUILD_FILE);
		PluginInfo pluginBuildInfo = (PluginInfo) FileUtil
				.getObjectFromXML(pluginBuildXml);

		// 3.1 target/temp/pom.xml
		File pluginPomFile = new File(baseDir + CommonConstants.fileSeparator
				+ tempDir, Constants.ARCHETYPE_POM);
		assertTrue("fail to generate plugin pom.xml for 'remoting' plugin",
				pluginPomFile.exists());
		Model pluginPom = this.pluginPomManager.readPom(pluginPomFile);
		assertEquals(
				"fail to generate the right plugin groupId of plugin pom file - groupId.",
				pluginBuildInfo.getGroupId(), pluginPom.getGroupId());
		assertEquals(
				"fail to generate the right plugin artifactId of plugin pom file - artifactId.",
				pluginBuildInfo.getArtifactId(), pluginPom.getArtifactId());
		assertEquals(
				"fail to generate the right plugin version of plugin pom file - version.",
				pluginBuildInfo.getVersion(), pluginPom.getVersion());

		// 3.2 target/temp/META-INF/anyframe/plugin.xml
		File pluginXMLFile = new File(baseDir + CommonConstants.fileSeparator
				+ metaInfDir, CommonConstants.PLUGIN_FILE);
		assertTrue("fail to generate plugin.xml for 'remoting' plugin",
				pluginXMLFile.exists());
		PluginInfo pluginInfo = (PluginInfo) FileUtil
				.getObjectFromXML(pluginXMLFile);
		assertEquals(
				"fail to generate the right plugin name of plugin.xml file - name.",
				pluginBuildInfo.getName(), pluginInfo.getName());
		assertEquals(
				"fail to generate the right plugin groupId of plugin.xml file - groupId.",
				pluginBuildInfo.getGroupId(), pluginInfo.getGroupId());
		assertEquals(
				"fail to generate the right plugin artifactId of plugin.xml file - artifactId.",
				pluginBuildInfo.getArtifactId(), pluginInfo.getArtifactId());
		assertEquals(
				"fail to generate the right plugin version of plugin.xml file - version.",
				pluginBuildInfo.getVersion(), pluginInfo.getVersion());

		// 3.3 target/temp/plugin-resources/pom.xml
		File pluginResourcesPomFile = new File(baseDir
				+ CommonConstants.fileSeparator + pluginResourcesDir,
				Constants.ARCHETYPE_POM);
		assertTrue("fail to generate pom.xml for 'remoting' plugin resources",
				pluginResourcesPomFile.exists());
		Model pluginResourcesPom = this.pluginPomManager
				.readPom(pluginResourcesPomFile);
		assertEquals(
				"fail to generate the right plugin groupId of plugin resources pom file - groupId.",
				pluginBuildInfo.getGroupId(), pluginResourcesPom.getGroupId());
		assertEquals(
				"fail to generate the right plugin artifactId of plugin resources pom file - artifactId.",
				pluginBuildInfo.getArtifactId(),
				pluginResourcesPom.getArtifactId());
		assertEquals(
				"fail to generate the right plugin version of plugin resources pom file - version.",
				pluginBuildInfo.getVersion(), pluginResourcesPom.getVersion());

		// 3.4 check filesets (includes/excludes)
		assertTrue(
				"fail to include a remoting plugin resource - java",
				new File(baseDir + CommonConstants.fileSeparator
						+ pluginResourcesDir,
						"/src/main/java/remoting/moviefinder/web/MovieFinderController.java")
						.exists());
		assertTrue("fail to include a remoting plugin resource - xml",
				new File(baseDir + CommonConstants.fileSeparator
						+ pluginResourcesDir,
						"/src/main/resources/spring/context-remoting.xml")
						.exists());
		assertTrue(
				"fail to include a remoting plugin resource - jsp",
				new File(baseDir + CommonConstants.fileSeparator
						+ pluginResourcesDir,
						"/src/main/webapp/WEB-INF/jsp/remoting/moviefinder/movie/list.jsp")
						.exists());
		assertFalse(
				"fail to exclude a core plugin resource - java",
				new File(baseDir + CommonConstants.fileSeparator
						+ pluginResourcesDir,
						"/src/main/java/core/moviefinder/service/MovieService.java")
						.exists());
		assertFalse(
				"fail to exclude a core plugin resource - jsp",
				new File(baseDir + CommonConstants.fileSeparator
						+ pluginResourcesDir,
						"/src/main/webapp/WEB-INF/jsp/core/moviefinder/movie/list.jsp")
						.exists());

		// 3.5 check JAR Archive file
		File jarFile = new File(baseDir + CommonConstants.fileSeparator
				+ "target", pluginBuildInfo.getArtifactId() + "-"
				+ pluginBuildInfo.getVersion() + ".jar");
		assertTrue("fail to generate remoting plugin jar file.",
				jarFile.exists());
	}

	/**
	 * [Flow #-2] Negative Case : fail to package a remoting plugin without a
	 * dependency with core plugin.
	 */
	public void testPackagePluginWithoutCoreDependency() throws Exception {
		// 1. set input arguments for packaging a remoting plugin
		String projectHome = "./src/test/resources/project/negative-nocoredependency";
		String baseDir = new File(projectHome).getAbsolutePath();

		// 2. package a sample plugin
		try {
			pluginPackager.packagePlugin(createRequest(""), baseDir);
		} catch (CommandException e) {
			assertEquals(
					"You need to have a dependency with essential plugins.(ex. core plugin)",
					e.getMessage());
		}
	}

	/**
	 * [Flow #-3] Negative Case : fail to package a remoting plugin without a
	 * plugin build script(plugin-build.xml) file.
	 */
	public void testPackagePluginWithoutPluginBuildXml() throws Exception {
		// 1. set input arguments for packaging a remoting plugin
		String projectHome = "./src/test/resources/project/negative-nopluginbuild";
		String baseDir = new File(projectHome).getAbsolutePath();

		// 2. package a sample plugin
		try {
			pluginPackager.packagePlugin(createRequest(""), baseDir);
		} catch (CommandException e) {
			assertEquals(
					"You need plugin-build.xml file to package your plugin. Please try activate-plugin first.",
					e.getMessage());
		}
	}

	/**
	 * [Flow #-4] Negative Case : fail to package a remoting plugin without a
	 * project information(project.mf) file.
	 */
	public void testPackagePluginWithoutProjectInfo() throws Exception {
		// 1. set input arguments for packaging a remoting plugin
		String projectHome = "./src/test/resources/project/negative-noprojectinfo";
		String baseDir = new File(projectHome).getAbsolutePath();

		// 2. package a sample plugin
		try {
			pluginPackager.packagePlugin(createRequest(""), baseDir);
		} catch (CommandException e) {
			File metadataFile = new File(new File(baseDir)
					+ CommonConstants.METAINF, CommonConstants.METADATA_FILE);
			assertEquals("Can not find a '" + metadataFile.getAbsolutePath()
					+ "' file. Please check a location of your project.",
					e.getMessage());
		}
	}

	/**
	 * [Flow #-5] Negative Case : fail to package a remoting plugin with
	 * mismatched dependent plugin version.
	 */
	public void testPackagePluginMismatchVersion() throws Exception {
		// 1. set input arguments for packaging a remoting plugin
		String projectHome = "./src/test/resources/project/negative-mismatchversion";
		String baseDir = new File(projectHome).getAbsolutePath();

		// 2. package a sample plugin
		try {
			pluginPackager.packagePlugin(createRequest(""), baseDir);
		} catch (CommandException e) {
			assertEquals(
					"You should modify the version range of dependent 'core' plugin in plugin-build.xml file. The version of installed 'core' plugin is not matched with the version range.",
					e.getMessage());
		}
	}
}
