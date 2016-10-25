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
package org.anyframe.ide.command.common.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an PluginInfo class. This class is a information for plugin.
 * 
 * @author SoYon Lim
 */
public class PluginInfo {

	// 1. basic information
	private String name;
	private String description;
	private String groupId;
	private String artifactId;
	private String version;
	private String latestVersion;

	private List<String> versions;
	private List<DependentPlugin> dependentPlugins;
	private List<PluginResource> resources;
	private PluginBuild build;

	private String samples;
	private PluginInterceptor interceptor;

	// 2. additional information
	private String essential;

	// 3. additional information for eclipse plugin ui
	private String checked;
	private String installed;
	private String customed;

	public PluginInfo() {

	}

	public PluginInfo(String groupId, String artifactId, String version) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(String latestVersion) {
		this.latestVersion = latestVersion;
	}

	public List<String> getVersions() {
		if (this.versions == null) {
			return new ArrayList<String>();
		}
		return this.versions;
	}

	public void setVersions(List<String> versions) {
		this.versions = versions;
	}

	public List<DependentPlugin> getDependentPlugins() {
		if (dependentPlugins == null) {
			return new ArrayList<DependentPlugin>();
		}
		return dependentPlugins;
	}

	public void setDependentPlugins(List<DependentPlugin> dependentPlugins) {
		this.dependentPlugins = dependentPlugins;
	}

	public List<PluginResource> getResources() {
		if (resources == null) {
			return new ArrayList<PluginResource>();
		}
		return resources;
	}

	public void setResources(List<PluginResource> resources) {
		this.resources = resources;
	}

	public boolean hasSamples() {
		return new Boolean(samples).booleanValue();
	}

	public void setSamples(String samples) {
		this.samples = samples;
	}

	public PluginInterceptor getInterceptor() {
		return interceptor;
	}

	public void setInterceptor(PluginInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	public PluginBuild getBuild() {
		return build;
	}

	public void setBuild(PluginBuild build) {
		this.build = build;
	}

	public boolean isEssential() {
		return new Boolean(essential).booleanValue();
	}

	public void setEssential(String essential) {
		this.essential = essential;
	}

	public boolean isChecked() {
		return new Boolean(checked).booleanValue();
	}

	public void setChecked(String checked) {
		this.checked = checked;
	}

	public boolean isInstalled() {
		return new Boolean(installed).booleanValue();
	}

	public void setInstalled(String installed) {
		this.installed = installed;
	}

	public boolean isCustomed() {
		if (customed == null || customed.equals("")) {
			return false;
		}
		return new Boolean(customed).booleanValue();
	}

	public void setCustomed(String customed) {
		this.customed = customed;
	}

	public String toString() {
		return String.format(
				"[%s:groupId=%s, artifactId=%s, version=%s, latestVersion=%s]",
				name, groupId, artifactId, version, latestVersion);
	}

}
