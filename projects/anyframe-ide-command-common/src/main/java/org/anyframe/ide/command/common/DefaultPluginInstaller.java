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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.anyframe.ide.command.common.plugin.DependentPlugin;
import org.anyframe.ide.command.common.plugin.Exclude;
import org.anyframe.ide.command.common.plugin.Include;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.plugin.PluginResource;
import org.anyframe.ide.command.common.plugin.TargetPluginInfo;
import org.anyframe.ide.command.common.plugin.versioning.VersionComparator;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.DBUtil;
import org.anyframe.ide.command.common.util.FileUtil;
import org.anyframe.ide.command.common.util.ObjectUtil;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.generic.EscapeTool;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

/**
 * This is an DefaultPluginInstaller class. This class is for installing a new
 * plugin based on selected plugin name
 * 
 * @plexus.component 
 *                   role="org.anyframe.ide.command.common.DefaultPluginInstaller"
 * @author SoYon Lim
 */
public class DefaultPluginInstaller extends AbstractLogEnabled implements
		PluginInstaller {
	/**
	 * Token delimiter.
	 */
	private static final String DELIMITER = "__";

	/**
	 * @plexus.requirement 
	 *                     role="org.anyframe.ide.command.common.DefaultPluginUninstaller"
	 */
	PluginUninstaller pluginUninstaller;

	/**
	 * @plexus.requirement 
	 *                     role="org.anyframe.ide.command.common.DefaultPluginInfoManager"
	 */
	PluginInfoManager pluginInfoManager;

	/**
	 * @plexus.requirement 
	 *                     role="org.anyframe.ide.command.common.DefaultPluginCatalogManager"
	 */
	PluginCatalogManager pluginCatalogManager;

	/** @plexus.requirement */
	ArchetypeArtifactManager archetypeArtifactManager;

	/**
	 * @plexus.requirement 
	 *                     role="org.anyframe.ide.command.common.DefaultPluginArtifactManager"
	 */
	DefaultPluginArtifactManager pluginArtifactManager;
	/**
	 * @plexus.requirement 
	 *                     role="org.anyframe.ide.command.common.DefaultPluginPomManager"
	 */
	DefaultPluginPomManager pluginPomManager;

	/** @plexus.requirement */
	private Prompter prompter;

	VelocityEngine velocity;

	boolean test = false;

	/**
	 * install a new plugin includes dependent plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder path to install a plugin
	 * @param pluginName
	 *            a target plugin name to install
	 * @param pluginVersion
	 *            a target plugin version to install
	 * @param pluginJar
	 *            a target plugin binary file to install
	 * @param encoding
	 *            file encoding style
	 * @param pomHandling
	 *            whether current project is based on maven or ant
	 * @param excludeSrc
	 *            whether plugin resources exclude source codes will be
	 *            installed
	 * @param templateHome
	 *            folder which have freemarker templates
	 * @param inspectionHome
	 *            folder which have inspection resources
	 * @param isCLIMode
	 *            execution mode
	 */
	public void install(ArchetypeGenerationRequest request, String baseDir,
			String pluginName, String pluginVersion, File pluginJar,
			String encoding, boolean pomHandling, boolean excludeSrc,
			String templateHome, String inspectionHome, boolean isCLIMode)
			throws Exception {
		getLogger().debug(
				DefaultPluginInstaller.class.getName() + " execution start.");

		ClassLoader old = Thread.currentThread().getContextClassLoader();

		try {

			// 1. check project metadata files
			PropertiesIO pio = checkProject(baseDir);

			checkTargetPlugin(request, pluginName);

			// 2. set properties for installing plugins
			Context context = prepareVelocityContext(pio, pomHandling,
					excludeSrc);

			// 3. install templates, inspection resources
			Map<String, PluginInfo> installedPlugins = pluginInfoManager
					.getInstalledPlugins(baseDir);
			if (installedPlugins.size() == 0) {
				pio.setProperty(CommonConstants.PROJECT_HOME, baseDir);
				pio.write();

				installFreemarkerTemplates(pio, templateHome);
				installInspectionResources(baseDir, pomHandling, inspectionHome);
			}

			// 4. get file encoding
			encoding = getEncoding(encoding);

			// 5. process to install (based on file or plugin name)
			PluginInfo pluginInfoFromJar = null;

			if (pluginJar != null) {
				// 5.1 process to install using plugin binary file
				if (!pluginJar.exists()) {
					throw new CommandException("Can not find a '"
							+ pluginJar.getAbsolutePath()
							+ "' file. Please check file location.");
				}

				pluginInfoFromJar = pluginInfoManager.getPluginInfo(pluginJar);
				pluginName = pluginInfoFromJar.getName();
				pluginVersion = pluginInfoFromJar.getVersion();
			}
			// 5.2 process to install using plugin name or version
			// 5.2.1 process comma-separator.
			String[] pluginNames = new String[] { pluginName };
			if (pluginName.indexOf(",") != -1) {
				pluginNames = pluginName.split(",");
			}

			Map<String, TargetPluginInfo> visitedPlugins = new ListOrderedMap();

			analyzePluginDependencies(request, baseDir, pluginNames,
					pluginVersion, pluginJar, pluginInfoFromJar, visitedPlugins);

			if (visitedPlugins.size() > 0) {
				System.out.println("Dependencies Resolved.");

				installPlugin(request, context, pio, baseDir, visitedPlugins,
						pluginJar, pluginInfoFromJar, encoding, pomHandling,
						isCLIMode);
			}
		} catch (Exception e) {
			if (e instanceof CommandException) {
				throw e;
			}
			throw new CommandException("Error occurred in installing a '"
					+ pluginName + "' plugin. The reason is a '"
					+ e.getMessage() + "'.");
		} finally {
			Thread.currentThread().setContextClassLoader(old);
		}
	}

	/**
	 * analyze plugin's dependencies to install plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder path to install a plugin
	 * @param pluginNames
	 *            target plugin names to install
	 * @param pluginVersion
	 *            a target plugin version to install
	 * @param pluginJar
	 *            a target plugin binary file to install
	 * @param pluginInfoFromJar
	 *            plugin information reading from plugin jar
	 * @param visitedPlugins
	 *            plugins to be installed
	 */
	public void analyzePluginDependencies(ArchetypeGenerationRequest request,
			String baseDir, String[] pluginNames, String pluginVersion,
			File pluginJar, PluginInfo pluginInfoFromJar,
			Map<String, TargetPluginInfo> visitedPlugins) throws Exception {
		getLogger().debug(
				"Call analyzePluginDependencies() of DefaultPluginInstaller");

		System.out.println("Resolving plugin dependencies ...");
		for (String installPluginName : pluginNames) {
			installPluginName = installPluginName.trim();
			// 5.2.2 get a specific plugin information
			PluginInfo pluginInfo;

			if (pluginJar == null) {
				if (pluginVersion == null) {
					pluginInfo = pluginInfoManager.getPluginInfo(request,
							installPluginName);
				} else {
					pluginInfo = pluginInfoManager.getPluginInfo(request,
							installPluginName, pluginVersion);
				}
			} else {
				pluginInfo = pluginInfoFromJar;
			}

			if (pluginInfo == null) {
				throw new CommandException("Can not find a '"
						+ installPluginName
						+ ((pluginVersion != null) ? " " + pluginVersion : "")
						+ "' plugin. Please check your repository.");
			}

			PluginInfo installedPluginInfo = pluginInfoManager
					.getInstalledPluginInfo(request, baseDir, installPluginName);

			TargetPluginInfo installPluginInfo = new TargetPluginInfo(
					pluginInfo);
			if (installedPluginInfo == null) {
				installPluginInfo.setInstalledVersion(pluginInfo.getVersion());
				installPluginInfo.setUpdate(false);
			} else {
				if (installedPluginInfo.getVersion().equals(
						pluginInfo.getVersion())) {
					System.out.println("'" + pluginInfo.getName()
							+ "' plugin can't be installed. The reason is a '"
							+ pluginInfo.getName() + " "
							+ pluginInfo.getVersion()
							+ "' plugin is already installed.");
					continue;
				}
				installPluginInfo.setInstalledVersion(installedPluginInfo
						.getVersion());
				installPluginInfo.setUpdate(true);
			}

			visitedPlugins.put(installPluginName, installPluginInfo);

			findPlugins(request, baseDir, pluginInfo, visitedPlugins, false);
			findPlugins(request, baseDir, pluginInfo, visitedPlugins, true);
		}
	}

	/**
	 * process to install all plugins in order as (core -> tiles -> etc.) which
	 * visitedPlugins has
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param context
	 *            velocity context
	 * @param pio
	 *            properties in project.mf
	 * @param baseDir
	 *            target project folder path to install a plugin
	 * @param visitedPlugins
	 *            plugins to be installed
	 * @param pluginJar
	 *            plugin binary file
	 * @param pluginInfoFromJar
	 *            plugin information reading from plugin jar
	 * @param encoding
	 *            file encoding style
	 * @param pomHandling
	 *            whether current project is based on maven or ant
	 * @param isCLIMode
	 *            execution mode
	 */
	private void installPlugin(ArchetypeGenerationRequest request,
			Context context, PropertiesIO pio, String baseDir,
			Map<String, TargetPluginInfo> visitedPlugins, File pluginJar,
			PluginInfo pluginInfoFromJar, String encoding, boolean pomHandling,
			boolean isCLIMode) throws Exception {
		getLogger().debug("Call installPlugin() of DefaultPluginInstaller");

		if (isCLIMode) {
			checkInstall(visitedPlugins, isCLIMode);
		}

		File springPluginJar = null;
		PluginInfo springPlugin = null;
		// 1. already install
		Map<String, PluginInfo> installedPlugins = pluginInfoManager
				.getInstalledPlugins(baseDir);

		if (installedPlugins != null
				&& installedPlugins.containsKey(CommonConstants.SPRING_PLUGIN)) {
			springPlugin = installedPlugins.get(CommonConstants.SPRING_PLUGIN);
			if (springPlugin != null)
				springPluginJar = pluginInfoManager
						.getPluginFile(request, springPlugin.getGroupId(),
								springPlugin.getArtifactId(),
								springPlugin.getVersion());
		} else {
			// 2. uninstall
			if (visitedPlugins.get(CommonConstants.SPRING_PLUGIN) != null) {
				springPlugin = visitedPlugins
						.get(CommonConstants.SPRING_PLUGIN).getPluginInfo();
				springPluginJar = pluginInfoManager
						.getPluginFile(request, springPlugin.getGroupId(),
								springPlugin.getArtifactId(),
								springPlugin.getVersion());
			}
		}

		Properties springProperties = new Properties();
		if (springPluginJar != null) {
			Model model = pluginPomManager.getPluginPom(springPluginJar);
			springProperties = model.getProperties();
		}

		installPlugin(request, context, pio, baseDir,
				CommonConstants.CORE_PLUGIN, visitedPlugins, pluginJar,
				pluginInfoFromJar, encoding, pomHandling, springProperties);
		visitedPlugins.remove(CommonConstants.CORE_PLUGIN);

		installPlugin(request, context, pio, baseDir,
				CommonConstants.TILES_PLUGIN, visitedPlugins, pluginJar,
				pluginInfoFromJar, encoding, pomHandling, springProperties);
		visitedPlugins.remove(CommonConstants.TILES_PLUGIN);

		List<TargetPluginInfo> targetPlugins = (List<TargetPluginInfo>) visitedPlugins
				.values();

		for (int i = targetPlugins.size() - 1; i >= 0; i--) {
			TargetPluginInfo targetPluginInfo = targetPlugins.get(i);
			installPlugin(request, context, pio, baseDir, targetPluginInfo
					.getPluginInfo().getName(), visitedPlugins, pluginJar,
					pluginInfoFromJar, encoding, pomHandling, springProperties);
		}

	}

	/**
	 * process to install all plugins which visitedPlugins has (except core,
	 * tiles)
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param context
	 *            velocity context
	 * @param pio
	 *            properties in project.mf
	 * @param baseDir
	 *            target project folder path to install a plugin
	 * @param pluginName
	 *            a target plugin name to install
	 * @param visitedPlugins
	 *            plugins to be installed
	 * @param pluginJar
	 *            plugin binary file
	 * @param pluginInfoFromJar
	 *            plugin information reading from plugin jar
	 * @param encoding
	 *            file encoding style
	 * @param pomHandling
	 *            whether current project is based on maven or ant
	 * @param pomProperties
	 *            properties of pom in current project
	 */
	private void installPlugin(ArchetypeGenerationRequest request,
			Context context, PropertiesIO pio, String baseDir,
			String pluginName, Map<String, TargetPluginInfo> visitedPlugins,
			File pluginJar, PluginInfo pluginInfoFromJar, String encoding,
			boolean pomHandling, Properties pomProperties) throws Exception {
		getLogger().debug("Call installPlugin() of DefaultPluginInstaller");

		File targetPluginJar = null;
		if (visitedPlugins.containsKey(pluginName)) {
			TargetPluginInfo targetPluginInfo = visitedPlugins.get(pluginName);
			if (targetPluginInfo.isUpdate()) {
				pluginUninstaller.uninstall(request, baseDir, pluginName, "",
						encoding, pomHandling, true);
			}

			if (pluginInfoFromJar != null) {
				if (targetPluginInfo.getPluginInfo().getName()
						.equals(pluginInfoFromJar.getName())) {
					targetPluginJar = pluginJar;
				}
			}
			installPlugin(request, context, pio, baseDir,
					targetPluginInfo.getPluginInfo(), targetPluginJar,
					encoding, pomHandling, pomProperties);
		}
	}

	/**
	 * find dependent(child) or depended(parent) plugins
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder path to install a plugin
	 * @param currentPluginInfo
	 *            target plugin detail information
	 * @param visitedPlugins
	 *            plugins to be installed
	 * @param isChild
	 *            whether to find dependent plugins or not
	 */
	public void findPlugins(ArchetypeGenerationRequest request, String baseDir,
			PluginInfo currentPluginInfo,
			Map<String, TargetPluginInfo> visitedPlugins, boolean isChild)
			throws Exception {
		// 1. get dependent/depended plugins based on current plugin
		Map<String, String> plugins;
		if (!isChild) {
			plugins = pluginInfoManager.getDependentPlugins(request,
					currentPluginInfo);
		} else {
			plugins = pluginInfoManager.getDependedPlugins(request, baseDir,
					currentPluginInfo);
		}

		// 2. collect plugins to install/update
		Set<String> pluginNames = plugins.keySet();
		Iterator<String> pluginNameItr = pluginNames.iterator();

		while (pluginNameItr.hasNext()) {
			String pluginName = pluginNameItr.next();
			String version = plugins.get(pluginName);

			PluginInfo pluginSummary = pluginCatalogManager.getPlugin(request,
					pluginName);

			boolean isInstalled = false;
			TargetPluginInfo targetPluginInfo = null;

			if (isChild) {
				// in case of depended plugin, is already installed

				if (visitedPlugins.containsKey(pluginName)) {
					// check using target version in visitedPlugins
					TargetPluginInfo visitedPluginInfo = visitedPlugins
							.get(pluginName);

					if (isMatchedToDependentPlugin(request, pluginName,
							visitedPluginInfo.getPluginInfo().getVersion(),
							currentPluginInfo)) {
						continue;
					}

					throw new CommandException(
							"Can't resolve plugin dependencies. The reason is '"
									+ pluginName
									+ " "
									+ visitedPluginInfo.getPluginInfo()
											.getVersion()
									+ "' plugin isn't matched to a '"
									+ currentPluginInfo.getName() + " "
									+ currentPluginInfo.getVersion()
									+ "' plugin.");
				}

				// check using installed version
				if (isMatchedToDependentPlugin(request, pluginName, version,
						currentPluginInfo)) {
					continue;
				}

				// check using latest version
				String latestVersion = pluginSummary.getLatestVersion();

				if (latestVersion == null
						|| !isMatchedToDependentPlugin(request, pluginName,
								latestVersion, currentPluginInfo)) {
					latestVersion = VersionComparator.getLatest(pluginSummary
							.getVersions());

					if (!isMatchedToDependentPlugin(request, pluginName,
							latestVersion, currentPluginInfo)) {
						throw new CommandException(
								"Can't resolve plugin dependencies. The reason is latest version ('latestVersion' value or latest version among <versions> in plugin-catalog-xxx.xml) of '"
										+ pluginName
										+ "' plugin isn't matched to a '"
										+ currentPluginInfo.getName()
										+ " "
										+ currentPluginInfo.getVersion()
										+ "' plugin.");
					}
				}

				isInstalled = true;
				currentPluginInfo = pluginInfoManager.getPluginInfo(request,
						pluginName, latestVersion);
				targetPluginInfo = new TargetPluginInfo(currentPluginInfo,
						version, true);
			} else {
				// in case of dependent plugin

				// check whether a plugin is already put to visitedPlugins
				if (visitedPlugins.containsKey(pluginName)) {
					// if target version in visitedPlugins isn't matched
					// to
					// current version range, stop installing, else move next
					TargetPluginInfo visitedPluginInfo = visitedPlugins
							.get(pluginName);

					if (VersionComparator.isMatched(version, visitedPluginInfo
							.getPluginInfo().getVersion())) {
						continue;
					}

					throw new CommandException(
							"Can't resolve plugin dependencies. The reason is '"
									+ currentPluginInfo.getName()
									+ " "
									+ currentPluginInfo.getVersion()
									+ "' plugin isn't matched to a '"
									+ pluginName
									+ " "
									+ visitedPluginInfo.getPluginInfo()
											.getVersion() + "' plugin.");
				}

				// get a latest version based on current version range and
				// plugin summary
				String latestVersion = pluginSummary.getLatestVersion();

				if (latestVersion == null
						|| !VersionComparator.isMatched(version, latestVersion)) {
					latestVersion = VersionComparator.getLatest(version,
							pluginSummary.getVersions());
				}

				if (latestVersion == null) {
					throw new CommandException(
							"Can't resolve plugin dependencies. The reason is '"
									+ pluginName
									+ " doesn't have release version in defined version range '"
									+ version + "'.");
				}

				PluginInfo pluginInfo = pluginInfoManager.getPluginInfo(
						request, pluginName, latestVersion);

				PluginInfo installedPluginInfo = pluginInfoManager
						.getInstalledPluginInfo(request, baseDir, pluginName);
				if (installedPluginInfo == null) {
					// if a plugin isn't installed yet
					currentPluginInfo = pluginInfo;
					targetPluginInfo = new TargetPluginInfo(currentPluginInfo,
							latestVersion, false);
				} else {
					// a plugin is already installed
					if (VersionComparator.isMatched(version,
							installedPluginInfo.getVersion())) {
						continue;
					}

					isInstalled = true;
					currentPluginInfo = pluginInfo;
					targetPluginInfo = new TargetPluginInfo(currentPluginInfo,
							installedPluginInfo.getVersion(), true);
				}
			}

			visitedPlugins.put(pluginName, targetPluginInfo);
			findPlugins(request, baseDir, currentPluginInfo, visitedPlugins,
					false);

			if (isInstalled) {
				findPlugins(request, baseDir, currentPluginInfo,
						visitedPlugins, true);
			}
		}
	}

	/**
	 * get version range of plugin which a target plugin depends on
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param targetPluginName
	 *            a target plugin name to install
	 * @param targetPluginVersion
	 *            a target plugin version to install
	 * @param dependentPluginName
	 *            a dependent plugin name
	 * @return version range of a dependent plugin
	 */
	private String getDependentPluginVersionRange(
			ArchetypeGenerationRequest request, String targetPluginName,
			String targetPluginVersion, String dependentPluginName)
			throws Exception {
		PluginInfo pluginInfo = pluginInfoManager.getPluginInfo(request,
				targetPluginName, targetPluginVersion);
		Map<String, String> dependentPlugins = pluginInfoManager
				.getDependentPlugins(request, pluginInfo);
		return dependentPlugins.get(dependentPluginName);
	}

	/**
	 * check version of dependent plugin is belong to defined version range in
	 * plugin.xml
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param targetPluginName
	 *            a target plugin name to install
	 * @param targetPluginVersion
	 *            a target plugin version to install
	 * @param dependentPluginInfo
	 *            detail information of a dependent plugin
	 * @return whether to match version range to version of dependent plugin
	 */
	private boolean isMatchedToDependentPlugin(
			ArchetypeGenerationRequest request, String targetPluginName,
			String targetPluginVersion, PluginInfo dependentPluginInfo)
			throws Exception {
		String versionRange = getDependentPluginVersionRange(request,
				targetPluginName, targetPluginVersion,
				dependentPluginInfo.getName());

		if (VersionComparator.isMatched(versionRange,
				dependentPluginInfo.getVersion())) {
			return true;
		}

		return false;
	}

	/**
	 * install a plugin resources into current project
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param context
	 *            velocity context
	 * @param pio
	 *            properties in project.mf
	 * @param baseDir
	 *            target project folder path to install a plugin
	 * @param pluginInfo
	 *            plugin detail information
	 * @param pluginJar
	 *            plugin binary file
	 * @param encoding
	 *            file encoding style
	 * @param encoding
	 *            file encoding style
	 * @param pomHandling
	 *            whether current project is based on maven or ant
	 * @param pomProperties
	 *            properties of pom in current project
	 */
	@SuppressWarnings("unchecked")
	private void installPlugin(ArchetypeGenerationRequest request,
			Context context, PropertiesIO pio, String baseDir,
			PluginInfo pluginInfo, File pluginJar, String encoding,
			boolean pomHandling, Properties pomProperties) throws Exception {
		getLogger().debug("Call installPlugin() of DefaultPluginInstaller");

		// 1. download archetype jar from maven repository
		if (pluginJar == null) {
			pluginJar = pluginInfoManager.getPluginFile(request,
					pluginInfo.getGroupId(), pluginInfo.getArtifactId(),
					pluginInfo.getVersion());
		}

		// 2. get zipfile from plugin jar file
		ZipFile pluginZip = archetypeArtifactManager
				.getArchetypeZipFile(pluginJar);

		// 3. set classloader for VelocityComponent can load templates
		// inside plugin library
		ClassLoader pluginJarLoader = archetypeArtifactManager
				.getArchetypeJarLoader(pluginJar);
		Thread.currentThread().setContextClassLoader(pluginJarLoader);

		// 4. get interceptor class of a target plugin
		Class interceptor = getInterceptor(request, pluginJarLoader,
				pluginInfo, pluginJar, pluginZip);

		// 5. process to install
		invokeInterceptor(baseDir, pluginInfo.getName(), pluginJar,
				interceptor, "preInstall");
		process(request, context, pio, baseDir, pluginInfo, pluginJar,
				pluginZip, encoding, pomProperties);
		invokeInterceptor(baseDir, pluginInfo.getName(), pluginJar,
				interceptor, "postInstall");

		System.out.println("'" + pluginInfo.getName() + " "
				+ pluginInfo.getVersion()
				+ "' plugin is installed successfully.");
	}

	/**
	 * copy all freemarker template files (*.ftl)
	 * 
	 * @param pio
	 *            properties in project.mf
	 * @param templateHome
	 *            folder which have freemarker templates
	 */
	private void installFreemarkerTemplates(PropertiesIO pio,
			String templateHome) throws Exception {
		getLogger().debug("Call installTemplates() of DefaultPluginInstaller");

		if (templateHome != null) {
			// 1. find templates directory
			File templateHomeDir = new File(templateHome);

			// 2. write to project.mf
			pio.setProperty(CommonConstants.PROJECT_TEMPLATE_HOME,
					templateHomeDir.getAbsolutePath());
			pio.write();

			if (templateHomeDir.exists()) {
				File[] templateTypes = FileUtil
						.dirListByAscAlphabet(templateHomeDir);

				if (templateTypes != null && templateTypes.length != 0) {

					for (File templateType : templateTypes) {
						if (FileUtil.isExistsTemplates(templateType)) {
							getLogger()
									.debug("Templates directory ["
											+ templateHome
											+ "] already exists. So, Templates installation is skipped.");
							return;
						}
					}
				}
			}

			templateHomeDir.mkdirs();

			// 3. copy templates
			copyFile("templates", templateHome);
			System.out.println("Templates directory [" + templateHome
					+ "] is created successfully.");
		}
	}

	/**
	 * copy all inspection resources
	 * 
	 * @param baseDir
	 *            target project folder path to install a plugin
	 * @param pomHandling
	 *            whether current project is based on maven or ant
	 * @param inspectionHome
	 *            folder which have inspection resources
	 */
	private void installInspectionResources(String baseDir,
			boolean pomHandling, String inspectionHome) throws Exception {
		getLogger().debug(
				"Call installInspectionResources() of DefaultPluginInstaller");

		if (inspectionHome != null) {
			// 1. modify pom.xml
			if (pomHandling) {
				File pomFile = new File(baseDir, Constants.ARCHETYPE_POM);
				FileUtil.replaceFileContent(pomFile,
						"<inspection.dir>${INSPECTION_DIR}</inspection.dir>",
						"<inspection.dir>" + inspectionHome
								+ "</inspection.dir>");
			}

			// 2. find templates directory
			File inspectionHomeDir = new File(inspectionHome);

			if (inspectionHomeDir.exists()) {
				getLogger()
						.debug("Inspection resource directory ["
								+ inspectionHome
								+ "] already exists. So, Inspection resource installation is skipped.");
				return;
			}

			inspectionHomeDir.mkdirs();

			// 3. copy templates
			copyFile("inspection", inspectionHome);
		}
	}

	/**
	 * copy specified resources to target folder
	 * 
	 * @param resourceFolderName
	 *            folder includes specified resources
	 * @param target
	 *            target folder to be copied resources
	 */
	private void copyFile(String resourceFolderName, String target)
			throws Exception {
		// 3. copy templates
		try {
			URL fileLocation = this.getClass().getProtectionDomain()
					.getCodeSource().getLocation();

			File jarFile = new File(fileLocation.getFile());

			List<String> fileNames = FileUtil.resolveFileNames(jarFile);
			List<String> templates = FileUtil.findFiles(fileNames,
					resourceFolderName + CommonConstants.fileSeparator, "**",
					null);

			ZipFile zipFile = new ZipFile(jarFile);

			for (int i = 0; i < templates.size(); i++) {
				String template = (String) templates.get(i);

				ZipEntry zipEntry = zipFile.getEntry(template);
				InputStream inputStream = zipFile.getInputStream(zipEntry);
				
				template = StringUtils.replaceOnce(template,
						resourceFolderName, "");

				File outputFile = new File(target, template);
				outputFile.getParentFile().mkdirs();
				OutputStreamWriter converter = null;
				try {
					IOUtil.copy(inputStream, new FileOutputStream(outputFile));
				} finally {
					IOUtil.close(inputStream);
					IOUtil.close(converter);
				}
			}
		} catch (Exception e) {
			getLogger().warn(
					"Installing a template/inspection resources is skipped. The reason is a '"
							+ e.getMessage() + "'.");
		}
	}

	/**
	 * process to install plugin resources
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param context
	 *            velocity context
	 * @param pio
	 *            properties in project.mf
	 * @param baseDir
	 *            target project folder path to install a plugin
	 * @param pluginInfo
	 *            plugin detail information
	 * @param pluginJar
	 *            plugin binary file
	 * @param pluginZip
	 *            zip file includes plugin binary file
	 * @param encoding
	 *            file encoding style
	 * @param pomProperties
	 *            properties of pom in current project
	 */
	private void process(ArchetypeGenerationRequest request, Context context,
			PropertiesIO pio, String baseDir, PluginInfo pluginInfo,
			File pluginJar, ZipFile pluginZip, String encoding,
			Properties pomProperties) throws Exception {
		getLogger().debug("Call process() of DefaultPluginInstaller");
		// 1. get all file names from archetypeFile

		List<String> fileNames = FileUtil.resolveFileNames(pluginJar);

		File targetDir = new File(baseDir);

		boolean pomHandling = ((Boolean) context.get("pomHandling"))
				.booleanValue();

		// 2. process dependent libraries
		if (pomHandling) {
			// 2.1 process pom file
			processPom(targetDir, pluginZip, fileNames);
			removeProcessPom(targetDir, pluginZip, fileNames);

		} else {
			// 2.2 copy dependent libraries to target folder
			String projectType = pio.readValue(CommonConstants.PROJECT_TYPE);
			processDependencyLibs(request, projectType, targetDir, pluginJar,
					pomProperties);
			removeDependencyLibs(projectType, targetDir, pluginZip, pluginJar);
		}

		if (!pomHandling
				&& pio.readValue(CommonConstants.PROJECT_TYPE)
						.equalsIgnoreCase(CommonConstants.PROJECT_TYPE_SERVICE)) {
			// update .classpath file for service type project
			processClasspath(request, targetDir, pluginInfo, pomHandling,
					pomProperties);
		}

		String projectType = pio.readValue(CommonConstants.PROJECT_TYPE);
		boolean excludeSrc = ((Boolean) context.get("excludeSrc"))
				.booleanValue();

		if (!excludeSrc
				&& projectType
						.equalsIgnoreCase(CommonConstants.PROJECT_TYPE_WEB)) {
			// 3. copy files into target and merge those with velocity
			// template based on plugin descriptor
			processTemplates(context, targetDir, pluginInfo, pluginJar,
					pluginZip, fileNames, encoding);

			// 4. add a installed plugin name to welcome file
			processWelcomeFile(targetDir, pluginInfo.getName(),
					pluginInfo.getVersion(), pluginJar, encoding);
			
			// 5. change db information of context.properties file.
			if(pluginInfo.getName().equals(CommonConstants.CORE_PLUGIN)){
				processDBProperties(pio, targetDir);
				processDBLibs(pio, pio.readValue(CommonConstants.PROJECT_BUILD_TYPE), pio.readValue(CommonConstants.DB_TYPE), baseDir);
			}

			// 5. replace plugin resources by db type
			// processDBResource((String) context.get("dbType"), targetDir,
			// pluginInfo.getName(), pluginJar, fileNames);

			// 6. create custom table, insert data to DB
			processInitialData(pio, targetDir, pluginInfo.getName(), pluginZip,
					fileNames, encoding);

			if (pluginInfo.getName().equals(CommonConstants.HIBERNATE_PLUGIN))
				processTransactionFile(targetDir);

			if (pluginInfo.getName().equals(CommonConstants.I18N_PLUGIN))
				processMessageFile(targetDir);

		}

		// 13. add installation information to plugin-installed.xml file,
		// plugin-build.xml file
		updateInstallationInfo(request, targetDir, pluginInfo,
				pluginInfo.getName(), excludeSrc);
	}

	/**
	 * change context.properties file
	 * 
	 * @param pio
	 *            properties includes db information
	 * @param baseDir
	 *            current project folder
	 */
	private void processDBProperties(PropertiesIO pio, File targetDir)
			throws Exception {
		getLogger().debug(
				"Call processDBProperties() of DefaultPluginDBChanger");

		File dbPropertiesFile = null;
		try {
			dbPropertiesFile = new File(targetDir,
					CommonConstants.SRC_MAIN_RESOURCES
							+ CommonConstants.CONTEXT_PROPERTIES);

			if (!dbPropertiesFile.exists()) {
				getLogger()
						.warn("'"
								+ dbPropertiesFile.getAbsolutePath()
								+ "' is not found. Please check a location of your project.");

				return;
			}

			PropertiesIO contextPio = new PropertiesIO(targetDir
					+ CommonConstants.SRC_MAIN_RESOURCES
					+ CommonConstants.CONTEXT_PROPERTIES);

			contextPio.setProperty(CommonConstants.APP_DB_DRIVER_CLASS,
					pio.readValue(CommonConstants.DB_DRIVER_CLASS));
			contextPio.setProperty(CommonConstants.APP_DB_URL,
					pio.readValue(CommonConstants.DB_URL));
			contextPio.setProperty(CommonConstants.APP_DB_USERNAME,
					pio.readValue(CommonConstants.DB_USERNAME));
			contextPio.setProperty(CommonConstants.APP_DB_PASSWORD,
					pio.readValue(CommonConstants.DB_PASSWORD));

			contextPio.write();
		} catch (Exception e) {
			getLogger().warn(
					"Replacing db properties in "
							+ dbPropertiesFile.getAbsolutePath()
							+ " is skipped. The reason is a '" + e.getMessage()
							+ "'.");
		}
	}
	
	/**
	 * change .classpath file and process library. if current project is based
	 * on maven, change pom.xml file. if current project is based on ant, copy
	 * library to specific target
	 * 
	 * @param pio
	 *            properties includes db information
	 * @param buildType
	 *            build type of project ('maven' or 'ant')
	 * @param dbType
	 *            db type (hsqldb, oracle, sybase, ...)
	 * @param baseDir
	 *            current project folder
	 */
	private void processDBLibs(PropertiesIO pio, String buildType,
			String dbType, String baseDir) throws Exception {
		// 1. maven --> pom.xml, .classpath (maven)
		if (buildType
				.equalsIgnoreCase(CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
			String groupId = pio.readValue(CommonConstants.DB_GROUPID);
			String artifactId = pio.readValue(CommonConstants.DB_ARTIFACTID);
			String version = pio.readValue(CommonConstants.DB_VERSION);
			processPom(baseDir, groupId, artifactId, version);

			return;
		}
		// 2. ant --> .classpath, copy
		String driverPath = pio.readValue(CommonConstants.DB_DRIVER_PATH);
		copyDBLibs(pio, baseDir, driverPath);

		String projectType = pio.readValue(CommonConstants.PROJECT_TYPE);
		if (projectType.equalsIgnoreCase(CommonConstants.PROJECT_TYPE_SERVICE)) {
			changeClasspath(pio, baseDir, dbType,
					getDBDriverPath(baseDir, driverPath));
		}
	}
	

	/**
	 * change pom.xml file
	 * 
	 * @param baseDir
	 *            current project folder
	 * @param groupId
	 *            groupId of db library
	 * @param artifactId
	 *            artifactId of db library
	 * @param version
	 *            version of db library
	 */
	@SuppressWarnings("unchecked")
	private void processPom(String baseDir, String groupId, String artifactId,
			String version) throws Exception {
		// 1. get pom file
		File pomFile = new File(baseDir, Constants.ARCHETYPE_POM);

		// 2. add dependencies of pom file
		if (pomFile.exists()) {
			try {
				Model model = pluginPomManager.readPom(pomFile);

				// 2.1 process dependencies
				List<Dependency> dependencies = model.getDependencies();

				boolean isDefined = false;
				for (int i = 0; i < dependencies.size(); i++) {
					Dependency dependency = dependencies.get(i);
					if (dependency.getGroupId().equals(groupId)
							&& dependency.getArtifactId().equals(artifactId)
							&& dependency.getVersion().equals(version)) {
						isDefined = true;
						break;
					}
				}

				Dependency dependency = new Dependency();
				dependency.setGroupId(groupId);
				dependency.setArtifactId(artifactId);
				dependency.setVersion(version);

				if (!isDefined) {
					model.addDependency(dependency);
				}

				// 2.2 process build-plugin dependencies
				Map<String, Plugin> buildPluginMap = model.getBuild()
						.getPluginsAsMap();

				String anyframePlugin = "org.codehaus.mojo:anyframe-maven-plugin";

				if (buildPluginMap.containsKey(anyframePlugin)) {
					Plugin plugin = buildPluginMap.get(anyframePlugin);

					List<Dependency> pluginDependencies = new ArrayList<Dependency>();
					pluginDependencies.add(dependency);
					plugin.setDependencies(pluginDependencies);
				}

				pluginPomManager.writePom(model, pomFile, pomFile);
			} catch (Exception e) {
				getLogger().warn(
						"Processing a pom.xml of current project is skipped. The reason is a '"
								+ e.getMessage() + "'.");
			}
		}
	}

	/**
	 * copy db library to specific folder ('web' type project ->
	 * baseDir/src/main/webapp/WEB-INF/lib, 'service' type project ->
	 * baseDir/lib)
	 * 
	 * @param pio
	 *            properties includes db information
	 * @param baseDir
	 *            current project folder
	 * @param driverPath
	 *            path includes db library
	 */
	private void copyDBLibs(PropertiesIO pio, String baseDir, String driverPath)
			throws Exception {
		driverPath = driverPath.trim();

		File dbLibFile = new File(driverPath);
		if (!dbLibFile.exists()) {
			dbLibFile = new File(baseDir, driverPath);
		}

		if (!dbLibFile.exists()) {
			return;
		}

		String projectType = pio.readValue(CommonConstants.PROJECT_TYPE);

		// 1. if project.type is web, then copy jdbc jar into WEB-INF/lib folder
		if (projectType.equals(CommonConstants.PROJECT_TYPE_WEB)) {
			String projectName = pio.readValue(CommonConstants.PROJECT_NAME);
			if (projectName != null && projectName.trim().length() > 0) {
				try {
					String inDestinationDirectory = baseDir
							+ CommonConstants.SRC_MAIN_WEBAPP_LIB;
					FileUtil.copyJars(
							pio.readValue(CommonConstants.DB_DRIVER_PATH),
							inDestinationDirectory, false);
				} catch (Exception e) {
					getLogger()
							.warn("Copying jdbc jar file into /src/main/webapp/WEB-INF/lib is skipped. The reason is jdbc jar file is not found in "
									+ driverPath + ".");
				}
			}
			// 2. if project.type is service, then copy jdbc jar into [project
			// home]/lib folder
		} else if (projectType.equals(CommonConstants.PROJECT_TYPE_SERVICE)) {
			try {
				String inDestinationDirectory = baseDir
						+ CommonConstants.fileSeparator + "lib";
				FileUtil.copyJars(
						pio.readValue(CommonConstants.DB_DRIVER_PATH),
						inDestinationDirectory, false);
			} catch (Exception e) {
				// ignore Exception
				getLogger()
						.warn("Copying jdbc jar file into /lib is skipped. The reason is jdbc jar file is not found in "
								+ driverPath + ".");
			}
		}
	}

	/**
	 * make classpath information (in case of 'service' type project)
	 * 
	 * @param baseDir
	 *            current project folder
	 * @param driverPath
	 *            path includes db library
	 * @return db library path
	 */
	private String getDBDriverPath(String baseDir, String driverPath)
			throws Exception {
		int idx = driverPath.lastIndexOf("/");
		if (idx == -1) {
			idx = driverPath.lastIndexOf("\\");
		}

		String fileName = "";
		if (idx != -1) {
			fileName = driverPath.substring(idx + 1);
			return baseDir + "/" + "lib" + "/" + fileName;
		}

		return baseDir + "/" + "lib" + "/" + driverPath;
	}
	
	/**
	 * change .classpath file
	 * 
	 * @param pio
	 *            properties includes db information
	 * @param baseDir
	 *            current project folder
	 * @param dbType
	 *            db type (hsqldb, oracle, sybase, ...)
	 * @param dbDriverPath
	 *            path includes db library
	 */
	private void changeClasspath(PropertiesIO pio, String baseDir,
			String dbType, String dbDriverPath) throws Exception {
		try {
			// 1. find .classpath file
			File classpathFile = new File(baseDir, ".classpath");

			// 2. remove previous driver jar path
			FileUtil.removeFileContent(classpathFile, "Driver jar path", "",
					true);

			if (dbType.equalsIgnoreCase("hsqldb")) {
				// 3. add current driver jar path
				String replaceString = "<!--Driver jar path-START-->\n"
						+ "<!--Driver jar path-END-->";
				FileUtil.addFileContent(classpathFile,
						"<!--Driver jar path here-->",
						"<!--Driver jar path here-->\n" + replaceString, true);

				return;
			}

			int idx = dbDriverPath.lastIndexOf("/");
			if (idx == -1) {
				idx = dbDriverPath.lastIndexOf("\\");
			}

			String dbLibFileName = dbDriverPath.substring(idx + 1);

			// 3. add current driver jar path
			String replaceString = "<!--Driver jar path-START-->\n"
					+ "<classpathentry kind=\"lib\" path=\"lib/"
					+ dbLibFileName + "\"/>\n" + "<!--Driver jar path-END-->";
			FileUtil.addFileContent(classpathFile,
					"<!--Driver jar path here-->",
					"<!--Driver jar path here-->\n" + replaceString, true);
		} catch (Exception e) {
			// ignore Exception
			catchMsg(e, "Replacing driver jar path", "/.classpath",
					"<!--Driver jar path here-->", baseDir);
		}
		
	}
	
	/**
	 * logging warning message
	 * 
	 * @param e
	 *            exception
	 * @param exceptionMsg
	 *            exception message
	 * @param fileName
	 *            file name which have problem
	 * @param tokenName
	 * @param path
	 */
	public void catchMsg(Exception e, String exceptionMsg, String fileName,
			String tokenName, String path) {
		if (e instanceof FileNotFoundException)
			getLogger().warn(
					exceptionMsg + " in " + fileName
							+ " is skipped. The reason is " + fileName
							+ " is not found in " + path + ".");
		else
			getLogger()
					.warn(exceptionMsg + " in " + fileName
							+ " is skipped. The reason is " + tokenName
							+ " token is not found in " + path + fileName + ".");

	}
	
	/**
	 * in case of installing using maven, merge a new pom file of plugin with
	 * current pom file
	 * 
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginZip
	 *            zip file includes plugin binary file
	 * @param fileNames
	 *            resources in plugin jar file
	 */
	private void processPom(File targetDir, ZipFile pluginZip,
			List<String> fileNames) throws Exception {
		getLogger().debug("Call processPom() of DefaultPluginInstaller");

		// 1. extract pom file from plugin jar file
		List<String> pomFiles = FileUtil.findFiles(fileNames,
				CommonConstants.PLUGIN_RESOURCES, "**\\"
						+ Constants.ARCHETYPE_POM, null);
		// 2. merge dependencies of pom file with current dependencies
		if (pomFiles.size() > 0) {
			try {
				File temporaryPomFile = new File(targetDir, "pom.tmp");
				temporaryPomFile.getParentFile().mkdirs();

				copyFile(pluginZip, (String) pomFiles.get(0), temporaryPomFile);

				pluginPomManager.mergePom(new File(targetDir,
						Constants.ARCHETYPE_POM), temporaryPomFile);

				FileUtil.deleteFile(temporaryPomFile);
			} catch (Exception e) {
				getLogger().warn(
						"Processing a pom.xml of current project is skipped. The reason is a '"
								+ e.getMessage() + "'.");
			}
		} else {
			getLogger()
					.warn("Merging current pom file with that of plugin is skipped. The reason is a pom.xml in "
							+ pluginZip.getName() + " doesn't exist.");
		}
	}

	/**
	 * in case of installing using maven, merge a new pom file of plugin with
	 * current pom file
	 * 
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginZip
	 *            zip file includes plugin binary file
	 * @param fileNames
	 *            resources in plugin jar file
	 */
	private void removeProcessPom(File targetDir, ZipFile pluginZip,
			List<String> fileNames) throws Exception {
		getLogger().debug("Call removeProcessPom() of DefaultPluginInstaller");

		// 1. extract pom file from plugin jar file
		List<String> pomFiles = FileUtil.findFiles(fileNames,
				CommonConstants.PLUGIN_RESOURCES, "**\\"
						+ CommonConstants.ARCHETYPE_REMOVE_POM, null);
		// 2. merge dependencies of pom file with current dependencies
		if (pomFiles.size() > 0) {
			try {
				File temporaryPomFile = new File(targetDir, "remove-pom.xml");
				temporaryPomFile.getParentFile().mkdirs();
				copyFile(pluginZip, (String) pomFiles.get(0), temporaryPomFile);
				pluginPomManager.removePomSpecificDependencies(new File(
						targetDir, Constants.ARCHETYPE_POM), temporaryPomFile);
				FileUtil.deleteFile(temporaryPomFile);
			} catch (Exception e) {
				getLogger().debug(
						"Processing a remove-pom.xml of current project is skipped. The reason is a '"
								+ e.getMessage() + "'.");
			}
		} else {
			getLogger()
					.debug("Removing current pom file with that of plugin is skipped. The reason is a remove-pom.xml in "
							+ pluginZip.getName() + " doesn't exist.");
		}
	}

	/**
	 * copy dependent libraries of plugin to target folder
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param projectType
	 *            generated project's style ('web' or 'service')
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginJar
	 *            plugin binary file
	 * @param pomProperties
	 *            properties of pom in current project
	 */
	public void processDependencyLibs(ArchetypeGenerationRequest request,
			String projectType, File targetDir, File pluginJar,
			Properties pomProperties) {
		getLogger().debug(
				"Call processDependencyLibs() of DefaultPluginInstaller");

		// 1. find a destination for copying dependent libararies
		File destination = null;
		if (projectType.equalsIgnoreCase(CommonConstants.PROJECT_TYPE_SERVICE)) {
			destination = new File(targetDir, "lib");
		} else if (projectType
				.equalsIgnoreCase(CommonConstants.PROJECT_TYPE_WEB)) {
			destination = new File(targetDir,
					CommonConstants.SRC_MAIN_WEBAPP_LIB);
		}

		if (!destination.exists()) {
			destination.mkdirs();
		}

		try {
			// 2. find dependency information to be added
			// 2.1 get new dependencies from plugin resource
			List<Dependency> targetDependencies = pluginPomManager
					.getDependencies(pluginJar, pomProperties);
			Map<String, Dependency> convertedTargetDependencies = pluginPomManager
					.convertDependencyList(targetDependencies);

			// 2.2 get installed plugin libraries
			Map<String, File> installedPluginJars = pluginInfoManager
					.getInstalledPluginJars(request,
							targetDir.getAbsolutePath());
			Iterator<String> keyItr = installedPluginJars.keySet().iterator();
			while (keyItr.hasNext()) {
				String installedPluginName = keyItr.next();

				// 2.3 get installed dependencies
				List<Dependency> installedDependencies = pluginPomManager
						.getDependencies(
								installedPluginJars.get(installedPluginName),
								pomProperties);
				Map<String, Dependency> convertedInstalledDependencies = pluginPomManager
						.convertDependencyList(installedDependencies);

				Iterator<String> installedDependencyItr = convertedInstalledDependencies
						.keySet().iterator();
				// 2.4 check whether latest dependency
				while (installedDependencyItr.hasNext()) {
					String installedDependency = installedDependencyItr.next();
					if (convertedTargetDependencies
							.containsKey(installedDependency)) {
						// 2.4.1 if same dependency with different version
						// exist, compare version
						String installedVersion = convertedInstalledDependencies
								.get(installedDependency).getVersion();
						String targetVersion = convertedTargetDependencies.get(
								installedDependency).getVersion();

						if (VersionComparator.greaterThan(targetVersion,
								installedVersion)) {
							// 2.4.1.1 if target version is greater than current
							// version, remove current library
							Dependency installedDependencyInfo = convertedInstalledDependencies
									.get(installedDependency);
							File installedDependencyLib = new File(
									destination,
									installedDependencyInfo.getArtifactId()
											+ "-"
											+ installedDependencyInfo
													.getVersion()
											+ (StringUtils
													.isEmpty(installedDependencyInfo
															.getClassifier()) ? ""
													: "-"
															+ installedDependencyInfo
																	.getClassifier())
											+ ".jar");
							FileUtil.deleteFile(installedDependencyLib);
						} else {
							// 2.4.1.2 if target version is less than or equal
							// to current version, remove target library
							targetDependencies
									.remove(convertedTargetDependencies
											.get(installedDependency));
						}
					}
				}
			}

			Set<Artifact> dependencyArtifacts = pluginArtifactManager
					.downloadArtifact(request, targetDependencies);

			getLogger().debug(
					"Copy " + dependencyArtifacts.size()
							+ " dependent libraries into "
							+ destination.getAbsolutePath());

			Iterator<Artifact> dependencyItr = dependencyArtifacts.iterator();

			while (dependencyItr.hasNext()) {
				Artifact dependencyArtifact = dependencyItr.next();
				if (dependencyArtifact.getScope() == null
						|| dependencyArtifact.getScope().equals("")
						|| dependencyArtifact.getScope().equals("compile")) {

					FileUtil.copyDir(dependencyArtifact.getFile()
							.getCanonicalFile(), destination);
				}
			}
		} catch (Exception e) {
			getLogger().warn(
					"Copying dependent libraries into '" + targetDir
							+ " is skipped. The reason is a '" + e.getMessage()
							+ "'.");
		}
	}

	/**
	 * copy dependent libraries of plugin to target folder
	 * 
	 * @param projectType
	 *            generated project's style ('web' or 'service')
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginJar
	 *            plugin binary file
	 */
	public void removeDependencyLibs(String projectType, File targetDir,
			ZipFile pluginZip, File pluginJar) throws Exception {
		getLogger().debug(
				"Call removeDependencyLibs() of DefaultPluginInstaller");

		// 1. remove generated libraries
		File destination = null;
		if (projectType.equalsIgnoreCase(CommonConstants.PROJECT_TYPE_SERVICE)) {
			destination = new File(targetDir, "lib");
		} else if (projectType
				.equalsIgnoreCase(CommonConstants.PROJECT_TYPE_WEB)) {
			destination = new File(targetDir,
					CommonConstants.SRC_MAIN_WEBAPP_LIB);
		}

		if (!destination.exists()) {
			return;
		}

		List<String> fileNames = FileUtil.resolveFileNames(pluginJar);

		List<String> pomFiles = FileUtil.findFiles(fileNames,
				CommonConstants.PLUGIN_RESOURCES, "**\\"
						+ CommonConstants.ARCHETYPE_REMOVE_POM, null);
		// 2. merge dependencies of pom file with current dependencies
		if (pomFiles.size() > 0) {
			try {

				File temporaryPomFile = new File(targetDir, "remove-pom.tmp");
				temporaryPomFile.getParentFile().mkdirs();
				InputStream is = pluginInfoManager.getPluginResource(
						pomFiles.get(0), pluginJar);
				IOUtil.copy(is, new FileOutputStream(temporaryPomFile));

				Model removeModel = pluginPomManager.readPom(temporaryPomFile);

				List<Dependency> dependencies = removeModel.getDependencies();
				List<String> removeFileNames = new ArrayList<String>();

				for (Dependency dependency : dependencies) {
					removeFileNames
							.add(dependency.getArtifactId()
									+ "-"
									+ dependency.getVersion()
									+ (StringUtils.isEmpty(dependency
											.getClassifier()) ? "" : "-"
											+ dependency.getClassifier())
									+ ".jar");
				}

				FileUtil.deleteFile(destination, removeFileNames);
				FileUtil.deleteFile(temporaryPomFile);
			} catch (Exception e) {
				getLogger().debug(
						"Processing a remove-pom.xml of current project is skipped. The reason is a '"
								+ e.getMessage() + "'.");
			}
		} else {
			getLogger()
					.debug("Removing current pom file with that of plugin is skipped. The reason is a remove-pom.xml in "
							+ pluginZip.getName() + " doesn't exist.");
		}

	}

	/**
	 * change classpath entries
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginInfo
	 *            plugin detail information
	 * @param pomHandling
	 *            whether current project is based on maven or ant
	 * @param pomProperties
	 *            properties of pom in current project
	 */
	public void processClasspath(ArchetypeGenerationRequest request,
			File targetDir, PluginInfo pluginInfo, boolean pomHandling,
			Properties pomProperties) throws Exception {

		getLogger().debug("Call processClasspath() of DefaultPluginInstaller");

		File classpath = new File(targetDir, ".classpath");
		if (!classpath.exists()) {
			getLogger()
					.warn("'"
							+ classpath.getAbsolutePath()
							+ "' file is not found. Please check a location of your project.");

			return;
		}

		Map<String, PluginInfo> installedPlugins = pluginInfoManager
				.getInstalledPlugins(targetDir.getAbsolutePath());
		if (pluginInfo != null) {
			installedPlugins.put(pluginInfo.getName(), pluginInfo);
		}
		Collection<PluginInfo> insatlledPluginsValues = installedPlugins
				.values();

		List<String> classentries = new ArrayList<String>();
		StringBuffer classpathentries = new StringBuffer();

		for (PluginInfo installedPluginInfo : insatlledPluginsValues) {
			File pluginJar = pluginInfoManager.getPluginFile(request,
					installedPluginInfo.getGroupId(),
					installedPluginInfo.getArtifactId(),
					installedPluginInfo.getVersion());

			List<Dependency> dependencies = pluginPomManager.getDependencies(
					pluginJar, pomProperties);

			for (Dependency dependency : dependencies) {
				if (classentries.contains(dependency.getGroupId() + ","
						+ dependency.getArtifactId() + ","
						+ dependency.getVersion())) {
					continue;
				}

				String groupPath = dependency.getGroupId().replace(".", "/");

				if (pomHandling) {
					classpathentries
							.append("<classpathentry kind=\"var\" path=\"M2_REPO/"
									+ groupPath
									+ "/"
									+ dependency.getArtifactId()
									+ "/"
									+ dependency.getVersion()
									+ "/"
									+ dependency.getArtifactId()
									+ "-"
									+ dependency.getVersion()
									+ ((dependency.getClassifier() != null) ? "-"
											+ dependency.getClassifier()
											: "") + ".jar\"/> \n");
				} else {
					if (dependency.getScope() == null
							|| dependency.getScope().equals("")
							|| dependency.getScope().equals("compile")) {
						classpathentries
								.append("<classpathentry kind=\"lib\" path=\""
										+ "lib/"
										+ dependency.getArtifactId()
										+ "-"
										+ dependency.getVersion()
										+ ((dependency.getClassifier() != null) ? "-"
												+ dependency.getClassifier()
												: "") + ".jar\"/> \n");
					}
				}

				classentries.add(dependency.getGroupId() + ","
						+ dependency.getArtifactId() + ","
						+ dependency.getVersion());
			}
		}

		FileUtil.replaceFileContent(
				classpath,
				"<!--Add new classpathentry here-->",
				"</classpath>",
				"<!--Add new classpathentry here-->\n</classpath>",
				"<!--Add new classpathentry here-->",
				"<!--Add new classpathentry here-->\n"
						+ classpathentries.toString());
	}

	/**
	 * merge plugin resources and copy merged files to target folder
	 * 
	 * @param context
	 *            velocity context
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginInfo
	 *            plugin detail information
	 * @param pluginJar
	 *            plugin binary file
	 * @param pluginZip
	 *            zip file includes plugin binary file
	 * @param fileNames
	 *            resources in plugin jar file
	 * @param encoding
	 *            file encoding style
	 */
	private void processTemplates(Context context, File targetDir,
			PluginInfo pluginInfo, File pluginJar, ZipFile pluginZip,
			List<String> fileNames, String encoding) throws Exception {
		getLogger().debug("Call processTemplates() of DefaultPluginInstaller");
		// 1. get resource list
		List<PluginResource> pluginResources = pluginInfo.getResources();
		for (PluginResource pluginResource : pluginResources) {
			getLogger().debug(
					"Processing resources in directory '"
							+ pluginResource.getDir() + "' ["
							+ CommonConstants.METAINF_ANYFRAME
							+ CommonConstants.PLUGIN_FILE + "]");

			// 2. get file list from current resource
			// 2.1 set include
			List<Include> includeResources = pluginResource.getIncludes();
			List<String> includes = new ArrayList<String>();
			for (Include include : includeResources) {
				includes.add(include.getName());
			}

			// 2.2 set exclude
			List<Exclude> excludeResources = pluginResource.getExcludes();
			List<String> excludes = new ArrayList<String>();
			List<String> replaces = new ArrayList<String>();
			for (Exclude exclude : excludeResources) {
				excludes.add(exclude.getName());
				if (exclude.isMerged()) {
					getLogger().debug("merged file : " + exclude.getName());
					replaces.add(exclude.getName());
				}

			}
			getLogger().debug("The size of merged file is " + replaces.size());

			// 2.3 scan resources
			List<String> templates = FileUtil.findFiles(
					fileNames,
					CommonConstants.PLUGIN_RESOURCES
							+ CommonConstants.fileSeparator
							+ pluginResource.getDir(), includes, excludes);
			// 3. make directory for copying plugin resource
			getOutput(context, targetDir, pluginResource.getDir(), "",
					pluginResource.isPackaged(),
					(String) context.get("package")).mkdirs();
			getLogger().debug("Copying resources " + pluginResource);

			// 4. copy files to output directory
			processTemplate(context, pluginInfo.getName(), targetDir,
					pluginJar, pluginZip, pluginResource.getDir(),
					(String) context.get("package"),
					pluginResource.isPackaged(), pluginResource.isFiltered(),
					templates, encoding);

			getLogger().debug("Copied " + templates.size() + " files");

			if (replaces.size() > 0) {
				List<String> replaceFiles = FileUtil.findFiles(fileNames,
						CommonConstants.PLUGIN_RESOURCES
								+ CommonConstants.fileSeparator
								+ pluginResource.getDir(), replaces, null);

				// 5. merge files to output directory
				processReplace(context, pluginInfo.getName(), targetDir,
						pluginJar, pluginResource.getDir(),
						(String) context.get("package"),
						pluginResource.isPackaged(),
						pluginResource.isFiltered(), replaceFiles, encoding);

				getLogger().debug("Merged " + replaces.size() + " files");
			}

		}
	}

	/**
	 * merge a plugin resource and copy a merged file to target folder
	 * 
	 * @param context
	 *            velocity context
	 * @param pluginName
	 *            a target plugin name to install
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginJar
	 *            plugin binary file
	 * @param pluginZip
	 *            zip file includes plugin binary file
	 * @param resourceDir
	 *            plugin resources folder
	 * @param packageName
	 *            project's base package name
	 * @param packaged
	 *            whether a plugin resource has package (ex. java)
	 * @param filtered
	 *            whether a plugin resource will be merged with velocity context
	 * @param templates
	 *            plugin resources
	 * @param encoding
	 *            file encoding style
	 */
	private void processTemplate(Context context, String pluginName,
			File targetDir, File pluginJar, ZipFile pluginZip,
			String resourceDir, String packageName, boolean packaged,
			boolean filtered, List<String> templates, String encoding)
			throws Exception {
		Iterator<String> templateItr = templates.iterator();

		while (templateItr.hasNext()) {
			String template = templateItr.next();

			if (template.startsWith(CommonConstants.DB_RESOURCES)) {
				continue;
			}

			// 1. merge template
			File output = getOutput(context, targetDir, resourceDir, template,
					packaged, packageName);
			output.getParentFile().mkdirs();
			copyFile(pluginZip, template, output);
			if (filtered) {
				mergeTemplate(context, template, output, encoding);
			}

			// 2. replace current configuration file based on db template in
			// plugin
			String templateName = StringUtils.replaceOnce(template,
					CommonConstants.PLUGIN_RESOURCES, "");

			String dbResourceName = CommonConstants.DB_RESOURCES
					+ (String) context.get("dbType") + templateName;

			ZipEntry zipEntry = pluginZip.getEntry(dbResourceName);
			if (zipEntry != null) {
				replaceDBResource(pluginName, pluginJar, output, dbResourceName);
			}
		}
	}

	/**
	 * merge a archetype resource and copy a merged file to target folder
	 * 
	 * @param context
	 *            velocity context
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginZip
	 *            zip file includes plugin binary file
	 * @param resourceDir
	 *            plugin resources folder
	 * @param packageName
	 *            project's base package name
	 * @param packaged
	 *            whether a plugin resource has package (ex. java)
	 * @param filtered
	 *            whether a plugin resource will be merged with velocity context
	 * @param originalTemplates
	 *            original archetype resources
	 * @param changedTemplates
	 *            changed archetype resources (archetype-resources ->
	 *            plugin-resources)
	 * @param encoding
	 *            file encoding style
	 */
	public void processTemplate(Context context, File targetDir,
			ZipFile pluginZip, String resourceDir, String packageName,
			boolean packaged, boolean filtered, List<String> originalTemplates,
			List<String> changedTemplates, String encoding) throws Exception {

		for (int i = 0; i < originalTemplates.size(); i++) {
			String originalTemplate = originalTemplates.get(i);

			File output = getOutput(context, targetDir, resourceDir,
					changedTemplates.get(i), packaged, packageName);
			output.getParentFile().mkdirs();

			copyFile(pluginZip, originalTemplate, output);
			if (filtered) {
				mergeTemplate(context, originalTemplate, output, encoding);
			}
		}
	}

	/**
	 * merge a plugin resource and copy a merged file to target folder
	 * 
	 * @param context
	 *            velocity context
	 * @param plguinName
	 *            plugin name
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginJar
	 *            plugin binary file
	 * @param resourceDir
	 *            plugin resources folder
	 * @param packageName
	 *            project's base package name
	 * @param packaged
	 *            whether a plugin resource has package (ex. java)
	 * @param filtered
	 *            whether a plugin resource will be merged with velocity context
	 * @param replaceFiles
	 *            merge target resources
	 * @param encoding
	 *            file encoding style
	 */
	private void processReplace(Context context, String pluginName,
			File targetDir, File pluginJar, String resourceDir,
			String packageName, boolean packaged, boolean filtered,
			List<String> replaceFiles, String encoding) throws Exception {
		getLogger().debug("Call processReplace() of DefaultPluginInstaller");

		try {
			for (String replaceFile : replaceFiles) {

				File targetFile = getOutput(context, targetDir, resourceDir,
						replaceFile, packaged, packageName);

				if (!targetFile.exists()) {
					getLogger().warn(
							"merge target file ["
									+ targetFile.getAbsolutePath()
									+ "] not exist!");
					continue;
				}

				String contents = pluginInfoManager.readPluginResource(
						replaceFile, pluginJar, encoding);

				if (contents != null) {
					if (filtered)
						contents = processVelocityToContents(context, contents);

					if (targetFile.getName().endsWith(
							"." + CommonConstants.EXT_XML)) {

						if (pluginName.equals(CommonConstants.I18N_PLUGIN)
								&& targetFile.getName().endsWith(
										CommonConstants.CONFIG_MESSAGE_FILE)) {
							FileUtil.addFileContent(targetFile,
									"<!--Add new messagesource here-->",
									"<!--Add new messagesource here-->\n<!--"
											+ pluginName
											+ "-messagesource-START-->\n"
											+ contents + "\n<!--" + pluginName
											+ "-messagesource-END-->", true);
						} else if(pluginName.equals(CommonConstants.CORE_PLUGIN)
								&& targetFile.getName().endsWith(CommonConstants.WEB_XML_FILE)){
							FileUtil.addFileContent(targetFile,
									"<!--Add new configuration here-->",
									"<!--" + pluginName + "-configuration-START-->\n"
											+ contents + "\n<!--" + pluginName
											+ "-configuration-END-->\n" 
											+"<!--Add new configuration here-->", true);
						}else {
							FileUtil.addFileContent(targetFile,
									"<!--Add new configuration here-->",
									"<!--Add new configuration here-->\n<!--"
											+ pluginName
											+ "-configuration-START-->\n"
											+ contents + "\n<!--" + pluginName
											+ "-configuration-END-->", true);
						}
					} else if (targetFile.getName().endsWith(
							"." + CommonConstants.EXT_JSP)) {
						FileUtil.addFileContent(targetFile,
								"<!--Add new configuration here-->",
								"<!--Add new configuration here-->\n<!--"
										+ pluginName
										+ "-configuration-START-->\n"
										+ contents + "\n<!--" + pluginName
										+ "-configuration-END-->", false);
					} else if (targetFile.getName().endsWith(
							"." + CommonConstants.EXT_JAVA)) {
						FileUtil.addFileContent(targetFile,
								"//Add new configuration here",
								"//Add new configuration here\n//" + pluginName
										+ "-configuration-START\n" + contents
										+ "//" + pluginName
										+ "-configuration-END\n", false);
					} else if (targetFile.getName().endsWith(
							"." + CommonConstants.EXT_PROPERTIES)) {
						FileUtil.addFileContent(targetFile,
								"#Add new configuration here",
								"#Add new configuration here\n#" + pluginName
										+ "-configuration-START\n" + contents
										+ "#" + pluginName
										+ "-configuration-END\n", false);
					}
				}
			}
		} catch (Exception e) {
			getLogger().warn(
					"Merging a file into current project is skipped. The reason is a '"
							+ e.getMessage() + "'.");
		}

	}

	/**
	 * in case of installing hibernate plugin, change transaction configuration
	 * 
	 * @param targetDir
	 *            target project folder to install a plugin
	 */
	private void processTransactionFile(File targetDir) throws Exception {
		getLogger().debug(
				"Call processTransactionFile() of DefaultPluginInstaller");
		try {

			// 1. get a transaction configuration file
			File file = new File(targetDir.getAbsolutePath()
					+ CommonConstants.SRC_MAIN_RESOURCES + "spring",
					CommonConstants.CONFIG_TX_FILE);

			FileUtil.replaceFileContent(
					file,
					"id=\"txManager\" class=\"org.springframework.jdbc.datasource.DataSourceTransactionManager\"",
					"id=\"txManager\" class=\"org.springframework.orm.hibernate3.HibernateTransactionManager\"");

			FileUtil.replaceFileContent(file,
					"<property name=\"dataSource\" ref=\"dataSource\" />",
					"<property name=\"sessionFactory\" ref=\"sessionFactory\" />");
		} catch (Exception e) {
			getLogger()
					.warn("Processing a context-transaction.xml of current project is skipped. The reason is a '"
							+ e.getMessage() + "'.");
		}

	}

	/**
	 * in case of installing i18n plugin, change messageSource configuration
	 * 
	 * @param targetDir
	 *            target project folder to install a plugin
	 */
	private void processMessageFile(File targetDir) throws Exception {
		getLogger()
				.debug("Call processMessageFile() of DefaultPluginInstaller");
		try {

			// 1. get a transaction configuration file
			File file = new File(targetDir.getAbsolutePath()
					+ CommonConstants.SRC_MAIN_RESOURCES + "spring",
					CommonConstants.CONFIG_MESSAGE_FILE);

			FileUtil.replaceFileContent(file, "<bean id=\"messageSource\"",
					"<bean id=\"fileMessageSource\"");
		} catch (Exception e) {
			getLogger()
					.warn("Processing a context-message.xml of current project is skipped. The reason is a '"
							+ e.getMessage() + "'.");
		}
	}

	/**
	 * add a hyperlink to welcome file
	 * 
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginName
	 *            a target plugin name to install
	 * @param pluginVersion
	 *            a target plugin version to install
	 * @param pluginJar
	 *            plugin binary file
	 * @param encoding
	 *            file encoding style
	 */
	private void processWelcomeFile(File targetDir, String pluginName,
			String pluginVersion, File pluginJar, String encoding)
			throws Exception {
		getLogger()
				.debug("call processWelcomeFile() of DefaultPluginInstaller");

		try {
			String filePath = CommonConstants.SRC_MAIN_WEBAPP
					+ CommonConstants.WELCOME_FILE;

			File file = new File(targetDir, filePath);

			String contents = pluginInfoManager.readPluginResource(
					CommonConstants.PLUGIN_RESOURCES + filePath, pluginJar,
					encoding);

			if (contents == null) {
				String menuName = pluginName.substring(0, 1).toUpperCase()
						+ pluginName.substring(1);

				FileUtil.addFileContent(file,
						"<!--Add new configuration here-->",
						"<!--Add new configuration here-->\n<!--" + pluginName
								+ "-configuration-START-->\n" + "<li>"
								+ menuName + " " + pluginVersion + "</li>"
								+ "\n<!--" + pluginName
								+ "-configuration-END-->", false);
			}
		} catch (Exception e) {
			getLogger()
					.warn("Adding a inform of installed plugin into current project is skipped. The reason is a '"
							+ e.getMessage() + "'.");
		}
	}

	private String processVelocityToContents(Context context, String contents)
			throws Exception {
		StringWriter newContents = new StringWriter();
		velocity.evaluate(context, newContents, "newContents", contents);
		return newContents.toString();
	}

	/**
	 * create custom table, insert data to DB
	 * 
	 * @param pio
	 *            properties in project.mf
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginName
	 *            a target plugin name to install
	 * @param pluginZip
	 *            zip file includes plugin binary file
	 * @param fileNames
	 *            resources in plugin jar file
	 * @param encoding
	 *            file encoding style
	 */
	private void processInitialData(PropertiesIO pio, File targetDir,
			String pluginName, ZipFile pluginZip, List<String> fileNames,
			String encoding) throws Exception {
		getLogger()
				.debug("call processInitialData() of DefaultPluginInstaller");

		String dbType = pio.readValue(CommonConstants.DB_TYPE);

		List<String> dbScripts = FileUtil.findFiles(fileNames,
				CommonConstants.PLUGIN_RESOURCES, "**\\" + pluginName
						+ "-insert-data-" + dbType + ".sql", null);

		if (dbScripts.size() > 0) {
			try {
				DBUtil.runStatements(targetDir, pluginName, pluginZip,
						dbScripts, encoding, pio.getProperties());
				getLogger().debug(
						"Run " + dbScripts + " dbscripts of plugin ["
								+ pluginName + "] successfully.");
			} catch (Exception e) {
				if (e.getCause() instanceof SQLException) {
					getLogger().warn(
							"Executing db script of " + pluginName
									+ " plugin is skipped. The reason is "
									+ e.getMessage());
				} else {
					getLogger().warn(
							"Processing initial data for " + pluginName
									+ " is skipped. The reason is a '"
									+ e.getMessage() + "'.");
				}
			}
		}
	}

	/**
	 * add installation information to plugin-installed.xml file
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginInfo
	 *            plugin detail information
	 * @param pluginName
	 *            a target plugin name to install
	 * @param excludeSrc
	 *            whether plugin resources exclude source codes will be
	 *            installed
	 */
	@SuppressWarnings("unchecked")
	private void updateInstallationInfo(ArchetypeGenerationRequest request,
			File targetDir, PluginInfo pluginInfo, String pluginName,
			boolean excludeSrc) throws Exception {
		getLogger().debug(
				"Call updateInstallationInfo() of DefaultPluginInstaller");

		// 1. find a plugin-installed.xml
		File pluginInstalledXML = new File(targetDir, CommonConstants.METAINF
				+ CommonConstants.PLUGIN_INSTALLED_FILE);

		Map<String, PluginInfo> pluginMap = (Map<String, PluginInfo>) FileUtil
				.getObjectFromXML(pluginInstalledXML);

		// 2. set installation information about a specific plugin
		PluginInfo targetPluginInfo = new PluginInfo();
		targetPluginInfo.setName(pluginInfo.getName());
		targetPluginInfo.setGroupId(pluginInfo.getGroupId());
		targetPluginInfo.setArtifactId(pluginInfo.getArtifactId());
		targetPluginInfo.setVersion(pluginInfo.getVersion());
		targetPluginInfo.setEssential(new Boolean(pluginCatalogManager
				.isEssential(request, pluginName, pluginInfo.getVersion()))
				.toString());
		pluginMap.put(pluginName, targetPluginInfo);

		// 3. update
		FileUtil.getObjectToXML(pluginMap, pluginInstalledXML);

		// 1. find a plugin-build.xml
		File pluginBuildFile = new File(targetDir,
				CommonConstants.PLUGIN_BUILD_FILE);

		if (pluginBuildFile.exists()) {
			PluginInfo buildPluginInfo = (PluginInfo) FileUtil
					.getObjectFromXML(pluginBuildFile);

			List<DependentPlugin> dependentPlugins = buildPluginInfo
					.getDependentPlugins();

			DependentPlugin dependentPlugin = new DependentPlugin();
			dependentPlugin.setName(pluginInfo.getName());
			dependentPlugin.setVersion(pluginInfo.getVersion());
			dependentPlugins.add(dependentPlugin);

			FileUtil.getObjectToXML(buildPluginInfo, pluginBuildFile);
		}
	}

	/**
	 * copy an installed db resource to a plugin's db resource
	 * 
	 * @param pluginName
	 *            a target plugin name to install
	 * @param pluginJar
	 *            plugin binary file
	 * @param dbResource
	 *            installed configuration file related to db
	 * @param dbResourceTemplate
	 *            a plugin's db resource
	 */
	private void replaceDBResource(String pluginName, File pluginJar,
			File dbResource, String dbResourceTemplate) throws Exception {
		// 1. find map includes replace string
		InputStream inputStream = pluginInfoManager.getPluginResource(
				dbResourceTemplate, pluginJar);

		Map<String, String> replaceStringMap = null;
		Map<String, String> tokenMap = null;
		if (dbResource.getName().endsWith(CommonConstants.EXT_JAVA)) {
			replaceStringMap = FileUtil.findReplaceRegionOfClass(inputStream,
					pluginName);
			// 2. find token to be replaced
			tokenMap = FileUtil.findReplaceRegionOfClass(new FileInputStream(
					dbResource), pluginName);
		}

		if (dbResource.getName().endsWith(CommonConstants.EXT_XML)) {
			replaceStringMap = FileUtil.findReplaceRegion(inputStream,
					pluginName);

			// 2. find token to be replaced
			tokenMap = FileUtil.findReplaceRegion(new FileInputStream(
					dbResource), pluginName);
		}

		getLogger().debug("token size : " + tokenMap.size());
		getLogger().debug("replaceString size : " + replaceStringMap.size());

		// 3. replace
		if (tokenMap.size() > 0) {
			Set<String> commentKeySet = tokenMap.keySet();
			Iterator<String> commentKeyItr = commentKeySet.iterator();

			while (commentKeyItr.hasNext()) {
				String commentKey = commentKeyItr.next();
				getLogger().debug("commentKey : " + commentKey);

				if (replaceStringMap.containsKey(commentKey)) {
					String startToken = "<!--" + pluginName + "-" + commentKey
							+ "-START-->";
					String endToken = "<!--" + pluginName + "-" + commentKey
							+ "-END-->";

					if (dbResource.getName().endsWith(CommonConstants.EXT_JAVA)) {
						startToken = "//" + pluginName + "-" + commentKey
								+ "-START";
						endToken = "//" + pluginName + "-" + commentKey
								+ "-END";
					}

					String value = startToken + "\n"
							+ replaceStringMap.get(commentKey) + "\n"
							+ endToken;
					FileUtil.replaceFileContent(dbResource, startToken,
							endToken, startToken + endToken, value, false);
				}
			}
		}
		// }
	}

	/**
	 * merge velocity template with ouput file
	 * 
	 * @param context
	 *            velocity context
	 * @param template
	 *            a plugin resource to be merged
	 * @param output
	 *            merged file
	 * @param encoding
	 *            file encoding style
	 */
	private void mergeTemplate(Context context, String template, File output,
			String encoding) throws Exception {
		try {
			Writer writer = new OutputStreamWriter(
					new FileOutputStream(output), encoding);
			velocity.mergeTemplate(template, encoding, context, writer);
			writer.flush();
		} catch (Exception e) {
			getLogger().warn(
					"Merging of " + output.getName() + " with template"
							+ " is skipped. The reason is a '" + e.getMessage()
							+ "'.");
		}
	}

	/**
	 * make a output file
	 * 
	 * @param context
	 *            velocity context
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param resourceDir
	 *            plugin resources folder
	 * @param template
	 *            a plugin resource
	 * @param packaged
	 *            whether a plugin resource has package (ex. java)
	 * @param packageName
	 *            project's base package name
	 * @return output file
	 */
	public File getOutput(Context context, File targetDir, String resourceDir,
			String template, boolean packaged, String packageName) {
		template = StringUtils.replaceOnce(template,
				CommonConstants.PLUGIN_RESOURCES + "/" + resourceDir, "");

		String outputName = resourceDir + CommonConstants.fileSeparator
				+ (packaged ? FileUtil.changePackageForDir(packageName) : "")
				+ CommonConstants.fileSeparator + template;

		Pattern tokenPattern = Pattern.compile(".*" + DELIMITER + ".*"
				+ DELIMITER + ".*");

		if (tokenPattern.matcher(outputName).matches()) {
			outputName = replaceOutputName(context, outputName);
		}

		File output = new File(targetDir, outputName);

		return output;
	}

	/**
	 * replace output file name
	 * 
	 * @param context
	 *            velocity context
	 * @param outputName
	 *            output file name
	 * @return changed output file name
	 */
	private String replaceOutputName(Context context, String outputName) {
		String interpolatedResult = outputName;
		String propertyToken = null;
		String contextPropertyValue = null;

		int start = 0;
		int end = 0;
		int skipUndefinedPropertyIndex = 0;

		int maxAttempts = StringUtils.countMatches(interpolatedResult,
				DELIMITER) / 2;

		for (int x = 0; x < maxAttempts && start != -1; x++) {
			start = interpolatedResult.indexOf(DELIMITER,
					skipUndefinedPropertyIndex);

			if (start != -1) {
				end = interpolatedResult.indexOf(DELIMITER,
						start + DELIMITER.length());

				if (end != -1) {
					propertyToken = interpolatedResult.substring(start
							+ DELIMITER.length(), end);
				}

				contextPropertyValue = (String) context.get(propertyToken);

				if (!StringUtils.isEmpty(contextPropertyValue)) {

					interpolatedResult = StringUtils.replace(
							interpolatedResult, DELIMITER + propertyToken
									+ DELIMITER, contextPropertyValue);

				} else {
					// Need to skip the undefined
					// property
					skipUndefinedPropertyIndex = end + DELIMITER.length() + 1;

					getLogger().warn(
							"Replacing a token in '" + interpolatedResult
									+ "' is skipped. The reason is property '"
									+ propertyToken + "' was not specified.");
				}
			}
		}

		return interpolatedResult;
	}

	/**
	 * copy a template to output file
	 * 
	 * @param pluginZip
	 *            zip file includes plugin binary file
	 * @param template
	 *            a plugin resource to copy
	 * @param output
	 *            a target file
	 */
	private void copyFile(final ZipFile pluginZip, final String template,
			final File output) throws Exception {
		InputStream inputStream = null;
		try {
			ZipEntry zipEntry = pluginZip.getEntry(template);
			inputStream = pluginZip.getInputStream(zipEntry);

			IOUtil.copy(inputStream, new FileOutputStream(output));
		} catch (Exception e) {
			getLogger().warn(
					"Copying a '" + template + "' into " + output.getName()
							+ " is skipped. The reason is a '" + e.getMessage()
							+ "'.");
		} finally {
			IOUtil.close(inputStream);
		}
	}

	/**
	 * invoke a method (preInstall() or postInstall()) in interceptor class
	 * 
	 * @param baseDir
	 *            target project folder to install a plugin
	 * @param pluginName
	 *            a target plugin name to install
	 * @param pluginJar
	 *            plugin binary file
	 * @param interceptor
	 *            interceptor class for installing a plugin
	 * @param interceptorMethodName
	 *            interceptor method
	 */
	@SuppressWarnings("unchecked")
	private void invokeInterceptor(String baseDir, String pluginName,
			File pluginJar, Class interceptor, String interceptorMethodName)
			throws Exception {
		if (interceptor != null) {
			getLogger().debug(
					"Before calling invokeInterceptor() of "
							+ interceptor.getName());
			try {
				ObjectUtil.invokeMethod(interceptor, interceptorMethodName,
						baseDir, pluginJar);
				getLogger().debug(
						"Invoke " + interceptor + "." + interceptorMethodName
								+ "() of plugin '" + pluginName
								+ "' successfully.");
			} catch (Exception e) {
				getLogger().warn(
						"Invoking of a " + interceptor.getName()
								+ " is skipped. The reason is a '"
								+ e.getMessage() + "'.");
				return;
			}
		}
	}

	/**
	 * get a interceptor class of a target plugin
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginjarLoader
	 *            classloader for loading plugin libraries
	 * @param pluginInfo
	 *            plugin detail information
	 * @param pluginJar
	 *            plugin binary file
	 * @param pluginZip
	 *            zip file includes plugin binary file
	 * @return interceptor class
	 */
	@SuppressWarnings("unchecked")
	private Class getInterceptor(ArchetypeGenerationRequest request,
			ClassLoader pluginJarLoader, PluginInfo pluginInfo, File pluginJar,
			ZipFile pluginZip) throws Exception {
		Class interceptor = null;
		if (pluginInfo.getInterceptor() != null) {

			URL[] originalUrls = ((URLClassLoader) pluginJarLoader).getURLs();
			Model model = pluginPomManager.readPom(pluginZip);

			URLClassLoader interceptorLoader = pluginArtifactManager
					.makeArtifactClassLoader(request, model.getGroupId(),
							model.getArtifactId(), model.getVersion(),
							originalUrls);

			interceptor = ObjectUtil.loadClass(interceptorLoader, pluginJar,
					pluginInfo.getInterceptor().getClassName().trim());
		}

		return interceptor;
	}

	/**
	 * check to proceed install/update plugins
	 * 
	 * @param visitedPlugins
	 *            install/update plugins
	 * @param isCLIMode
	 *            execution mode
	 * @return prompt message
	 */
	public String checkInstall(Map<String, TargetPluginInfo> visitedPlugins,
			boolean isCLIMode) throws Exception {
		// for testing
		if (test) {
			return "";
		}

		StringBuffer queryBuffer = new StringBuffer();

		Collection<TargetPluginInfo> visitedPluginValues = visitedPlugins
				.values();

		queryBuffer
				.append("------------------------------------------------------------------------------ \n");
		Formatter installPluginInfoFormatter = new Formatter();
		installPluginInfoFormatter.format(CommonConstants.INSTALL_PLUGINS,
				"<action>", "<name>", "<version>");
		queryBuffer.append(installPluginInfoFormatter.toString() + "\n");

		for (TargetPluginInfo visitedPluginInfo : visitedPluginValues) {
			boolean isUpdate = visitedPluginInfo.isUpdate();

			StringBuilder builder = new StringBuilder();
			Formatter pluginInfoFormatter = new Formatter(builder);
			pluginInfoFormatter.format(CommonConstants.INSTALL_PLUGINS,
					(isUpdate ? "Update" : "Install"), visitedPluginInfo
							.getPluginInfo().getName(),
					(isUpdate ? visitedPluginInfo.getInstalledVersion()
							+ " -> "
							+ visitedPluginInfo.getPluginInfo().getVersion()
							: visitedPluginInfo.getPluginInfo().getVersion()));
			queryBuffer.append(pluginInfoFormatter.toString() + "\n");
		}
		queryBuffer
				.append("------------------------------------------------------------------------------ \n");

		if (isCLIMode) {
			queryBuffer.append("Is this OK? (y, n)");

			String message = queryBuffer.toString();

			String answer = "";

			while (StringUtils.isEmpty(answer)
					|| !(answer.equalsIgnoreCase("n") || answer
							.equalsIgnoreCase("y"))) {
				answer = prompter.prompt(message);
			}

			if (answer.equalsIgnoreCase("n")) {
				System.out.println("\nUser canceled operation.\n");
				System.exit(0);
			}
		}

		return queryBuffer.toString();
	}

	/**
	 * check current project information
	 * 
	 * @param baseDir
	 *            target folder to install
	 * @return project properties
	 * 
	 */
	private PropertiesIO checkProject(String baseDir) throws Exception {
		getLogger().debug("Call checkProject() of DefaultPluginInstaller");

		// 1. check a project.mf file
		File metadataFile = new File(new File(baseDir)
				+ CommonConstants.METAINF, CommonConstants.METADATA_FILE);
		if (!metadataFile.exists()) {
			throw new CommandException("Can not find a '"
					+ metadataFile.getAbsolutePath()
					+ "' file. Please check a location of your project.");
		}

		PropertiesIO pio = new PropertiesIO(metadataFile.getAbsolutePath());

		// 2. check plugin-installed.xml file
		File pluginsXMLFile = new File(new File(baseDir)
				+ CommonConstants.METAINF,
				CommonConstants.PLUGIN_INSTALLED_FILE);

		if (!pluginsXMLFile.exists()) {
			throw new CommandException("Can not find a '"
					+ pluginsXMLFile.getAbsolutePath()
					+ "' file. Please check a location of your project.");
		}

		getLogger().debug("Current target directory is a " + baseDir);

		return pio;
	}

	/**
	 * initialize velocity runtime configuration
	 */
	public void initializeVelocity() throws Exception {
		// 1. initialize velocity engine
		velocity = new VelocityEngine();
		velocity.setProperty("runtime.log.logsystem.log4j.logger.level",
				"WARNING");
		velocity.setProperty("velocimacro.library", "");
		velocity.setProperty("resource.loader", "classpath");
		velocity.setProperty("classpath.resource.loader.class",
				"org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader");
		velocity.setProperty("runtime.log.logsystem.class",
				"org.apache.velocity.runtime.log.NullLogSystem");
		velocity.init();
	}

	/**
	 * set velocity context with input arguments
	 * 
	 * @param pio
	 *            properties in project.mf
	 * @param pomHandling
	 *            whether current project is based on maven or ant
	 * @param excludeSrc
	 *            whether plugin resources exclude source codes will be
	 *            installed
	 * @return velocity context
	 */
	private Context prepareVelocityContext(PropertiesIO pio,
			boolean pomHandling, boolean excludeSrc) throws Exception {
		getLogger().debug(
				"Call prepareVelocityContext() of DefaultPluginInstaller");

		// 1. initialize velocity context
		initializeVelocity();
		VelocityContext context = new VelocityContext();
		// 2.0 register escape tool (for escaping #, $, ", ', etc.)
		context.put("esc", new EscapeTool());

		// 2.1 put common properties into velocity context
		// context.put(CommonConstants.VELOCITY_SHARP, "#");
		// context.put(CommonConstants.VELOCITY_SHARP_BRACE, "#{");

		context.put("pomHandling", new Boolean(pomHandling));
		context.put("excludeSrc", new Boolean(excludeSrc));

		if (pio.readAllKeys().size() > 0) {
			// 2.2 read project properties
			String packageName = pio.readValue("package.name").trim();
			String pjtName = pio.readValue("project.name").trim();

			// 2.3 read db properties
			String dbType = pio.readValue("db.type").trim();
			String dbDriver = pio.readValue("db.driver").trim();
			String dbUrl = pio.readValue("db.url").trim();
			String dbUserId = pio.readValue("db.userId").trim();
			String dbPassword = pio.readValue("db.password").trim();

			// 2.4 put properties into velocity context
			context.put("package", packageName);
			context.put("artifactId", pjtName);
			context.put("dbType", dbType);
			context.put("dbDriver", dbDriver);
			context.put("dbUrl", dbUrl);
			context.put("dbUserId", dbUserId);
			context.put("dbPassword", dbPassword);

			getLogger().debug(
					"You may defined a package of sample project as '"
							+ packageName + "'.");
			getLogger()
					.debug("You may defined a pjtname as '" + pjtName + "'.");
		} else {
			getLogger().warn("Reading properties of a project.mf is skipped.");
		}

		return context;
	}

	/**
	 * check whether target plugin is installable
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param pluginName
	 *            a target plugin name to install
	 */
	private void checkTargetPlugin(ArchetypeGenerationRequest request,
			String pluginName) throws Exception {
		getLogger().debug("Call checkTargetPlugin() of DefaultPluginInstaller");
		Map<String, PluginInfo> essentialPlugins = pluginCatalogManager
				.getEssentialPlugins(request);

		if (!pluginName.equals(CommonConstants.CORE_PLUGIN)
				&& essentialPlugins.containsKey(pluginName)) {
			throw new CommandException(
					"Can't install '"
							+ pluginName
							+ "' by the piece. Plugin '"
							+ pluginName
							+ "' can be installed by installing a core plugin. Please try to install a core plugin.");
		}

	}

	/**
	 * get encoding of file content
	 * 
	 * @param encoding
	 *            file encoding style
	 * @return encoding style, if entered argument is null, return "UTF-8"
	 */
	private String getEncoding(String encoding) {
		return ((null == encoding) || "".equals(encoding)) ? "UTF-8" : encoding;
	}

	/**
	 * for testing update plugin version
	 * 
	 * @param test
	 *            test or not
	 */
	public void setTest(boolean test) {
		this.test = test;
	}
}
