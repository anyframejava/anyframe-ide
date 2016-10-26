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

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.maven.archetype.common.Constants;

/**
 * TestCase Name : DefaultPluginUninstallTest <br>
 * <br>
 * [Description] : Test for Component 'PluginUninstaller'<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : install a 'core' plugin and uninstall a 'core' plugin
 * plugin</li>
 * <li>#-2 Negative Case : install a 'mip-query' plugin and uninstall a 'query'
 * plugin</li>
 * <li>#-3 Positive Case : install a 'mip-query' plugin and force uninstall a 'query'
 * plugin</li>
 * <li>#-4 Positive Case : install a 'mip-query' plugin and uninstall a
 * 'mip-query' plugin</li>
 * </ul>
 */
public class DefaultPluginUninstallTest extends AbstractCommandTest {
	DefaultPluginUninstaller pluginUninstaller;
	DefaultPluginCatalogManager pluginCatalogManager;
	DefaultPluginInstaller pluginInstaller;
	DefaultPluginInfoManager pluginInfoManager;

	/**
	 * lookup DefaultPluginCatalogManager, DefaultPluginUninstaller
	 */
	protected void setUp() throws Exception {
		super.setUp();
		pluginUninstaller = (DefaultPluginUninstaller) lookup(DefaultPluginUninstaller.class
				.getName());
		pluginCatalogManager = (DefaultPluginCatalogManager) lookup(DefaultPluginCatalogManager.class
				.getName());
		pluginInstaller = (DefaultPluginInstaller) lookup(DefaultPluginInstaller.class
				.getName());
		pluginInfoManager = (DefaultPluginInfoManager) lookup(DefaultPluginInfoManager.class
				.getName());
	}

	/**
	 * [Flow #-1] Positive Case : install a 'core' plugin and uninstall a 'core'
	 * plugin
	 * 
	 * @throws Exception
	 */
	public void testUninstallEssentialPlugin() throws Exception {

		// 1. prepare test project
		File baseDir = new File("./temp/myproject");

		if (baseDir.exists()) {
			FileUtil.deleteDir(baseDir);
		}

		baseDir.mkdirs();
		prepareProject(baseDir.getAbsolutePath());

		// 2. install a 'core' plugin
		String pluginName = "core";
		String pluginVersion = "1.0.0";
		String encoding = "UTF-8";
		String excludes = "";
		boolean pomHandling = true;

		((DefaultPluginInstaller) pluginInstaller).setTest(true);
		pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				pluginName, pluginVersion, null, encoding, pomHandling, false,
				null, null, false);

		// 3. uninstall a 'core' plugin
		pluginUninstaller.uninstall(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				pluginName, excludes, encoding, pomHandling);

		// 4. assert
		assertFalse("fail to check if 'core' plugin is uninstalled.",
				pluginInfoManager.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir
						.getAbsolutePath(), pluginName, pluginVersion));
	}

	/**
	 * [Flow #-2] Negative Case : install a 'mip-query' plugin and uninstall a
	 * 'query' plugin
	 * 
	 * @throws Exception
	 */
	public void testUninstallDependedPlugin() throws Exception {
		File baseDir = new File("./temp/myproject");

		// 1. install 'mip-query' plugin
		installPlugin();

		// 2. uninstall a 'query' plugin
		String pluginName = "query";
		String pluginVersion = "1.0.0";
		String encoding = "UTF-8";
		String excludes = "";
		boolean pomHandling = true;

		pluginUninstaller.uninstall(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				pluginName, excludes, encoding, pomHandling);

		// 3. assert
		assertTrue("fail to check if 'query' plugin is not uninstalled.",
				pluginInfoManager.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir
						.getAbsolutePath(), pluginName, pluginVersion));
	}

	/**
	 * [Flow #-3] Positive Case : install a 'mip-query' plugin and uninstall a
	 * 'query' plugin
	 * 
	 * @throws Exception
	 */
	public void testForceUninstallDependedPlugin() throws Exception {
		File baseDir = new File("./temp/myproject");

		// 1. install 'mip-query' plugin
		installPlugin();

		// 2. uninstall a 'query' plugin
		String pluginName = "query";
		String pluginVersion = "1.0.0";
		String encoding = "UTF-8";
		String excludes = "";
		boolean pomHandling = true;

		pluginUninstaller.uninstall(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				pluginName, excludes, encoding, pomHandling, true);

		// 3. assert
		assertFalse("fail to check if 'query' plugin is uninstalled.",
				pluginInfoManager.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir
						.getAbsolutePath(), pluginName, pluginVersion));
	}

	/**
	 * [Flow #-4] Positive Case : install a 'mip-query' plugin and uninstall a
	 * 'mip-query' plugin
	 * 
	 * @throws Exception
	 */
	public void testUninstallPlugin() throws Exception {
		File baseDir = new File("./temp/myproject");

		// 1. install 'mip-query' plugin
		installPlugin();

		// 2. uninstall a 'mip-query' plugin
		String pluginName = "mip-query";
		String pluginVersion = "1.0.0";
		String encoding = "UTF-8";
		String excludes = "";
		boolean pomHandling = true;

		pluginUninstaller.uninstall(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				pluginName, excludes, encoding, pomHandling);

		assertFalse("fail to check if 'mip-query' plugin is uninstalled.",
				pluginInfoManager.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir
						.getAbsolutePath(), pluginName, pluginVersion));

		// TODO : 다른 plugin이 사용하는 dependency library 삭제 되지 않았는지 확인
	}

	/**
	 * for test fixture : install 'mip-query' plugin
	 * 
	 * @throws Exception
	 */
	private void installPlugin() throws Exception {
		// 1. prepare test project
		File baseDir = new File("./temp/myproject");

		if (baseDir.exists()) {
			FileUtil.deleteDir(baseDir);
		}

		baseDir.mkdirs();
		prepareProject(baseDir.getAbsolutePath());

		// 2. install a mip-query plugin
		String pluginName = "mip-query";
		String pluginVersion = "1.0.0";
		String encoding = "UTF-8";
		boolean pomHandling = true;
		boolean excludeSrc = false;

		((DefaultPluginInstaller) pluginInstaller).setTest(true);
		pluginInstaller.install(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
				pluginName, pluginVersion, null, encoding, pomHandling,
				excludeSrc, null, null, false);

		// 3. assert
		assertTrue("fail to install 'core' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"core", "1.0.0"));
		assertTrue("fail to install 'query' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						"query", "1.0.0"));
		assertTrue("fail to install 'mip-query' plugin", pluginInfoManager
				.isInstalled(createRequest(baseDir.getAbsolutePath()), baseDir.getAbsolutePath(),
						pluginName, pluginVersion));
	}

	/**
	 * for test fixture : prepare test project
	 * 
	 * @throws Exception
	 */
	private void prepareProject(String baseDir) throws Exception {

		File userHome = new File(currentPath
				+ CommonConstants.SRC_TEST_RESOURCES, "/user.home/1.0.0");
		System.setProperty("user.home", userHome.getAbsolutePath());

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

		FileUtil.copyDir(new File(PATH_SAMPLE_PROJECT
				+ "/src/main/webapp/WEB-INF/", CommonConstants.WEB_XML_FILE),
				webDir);

		new File(baseDir, "src/main/java").mkdirs();
		new File(baseDir, "src/main/resources/spring").mkdirs();
		new File(baseDir, "src/test/java").mkdirs();
		new File(baseDir, "src/test/resources").mkdirs();
		new File(baseDir, "src/main/webapp/WEB-INF/jsp").mkdirs();
	}
}
