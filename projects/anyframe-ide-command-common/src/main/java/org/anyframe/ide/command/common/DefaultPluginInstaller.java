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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
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
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
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

			Map<String, TargetPluginInfo> visitedPlugins = new HashMap<String, TargetPluginInfo>();

			analyzePluginDependencies(request, baseDir, pluginNames,
					pluginVersion, pluginJar, pluginInfoFromJar, visitedPlugins);

			if (visitedPlugins.size() > 0) {
				getLogger().info("Dependencies Resolved.");
				installPlugin(request, context, pio, baseDir, visitedPlugins,
						pluginJar, pluginInfoFromJar, encoding, pomHandling,
						isCLIMode);
			}
		} catch (Exception e) {
			if (e instanceof CommandException) {
				throw e;
			}
			getLogger().warn(
					"Error occurred in installing a '" + pluginName
							+ "' plugin. The reason is a '" + e.getMessage()
							+ "'.");
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
		getLogger().info("Resolving plugin dependencies ...");
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
						+ installPluginName + " "
						+ ((pluginVersion != null) ? pluginVersion : "")
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
					getLogger()
							.info(
									"'"
											+ pluginInfo.getName()
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

	private void installPlugin(ArchetypeGenerationRequest request,
			Context context, PropertiesIO pio, String baseDir,
			Map<String, TargetPluginInfo> visitedPlugins, File pluginJar,
			PluginInfo pluginInfoFromJar, String encoding, boolean pomHandling,
			boolean isCLIMode) throws Exception {

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
								springPlugin.getArtifactId(), springPlugin
										.getVersion());
		} else {
			// 2. uninstall
			if (visitedPlugins.get(CommonConstants.SPRING_PLUGIN) != null) {
				springPlugin = visitedPlugins
						.get(CommonConstants.SPRING_PLUGIN).getPluginInfo();
				springPluginJar = pluginInfoManager
						.getPluginFile(request, springPlugin.getGroupId(),
								springPlugin.getArtifactId(), springPlugin
										.getVersion());
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

		Collection<TargetPluginInfo> targetPlugins = visitedPlugins.values();
		for (TargetPluginInfo targetPluginInfo : targetPlugins) {
			installPlugin(request, context, pio, baseDir, targetPluginInfo
					.getPluginInfo().getName(), visitedPlugins, pluginJar,
					pluginInfoFromJar, encoding, pomHandling, springProperties);
		}
	}

	private void installPlugin(ArchetypeGenerationRequest request,
			Context context, PropertiesIO pio, String baseDir,
			String pluginName, Map<String, TargetPluginInfo> visitedPlugins,
			File pluginJar, PluginInfo pluginInfoFromJar, String encoding,
			boolean pomHandling, Properties pomProperties) throws Exception {
		File targetPluginJar = null;
		if (visitedPlugins.containsKey(pluginName)) {
			TargetPluginInfo targetPluginInfo = visitedPlugins.get(pluginName);
			if (targetPluginInfo.isUpdate()) {
				pluginUninstaller.uninstall(request, baseDir, pluginName, "",
						encoding, pomHandling, true);
			}

			if (pluginInfoFromJar != null) {
				if (targetPluginInfo.getPluginInfo().getName().equals(
						pluginInfoFromJar.getName())) {
					targetPluginJar = pluginJar;
				}
			}
			installPlugin(request, context, pio, baseDir, targetPluginInfo
					.getPluginInfo(), targetPluginJar, encoding, pomHandling,
					pomProperties);
		}
	}

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

				if (!isMatchedToDependentPlugin(request, pluginName,
						latestVersion, currentPluginInfo)) {
					throw new CommandException(
							"Can't resolve plugin dependencies. The reason is '"
									+ pluginName + " " + latestVersion
									+ "' plugin isn't matched to a '"
									+ currentPluginInfo.getName() + " "
									+ currentPluginInfo.getVersion()
									+ "' plugin.");
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
				String latestVersion = VersionComparator.getLatest(version,
						pluginSummary.getVersions());

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

	private boolean isMatchedToDependentPlugin(
			ArchetypeGenerationRequest request, String targetPluginName,
			String targetPluginVersion, PluginInfo dependentPluginInfo)
			throws Exception {
		String versionRange = getDependentPluginVersionRange(request,
				targetPluginName, targetPluginVersion, dependentPluginInfo
						.getName());

		if (VersionComparator.isMatched(versionRange, dependentPluginInfo
				.getVersion())) {
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
	 */
	@SuppressWarnings("unchecked")
	private void installPlugin(ArchetypeGenerationRequest request,
			Context context, PropertiesIO pio, String baseDir,
			PluginInfo pluginInfo, File pluginJar, String encoding,
			boolean pomHandling, Properties pomProperties) throws Exception {
		getLogger().debug("Call installPlugin() of DefaultPluginInstaller");

		// 1. check whether a target plugin can install
		// if (checkPlugin(request, baseDir, pluginInfo, encoding, pomHandling))
		// {

		// 2. download archetype jar from maven repository
		if (pluginJar == null) {
			pluginJar = pluginInfoManager.getPluginFile(request, pluginInfo
					.getGroupId(), pluginInfo.getArtifactId(), pluginInfo
					.getVersion());
		}

		// 3. get zipfile from plugin jar file
		ZipFile pluginZip = archetypeArtifactManager
				.getArchetypeZipFile(pluginJar);

		// 4. set classloader for VelocityComponent can load templates
		// inside plugin library
		ClassLoader pluginJarLoader = archetypeArtifactManager
				.getArchetypeJarLoader(pluginJar);
		Thread.currentThread().setContextClassLoader(pluginJarLoader);

		// 5. get interceptor class of a target plugin
		Class interceptor = getInterceptor(request, pluginJarLoader,
				pluginInfo, pluginJar, pluginZip);

		// 6. process to install
		invokeInterceptor(baseDir, pluginInfo.getName(), pluginJar,
				interceptor, "preInstall");
		process(request, context, pio, baseDir, pluginInfo, pluginJar,
				pluginZip, encoding, pomProperties);
		invokeInterceptor(baseDir, pluginInfo.getName(), pluginJar,
				interceptor, "postInstall");

		getLogger().info(
				"'" + pluginInfo.getName() + " " + pluginInfo.getVersion()
						+ "' plugin is installed successfully.");
		// }
	}

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
									.debug(
											"Templates directory ["
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
			getLogger().info(
					"Templates directory [" + templateHome
							+ "] is created successfully.");
		}
	}

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
						.debug(
								"Inspection resource directory ["
										+ inspectionHome
										+ "] already exists. So, Inspection resource installation is skipped.");
				return;
			}

			inspectionHomeDir.mkdirs();

			// 3. copy templates
			copyFile("inspection", inspectionHome);
		}
	}

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

				try {
					OutputStreamWriter converter = new OutputStreamWriter(
							new FileOutputStream(outputFile));
					IOUtil.copy(inputStream, converter, "UTF-8");
				} finally {
					IOUtil.close(inputStream);
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
	 */
	private void process(ArchetypeGenerationRequest request, Context context,
			PropertiesIO pio, String baseDir, PluginInfo pluginInfo,
			File pluginJar, ZipFile pluginZip, String encoding,
			Properties pomProperties) throws Exception {
		// 1. get all file names from archetypeFile
		List<String> fileNames = FileUtil.resolveFileNames(pluginJar);

		File targetDir = new File(baseDir);

		boolean pomHandling = ((Boolean) context.get("pomHandling"))
				.booleanValue();

		// 2. process dependent libraries
		if (pomHandling) {
			// 2.1 process pom file
			processPom(targetDir, pluginZip, fileNames);
		} else {
			// 2.2 copy dependent libraries to target folder
			String projectType = pio.readValue(CommonConstants.PROJECT_TYPE);
			processDependencyLibs(request, projectType, targetDir, pluginJar,
					pomProperties);
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
			processTemplates(context, targetDir, pluginInfo, pluginZip,
					fileNames, encoding);

			// 4. process web.xml of current project
			processWebXMLFile(context, targetDir, pluginInfo.getName(),
					pluginJar, encoding);

			// 5. process transaction configuration file of current project
			processTransactionFile(targetDir, pluginInfo.getName());

			// 6. add a hyperlink to welcome file
			processWelcomeFile(targetDir, pluginInfo.getName(), pluginInfo
					.getVersion(), pluginJar, encoding);

			// 7. add a tiles definition
			processTiles(targetDir, pluginInfo.getName(), pluginJar, encoding);

			// 8. replace plugin resources by db type
			processDBResource((String) context.get("dbType"), targetDir,
					pluginInfo.getName(), pluginJar, fileNames);

			// 9. create custom table, insert data to DB
			processInitialData(pio, targetDir, pluginInfo.getName(), pluginZip,
					fileNames, encoding);
		}

		// 10. add installation information to plugin-installed.xml file,
		// plugin-build.xml file
		updateInstallationInfo(request, targetDir, pluginInfo, pluginInfo
				.getName(), excludeSrc);
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
					.warn(
							"Merging current pom file with that of plugin is skipped. The reason is a pom.xml in "
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
					.getInstalledPluginJars(request, targetDir
							.getAbsolutePath());
			Iterator<String> keyItr = installedPluginJars.keySet().iterator();
			while (keyItr.hasNext()) {
				String installedPluginName = keyItr.next();

				// 2.3 get installed dependencies
				List<Dependency> installedDependencies = pluginPomManager
						.getDependencies(installedPluginJars
								.get(installedPluginName), pomProperties);
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
																	.getClassifier()) + ".jar");
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
	 * change classpath entries
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            target project folder to install a plugin
	 * @param pomHandling
	 *            whether current project is based on maven or ant
	 */
	public void processClasspath(ArchetypeGenerationRequest request,
			File targetDir, PluginInfo pluginInfo, boolean pomHandling,
			Properties pomProperties) throws Exception {

		getLogger().debug(
				"Call processClasspathFile() of DefaultPluginInstaller");

		File classpath = new File(targetDir, ".classpath");
		if (!classpath.exists()) {
			getLogger()
					.warn(
							"'"
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
					installedPluginInfo.getGroupId(), installedPluginInfo
							.getArtifactId(), installedPluginInfo.getVersion());

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

		FileUtil.replaceFileContent(classpath,
				"<!--Add new classpathentry here-->", "</classpath>",
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
	 * @param pluginZip
	 *            zip file includes plugin binary file
	 * @param fileNames
	 *            resources in plugin jar file
	 * @param encoding
	 *            file encoding style
	 */
	private void processTemplates(Context context, File targetDir,
			PluginInfo pluginInfo, ZipFile pluginZip, List<String> fileNames,
			String encoding) throws Exception {
		getLogger().debug("Call processTemplates() of DefaultPluginInstaller");
		// 1. get fileset list
		List<PluginResource> pluginResources = pluginInfo.getResources();

		for (PluginResource pluginResource : pluginResources) {
			getLogger().debug(
					"Processing filesets in ["
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
			for (Exclude exclude : excludeResources) {
				excludes.add(exclude.getName());
			}

			// 2.3 scan resources
			List<String> templates = FileUtil.findFiles(fileNames,
					CommonConstants.PLUGIN_RESOURCES
							+ CommonConstants.fileSeparator
							+ pluginResource.getDir(), includes, excludes);

			// 3. make directory for copying plugin resource
			getOutput(context, targetDir, pluginResource.getDir(), "",
					pluginResource.isPackaged(),
					(String) context.get("package")).mkdirs();
			getLogger().debug("Copying fileset " + pluginResource);

			// 4. merge template and copy files to output directory
			processTemplate(context, targetDir, pluginZip, pluginResource
					.getDir(), (String) context.get("package"), pluginResource
					.isPackaged(), pluginResource.isFiltered(), templates,
					encoding);
			getLogger().debug("Copied " + templates.size() + " files");
		}
	}

	/**
	 * merge a plugin resource and copy a merged file to target folder
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
	 * @param templates
	 *            plugin resources
	 * @param encoding
	 *            file encoding style
	 */
	private void processTemplate(Context context, File targetDir,
			ZipFile pluginZip, String resourceDir, String packageName,
			boolean packaged, boolean filtered, List<String> templates,
			String encoding) throws Exception {
		Iterator<String> templateItr = templates.iterator();

		while (templateItr.hasNext()) {
			String template = templateItr.next();
			File output = getOutput(context, targetDir, resourceDir, template,
					packaged, packageName);
			output.getParentFile().mkdirs();

			copyFile(pluginZip, template, output);
			if (filtered) {
				mergeTemplate(context, template, output, encoding);
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
	 * change web.xml of current project (servlet definition, servlet-mapping,
	 * etc.)
	 * 
	 * @param context
	 *            velocity context
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginName
	 *            a target plugin name to install
	 * @param pluginJar
	 *            plugin binary file
	 * @param encoding
	 *            file encoding style
	 */
	private void processWebXMLFile(Context context, File targetDir,
			String pluginName, File pluginJar, String encoding)
			throws Exception {
		getLogger().debug("Call processWebXMLFile() of DefaultPluginInstaller");

		try {
			// 1. get a web.xml file
			File file = new File(targetDir,
					CommonConstants.SRC_MAIN_WEBAPP_WEBINF
							+ CommonConstants.WEB_XML_FILE);

			String contents = pluginInfoManager
					.readPluginResource(CommonConstants.PLUGIN_RESOURCES
							+ CommonConstants.SRC_MAIN_WEBAPP_WEBINF
							+ CommonConstants.WEB_XML_FILE, pluginJar, encoding);

			if (contents != null) {
				if (pluginName.equals(CommonConstants.CORE_PLUGIN)) {
					FileUtil
							.addFileContent(
									file,
									"<!--Add new configuration here-->",
									"<!--"
											+ pluginName
											+ "-configuration-START-->\n"
											+ contents
											+ "\n<!--"
											+ pluginName
											+ "-configuration-END-->\n<!--Add new configuration here-->",
									true);
				} else {
					FileUtil.addFileContent(file,
							"<!--Add new configuration here-->",
							"<!--Add new configuration here-->\n<!--"
									+ pluginName + "-configuration-START-->\n"
									+ contents + "\n<!--" + pluginName
									+ "-configuration-END-->", true);

				}

			}
		} catch (Exception e) {
			getLogger().warn(
					"Processing a web.xml of current project is skipped. The reason is a '"
							+ e.getMessage() + "'.");
		}
	}

	/**
	 * in case of installing hibernate plugin, change transaction configuration
	 * 
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginName
	 *            a target plugin name to install
	 */
	private void processTransactionFile(File targetDir, String pluginName)
			throws Exception {
		getLogger().debug(
				"Call processTransactionFile() of DefaultPluginInstaller");

		try {
			if (pluginName.equals(CommonConstants.HIBERNATE_PLUGIN)) {
				// 1. get a transaction configuration file
				File file = new File(targetDir,
						CommonConstants.SRC_MAIN_RESOURCES + "spring"
								+ CommonConstants.CONFIG_TX_FILE);
				FileUtil
						.replaceFileContent(
								file,
								"\"id=\"txManager\" class=\"org.springframework.jdbc.datasource.DataSourceTransactionManager\"",
								"\"id=\"txManager\" class=\"org.springframework.orm.hibernate3.HibernateTransactionManager\"");
				FileUtil
						.replaceFileContent(
								file,
								"<property name=\"dataSource\" ref=\"dataSource\"/>",
								"<property name=\"sessionFactory\" ref=\"sessionFactory\"/>");
			}
		} catch (Exception e) {
			getLogger()
					.warn(
							"Processing a context-transaction.xml of current project is skipped. The reason is a '"
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
			String separator = "menu";

			File file = new File(targetDir, filePath);

			String contents = pluginInfoManager.readPluginResource(
					CommonConstants.PLUGIN_RESOURCES + filePath, pluginJar,
					encoding);

			// 2. add menu to display installed plugin name
			if (!pluginName.equals(CommonConstants.CORE_PLUGIN)
					&& contents != null) {
				// 2.1 add sample link
				FileUtil.addFileContent(file, "<!--Add new " + separator
						+ " here-->", "<!--Add new " + separator
						+ " here-->\n<!--" + pluginName + "-" + separator
						+ "-START-->\n" + contents + "\n<!--" + pluginName
						+ "-" + separator + "-END-->", false);
			}

			if (!pluginName.equals(CommonConstants.CORE_PLUGIN)
					&& contents == null) {
				String menuName = pluginName.substring(0, 1).toUpperCase()
						+ pluginName.substring(1);

				FileUtil.addFileContent(file, "<!--Add new " + separator
						+ " here-->", "<!--Add new " + separator
						+ " here-->\n<!--" + pluginName + "-" + separator
						+ "-START-->\n" + "<font size=\"2\">- " + menuName
						+ " " + pluginVersion + "</font><br/>" + "\n<!--"
						+ pluginName + "-" + separator + "-END-->", false);
			}
		} catch (Exception e) {
			getLogger().warn(
					"Adding a menu into current project is skipped. The reason is a '"
							+ e.getMessage() + "'.");
		}
	}

	/**
	 * add a tiles information to tiles definition file
	 * 
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginName
	 *            a target plugin name to install
	 * @param pluginJar
	 *            plugin binary file
	 * @param encoding
	 *            file encoding style
	 */

	private void processTiles(File targetDir, String pluginName,
			File pluginJar, String encoding) throws Exception {
		getLogger().debug("Call processTiles() of DefaultPluginInstaller");

		try {
			String filePath = CommonConstants.SRC_MAIN_WEBAPP_WEBINF
					+ CommonConstants.TILES_XML_FILE;
			String separator = "tiles-definition";

			File file = new File(targetDir, filePath);

			String contents = pluginInfoManager.readPluginResource(
					CommonConstants.PLUGIN_RESOURCES + filePath, pluginJar,
					encoding);

			// 2. add menu to display installed plugin name
			if (!pluginName.equals(CommonConstants.TILES_PLUGIN)
					&& contents != null) {
				// 2.1 add sample link
				FileUtil.addFileContent(file, "<!--Add new " + separator
						+ " here-->", "<!--Add new " + separator
						+ " here-->\n<!--" + pluginName + "-" + separator
						+ "-START-->\n" + contents + "\n<!--" + pluginName
						+ "-" + separator + "-END-->", true);
			}
		} catch (Exception e) {
			getLogger().warn(
					"Adding a tiles definition into current project is skipped. The reason is a '"
							+ e.getMessage() + "'.");
		}
	}

	/**
	 * replace current configuration file based on db template in plugin
	 * 
	 * @param dbType
	 *            db type of a current project
	 * @param targetDir
	 *            target project folder to install a plugin
	 * @param pluginName
	 *            a target plugin name to install
	 * @param pluginJar
	 *            plugin binary file
	 * @param fileNames
	 *            resources in plugin jar file
	 */
	private void processDBResource(String dbType, File targetDir,
			String pluginName, File pluginJar, List<String> fileNames)
			throws Exception {
		getLogger().debug("Call processDBResource() of DefaultPluginInstaller");
		if (!dbType.equals("hsqldb")) {
			String path = CommonConstants.DB_RESOURCES
					+ CommonConstants.fileSeparator + dbType;
			List<String> dbResources = FileUtil.findFiles(fileNames, path,
					"**", null);

			for (int i = 0; i < dbResources.size(); i++) {
				String dbResourceTemplate = (String) dbResources.get(i);

				File dbResource = new File(targetDir, dbResourceTemplate
						.substring(path.length()));
				replaceDBResource(pluginName, pluginJar, dbResource,
						dbResourceTemplate);
			}
		}
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
					getLogger().info(
							"Executing db script of " + pluginName
									+ " plugin is skipped. The reason is "
									+ e.getMessage());
				} else {
					getLogger().info(
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
		if (dbResource.exists()) {
			// 1. find map includes replace string
			InputStream inputStream = pluginInfoManager.getPluginResource(
					dbResourceTemplate, pluginJar);

			Map<String, String> replaceStringMap = FileUtil.findReplaceRegion(
					inputStream, pluginName);

			// 2. find token to be replaced
			Map<String, String> tokenMap = FileUtil.findReplaceRegion(
					new FileInputStream(dbResource), pluginName);

			getLogger().debug("token size : " + tokenMap.size());
			getLogger()
					.debug("replaceString size : " + replaceStringMap.size());

			// 3. replace
			if (tokenMap.size() > 0) {
				Set<String> commentKeySet = tokenMap.keySet();
				Iterator<String> commentKeyItr = commentKeySet.iterator();

				while (commentKeyItr.hasNext()) {
					String commentKey = commentKeyItr.next();
					getLogger().debug("commentKey : " + commentKey);

					if (replaceStringMap.containsKey(commentKey)) {
						String startToken = "<!--" + pluginName + "-"
								+ commentKey + "-START-->";
						String endToken = "<!--" + pluginName + "-"
								+ commentKey + "-END-->";

						String value = startToken + "\n"
								+ replaceStringMap.get(commentKey) + "\n"
								+ endToken;
						FileUtil.replaceFileContent(dbResource, startToken,
								endToken, startToken + endToken, value, false);
					}
				}
			}
		}
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
		getLogger().debug("Call mergeTemplate() of DefaultPluginInstaller");
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
				end = interpolatedResult.indexOf(DELIMITER, start
						+ DELIMITER.length());

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
				getLogger().info(
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
			Model model = pluginPomManager.getPom(pluginZip);

			URLClassLoader interceptorLoader = pluginArtifactManager
					.makeArtifactClassLoader(request, model.getGroupId(), model
							.getArtifactId(), model.getVersion(), originalUrls);

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
				"action", "name", "version");
		queryBuffer.append(installPluginInfoFormatter.toString() + "\n");
		queryBuffer
				.append("------------------------------------------------------------------------------ \n");

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
		velocity
				.setProperty("classpath.resource.loader.class",
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
		context.put(CommonConstants.VELOCITY_SHARP, "#");
		context.put(CommonConstants.VELOCITY_SHARP_BRACE, "#{");

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
