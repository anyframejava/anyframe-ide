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
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.anyframe.ide.common.CommonActivator;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.messages.Message;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;

/**
 * This is PluginUtil class.
 * 
 * @author Sujeong Lee
 */
public class ProjectUtil {

	public static String anyframeHome = System.getenv("ANYFRAME_HOME");
	public static String projectHome = "./";

	protected ProjectUtil() {
		throw new UnsupportedOperationException(); // prevents calls from
													// subclass
	}

	public static IProject findProject(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projectlist = workspace.getRoot().getProjects();

		IProject project = null;
		for (int i = 0; i < projectlist.length; i++) {
			if (projectlist[i].getName().equals(projectName)) {
				project = projectlist[i];
			}
		}

		return project;
	}

	public static void enableNature(String projectName) {
		// TODO
		// dependency with m2eclipse...
	}

	public static Properties getProjectProperties(IProject currentProject)
			throws Exception {
		String projectLocation = currentProject.getLocation().toOSString();
		return getProjectProperties(projectLocation);
	}

	public static Properties getProjectProperties(String projectLocation)
			throws Exception {
		PropertyUtil appProps = new PropertyUtil(projectLocation
				+ Constants.METAINF + Constants.METADATA_FILE);
		return appProps.getProperties();
	}

	public static boolean validateName(String validateName) {
		Pattern p = Pattern
				.compile("^[a-zA-Z_0-9-]*+(\\.[a-zA-Z_0-9-][a-zA-Z_0-9-]*)*$");
		Matcher m = p.matcher(validateName);
		if (m.matches()) {
			return true;
		}
		return false;
	}

	public static boolean validatePkgName(String validatePkgName) {
		Pattern p = Pattern
				.compile("^[a-zA-Z_]+[a-zA-Z_0-9]*+(\\.[a-zA-Z_][a-zA-Z_0-9]*)*$");
		Matcher m = p.matcher(validatePkgName);
		
		IStatus sta = JavaConventions.validatePackageName(validatePkgName, null, null);
		
		if (m.matches() && sta.isOK()) {
			return true;
		}
		return false;
	}

	public static boolean existPath(String loc) {
		File path = new File(loc);
		return path.exists();
	}

	public static void refreshProject(String projectName) {
		try {

			IProject project = findProject(projectName);
			project.refreshLocal(IResource.DEPTH_INFINITE, null);

			// IWorkspace workspace =
			// ResourcesPlugin.getWorkspace();
			// workspace.getRoot().getProject(projectName)
			// .refreshLocal(IResource.DEPTH_INFINITE,
			// null);

		} catch (CoreException e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID,
					Message.exception_log_refresh, e);
		}
	}
	
	public static boolean validateComment(String str) {
		String newString = str.replaceAll("\\\\u002[Ff]", "/"); //$NON-NLS-1$//$NON-NLS-2$
		newString = newString.replaceAll("\\\\u002[Aa]", "*"); //$NON-NLS-1$//$NON-NLS-2$
		return newString.indexOf("*/") < 0; //$NON-NLS-1$
	}
}
