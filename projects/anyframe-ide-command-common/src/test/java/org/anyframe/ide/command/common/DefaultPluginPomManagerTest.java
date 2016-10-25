/*
 * Copyright 2002-2008 the original author or authors.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

/**
 * TestCase Name : DefaultPluginPomManagerTest <br>
 * <br>
 * [Description] : Test for Compoment 'PluginPomManager'<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : ind dependent libraries to be removed in current
 * project.</li>
 * <li>#-2 Positive Case : get dependencies about a custom plugin.</li>
 * <li>#-3 Positive Case : get a pom information about a custom plugin.</li>
 * <li>#-4 Positive Case : merge a sample-project pom file with pom file of
 * custom plugin.</li>
 * <li>#-5 Positive Case : read a pom file of cusom plugin.</li>
 * <li>#-6 Positive Case : find dependent libraries to be removed in sample
 * project and rewrite a pom file.</li>
 * </ul>
 */
public class DefaultPluginPomManagerTest extends AbstractCommandTest {

	DefaultPluginPomManager pomManager;
	DefaultPluginInfoManager pluginInfoManager;

	/**
	 * initialize
	 */
	protected void setUp() throws Exception {
		super.setUp();
		pomManager = (DefaultPluginPomManager) lookup(DefaultPluginPomManager.class
				.getName());
		pluginInfoManager = (DefaultPluginInfoManager) lookup(DefaultPluginInfoManager.class
				.getName());
	}

	/**
	 * [Flow #-1] Positive Case : find dependent libraries to be removed in
	 * current project.
	 * 
	 * @throws Exception
	 */
	public void testFindRemovedDependencies() throws Exception {
		// 1. make plugin information which are already installed
		Map<String, File> installedPluginJars = getPluginJars();
		File removePluginJar = installedPluginJars.get("duplicate-libraries");

		installedPluginJars.remove("duplicate-libraries");

		// 2. find dependent libraries to be removed
		List<Dependency> removes = pomManager.findRemovedDependencies(
				installedPluginJars, removePluginJar, new Properties());

		// 3. assert
		assertEquals("Fail to find file list to be removed.", 1, removes.size());

		Dependency dependency = removes.get(0);
		assertEquals("Fail to find a filename to be removed.",
				"struts-core-1.3.10.jar", dependency.getArtifactId() + "-"
						+ dependency.getVersion() + ".jar");
	}

	/**
	 * [Flow #-2] Positive Case : get dependencies about a custom plugin.
	 * 
	 * @throws Exception
	 */
	public void testGetCompileScopeDependencies() throws Exception {
		// 1. get dependencies of remoting plugin
		Map<String, File> pluginJars = getPluginJars();
		List<Dependency> dependencies = pomManager
				.getCompileScopeDependencies(pluginJars.get("remoting"));

		// 2. assert
		assertEquals("Fail to find dependencies of custom plugin.", 1,
				dependencies.size());
	}

	/**
	 * [Flow #-3] Positive Case : get a pom information about a custom plugin.
	 * 
	 * @throws Exception
	 */
	public void testGetPluginPom() throws Exception {
		// 1. get plugin jars
		Map<String, File> pluginJars = getPluginJars();

		// 2. assert
		if (pluginJars.get("remoting").exists()) {
			Model model = pomManager.getPluginPom(pluginJars.get("remoting"));
			assertEquals("Fail to find a plugin info - groupId",
					"org.anyframe.plugin", model.getGroupId());
			assertEquals("Fail to find a plugin info - artifactId",
					"anyframe-remoting-pi", model.getArtifactId());
			assertEquals("Fail to find a plugin info - version", "1.0.0", model
					.getVersion());
			assertEquals("Fail to find a plugin info - dependencies", 1, model
					.getDependencies().size());
		} else {
			fail("Fail to find a pluginjar file. Check a anyframe-remoting-pi-1.0.0.jar file in src/test/resources folder.");
		}
	}

	/**
	 * [Flow #-4] Positive Case : merge a sample-project pom file with pom file
	 * of custom plugin.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void testMergeDependencies() throws Exception {
		// 1. find a pom file of sample project
		File currentPom = new File(PATH_SAMPLE_PROJECT, "pom.xml");

		// 2. find a pom file of remoting plugin
		Map<String, File> pluginJars = getPluginJars();
		File remotingPluginJar = pluginJars.get("remoting");
		InputStream inputStream = pluginInfoManager.getPluginResource(
				"plugin-resources/pom.xml", remotingPluginJar);
		File newPom = readFileContent(inputStream);

		// 3. merge a sample-project pom file with pom file of remoting plugin
		List dependencies = pomManager.mergePom(newPom, currentPom);

		// 4. assert
		assertEquals("Fail to merge a new pom with current pom.", 53,
				dependencies.size());

		Model model = pomManager.readPom(newPom);
		assertEquals("Fail to find a model info - dependencies", 53, model
				.getDependencies().size());
	}

	/**
	 * [Flow #-5] Positive Case : read a pom file of cusom plugin.
	 * 
	 * @throws Exception
	 */
	public void testReadPom() throws Exception {
		// 1. find a pom file of custom plugin
		File pomFile = new File(PATH_SAMPLE_PROJECT, "pom.xml");

		// 2. get model from a pom file
		Model model = pomManager.readPom(pomFile);

		// 3. assert
		assertEquals("Fail to find a model info - groupId",
				"org.anyframe.plugin", model.getGroupId());
		assertEquals("Fail to find a model info - artifactId",
				"anyframe-remoting-pi", model.getArtifactId());
		assertEquals("Fail to find a model info - version", "1.0.0", model
				.getVersion());
		assertEquals("Fail to find a model info - dependencies", 53, model
				.getDependencies().size());
	}

	/**
	 * [Flow #-6] Positive Case : find dependent libraries to be removed in
	 * sample project and rewrite a pom file.
	 * 
	 * @throws Exception
	 */
	public void testRemovePomDependencies() throws Exception {
		// 1. get a pom file of sample project
		File baseDir = new File("./temp/sample");
		if (baseDir.exists()) {
			FileUtil.deleteDir(baseDir);
		}
		baseDir.mkdirs();

		File pomFile = new File(baseDir, Constants.ARCHETYPE_POM);
		pomFile.createNewFile();

		FileUtil
				.copyDir(
						new File(PATH_SAMPLE_PROJECT, Constants.ARCHETYPE_POM),
						baseDir);

		// 2. add a dependency
		Model currentModel = pomManager.readPom(pomFile);
		Dependency dependency = new Dependency();
		dependency.setGroupId("org.apache.struts");
		dependency.setArtifactId("struts-core");
		dependency.setVersion("1.3.10");
		currentModel.addDependency(dependency);

		pomManager.writePom(currentModel, pomFile, pomFile);

		// 3. assert
		Model model = pomManager.readPom(pomFile);
		assertEquals("Fail to find a model info - dependencies", 54, model
				.getDependencies().size());

		// 4. make plugin information which are already installed
		Map<String, File> installedPluginJars = getPluginJars();
		File removePluginJar = installedPluginJars.get("duplicate-libraries");

		installedPluginJars.remove("duplicate-libraries");

		// 5. try to remove dependencies of current pom file
		pomManager.removePomDependencies(pomFile, installedPluginJars,
				removePluginJar, new Properties());

		// 6. assert
		model = pomManager.readPom(pomFile);
		assertEquals("Fail to find a model info - dependencies", 53, model
				.getDependencies().size());
	}

	/**
	 * find install plugins jars.
	 */
	private Map<String, File> getPluginJars() throws Exception {
		Map<String, File> installedPluginJars = new HashMap<String, File>();
		installedPluginJars
				.put(
						"core",
						new File(REPO,
								"/org/anyframe/plugin/anyframe-core-pi/1.0.0/anyframe-core-pi-1.0.0.jar"));
		installedPluginJars
				.put(
						"remoting",
						new File(
								REPO,
								"/org/anyframe/plugin/anyframe-remoting-pi/1.0.0/anyframe-remoting-pi-1.0.0.jar"));
		installedPluginJars
				.put(
						"duplicate-libraries",
						new File(
								REPO,
								"/org/anyframe/plugin/anyframe-duplicate-libraries-pi/1.0.0/anyframe-duplicate-libraries-pi-1.0.0.jar"));

		return installedPluginJars;
	}

	/**
	 * read pom.xml file and return
	 */
	private File readFileContent(InputStream inputStream) throws Exception {
		File file = new File("./temp/pom/pom.xml");

		if (file.exists()) {
			FileUtil.deleteFile(file);
		}
		file.getParentFile().mkdirs();
		file.createNewFile();

		try {
			OutputStream outputStream = new FileOutputStream(file);
			byte buf[] = new byte[1024];
			int len;
			while ((len = inputStream.read(buf)) > 0)
				outputStream.write(buf, 0, len);
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			fail("fail to temporary pom.xml");
		}

		return file;
	}
}
