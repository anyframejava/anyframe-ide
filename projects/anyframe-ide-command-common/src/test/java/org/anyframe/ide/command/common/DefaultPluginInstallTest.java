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
import java.util.List;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

/**
 * TestCase Name : DefaultPluginInstallTest <br>
 * <br>
 * [Description] : Test for Compoment 'PluginInstaller'<br>
 * 
 * <pre>
 * 1) core depends on (datasource, logging, spring)
 * 2) tiles, query, fileupload depends on (core)
 * 3) struts depends on (query, fileupload)
 * 4) query-ria depends on query
 * </pre>
 * 
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : install query 1.0.0 plugin and assert</li>
 * <li>#-2 Positive Case : install query 1.0.0 plugin, install core 2.0.0 plugin
 * and assert</li>
 * <li>#-3 Positive Case : install core 1.0.0 plugin, install struts 1.0.0
 * plugin and assert</li>
 * <li>#-4 Negative Case : install query-ria 1.0.0 plugin, install struts 3.0.0
 * plugin. version of core plugn is mismatched to eache other because struts
 * 3.0.0 plugin depends on query 2.0.0 and fileupload 1.0.0</li>
 * <li>#-5 Negative Case : install struts 3.0.0 plugin. version of core plugn is
 * mismatched to each other because struts 3.0.0 plugin depends on query 2.0.0
 * and fileupload 1.0.0</li>
 * <li>#-6 Negative Case : install tiles 1.0.0 plugin, install core 2.0.0
 * plugin. tiles plugn 1.0.0 is mismatched to core 2.0.0</li>
 * <li>#-7 Positive Case : install core 1.0.0 plugin, check dependent libraries.
 * and install core 2.0.0 plugin, check whether latest libraries is installed</li>
 * <li>#-8 Positive Case : install core 2.0.0 plugin with resources, assert</li>
 * </ul>
 */
public class DefaultPluginInstallTest extends AbstractCommandTest {
	PluginInstaller pluginInstaller;
	PluginUninstaller pluginUninstaller;
	PluginInfoManager pluginInfoManager = null;
	DefaultPluginPomManager pluginPomManager;

	String encoding = "UTF-8";
	boolean pomHandling = true;
	File baseDir = new File("./temp/myproject");

	/**
	 * lookup DefaultPluginInfoManager, DefaultPluginInstaller,
	 * DefaultPluginUninstaller
	 */
	protected void setUp() throws Exception {
		super.setUp();
		pluginInstaller = (PluginInstaller) lookup(DefaultPluginInstaller.class
				.getName());
		pluginUninstaller = (PluginUninstaller) lookup(DefaultPluginUninstaller.class
				.getName());
		pluginInfoManager = (PluginInfoManager) lookup(DefaultPluginInfoManager.class
				.getName());
		pluginPomManager = (DefaultPluginPomManager) lookup(DefaultPluginPomManager.class
				.getName());

		if (baseDir.exists()) {
			FileUtil.deleteDir(baseDir);
		}
		prepareProject(baseDir.getAbsolutePath());
	}

	/**
	 * [Flow #-1] Positive Case : install query 1.0.0 plugin and assert
	 */
	public void testInstallQuery100() throws Exception {
		boolean excludeSrc = true;

		File userHome = new File(currentPath
				+ CommonConstants.SRC_TEST_RESOURCES, "/user.home/1.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

		((DefaultPluginInstaller) pluginInstaller).setTest(true);
		pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				"query", null, null, encoding, pomHandling, excludeSrc, null,
				null, false);

		assertTrue("fail to install 'core' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"core", "1.0.0"));
		assertTrue("fail to install 'datasource' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"datasource", "1.0.0"));
		assertTrue("fail to install 'logging' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"logging", "1.0.0"));
		assertTrue("fail to install 'query' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"query", "1.0.0"));
	}

	/**
	 * [Flow #-2] Positive Case : install query 1.0.0 plugin, install core 2.0.0
	 * plugin and assert
	 */
	public void testInstallQuery100UpdateCore200() throws Exception {
		boolean excludeSrc = true;
		((DefaultPluginInstaller) pluginInstaller).setTest(true);

		File userHome = new File(currentPath
				+ CommonConstants.SRC_TEST_RESOURCES, "/user.home/1.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

		pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				"query", null, null, encoding, pomHandling, excludeSrc, null,
				null, false);

		assertTrue("fail to install 'core' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"core", "1.0.0"));

		userHome = new File(currentPath + CommonConstants.SRC_TEST_RESOURCES,
				"/user.home/2.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

		pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				"core", null, null, encoding, pomHandling, excludeSrc, null,
				null, false);

		assertTrue("fail to install 'core' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"core", "2.0.0"));
		assertTrue("fail to install 'query' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"query", "2.0.0"));
	}

	/**
	 * [Flow #-3] Positive Case : install core 1.0.0 plugin, install struts
	 * 1.0.0 plugin and assert
	 */
	public void testInstallCore100InstallStruts100() throws Exception {
		boolean excludeSrc = true;
		((DefaultPluginInstaller) pluginInstaller).setTest(true);

		File userHome = new File(currentPath
				+ CommonConstants.SRC_TEST_RESOURCES, "/user.home/1.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

		pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				"core", "1.0.0", null, encoding, pomHandling, excludeSrc, null,
				null, false);

		assertTrue("fail to install 'core' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"core", "1.0.0"));

		pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				"struts", "1.0.0", null, encoding, pomHandling, excludeSrc,
				null, null, false);

		assertTrue("fail to install 'struts' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"struts", "1.0.0"));
		assertTrue("fail to install 'query' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"query", "1.0.0"));
		assertTrue("fail to install 'fileupload' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"fileupload", "1.0.0"));
	}

	/**
	 * [Flow #-4] Negative Case : install query-ria 1.0.0 plugin, install struts
	 * 3.0.0 plugin. version of core plugn is mismatched to eache other because
	 * struts 3.0.0 plugin depends on query 2.0.0 and fileupload 1.0.0
	 */
	public void testInstallQuyerRia100InstallStruts300() throws Exception {
		boolean excludeSrc = true;
		((DefaultPluginInstaller) pluginInstaller).setTest(true);

		File userHome = new File(currentPath
				+ CommonConstants.SRC_TEST_RESOURCES, "/user.home/1.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

		pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				"query-ria", "1.0.0", null, encoding, pomHandling, excludeSrc,
				null, null, false);

		assertTrue("fail to install 'query-ria' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"query-ria", "1.0.0"));

		userHome = new File(currentPath + CommonConstants.SRC_TEST_RESOURCES,
				"/user.home/3.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

		try {
			pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
					"struts", "3.0.0", null, encoding, pomHandling, excludeSrc,
					null, null, false);
			fail("fail to stop installing struts 3.0.0");
		} catch (Exception e) {
			assertTrue("fail to catch an exception",
					e instanceof CommandException);
		}
	}

	/**
	 * [Flow #-5] Negative Case : install struts 3.0.0 plugin. version of core
	 * plugn is mismatched to each other because struts 3.0.0 plugin depends on
	 * query 2.0.0 and fileupload 1.0.0
	 */
	public void testInstallStruts300() throws Exception {
		boolean excludeSrc = true;
		((DefaultPluginInstaller) pluginInstaller).setTest(true);

		File userHome = new File(currentPath
				+ CommonConstants.SRC_TEST_RESOURCES, "/user.home/3.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

		try {
			pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
					"struts", "3.0.0", null, encoding, pomHandling, excludeSrc,
					null, null, false);
			fail("fail to stop installing struts 3.0.0");
		} catch (Exception e) {
			assertTrue("fail to catch an exception",
					e instanceof CommandException);
		}
	}

	/**
	 * [Flow #-6] Negative Case : install tiles 1.0.0 plugin, install core 2.0.0
	 * plugin. tiles plugn 1.0.0 is mismatched to core 2.0.0
	 */
	public void testInstallTiles100UpdateCore200() throws Exception {
		boolean excludeSrc = true;
		((DefaultPluginInstaller) pluginInstaller).setTest(true);

		File userHome = new File(currentPath
				+ CommonConstants.SRC_TEST_RESOURCES, "/user.home/1.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

		pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				"tiles", "1.0.0", null, encoding, pomHandling, excludeSrc,
				null, null, false);

		assertTrue("fail to install 'tiles' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"tiles", "1.0.0"));

		userHome = new File(currentPath + CommonConstants.SRC_TEST_RESOURCES,
				"/user.home/2.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

		try {
			pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
					"core", "2.0.0", null, encoding, pomHandling, excludeSrc,
					null, null, false);
			fail("fail to stop installing core 2.0.0");
		} catch (Exception e) {
			assertTrue("fail to catch an exception",
					e instanceof CommandException);
		}

	}

	/**
	 * [Flow #-7] Positive Case : install core 1.0.0 plugin, check dependent
	 * libraries. and install core 2.0.0 plugin, check whether latest libraries
	 * is installed
	 */
	@SuppressWarnings("unchecked")
	public void testInstallLatestLibraries() throws Exception {
		boolean excludeSrc = true;
		((DefaultPluginInstaller) pluginInstaller).setTest(true);

		File userHome = new File(currentPath
				+ CommonConstants.SRC_TEST_RESOURCES, "/user.home/1.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

		pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				"core", "1.0.0", null, encoding, pomHandling, excludeSrc, null,
				null, false);

		assertTrue("fail to install 'core' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"core", "1.0.0"));

		Model currentModel = pluginPomManager.readPom(new File(baseDir,
				Constants.ARCHETYPE_POM));
		List<Dependency> currentDependencies = currentModel.getDependencies();
		for (Dependency currentDependency : currentDependencies) {
			if (currentDependency.getArtifactId().equals("commons-dbcp")) {
				assertEquals("fail to process dependencies.", "1.0",
						currentDependency.getVersion());
			}
			if (currentDependency.getArtifactId().equals("commons-pool")) {
				assertEquals("fail to process dependencies.", "1.0.1",
						currentDependency.getVersion());
			}
		}

		userHome = new File(currentPath + CommonConstants.SRC_TEST_RESOURCES,
				"/user.home/2.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

		pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				"core", "2.0.0", null, encoding, pomHandling, excludeSrc, null,
				null, false);

		assertTrue("fail to install 'core' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"core", "2.0.0"));

		currentModel = pluginPomManager.readPom(new File(baseDir,
				Constants.ARCHETYPE_POM));
		currentDependencies = currentModel.getDependencies();
		for (Dependency currentDependency : currentDependencies) {
			if (currentDependency.getArtifactId().equals("commons-dbcp")) {
				assertEquals("fail to process dependencies.", "1.2.2",
						currentDependency.getVersion());
			}
			if (currentDependency.getArtifactId().equals("commons-pool")) {
				assertEquals("fail to process dependencies.", "1.5.3",
						currentDependency.getVersion());
			}
		}
	}

	/**
	 * [Flow #-8] Positive Case : install core 2.0.0 plugin with resources,
	 * assert
	 */
	public void testInstallCore200WithSource() throws Exception {
		boolean excludeSrc = false;
		((DefaultPluginInstaller) pluginInstaller).setTest(true);

		File userHome = new File(currentPath
				+ CommonConstants.SRC_TEST_RESOURCES, "/user.home/2.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

		pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				"core", "2.0.0", null, encoding, pomHandling, excludeSrc, null,
				null, false);

		assertTrue("fail to install 'core' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"core", "2.0.0"));

		assertTrue(
				"fail to install 'core' plugin resources",
				new File(
						"./temp/myproject/src/main/java/org/anyframe/plugin/org/anyframe/plugin/core/moviefinder/MovieFinder.java")
						.exists());
	}

	/**
	 * prepare test project
	 */
	private void prepareProject(String baseDir) throws Exception {
		File sampleDir = new File("./src/test/resources/project/installsample");

		File metadataDir = new File(baseDir, "META-INF");
		metadataDir.mkdirs();

		FileUtil.copyDir(new File(sampleDir.getAbsolutePath() + "/META-INF/",
				CommonConstants.PLUGIN_INSTALLED_FILE), metadataDir);

		FileUtil.copyDir(new File(sampleDir.getAbsolutePath() + "/META-INF/",
				CommonConstants.METADATA_FILE), metadataDir);

		FileUtil.copyDir(new File(sampleDir, Constants.ARCHETYPE_POM),
				new File(baseDir));

		File webDir = new File(baseDir, "/src/main/webapp/WEB-INF/");
		webDir.mkdirs();

		new File(baseDir, "src/main/java").mkdirs();
		new File(baseDir, "src/main/resources/spring").mkdirs();
		new File(baseDir, "src/test/java").mkdirs();
		new File(baseDir, "src/test/resources").mkdirs();
		new File(baseDir, "src/main/webapp/WEB-INF/jsp").mkdirs();
	}
}
