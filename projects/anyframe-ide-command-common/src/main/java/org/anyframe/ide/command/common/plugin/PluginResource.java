package org.anyframe.ide.command.common.plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginResource {
	private String dir;
	private boolean filtered;
	private boolean packaged;

	private List<Include> includes;
	private List<Exclude> excludes;

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public boolean isFiltered() {
		return filtered;
	}

	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}

	public boolean isPackaged() {
		return packaged;
	}

	public void setPackaged(boolean packaged) {
		this.packaged = packaged;
	}

	public List<Include> getIncludes() {
		if(this.includes == null){
			return new ArrayList<Include>();
		}		
		return includes;
	}

	public void setIncludes(List<Include> includes) {
		this.includes = includes;
	}

	public List<Exclude> getExcludes() {
		if(this.excludes == null){
			return new ArrayList<Exclude>();
		}		
		return excludes;
	}

	public void setExcludes(List<Exclude> excludes) {
		this.excludes = excludes;
	}
}
