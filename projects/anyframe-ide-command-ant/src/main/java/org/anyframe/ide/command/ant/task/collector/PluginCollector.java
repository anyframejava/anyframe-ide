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
package org.anyframe.ide.command.ant.task.collector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.anyframe.ide.command.ant.task.container.PluginContainer;
import org.anyframe.ide.command.common.DefaultPluginCatalogManager;
import org.anyframe.ide.command.common.DefaultPluginInfoManager;
import org.anyframe.ide.command.common.PluginCatalogManager;
import org.anyframe.ide.command.common.PluginInfoManager;
import org.anyframe.ide.command.common.catalog.ArchetypeCatalogDataSource;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.maven.archetype.catalog.Archetype;

/**
 * This is an PluginCollector class. This class is for collecting plugin-list,
 * archetype-list, etc.
 * 
 * @author Jeryeon Kim
 */
public class PluginCollector {

	private String anyframeHome;
	PluginContainer container;

	/**
	 * initialize PlexusContainer and set repository based on settings.xml
	 * 
	 * @param anyframeHome
	 *            anyframe home includes repositories, ant, templates, etc.
	 * @param offline
	 *            offline mode
	 */
	public PluginCollector(String anyframeHome, String baseDir, boolean offline) {
		this.container = new PluginContainer(anyframeHome, baseDir, offline);
		this.anyframeHome = anyframeHome;
	}

	/**
	 * get latest archetype version
	 * 
	 * @param archetypeArtifactId
	 *            artifact id of a specific archetype
	 * @return latest version
	 */
	public String getLatestArchetypeVersion(String archetypeArtifactId)
			throws Exception {
		ArchetypeCatalogDataSource archetypeCatalogDataSource = (ArchetypeCatalogDataSource) container
				.lookup(ArchetypeCatalogDataSource.ROLE);

		return archetypeCatalogDataSource.getLatestArchetypeVersion(container
				.getRequest(), archetypeArtifactId,
				CommonConstants.PROJECT_BUILD_TYPE_ANT, this.anyframeHome);
	}

	/**
	 * get archetype version list
	 * 
	 * @param groupId
	 *            group id of a specific archetype
	 * @param archetypeArtifactId
	 *            artifact id of a specific archetype
	 * @return archetype versions
	 */
	public List<String> getArchetypeVersions(String groupId,
			String archetypeArtifactId) throws Exception {
		ArchetypeCatalogDataSource archetypeCatalogDataSource = (ArchetypeCatalogDataSource) container
				.lookup(ArchetypeCatalogDataSource.ROLE);

		Map<String, Archetype> archetypes = archetypeCatalogDataSource
				.getArchetypeVersions(container.getRequest(),
						CommonConstants.PROJECT_BUILD_TYPE_ANT,
						this.anyframeHome);

		List<String> archetypeVersions = new ArrayList<String>();
		for (Iterator<Archetype> i = archetypes.values().iterator(); i
				.hasNext();) {
			Archetype archetype = (Archetype) i.next();
			if (archetype.getGroupId().equals(groupId)
					&& archetype.getArtifactId().equals(archetypeArtifactId))
				archetypeVersions.add(archetype.getVersion());
		}

		return archetypeVersions;
	}

	/**
	 * get plugins with installation information
	 * 
	 * @param baseDir
	 *            project folder
	 * @return all plugins
	 */
	public Map<String, PluginInfo> getPlugins(String baseDir) throws Exception {
		PluginInfoManager pluginInfoManager = (PluginInfoManager) container
				.lookup(DefaultPluginInfoManager.class.getName());
		return pluginInfoManager.getPluginsWithInstallInfo(container
				.getRequest(), baseDir);
	}

	/**
	 * get installed plugins
	 * 
	 * @param baseDir
	 *            project folder
	 * @return installed plugins
	 */
	public Map<String, PluginInfo> getInstalledPlugins(String baseDir)
			throws Exception {
		PluginInfoManager pluginInfoManager = (PluginInfoManager) container
				.lookup(DefaultPluginInfoManager.class.getName());
		return pluginInfoManager.getInstalledPlugins(baseDir);
	}

	/**
	 * get plugin's names
	 * 
	 * @return plugin's names
	 */
	public Set<String> getPluginNames() throws Exception {
		PluginCatalogManager pluginCatalogManager = (PluginCatalogManager) container
				.lookup(DefaultPluginCatalogManager.class.getName());

		Map<String, PluginInfo> plugins = pluginCatalogManager
				.getPlugins(container.getRequest());

		return plugins.keySet();
	}

	public PluginContainer getContainer() {
		return this.container;
	}
}
