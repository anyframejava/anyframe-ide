package org.anyframe.ide.command.common.plugin;

import java.io.Serializable;

public class TargetPluginInfo implements Serializable {
	private PluginInfo pluginInfo;
	private String installedVersion;
	boolean isUpdate = false;

	public TargetPluginInfo() {
	}

	public TargetPluginInfo(PluginInfo pluginInfo) {
		super();
		this.pluginInfo = pluginInfo;
	}

	public TargetPluginInfo(PluginInfo pluginInfo, String installedVersion,
			boolean isUpdate) {
		super();
		this.pluginInfo = pluginInfo;
		this.installedVersion = installedVersion;
		this.isUpdate = isUpdate;
	}

	public PluginInfo getPluginInfo() {
		return pluginInfo;
	}

	public void setPluginInfo(PluginInfo pluginInfo) {
		this.pluginInfo = pluginInfo;
	}

	public String getInstalledVersion() {
		return installedVersion;
	}

	public void setInstalledVersion(String installedVersion) {
		this.installedVersion = installedVersion;
	}

	public boolean isUpdate() {
		return isUpdate;
	}

	public void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}
}
