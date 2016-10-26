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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.anyframe.ide.command.common.plugin.versioning.VersionComparator;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.common.DefaultPomManager;
import org.apache.maven.archetype.common.util.FileCharsetDetector;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.rits.cloning.Cloner;

/**
 * This is an DefaultPluginPomManager class. This class is for handling
 * pom-model, pom-file, etc. about a specified plugin.
 * 
 * @plexus.component 
 *                   role="org.anyframe.ide.command.common.DefaultPluginPomManager"
 * @author SoYon Lim
 */
public class DefaultPluginPomManager extends DefaultPomManager {
	/**
	 * @plexus.requirement 
	 *                     role="org.anyframe.ide.command.common.DefaultPluginInfoManager"
	 */
	PluginInfoManager pluginInfoManager;

	/**
	 * merge a current pom file with other pom file of a specified plugin
	 * 
	 * @param currentPom
	 *            current pom file
	 * @param newPom
	 *            other pom file of a specified plugin
	 * @return dependent libraries
	 */
	@SuppressWarnings("unchecked")
	public List mergePom(File currentPom, File newPom) throws IOException,
			XmlPullParserException {
		// 1. read a pom file
		Model currentModel = readPom(currentPom);
		Model newModel = readPom(newPom);

		// 2. merge
		mergeDependencies(currentModel, newModel);

		// 3. add properties
		Properties properties = newModel.getProperties();

		Set keySet = properties.keySet();
		if (!keySet.isEmpty()) {
			Iterator keyItr = keySet.iterator();
			while (keyItr.hasNext()) {
				String key = (String) keyItr.next();
				if (!currentModel.getProperties().contains(key)) {
					currentModel.getProperties().put(key,
							newModel.getProperties().getProperty(key));
				}
			}
		}

		// 4. write a merged information to current pom file
		writePom(currentModel, currentPom, currentPom);

		// 5. return dependencies to be appended
		return newModel.getDependencies();
	}

	/**
	 * find dependent libraries to be removed in current project. Although a
	 * plugin to be removed depends on a specific library, we doesn't add that
	 * library to remove-list, in case other installed plugin depends on that
	 * library except for foundation plugin
	 * 
	 * @param installedPluginJars
	 *            installed plugin list
	 * @param removePluginJar
	 *            plugin jar file to be removed
	 * @return dependencies to be removed
	 */
	@SuppressWarnings("unchecked")
	public List<Dependency> findRemovedDependencies(
			Map<String, File> installedPluginJars, File removePluginJar,
			Properties properties) throws Exception {
		// 1. read pom dependencies about installed plugins
		Map<String, Dependency> currentDependencies = getInstalledPluginDependencies(installedPluginJars);

		// 2. read pom dependencies to be removed
		Model removeModel = getPluginPom(removePluginJar);
		Map<String, Dependency> dependencyMap = convertDependencyList(removeModel
				.getDependencies());
		Iterator<String> dependencyItr = dependencyMap.keySet().iterator();

		// 3. check if it can be removed
		List<Dependency> removes = new ArrayList<Dependency>();

		while (dependencyItr.hasNext()) {
			String dependencyId = (String) dependencyItr.next();

			if (!currentDependencies.containsKey(dependencyId)) {
				Dependency dependency = (Dependency) dependencyMap
						.get(dependencyId);

				String version = dependency.getVersion();

				if (version.startsWith("$") && version.endsWith("}")) {
					version = version.substring(2, version.length() - 1);

					if (properties.containsKey(version)) {
						dependency.setVersion(properties.getProperty(version));
					}
				}

				removes.add(dependency);
			}
		}

		return removes;
	}

	/**
	 * compare dependency of base plugin with that of other installed plugin.
	 * find duplicated/different-version dependencies between base plugin and
	 * installed plugins.
	 * 
	 * @param installedPluginJars
	 *            installed plugin list
	 * @param basePluginJar
	 *            plugin jar file to be compared
	 * @return duplicated/different-version dependencies
	 */
	@SuppressWarnings("unchecked")
	public Map<Dependency, String> findDuplicatedDepencies(
			Map<String, File> installedPluginJars, File basePluginJar)
			throws Exception {
		// 1. read pom dependencies of base plugin
		Model baseModel = getPluginPom(basePluginJar);
		Map<String, Dependency> baseDependencyMap = convertDependencyList(baseModel
				.getDependencies());

		// 2. get dependencies of installed plugin
		Iterator<String> installedPluginItr = installedPluginJars.keySet()
				.iterator();
		Map<String, Map<String, Dependency>> installedDependencyMap = new HashMap<String, Map<String, Dependency>>();
		while (installedPluginItr.hasNext()) {
			String pluginName = installedPluginItr.next();
			Model model = getPluginPom(installedPluginJars.get(pluginName));
			Map<String, Dependency> dependencyMap = convertDependencyList(model
					.getDependencies());

			installedDependencyMap.put(pluginName, dependencyMap);
		}

		// 3. check duplicate/different-version
		Iterator<String> baseDependencyItr = baseDependencyMap.keySet()
				.iterator();

		Map<Dependency, String> duplicateDependencyMap = new HashMap<Dependency, String>();
		while (baseDependencyItr.hasNext()) {
			String key = baseDependencyItr.next();
			Dependency value = baseDependencyMap.get(key);

			Iterator<String> itr = installedDependencyMap.keySet().iterator();
			StringBuffer buffer = new StringBuffer();
			while (itr.hasNext()) {
				String pluginName = itr.next();
				Map<String, Dependency> map = installedDependencyMap
						.get(pluginName);
				if (map.containsKey(key)
						&& !value.getVersion()
								.equals(map.get(key).getVersion())) {
					buffer.append(pluginName + ",");
				}
			}

			if (!buffer.toString().equals("")) {
				duplicateDependencyMap.put(baseDependencyMap.get(key), buffer
						.toString());
			}
		}

		return duplicateDependencyMap;
	}

	/**
	 * find all dependent libraries to be removed in current project and rewrite
	 * a pom file
	 * 
	 * @param currentPom
	 *            current pom file
	 * @param removePluginJar
	 *            plugin jar file to be removed
	 */
	@SuppressWarnings("unchecked")
	public void removePomSpecificDependencies(File currentPom, File removePom)
			throws Exception {
		// 1. read pom dependencies to be removed
		Model removeModel = readPom(removePom);
		List<Dependency> dependencies = removeModel.getDependencies();

		// 2. write a pom file
		writePom(currentPom, dependencies);
	}

	/**
	 * find dependent libraries to be removed in current project and rewrite a
	 * pom file. if other installed pluin and remove plugin depends on libraries
	 * together, those will not be removed.
	 * 
	 * @param currentPom
	 *            current pom file
	 * @param installedPluginJars
	 *            installed plugin list
	 * @param removePluginJar
	 *            plugin jar file to be removed
	 */
	public void removePomDependencies(File currentPom,
			Map<String, File> installedPluginJars, File removePluginJar,
			Properties properties) throws Exception {

		// 1. find dependent libraries to be removed in current project.
		List<Dependency> removes = findRemovedDependencies(installedPluginJars,
				removePluginJar, properties);

		// 2. write pom file
		writePom(currentPom, removes);
	}

	/**
	 * remove dependencies in current pom file
	 * 
	 * @param currentPom
	 *            current pom file
	 * @param removes
	 *            dependencies to be removed
	 */
	@SuppressWarnings("unchecked")
	private void writePom(File currentPom, List<Dependency> removes)
			throws Exception {
		// 1. read pom dependencies about current sample project
		Model currentModel = readPom(currentPom);
		Map<String, Dependency> currentDependencyMap = convertDependencyList(currentModel
				.getDependencies());

		// 2. check if it can be removed
		for (int i = 0; i < removes.size(); i++) {
			Dependency dependency = (Dependency) removes.get(i);

			String dependencyId = dependency.getGroupId() + ":"
					+ dependency.getArtifactId();
			if (currentDependencyMap.containsKey(dependencyId))
				currentDependencyMap.remove(dependencyId);
		}

		currentModel
				.setDependencies(convertDependencyMap(currentDependencyMap));

		// 4. write pom file
		String newPom = currentPom.getAbsolutePath();
		FileUtil.deleteFile(currentPom);
		File initPom = new File(newPom);

		writePom(currentModel, initPom, initPom);
	}

	/**
	 * @Override read a pom file. We can't delete a pomFile because of model
	 *           object. so, we added logics about remove original model object
	 *           after cloning a model object.
	 * @param currentPom
	 *            current pom file
	 */
	public Model readPom(final File currentPom) throws IOException,
			XmlPullParserException {
		Model model;
		Reader pomReader = null;
		MavenXpp3Reader reader = null;
		try {
			FileCharsetDetector detector = new FileCharsetDetector(currentPom);

			String fileEncoding = detector.isFound() ? detector.getCharset()
					: "UTF-8";
			pomReader = new InputStreamReader(new FileInputStream(currentPom),
					fileEncoding);

			reader = new MavenXpp3Reader();
			model = reader.read(pomReader);

			if (StringUtils.isEmpty(model.getModelEncoding())) {
				model.setModelEncoding(fileEncoding);
			}

			try {
				// return (Model) ObjectUtils.copy(model);
				// refer http://robust-it.co.uk/clone
				Cloner cloner = new Cloner();
				return (Model) cloner.deepClone(model);
			} catch (Exception e) {
				throw new IOException(
						"Can not clone model object to another object.");
			}
		} finally {
			IOUtil.close(pomReader);
			pomReader = null;
			model = null;
		}
	}

	/**
	 * get a pom information about a specified plugin
	 * 
	 * @param pluginJar
	 *            plugin jar file
	 * @return pom information
	 */
	public Model getPluginPom(File pluginJar) throws Exception {
		// 1. return a pom information about a specified plugin
		InputStream stream = pluginInfoManager.getPluginResource(
				"plugin-resources/" + Constants.ARCHETYPE_POM, pluginJar);

		if (stream == null)
			stream = pluginInfoManager.getPluginResource("archetype-resources/"
					+ Constants.ARCHETYPE_POM, pluginJar);

		return readPom(stream);

	}
	
	/**
	 * get a pom information about a specified plugin
	 * 
	 * @param pluginJar
	 *            plugin jar file
	 * @return pom information
	 */
	public Model getPluginRemovePom(File pluginJar) throws Exception {
		// 1. return a pom information about a specified plugin
		
		InputStream stream = pluginInfoManager.getPluginResource(
				"plugin-resources/" + CommonConstants.ARCHETYPE_REMOVE_POM, pluginJar);
		if (stream == null)
			stream = pluginInfoManager.getPluginResource("archetype-resources/"
					+ CommonConstants.ARCHETYPE_REMOVE_POM, pluginJar);

		return readPom(stream);

	}

	/**
	 * read a pom file
	 * 
	 * @param pluginZip
	 *            plugin zip file
	 * @return model information related to current pom
	 */
	@SuppressWarnings("unchecked")
	public Model readPom(ZipFile pluginZip) throws Exception {
		Enumeration enumeration = pluginZip.entries();
		while (enumeration.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();

			String entry = zipEntry.getName();

			if (entry.startsWith("META-INF")
					&& entry.endsWith(Constants.ARCHETYPE_POM)) {
				ZipEntry resourceEntry = pluginZip.getEntry(entry);

				if (resourceEntry == null) {
					return null;
				}

				return readPom(pluginZip.getInputStream(resourceEntry));
			}
		}

		return null;
	}

	/**
	 * merge a current pom model with other pom model of a specified plugin
	 * 
	 * @param currentModel
	 *            model of current pom
	 * @param newModel
	 *            other model of a specified plugin
	 */
	@SuppressWarnings("unchecked")
	private void mergeDependencies(Model currentModel, Model newModel) {
		// 1. get current dependencies in pom.xml of project
		Map<String, Dependency> currentDependenciesByIds = convertDependencyList(currentModel
				.getDependencies());
		// 2. get new dependencies in plugin resource
		Map<String, Dependency> newDependenciesByIds = convertDependencyList(newModel
				.getDependencies());

		Iterator<String> generatedDependencyIds = newDependenciesByIds.keySet()
				.iterator();
		while (generatedDependencyIds.hasNext()) {
			String generatedDependencyId = generatedDependencyIds.next();
			// 3. check dependencies
			if (!currentDependenciesByIds.containsKey(generatedDependencyId)) {
				// 3.1 if current pom doesn't include new dependency
				currentModel.addDependency((Dependency) newDependenciesByIds
						.get(generatedDependencyId));
			} else {
				// 3.2 if current pom includes new dependency, update latest
				String currentVersion = currentDependenciesByIds.get(
						generatedDependencyId).getVersion();
				String newVersion = newDependenciesByIds.get(
						generatedDependencyId).getVersion();

				if (VersionComparator.greaterThan(newVersion, currentVersion)) {
					// 3.2.1 remove old dependency
					currentModel.removeDependency(currentDependenciesByIds
							.get(generatedDependencyId));
					// 3.2.2 add latest dependency
					currentModel.addDependency(newDependenciesByIds
							.get(generatedDependencyId));
				}
				getLogger().debug(
						"Replace a dependency with latest version  : "
								+ generatedDependencyId);
			}
		}
	}

	/**
	 * get all dependencies of installed plugins
	 * 
	 * @param installedPluginJars
	 *            installed plugin list
	 * @return all dependencies
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Dependency> getInstalledPluginDependencies(
			Map<String, File> installedPluginJars) throws Exception {
		Map<String, Dependency> dependencyMap = new HashMap<String, Dependency>();
		// 1. read a pom for removing and make dependencies map
		Iterator<String> pluginItr = installedPluginJars.keySet().iterator();
		while (pluginItr.hasNext()) {
			File pluginJar = (File) installedPluginJars.get(pluginItr.next());
			Model model = getPluginPom(pluginJar);
			dependencyMap
					.putAll(convertDependencyList(model.getDependencies()));
		}

		return dependencyMap;
	}

	/**
	 * convert dependency list to dependency map
	 * 
	 * @param dependencies
	 *            a list of depdendency list
	 * @return dependency map (key is groupId:artifactId)
	 */
	public Map<String, Dependency> convertDependencyList(
			List<Dependency> dependencies) {

		Map<String, Dependency> dependencyMap = new HashMap<String, Dependency>();

		for (Dependency dependency : dependencies) {
			dependencyMap.put(dependency.getGroupId() + ":"
					+ dependency.getArtifactId(), dependency);
		}
		return dependencyMap;
	}

	/**
	 * convert dependency map to dependency list
	 * 
	 * @param dependencyMap
	 *            a map of dependencies
	 * @return dependencies
	 */
	public List<Dependency> convertDependencyMap(
			Map<String, Dependency> dependencyMap) {
		List<Dependency> dependencies = new ArrayList<Dependency>();

		Iterator<String> keyItr = dependencyMap.keySet().iterator();

		while (keyItr.hasNext()) {
			Dependency dependency = (Dependency) dependencyMap
					.get((String) keyItr.next());
			dependencies.add(dependency);
		}

		return dependencies;
	}

	/**
	 * get compile scope dependencies about a specified plugin
	 * 
	 * @param pluginJar
	 *            plugin jar file
	 * @return dependencies
	 */
	@SuppressWarnings("unchecked")
	public List<Dependency> getCompileScopeDependencies(File pluginJar)
			throws Exception {
		Model model = getPluginPom(pluginJar);
		List<Dependency> dependencies = model.getDependencies();

		List<Dependency> results = new ArrayList<Dependency>();

		for (Dependency dependency : dependencies) {
			if (dependency.getScope() == null
					|| dependency.getScope().equals("")
					|| dependency.getScope().equals("compile")) {

				results.add(dependency);
			}
		}

		return results;
	}

	/**
	 * get all scope dependencies about a specified plugin
	 * 
	 * @param pluginJar
	 *            plugin jar file
	 * @return dependencies
	 */
	@SuppressWarnings("unchecked")
	public List<Dependency> getDependencies(File pluginJar,
			Properties properties) throws Exception {
		Model model = getPluginPom(pluginJar);
		model.setProperties(properties);
		List<Dependency> dependencies = model.getDependencies();
		List<Dependency> results = new ArrayList<Dependency>();

		for (Dependency dependency : dependencies) {
			String version = dependency.getVersion();

			if (version.startsWith("$") && version.endsWith("}")) {
				version = version.substring(2, version.length() - 1);

				if (properties.containsKey(version)) {
					dependency.setVersion(properties.getProperty(version));
				}
			}
			results.add(dependency);
		}

		return results;
	}

	/**
	 * get all scope dependencies about a specified plugin
	 * 
	 * @param pluginJar
	 *            plugin jar file
	 * @return dependences
	 */
	public List<Dependency> getDependencies(File pluginJar) throws Exception {
		return getDependencies(pluginJar, new Properties());
	}
}
