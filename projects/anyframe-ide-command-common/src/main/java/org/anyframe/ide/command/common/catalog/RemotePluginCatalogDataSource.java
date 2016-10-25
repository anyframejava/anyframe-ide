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
package org.anyframe.ide.command.common.catalog;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * This is an RemotePluginCatalogDataSource class. This class is for reading a
 * plugin catalog files from remote repository
 * 
 * @plexus.component 
 *                   role="org.anyframe.ide.command.common.catalog.RemotePluginCatalogDataSource"
 * @author Jeryeon Kim
 */
public class RemotePluginCatalogDataSource extends AbstractLogEnabled {

	/**
	 * read all plugin catalog files from remote repository
	 * 
	 * @param request
	 *            information includes maven repositories in settings.xml
	 * @return plugins defined plugin catalog files
	 */
	public Map<String, PluginInfo> readPluginCatalog(
			ArchetypeGenerationRequest request) {
		// Get essential plugins
		Map<String, PluginInfo> plugins = readPluginCatalog(request,
				CommonConstants.PLUGIN_TYPE_ESSENTIAL);
		// Get optional plugins
		plugins.putAll(readPluginCatalog(request,
				CommonConstants.PLUGIN_TYPE_OPTIONAL));
		// Get custom plugins
		plugins.putAll(readPluginCatalog(request,
				CommonConstants.PLUGIN_TYPE_CUSTOM));

		return plugins;
	}

	/**
	 * read a specified plugin catalog file from remote repository
	 * 
	 * @param request
	 *            information includes maven repositories in settings.xml
	 * @param pluginType
	 *            type of plugin (CommonConstants.PLUGIN_TYPE_ESSENTIAL,
	 *            CommonConstants.PLUGIN_TYPE_OPTIONAL,
	 *            CommonConstants.PLUGIN_TYPE_CUSTOM)
	 * @return plugins defined specified plugin catalog file
	 */
	@SuppressWarnings("unchecked")
	public Map<String, PluginInfo> readPluginCatalog(
			ArchetypeGenerationRequest request, int pluginType) {

		List<String> remoteRepositoryUrls = new ArrayList<String>();
		String catalogFileName = CommonConstants.PLUGIN_CATALOG_ESSENTIAL_FILE;

		// plugin catalog file name depend on plugin type
		switch (pluginType) {
		case CommonConstants.PLUGIN_TYPE_ESSENTIAL:
			remoteRepositoryUrls.add(CommonConstants.REMOTE_CATALOG_PATH);
			catalogFileName = CommonConstants.PLUGIN_CATALOG_ESSENTIAL_FILE;
			break;

		case CommonConstants.PLUGIN_TYPE_OPTIONAL:
			remoteRepositoryUrls.add(CommonConstants.REMOTE_CATALOG_PATH);
			catalogFileName = CommonConstants.PLUGIN_CATALOG_OPTIONAL_FILE;
			break;

		case CommonConstants.PLUGIN_TYPE_CUSTOM:
			List<ArtifactRepository> remoteRepositories = request
					.getRemoteArtifactRepositories();
			for (ArtifactRepository remoteRepository : remoteRepositories) {
				remoteRepositoryUrls.add(remoteRepository.getUrl());
			}
			catalogFileName = CommonConstants.PLUGIN_CATALOG_CUSTOM_FILE;
			break;
		}

		try {
			Map<String, PluginInfo> plugins = getPluginsFromRemoteRepositories(
					remoteRepositoryUrls, catalogFileName.substring(0,
							catalogFileName.length() - 4));

			downloadPluginCatalogFile(catalogFileName, plugins);

			return plugins;

		} catch (Exception e) {
			getLogger().warn(
					"Reading a plugin catalog file from remote [location="
							+ remoteRepositoryUrls.toString()
							+ "] is skipped. The reason is a '"
							+ e.getMessage() + "'.");

			return new ListOrderedMap();
		}
	}

	/**
	 * update plugin-catalog=xxx.xml file in remote repository
	 * 
	 * @param request
	 *            information includes maven repositories in settings.xml
	 * @param url
	 *            url for remote repository
	 * @param userName
	 *            user name to connect to remote repository
	 * @param password
	 *            password to connect to remote repository
	 * @param isEssential
	 *            whether the plugin is essential
	 * @param pluginInfo
	 *            information about plugin
	 */
	public void updatePluginCatalog(ArchetypeGenerationRequest request,
			File baseDir, String url, String userName, String password,
			boolean isEssential, PluginInfo pluginInfo) throws Exception {
		String catalogFileName = "";
		try {
			if (isEssential) {
				catalogFileName = CommonConstants.PLUGIN_CATALOG_ESSENTIAL_FILE;
			} else {
				catalogFileName = CommonConstants.PLUGIN_CATALOG_OPTIONAL_FILE;
				if (pluginInfo.isCustomed()) {
					catalogFileName = CommonConstants.PLUGIN_CATALOG_CUSTOM_FILE;
				}
			}

			getLogger().debug(
					"Ready to connect to a remote repository. [location=" + url
							+ "]");

			HttpClient client = new HttpClient();
			Credentials creds = new UsernamePasswordCredentials(userName,
					password);
			client.getState().setCredentials(AuthScope.ANY, creds);

			String remoteCatalogFileName = catalogFileName.substring(0,
					catalogFileName.length() - 4);
			GetMethod getMethod = new GetMethod(url + "/"
					+ remoteCatalogFileName + "." + CommonConstants.EXT_XML);
			int resultCode = client.executeMethod(getMethod);

			if (resultCode == HttpStatus.SC_OK) {
				InputStream is = getMethod.getResponseBodyAsStream();

				Map<String, PluginInfo> plugins = (Map<String, PluginInfo>) FileUtil
						.getObjectFromXML(is);

				String pluginName = pluginInfo.getName();
				String pluginVersion = pluginInfo.getVersion();

				boolean isUpdated = false;

				if (plugins.containsKey(pluginName)) {
					PluginInfo targetPluginInfo = plugins.get(pluginName);

					if (!targetPluginInfo.getLatestVersion().equals(
							pluginVersion)) {
						targetPluginInfo.setLatestVersion(pluginVersion);
						isUpdated = true;
					}

					List<String> versions = targetPluginInfo.getVersions();
					if (!versions.contains(pluginVersion)) {
						versions.add(pluginVersion);
						isUpdated = true;
					}
				} else {
					PluginInfo targetPluginInfo = new PluginInfo();
					targetPluginInfo.setName(pluginName);
					targetPluginInfo.setLatestVersion(pluginVersion);
					targetPluginInfo.setDescription(pluginName + " plugin");
					targetPluginInfo.setGroupId(pluginInfo.getGroupId());
					targetPluginInfo.setArtifactId(pluginInfo.getArtifactId());

					List<String> versions = new ArrayList<String>();
					versions.add(pluginVersion);
					targetPluginInfo.setVersions(versions);
					plugins.put(pluginName, targetPluginInfo);
					isUpdated = true;
				}

				if (isUpdated) {
					File temporaryFile = new File(baseDir
							+ CommonConstants.fileSeparator + "target"
							+ CommonConstants.fileSeparator + "temp",
							remoteCatalogFileName + "."
									+ CommonConstants.EXT_XML);
					temporaryFile.getParentFile().mkdirs();
					temporaryFile.createNewFile();

					FileUtil.getObjectToXML(plugins, temporaryFile);

					PutMethod method = new PutMethod(url + "/"
							+ remoteCatalogFileName + "."
							+ CommonConstants.EXT_XML);
					RequestEntity requestEntity = new InputStreamRequestEntity(
							new FileInputStream(temporaryFile));
					method.setRequestEntity(requestEntity);
					resultCode = client.executeMethod(method);

					if (resultCode == HttpStatus.SC_OK) {
						getLogger()
								.debug(
										"Updated "
												+ remoteCatalogFileName
												+ "."
												+ CommonConstants.EXT_XML
												+ " to a remote repository successfully.");
					}
				}
			}
		} catch (Exception e) {
			getLogger().warn(
					"Updating " + catalogFileName + " in "
							+ CommonConstants.REMOTE_CATALOG_PATH
							+ " is skipped. The reason is " + e.getMessage());
		}
	}

	private Map<String, PluginInfo> getPluginsFromRemoteRepositories(
			List<String> remoteRepositoryUrls, String fileName)
			throws Exception {
		Map<String, PluginInfo> plugins = new ListOrderedMap();
		for (String remoteRepository : remoteRepositoryUrls) {
			try {
				getLogger().debug(
						"Ready to connect to a remote repository. [location="
								+ remoteRepository + "]");

				HttpClient client = new HttpClient();
				client.getState().setCredentials(AuthScope.ANY, null);

				GetMethod getMethod = new GetMethod(remoteRepository + "/"
						+ fileName + "." + CommonConstants.EXT_XML);
				int resultCode = client.executeMethod(getMethod);

				if (resultCode == HttpStatus.SC_OK) {
					InputStream is = getMethod.getResponseBodyAsStream();

					plugins.putAll((Map<String, PluginInfo>) FileUtil
							.getObjectFromXML(is));
				}
			} catch (Exception e) {
				if (!fileName.equals("plugin-catalog-custom")) {
					getLogger().warn(
							"Downloading " + fileName + " from "
									+ remoteRepository
									+ " is skipped. The reason is "
									+ e.getMessage());
				}
			}
		}

		return plugins;
	}

	/**
	 * copy temporary file to
	 * {user.home}/.anyframe/plugin-catalog-{essential|optional|custom}.xml
	 * 
	 * @param fileName
	 *            target file name
	 * @param file
	 *            temporary catalog file object
	 * @return local catalog file object
	 * @throws Exception
	 */
	private File downloadPluginCatalogFile(String fileName,
			Map<String, PluginInfo> plugins) throws Exception {

		File userHomeAnyframeDir = new File(System.getProperty("user.home")
				+ CommonConstants.USER_HOME_ANYFRAME);
		if (!userHomeAnyframeDir.exists())
			userHomeAnyframeDir.mkdir();

		File pluginCatalogFile = new File(
				userHomeAnyframeDir.getAbsoluteFile(), fileName);

		if (pluginCatalogFile.exists())
			FileUtil.deleteFile(pluginCatalogFile);

		if (!plugins.isEmpty()) {

			FileUtil.getObjectToXML(plugins, pluginCatalogFile);

			getLogger().info(
					"Download " + pluginCatalogFile.getAbsolutePath()
							+ " from remote repository successfully.");
		}

		return pluginCatalogFile;
	}
}
