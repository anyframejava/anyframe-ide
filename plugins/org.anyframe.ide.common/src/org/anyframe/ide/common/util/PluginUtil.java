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
package org.anyframe.ide.common.util;

import java.io.File;

import org.anyframe.ide.common.CommonActivator;
import org.anyframe.ide.common.Constants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * This is PluginUtil class.
 * 
 * @author Sujeong Lee
 */
public class PluginUtil {
	public static String getProjectPath(IJavaProject project) {
		project.getPath().makeAbsolute().toString();
		IPath location = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(project.getPath()).getLocation();
		return location.toString();
	}

	public static String getProjectPath(IProject project) {
		if (project == null) {
			return null;
		}

		try {
			return PluginUtil
					.getProjectPath(getJavaProjectFromProject(project));
		} catch (CoreException e) {
			PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, e.getMessage());
		}

		return null;
	}

	public static IJavaProject getJavaProjectFromProject(IProject project)
			throws CoreException {
		return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
	}
	
	/**
	 * template home 경로를 리턴한다.
	 * @param project
	 * @return
	 */
	public static String getTemplateHomePath(IProject project){
		String projectLocation = project.getLocation().toOSString();
		String metaFile = projectLocation + Constants.METAINF + Constants.METADATA_FILE;
		File f = new File(metaFile);
		String property = null;
		if (f.exists()) {
			PropertyUtil propertyUtil = new PropertyUtil(metaFile);
			property = propertyUtil.getProperty(Constants.PROJECT_TEMPLATE_HOME);
			
		}
		return property;
	}
}
