package org.anyframe.ide.command.common.plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginInterceptor {

	private String className;
	private List<PluginInterceptorDependency> dependencies;

	public List<PluginInterceptorDependency> getDependencies() {
		if(this.dependencies == null){
			return new ArrayList<PluginInterceptorDependency>();
		}	
		return dependencies;
	}

	public void setDependencies(List<PluginInterceptorDependency> dependencies) {
		this.dependencies = dependencies;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
