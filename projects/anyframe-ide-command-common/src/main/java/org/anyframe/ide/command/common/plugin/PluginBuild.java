package org.anyframe.ide.command.common.plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginBuild {

	private List<Fileset> filesets;

	public List<Fileset> getFilesets() {
		if (this.filesets == null) {
			return new ArrayList<Fileset>();
		}
		return filesets;
	}

	public void setFilesets(List<Fileset> filesets) {
		this.filesets = filesets;
	}
}
