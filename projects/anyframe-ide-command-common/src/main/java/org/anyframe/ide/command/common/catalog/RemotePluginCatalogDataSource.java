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
import java.util.HashMap;
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
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.settings.Server;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;

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

		List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();
		String catalogFileName = CommonConstants.PLUGIN_CATALOG_ESSENTIAL_FILE;

		if (CommonConstants.PLUGIN_TYPE_ESSENTIAL == pluginType
				&& isTheFirstDownload()) {
			readPluginCatalog(request, CommonConstants.PLUGIN_TYPE_OPTIONAL);
		}

		// plugin catalog file name depend on plugin type
		switch (pluginType) {
		case CommonConstants.PLUGIN_TYPE_ESSENTIAL:
			remoteRepositories.add(new DefaultArtifactRepository(
					"anyframe-repository", CommonConstants.REMOTE_CATALOG_PATH,
					new DefaultRepositoryLayout()));
			catalogFileName = CommonConstants.PLUGIN_CATALOG_ESSENTIAL_FILE;
			break;

		case CommonConstants.PLUGIN_TYPE_OPTIONAL:
			remoteRepositories.add(new DefaultArtifactRepository(
					"anyframe-repository", CommonConstants.REMOTE_CATALOG_PATH,
					new DefaultRepositoryLayout()));
			catalogFileName = CommonConstants.PLUGIN_CATALOG_OPTIONAL_FILE;

			remoteRepositories.addAll(request.getRemoteArtifactRepositories());
			break;
		}

		try {
			List<Server> servers = request.getServers();
			Map<String, Server> serverMap = new HashMap();
			for (Server server : servers) {
				serverMap.put(server.getId(), server);
			}

			Map<String, PluginInfo> plugins = getPluginsFromRemotePluginCatalog(
					remoteRepositories, serverMap, catalogFileName.substring(0,
							catalogFileName.length() - 4));

			makePluginCatalogFile(catalogFileName, plugins);

			return plugins;

		} catch (Exception e) {
			getLogger().warn(
					"Reading a plugin catalog file from remote [location="
							+ remoteRepositories.toString()
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
	 * @param baseDir
	 *            the path of current project
	 * @param url
	 *            url for remote repository
	 * @param userName
	 *            user name to connect to remote repository
	 * @param password
	 *            password to connect to remote repository
	 * @param isEssential
	 *            whether the plugin is essential
	 * @param isLatest
	 *            whether version of target plugin is latest
	 * @param pluginInfo
	 *            information about plugin
	 */
	@SuppressWarnings("unchecked")
	public void updatePluginCatalog(ArchetypeGenerationRequest request,
			File baseDir, String url, String userName, String password,
			boolean isEssential, boolean isLatest, PluginInfo pluginInfo)
			throws Exception {
		String catalogFileName = (isEssential) ? CommonConstants.PLUGIN_CATALOG_ESSENTIAL_FILE
				: CommonConstants.PLUGIN_CATALOG_OPTIONAL_FILE;

		InputStream is = null;

		try {
			String remoteCatalogFileName = catalogFileName.substring(0,
					catalogFileName.length() - 4);

			// get plugin catalog from remote repository
			is = getResourceFromRemoteRepository(url, remoteCatalogFileName,
					userName, password);

			Map<String, PluginInfo> plugins = (Map<String, PluginInfo>) FileUtil
					.getObjectFromXML(is);

			String pluginName = pluginInfo.getName();
			String pluginVersion = pluginInfo.getVersion();
			String pluginDescription = pluginInfo.getDescription();

			boolean isUpdated = false;

			if (plugins.containsKey(pluginName)) {
				PluginInfo targetPluginInfo = plugins.get(pluginName);

				if (isLatest
						&& !targetPluginInfo.getLatestVersion().equals(
								pluginVersion)) {
					targetPluginInfo.setLatestVersion(pluginVersion);
					isUpdated = true;
				}

				if (!targetPluginInfo.getDescription()
						.equals(pluginDescription)) {
					targetPluginInfo.setDescription(pluginDescription);
					isUpdated = true;
				}

				List<String> versions = targetPluginInfo.getVersions();
				if (!versions.contains(pluginVersion)) {
					versions.add(pluginVersion);
					isUpdated = true;
				}
			} else {
				PluginInfo targetPluginInfo = makePluginInfo(pluginInfo);

				plugins.put(pluginName, targetPluginInfo);
				isUpdated = true;
			}

			if (isUpdated) {
				File temporaryFile = new File(baseDir
						+ CommonConstants.fileSeparator + "target"
						+ CommonConstants.fileSeparator + "temp",
						remoteCatalogFileName + "." + CommonConstants.EXT_XML);
				temporaryFile.getParentFile().mkdirs();
				temporaryFile.createNewFile();

				FileUtil.getObjectToXML(plugins, temporaryFile);

				// write plugin catalog to remote repository
				putResourceToRemoteRepository(url, remoteCatalogFileName,
						temporaryFile, userName, password);
			}
		} catch (Exception e) {
			getLogger().warn(
					"Updating " + catalogFileName + " in "
							+ CommonConstants.REMOTE_CATALOG_PATH
							+ " is skipped. The reason is " + e.getMessage());
		} finally {
			IOUtil.close(is);
		}
	}

	/**
	 * Get plugins from multiple remote repository
	 * 
	 * @param remoteRepositoryUrls
	 *            remote repository urls
	 * @param fileName
	 *            plugin catalog file name to read
	 * @return plugin objects map
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private Map<String, PluginInfo> getPluginsFromRemotePluginCatalog(
			List<ArtifactRepository> remoteRepositories,
			Map<String, Server> servers, String fileName) throws Exception {
		Map<String, PluginInfo> plugins = new ListOrderedMap();
		InputStream is = null;
		String username = null;
		String password = null;

		for (ArtifactRepository remoteRepository : remoteRepositories) {
			try {
				getLogger().debug(
						"Ready to connect to a remote repository. [location="
								+ remoteRepository + "]");

				Server server = servers.get(remoteRepository.getId());

				if (server != null) {
					username = server.getUsername();
					password = server.getPassword();
				}

				is = getResourceFromRemoteRepository(remoteRepository.getUrl(),
						fileName, username, password);

				if (is != null)
					plugins.putAll((Map<String, PluginInfo>) FileUtil
							.getObjectFromXML(is));
			} catch (Exception e) {
				getLogger().warn(
						"Downloading " + fileName + " from " + remoteRepository
								+ " is skipped. The reason is "
								+ e.getMessage());
			} finally {
				IOUtil.close(is);
			}
		}
		return plugins;
	}

	/**
	 * Read plugin-catalog-xxx.xml file from remote repository
	 * 
	 * @param url
	 *            url for remote repository
	 * @param remoteResource
	 *            resource (plugin catalog file) to read
	 * @param userName
	 *            user name to connect remote repository
	 * @param password
	 *            password to connect remote repository
	 * @return input stream of resource in remote repository
	 */
	private InputStream getResourceFromRemoteRepository(String url,
			String remoteResource, String userName, String password) {
		getLogger().debug(
				"Ready to get a resource from a remote repository. [location="
						+ url + "]");
		HttpClient client = new HttpClient();
		Credentials creds = null;
		if (userName != null && password != null) {
			creds = new UsernamePasswordCredentials(userName, password);
		}
		client.getState().setCredentials(AuthScope.ANY, creds);

		GetMethod getMethod = new GetMethod(url + "/" + remoteResource + "."
				+ CommonConstants.EXT_XML);
		InputStream is = null;
		try {
			int resultCode = client.executeMethod(getMethod);

			if (resultCode == HttpStatus.SC_OK) {
				is = getMethod.getResponseBodyAsStream();

				return is;
			} else if (resultCode == HttpStatus.SC_NOT_FOUND) {
				return null;
			} else {
				throw new Exception(
						"The result code from remote repository is '"
								+ resultCode + "' than 'OK'.");
			}
		} catch (Exception e) {
			IOUtil.close(is);
			getLogger().warn(
					"Reading " + remoteResource + ".xml from " + url
							+ " is skipped. The reason is " + e.getMessage());

		}

		return null;
	}

	/**
	 * Update plugin-catalog-xxx.xml file in remote repository
	 * 
	 * @param url
	 *            url for remote repository
	 * @param remoteResource
	 *            resource (plugin catalog file) to write
	 * @param srcFile
	 *            local temporary plugin catalog file
	 * @param userName
	 *            user name to connect remote repository
	 * @param password
	 *            password to connect remote repository
	 */
	private void putResourceToRemoteRepository(String url,
			String remoteResource, File srcFile, String userName,
			String password) {
		getLogger().debug(
				"Ready to put a resource to a remote repository. [location="
						+ url + "]");
		HttpClient client = new HttpClient();
		Credentials creds = null;
		if (userName != null && password != null) {
			creds = new UsernamePasswordCredentials(userName, password);
		}
		client.getState().setCredentials(AuthScope.ANY, creds);

		PutMethod method = new PutMethod(url + "/" + remoteResource + "."
				+ CommonConstants.EXT_XML);

		FileInputStream fis = null;

		try {
			fis = new FileInputStream(srcFile);
			RequestEntity requestEntity = new InputStreamRequestEntity(fis);

			method.setRequestEntity(requestEntity);
			int resultCode = client.executeMethod(method);

			if (resultCode == HttpStatus.SC_OK) {
				getLogger().debug(
						"Updated " + remoteResource + "."
								+ CommonConstants.EXT_XML
								+ " to a remote repository successfully.");
			}
		} catch (Exception e) {
			getLogger().warn(
					"Writing " + remoteResource + " to " + url
							+ " is skipped. The reason is " + e.getMessage());
		} finally {
			IOUtil.close(fis);
		}
	}

	/**
	 * Make plugin catalog file in local using plugin objects. Plugin catalog
	 * file path is
	 * {user.home}/.anyframe/plugin-catalog-{essential|optional|custom}.xml.
	 * 
	 * @param fileName
	 *            target file name
	 * @param plugins
	 *            plugin objects map
	 * @return local catalog file object
	 * @throws Exception
	 */
	private File makePluginCatalogFile(String fileName,
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

	/**
	 * Make PluginInfo object with full information
	 * 
	 * @param pluginInfo
	 *            PluginInfo object with basic information
	 * @return PluginInfo object with full information
	 */
	private PluginInfo makePluginInfo(PluginInfo pluginInfo) {
		PluginInfo targetPluginInfo = new PluginInfo();
		targetPluginInfo.setName(pluginInfo.getName());
		targetPluginInfo.setLatestVersion(pluginInfo.getVersion());
		// TODO : check!!
		// targetPluginInfo.setDescription(pluginInfo.getName() + " plugin");
		targetPluginInfo.setDescription(pluginInfo.getDescription());
		targetPluginInfo.setGroupId(pluginInfo.getGroupId());
		targetPluginInfo.setArtifactId(pluginInfo.getArtifactId());

		List<String> versions = new ArrayList<String>();
		versions.add(pluginInfo.getVersion());
		targetPluginInfo.setVersions(versions);

		return targetPluginInfo;
	}

	/**
	 * get plugin catalog file by plugin type
	 * 
	 * @param pluginType
	 *            type of plugin. (CommonConstants.PLUGIN_TYPE_ESSENTIAL,
	 *            CommonConstants.PLUGIN_TYPE_OPTIONAL,
	 *            CommonConstants.PLUGIN_TYPE_CUSTOM)
	 * @return plugin catalog file
	 */
	private File getPluginCatalogFile(int pluginType) {
		String catalogFileName = CommonConstants.PLUGIN_CATALOG_ESSENTIAL_FILE;

		switch (pluginType) {
		case CommonConstants.PLUGIN_TYPE_ESSENTIAL:
			catalogFileName = CommonConstants.PLUGIN_CATALOG_ESSENTIAL_FILE;
			break;

		case CommonConstants.PLUGIN_TYPE_OPTIONAL:
			catalogFileName = CommonConstants.PLUGIN_CATALOG_OPTIONAL_FILE;
			break;
		}

		return new File(System.getProperty("user.home")
				+ CommonConstants.USER_HOME_ANYFRAME, catalogFileName);
	}

	/**
	 * Checks whether the first catalog download
	 * 
	 * @return whether file download is the first
	 */
	private boolean isTheFirstDownload() {
		File essentialCatalogFile = getPluginCatalogFile(CommonConstants.PLUGIN_TYPE_ESSENTIAL);
		File optionalCatalogFile = getPluginCatalogFile(CommonConstants.PLUGIN_TYPE_OPTIONAL);

		if (!essentialCatalogFile.exists() && !optionalCatalogFile.exists()) {
			return true;
		} else {
			return false;
		}
	}
}
