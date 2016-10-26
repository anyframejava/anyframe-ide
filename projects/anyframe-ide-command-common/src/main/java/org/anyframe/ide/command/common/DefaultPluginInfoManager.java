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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.anyframe.ide.command.common.plugin.DependentPlugin;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

/**
 * This is a DefaultPluginInfoManager class. This implementation class is for
 * finding a plugin information
 * 
 * @plexus.component 
 *                   role="org.anyframe.ide.command.common.DefaultPluginInfoManager"
 * @author SoYon Lim
 */
public class DefaultPluginInfoManager implements PluginInfoManager {

	/** @plexus.requirement */
	ArchetypeRegistryManager archetypeRegistryManager;

	/** @plexus.requirement */
	ArchetypeArtifactManager archetypeArtifactManager;

	/**
	 * @plexus.requirement 
	 *                     role="org.anyframe.ide.command.common.DefaultPluginCatalogManager"
	 */
	PluginCatalogManager pluginCatalogManager;

	/**
	 * get a latest plugin information which includes name, groupId, artifactId,
	 * version, description, dependencies, etc.
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginName
	 *            plugin name to find
	 * @return plugin detail information without versions
	 */
	public PluginInfo getPluginInfo(ArchetypeGenerationRequest request,
			String pluginName) throws Exception {
		PluginInfo pluginSummary = pluginCatalogManager.getPlugin(request,
				pluginName);

		if (pluginSummary != null) {
			return getPluginInfo(request, pluginName, pluginSummary
					.getLatestVersion());
		}

		return null;
	}

	/**
	 * get a plugin information which includes name, groupId, artifactId,
	 * version, description, dependencies, etc.
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginName
	 *            plugin name to find
	 * @param pluginVersion
	 *            plugin version to find
	 * @return plugin detail information without versions
	 */
	public PluginInfo getPluginInfo(ArchetypeGenerationRequest request,
			String pluginName, String pluginVersion) throws Exception {
		PluginInfo pluginSummary = pluginCatalogManager.getPlugin(request,
				pluginName, pluginVersion);

		if (pluginSummary != null) {
			File pluginJar = getPluginFile(request, pluginSummary.getGroupId(),
					pluginSummary.getArtifactId(), pluginSummary.getVersion());

			PluginInfo pluginInfo = getPluginInfo(pluginJar);
			pluginInfo.setDescription(pluginSummary.getDescription());
			pluginInfo.setLatestVersion(pluginSummary.getLatestVersion());

			return pluginInfo;
		}
		return null;
	}

	/**
	 * get a plugin information which includes name, groupId, artifactId,
	 * version, etc.
	 * 
	 * @param pluginJar
	 *            plugin binary file
	 * @return plugin detail information without versions
	 */
	public PluginInfo getPluginInfo(File pluginJar) throws Exception {

		InputStream pluginInputStream = getPluginResource("META-INF/anyframe/"
				+ CommonConstants.PLUGIN_FILE, pluginJar);
		try {
			PluginInfo pluginInfo = (PluginInfo) FileUtil
					.getObjectFromXML(pluginInputStream);

			return pluginInfo;
		} finally {
			IOUtil.close(pluginInputStream);
		}
	}

	/**
	 * display a specific plugin information
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginName
	 *            plugin name to be printed
	 */
	public void showPluginInfo(ArchetypeGenerationRequest request,
			String pluginName) throws Exception {
		PluginInfo pluginSummary = pluginCatalogManager.getPlugin(request,
				pluginName);

		PluginInfo pluginInfo = getPluginInfo(request, pluginName);

		if (pluginInfo != null) {
			StringBuffer buffer = new StringBuffer();

			buffer.append(getFormattedString("Name", pluginSummary.getName(),
					false)
					+ "\n");
			buffer.append(getFormattedString("GroupId", pluginSummary
					.getGroupId(), false)
					+ "\n");
			buffer.append(getFormattedString("ArtifactId", pluginSummary
					.getArtifactId(), false)
					+ "\n");
			buffer.append(getFormattedString("Latest Version", pluginSummary
					.getLatestVersion(), false)
					+ "\n");
			if (pluginInfo.getInterceptor() != null) {
				buffer.append(getFormattedString("Interceptor", pluginInfo
						.getInterceptor().getClassName().trim(), false)
						+ "\n");
			}

			String sampleStr = "Included";

			if (!new Boolean(pluginInfo.hasSamples()).booleanValue()) {
				sampleStr = "Not Included";
			}

			buffer.append(getFormattedString("Samples", sampleStr, false)
					+ "\n");

			if (pluginSummary.getVersions().size() > 0) {
				String releases = "";
				for (String version : pluginSummary.getVersions()) {
					releases += version + ",";
				}

				buffer.append(getFormattedString("Releases", releases
						.substring(0, releases.length() - 1), false)
						+ "\n");
			}

			List<String> passPlugins = new ArrayList<String>();
			passPlugins.add(CommonConstants.DATASOURCE_PLUGIN);
			passPlugins.add(CommonConstants.LOGGING_PLUGIN);
			passPlugins.add(CommonConstants.SPRING_PLUGIN);

			if (pluginInfo.getDependentPlugins().size() > 0) {
				String dependencies = "";
				for (DependentPlugin dependentPlugin : pluginInfo
						.getDependentPlugins()) {
					if (pluginName.equals(CommonConstants.CORE_PLUGIN)
							|| !passPlugins.contains(dependentPlugin.getName())) {
						dependencies += dependentPlugin.getName() + "("
								+ dependentPlugin.getVersion() + "),";
					}
				}

				buffer.append(getFormattedString("Dependencies", dependencies
						.substring(0, dependencies.length() - 1), false)
						+ "\n");
			}

			String description = pluginSummary.getDescription().trim();
			int descriptionLength = description.length();

			for (int i = 0; i < descriptionLength; i = i + 58) {
				String remains = description.substring(i);

				int idx = i + 58;
				if (remains.length() < 58) {
					idx = i + remains.length();
				}

				if (i == 0) {
					buffer.append(getFormattedString("Description", description
							.substring(i, idx), false)
							+ "\n");
				} else {
					buffer.append(getFormattedString("", description.substring(
							i, idx), true)
							+ "\n");
				}
			}

			System.out.println(buffer.toString());
		} else {
			System.out.println("There is no information about '" + pluginName
					+ "'. Please check a plugin name.");
		}
	}

	/**
	 * convert value to formatted value
	 * 
	 * @param label
	 *            label to be printed
	 * @param value
	 *            value to be printed
	 * @return formatted value
	 */
	private String getFormattedString(String label, String value,
			boolean remains) {
		StringBuilder builder = new StringBuilder();
		Formatter pluginInfoFormatter = new Formatter(builder);
		if (!remains) {
			pluginInfoFormatter.format(CommonConstants.PLUGININFO_DETAIL,
					label, value);
		} else {
			pluginInfoFormatter.format(
					CommonConstants.PLUGININFO_DETAIL_REMAINS, label, value);
		}
		return pluginInfoFormatter.toString();
	}

	/**
	 * get latest plugins which a specific plugin depends on
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginInfo
	 *            plugin detail information without versions
	 * @return dependent plugins
	 */
	public Map<String, String> getDependentPlugins(
			ArchetypeGenerationRequest request, PluginInfo pluginInfo)
			throws Exception {
		Map<String, String> dependentPlugins = new HashMap<String, String>();

		List<DependentPlugin> dependencies = pluginInfo.getDependentPlugins();

		for (DependentPlugin dependency : dependencies) {
			String pluginName = dependency.getName();
			String versionRange = dependency.getVersion();

			dependentPlugins.put(pluginName, versionRange);
		}

		return dependentPlugins;
	}

	/**
	 * get plugins which depends on a specific plugin among installed plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @param pluginInfo
	 *            plugin detail information without versions
	 * @return depended plugins
	 */
	public Map<String, String> getDependedPlugins(
			ArchetypeGenerationRequest request, String baseDir,
			PluginInfo pluginInfo) throws Exception {
		Map<String, String> dependenedPlugins = new HashMap<String, String>();

		String pluginName = pluginInfo.getName();
		InputStream installedPluginInputStream = null;

		Map<String, PluginInfo> installedPlugins = getInstalledPlugins(baseDir);
		Iterator<String> keyItr = installedPlugins.keySet().iterator();

		try {
			while (keyItr.hasNext()) {
				String installedPluginName = keyItr.next();
				PluginInfo installedPluginInfo = installedPlugins
						.get(installedPluginName);

				File installedPluginJar = getPluginFile(request,
						installedPluginInfo.getGroupId(), installedPluginInfo
								.getArtifactId(), installedPluginInfo
								.getVersion());
				installedPluginInputStream = getPluginResource(
						"META-INF/anyframe/" + CommonConstants.PLUGIN_FILE,
						installedPluginJar);

				installedPluginInfo = (PluginInfo) FileUtil
						.getObjectFromXML(installedPluginInputStream);
				List<DependentPlugin> dependencies = installedPluginInfo
						.getDependentPlugins();

				if (dependencies != null) {
					for (DependentPlugin dependency : dependencies) {
						if (dependency.getName().equals(pluginName)) {
							dependenedPlugins.put(
									installedPluginInfo.getName(),
									installedPluginInfo.getVersion());
							break;
						}
					}
				}
			}
		} finally {
			IOUtil.close(installedPluginInputStream);
		}

		return dependenedPlugins;
	}

	/**
	 * get all dependent plugin jar files based on installed plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @param pluginInfo
	 *            plugin detail information without versions
	 * @return all dependent plugin binary files
	 */
	public Map<String, File> getAllDependentPluginJars(
			ArchetypeGenerationRequest request, String baseDir,
			PluginInfo pluginInfo) throws Exception {
		Map<String, File> installedPluginJars = getInstalledPluginJars(request,
				baseDir);

		List<DependentPlugin> dependentPlugins = pluginInfo
				.getDependentPlugins();

		Map<String, File> installedAllDependentPluginJars = new HashMap<String, File>();
		getAllDependentPluginJars(installedPluginJars, dependentPlugins,
				installedAllDependentPluginJars);

		return installedAllDependentPluginJars;
	}

	/**
	 * check whether dependent plugins finally depends on installed essential
	 * plugins
	 * 
	 * @param baseDir
	 *            which includes META-INF/plugin-installed.xml
	 * @param allDependentPluginJars
	 *            all dependent plugin binary files
	 * @return true if dependent plugins finally depends on installed essential,
	 *         else false
	 */
	public boolean hasEssentialPluginDependency(String baseDir,
			Map<String, File> allDependentPluginJars) throws Exception {

		if (allDependentPluginJars.size() == 0) {
			return false;
		}

		Map<String, PluginInfo> installedEssentialPlugins = getInstalledEssentialPlugins(baseDir);

		if (installedEssentialPlugins.size() == 0) {
			return false;
		}
		InputStream dependentPluginInputStream = null;

		Iterator<String> essentialPluginItr = installedEssentialPlugins
				.keySet().iterator();
		try {
			while (essentialPluginItr.hasNext()) {
				String essentialPluginName = essentialPluginItr.next();

				if (allDependentPluginJars.containsKey(essentialPluginName)) {
					String essentialPluginVersion = installedEssentialPlugins
							.get(essentialPluginName).getVersion();

					File dependentPluginJar = allDependentPluginJars
							.get(essentialPluginName);
					dependentPluginInputStream = getPluginResource(
							"META-INF/anyframe/" + CommonConstants.PLUGIN_FILE,
							dependentPluginJar);

					PluginInfo dependentPluginInfo = (PluginInfo) FileUtil
							.getObjectFromXML(dependentPluginInputStream);

					if (!dependentPluginInfo.getVersion().equals(
							essentialPluginVersion)) {
						return false;
					}
				}
			}
		} finally {
			IOUtil.close(dependentPluginInputStream);
		}

		return true;
	}

	/**
	 * get all plugin information which have 'installed' property value
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @return all plugins
	 */
	public Map<String, PluginInfo> getPluginsWithInstallInfo(
			ArchetypeGenerationRequest request, String baseDir)
			throws Exception {
		// 1. get information about installed plugin
		Map<String, PluginInfo> installedPlugins = getInstalledPlugins(baseDir);

		// 2. get all plugin list
		Map<String, PluginInfo> allPlugins = pluginCatalogManager
				.getPlugins(request);

		Iterator<String> keyItr = allPlugins.keySet().iterator();
		while (keyItr.hasNext()) {
			String pluginName = keyItr.next();
			PluginInfo pluginInfo = allPlugins.get(pluginName);

			if (installedPlugins.containsKey(pluginName)) {
				PluginInfo installedPluginInfo = installedPlugins
						.get(pluginName);
				pluginInfo.setInstalled("true");
				pluginInfo.setVersion(installedPluginInfo.getVersion());
			} else {
				pluginInfo.setVersion(pluginInfo.getLatestVersion());
			}
		}

		return allPlugins;
	}

	/**
	 * get a installed plugin information
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @param pluginName
	 *            plugin name to find
	 * @return plugin detail information includes groupId, artifactId, version,
	 *         dependencies, samples, interceptor, etc.
	 */
	public PluginInfo getInstalledPluginInfo(
			ArchetypeGenerationRequest request, String baseDir,
			String pluginName) throws Exception {
		Map<String, PluginInfo> installedPlugins = getInstalledPlugins(baseDir);

		if (installedPlugins.containsKey(pluginName)) {
			PluginInfo installedPluginInfo = installedPlugins.get(pluginName);
			installedPluginInfo = getPluginInfo(request, pluginName,
					installedPluginInfo.getVersion());
			return installedPluginInfo;
		} else {
			return null;
		}
	}

	/**
	 * get all installed plugin binary files from local/remote repository
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @return plugin binary files
	 */
	public Map<String, File> getInstalledPluginJars(
			ArchetypeGenerationRequest request, String baseDir)
			throws Exception {
		Map<String, PluginInfo> installedPlugins = getInstalledPlugins(baseDir);

		Map<String, File> installedPluginJars = new HashMap<String, File>();

		Iterator<String> keyItr = installedPlugins.keySet().iterator();
		while (keyItr.hasNext()) {
			PluginInfo installedPluginInfo = installedPlugins
					.get(keyItr.next());
			File installedPluginJar = getPluginFile(request,
					installedPluginInfo.getGroupId(), installedPluginInfo
							.getArtifactId(), installedPluginInfo.getVersion());
			installedPluginJars.put(installedPluginInfo.getName(),
					installedPluginJar);

		}
		return installedPluginJars;
	}

	/**
	 * check if a plugin with a specific version is already installed. (in case
	 * of essential plugin, check only plugin name)
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @param pluginName
	 *            plugin name to check
	 * @param pluginVersion
	 *            plugin version to check
	 * @return true if a plugin with a specific version is installed, else false
	 */
	public boolean isInstalled(ArchetypeGenerationRequest request,
			String baseDir, String pluginName, String pluginVersion)
			throws Exception {
		Map<String, PluginInfo> installedPlugins = getInstalledPlugins(baseDir);

		if (installedPlugins.containsKey(pluginName)) {
			PluginInfo installedPluginInfo = installedPlugins.get(pluginName);

			if (pluginCatalogManager.isEssential(request, pluginName,
					pluginVersion)) {
				return true;
			}

			if (installedPluginInfo.getVersion().equals(pluginVersion)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * get all installable plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder
	 * @return all installable plugins
	 */
	public Map<String, PluginInfo> getInstallablePlugins(
			ArchetypeGenerationRequest request, String baseDir)
			throws Exception {
		// 1. get information about installed plugin
		Map<String, PluginInfo> installedPlugins = getInstalledPlugins(baseDir);

		// 2. get all plugin list
		Map<String, PluginInfo> allPlugins = pluginCatalogManager
				.getPlugins(request);

		Map<String, PluginInfo> installablePlugins = new HashMap<String, PluginInfo>();

		Iterator<String> keyItr = allPlugins.keySet().iterator();
		while (keyItr.hasNext()) {
			String pluginName = keyItr.next();
			PluginInfo pluginInfo = allPlugins.get(pluginName);

			if (installedPlugins.containsKey(pluginName)) {
				if (installedPlugins.get(pluginName).getVersion().equals(
						pluginInfo.getLatestVersion())) {
					continue;
				}
			}
			installablePlugins.put(pluginName, pluginInfo);
		}

		return installablePlugins;
	}

	/**
	 * extract plugin resource like pom.xml, plugin.xml, etc. from plugin
	 * library
	 * 
	 * @param resourceName
	 *            file name of plugin resource
	 * @param pluginJar
	 *            plugin binary file
	 * @return inputstream of a specific plugin resource
	 */
	public InputStream getPluginResource(String resourceName, File pluginJar)
			throws Exception {

		resourceName = StringUtils.replace(resourceName,
				CommonConstants.fileSeparator, "/");

		// 1. read a plugin jar file
		ZipFile pluginZip = archetypeArtifactManager
				.getArchetypeZipFile(pluginJar);

		// 2. get a resource entry
		ZipEntry zipEntry = pluginZip.getEntry(resourceName);

		if (zipEntry == null) {
			return null;
		}

		return pluginZip.getInputStream(zipEntry);
	}

	/**
	 * read plugin resource like pom.xml, plugin.xml, etc. from plugin library
	 * 
	 * @param resourceName
	 *            file name of plugin resource
	 * @param pluginJar
	 *            plugin binary file
	 * @param encoding
	 *            file encoding style
	 * @return file contents
	 */
	public String readPluginResource(String resourceName, File pluginJar,
			String encoding) throws Exception {
		InputStream inputStream = getPluginResource(resourceName, pluginJar);

		if (inputStream != null) {
			String line = null;
			StringBuffer buffer = new StringBuffer();
			BufferedReader bufferedReader = null;

			try {
				bufferedReader = new BufferedReader(new InputStreamReader(
						inputStream, encoding));

				while ((line = bufferedReader.readLine()) != null)
					buffer.append(line).append("\n");

				return buffer.toString();

			} finally {
				IOUtil.close(inputStream);
				IOUtil.close(bufferedReader);
			}
		}
		return null;
	}

	/**
	 * get a plugin binary file
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param groupId
	 *            group id of plugin
	 * @param artifactId
	 *            artifact id of plugin
	 * @param version
	 *            version of plugin
	 * @return plugin binary file
	 */
	@SuppressWarnings("unchecked")
	public File getPluginFile(ArchetypeGenerationRequest request,
			String groupId, String artifactId, String version) throws Exception {
		ArtifactRepository archetypeRepository = null;
		List<ArtifactRepository> repositories = new ArrayList<ArtifactRepository>();

		// 1. set archetype repository
		if (request.getArchetypeRepository() != null) {
			archetypeRepository = archetypeRegistryManager.createRepository(
					request.getArchetypeRepository(), request
							.getArchetypeArtifactId()
							+ "-repo");
			repositories.add(archetypeRepository);
		}
		// 2. set remote artifact repository
		if (request.getRemoteArtifactRepositories() != null) {
			repositories.addAll(request.getRemoteArtifactRepositories());
		}

		// 3. get a archetype file
		File pluginJar = archetypeArtifactManager.getArchetypeFile(groupId,
				artifactId, version, archetypeRepository, request
						.getLocalRepository(), repositories);

		if (!pluginJar.exists()) {
			throw new UnknownArchetype(
					"The desired pluginName does not exist ("
							+ request.getArchetypeGroupId() + ":"
							+ request.getArchetypeArtifactId() + ":"
							+ request.getArchetypeVersion() + ")");
		}

		return pluginJar;
	}

	/**
	 * get all installed plugins
	 * 
	 * @param baseDir
	 *            folder which includes META-INF/plugin-installed.xml
	 * @return all installed plugins
	 */
	@SuppressWarnings("unchecked")
	public Map<String, PluginInfo> getInstalledPlugins(String baseDir)
			throws Exception {
		File pluginFile = new File(baseDir + CommonConstants.METAINF,
				CommonConstants.PLUGIN_INSTALLED_FILE);

		if (!pluginFile.exists())
			throw new CommandException("Can not find a '"
					+ pluginFile.getAbsolutePath()
					+ "' file. Please check a location of your project.");

		return (Map<String, PluginInfo>) FileUtil.getObjectFromXML(pluginFile);
	}

	/**
	 * display installed plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            folder which includes META-INF/plugin-installed.xml
	 */
	public void showInstalledPlugins(ArchetypeGenerationRequest request,
			String baseDir) throws Exception {
		Map<String, PluginInfo> installedPlugins = getInstalledPlugins(baseDir);
		Map<String, PluginInfo> allPlugins = pluginCatalogManager
				.getPlugins(request);

		if (installedPlugins.size() > 0) {
			StringBuffer buffer = new StringBuffer();

			Formatter formatter = new Formatter();

			formatter.format(
					CommonConstants.PLUGININFO_NAME_VERSION_LATEST_TITLE,
					"<name>", "<current>", "<latest>");
			buffer.append(formatter.toString() + "\n");

			Collection<PluginInfo> installedPluginValues = installedPlugins
					.values();

			int i = 0;
			for (PluginInfo installedPluginInfo : installedPluginValues) {
				StringBuilder builder = new StringBuilder();
				Formatter pluginInfoFormatter = new Formatter(builder);

				String installedPluginName = installedPluginInfo.getName();
				if (allPlugins.containsKey(installedPluginName)) {
					installedPluginInfo.setLatestVersion(allPlugins.get(
							installedPluginName).getLatestVersion());
				} else {
					installedPluginInfo.setLatestVersion(installedPluginInfo
							.getVersion());
				}

				String pluginName = installedPluginInfo.getName();

				pluginInfoFormatter.format(
						CommonConstants.PLUGININFO_NAME_VERSION_LATEST,
						pluginName, installedPluginInfo.getVersion(),
						installedPluginInfo.getLatestVersion());
				buffer.append(pluginInfoFormatter.toString());

				if (i++ != installedPluginValues.size() - 1) {
					buffer.append("\n");
				}
			}

			System.out.println(buffer.toString());
		} else {
			System.out
					.println("There is no installed plugins. Try check installed plugins.");

			return;
		}
	}

	/**
	 * display all updatable plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            folder which includes META-INF/plugin-installed.xml
	 */
	public void showUpdatablePlugins(ArchetypeGenerationRequest request,
			String baseDir) throws Exception {
		Map<String, PluginInfo> installedPlugins = getInstalledPlugins(baseDir);
		Map<String, PluginInfo> allPlugins = pluginCatalogManager
				.getPlugins(request);

		List<PluginInfo> updatablePlugins = new ArrayList<PluginInfo>();

		Collection<PluginInfo> installedPluginValues = installedPlugins
				.values();
		for (PluginInfo installedPluginInfo : installedPluginValues) {
			String installedPluginName = installedPluginInfo.getName();

			if (allPlugins.containsKey(installedPluginName)) {
				PluginInfo pluginInfo = allPlugins.get(installedPluginName);
				if (!pluginInfo.getLatestVersion().equals(
						installedPluginInfo.getVersion())) {
					installedPluginInfo.setLatestVersion(pluginInfo
							.getLatestVersion());
					updatablePlugins.add(installedPluginInfo);
				}
			}
		}

		if (updatablePlugins.size() > 0) {
			StringBuffer buffer = new StringBuffer();

			Formatter formatter = new Formatter();

			formatter.format(
					CommonConstants.PLUGININFO_NAME_VERSION_LATEST_TITLE,
					"<name>", "<current>", "<latest>");
			buffer.append(formatter.toString() + "\n");

			for (int i = 0; i < updatablePlugins.size(); i++) {
				PluginInfo updatablePluginInfo = updatablePlugins.get(i);
				StringBuilder builder = new StringBuilder();
				Formatter pluginInfoFormatter = new Formatter(builder);

				String installedPluginName = updatablePluginInfo.getName();
				if (allPlugins.containsKey(installedPluginName)) {
					updatablePluginInfo.setLatestVersion(allPlugins.get(
							installedPluginName).getLatestVersion());
				} else {
					updatablePluginInfo.setLatestVersion(updatablePluginInfo
							.getVersion());
				}

				String pluginName = updatablePluginInfo.getName();

				pluginInfoFormatter.format(
						CommonConstants.PLUGININFO_NAME_VERSION_LATEST,
						pluginName, updatablePluginInfo.getVersion(),
						updatablePluginInfo.getLatestVersion());
				buffer.append(pluginInfoFormatter.toString());

				if (i != updatablePlugins.size() - 1) {
					buffer.append("\n");
				}
			}

			System.out.println(buffer.toString());

		} else {
			System.out.println("There is no updatable plugins.");

			return;
		}
	}

	/**
	 * get all dependent plugin jar files based on installed plugins
	 * 
	 * @param installedPluginJars
	 *            plugin binary files
	 * @param dependentPlugins
	 *            dependent plugins which a specific plugin depends on
	 * @param installedAllDependentPluginJars
	 *            map of all dependent plugin binary files
	 */
	private void getAllDependentPluginJars(
			Map<String, File> installedPluginJars,
			List<DependentPlugin> dependentPlugins,
			Map<String, File> installedAllDependentPluginJars) throws Exception {
		InputStream inputStream = null;
		try {
			for (DependentPlugin dependentPlugin : dependentPlugins) {
				String dependentPluginName = dependentPlugin.getName();

				if (installedPluginJars.containsKey(dependentPluginName)) {
					File installedPluginJar = installedPluginJars
							.get(dependentPluginName);

					installedAllDependentPluginJars.put(dependentPluginName,
							installedPluginJar);

					inputStream = getPluginResource("META-INF/anyframe/"
							+ CommonConstants.PLUGIN_FILE, installedPluginJar);

					PluginInfo installedDependentPluginInfo = (PluginInfo) FileUtil
							.getObjectFromXML(inputStream);

					getAllDependentPluginJars(installedPluginJars,
							installedDependentPluginInfo.getDependentPlugins(),
							installedAllDependentPluginJars);
				}
			}
		} finally {
			IOUtil.close(inputStream);
		}
	}

	/**
	 * get all installed essential plugins
	 * 
	 * @param baseDir
	 *            folder which includes META-INF/plugin-installed.xml
	 * @return all installed essential plugins
	 */
	private Map<String, PluginInfo> getInstalledEssentialPlugins(String baseDir)
			throws Exception {
		Map<String, PluginInfo> installedPlugins = getInstalledPlugins(baseDir);

		Collection<PluginInfo> installedPluginValues = installedPlugins
				.values();

		Map<String, PluginInfo> installedEssentialPlugins = new HashMap<String, PluginInfo>();
		for (PluginInfo pluginInfo : installedPluginValues) {
			if (pluginInfo.isEssential()) {
				installedEssentialPlugins.put(pluginInfo.getName(), pluginInfo);
			}
		}
		return installedEssentialPlugins;
	}

}
