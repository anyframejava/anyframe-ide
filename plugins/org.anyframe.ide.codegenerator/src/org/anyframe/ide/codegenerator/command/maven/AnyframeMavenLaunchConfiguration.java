/*   
 * Copyright 2002-2012 the original author or authors.   
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
package org.anyframe.ide.codegenerator.command.maven;

import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.command.vo.CommandVO;
import org.anyframe.ide.codegenerator.preferences.IdePreferencesPage;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.command.cli.util.CommandUtil;
import org.anyframe.ide.command.maven.mojo.GenerateCodeMojo;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * This is an AnyframeMavenLaunchConfiguration class.
 * 
 * @author Sooyeon Park
 */
public class AnyframeMavenLaunchConfiguration {

	public static String MAVEN_LAUNCH_CONFIGURATION_TYPE = "org.anyframe.ide.codegenerator.command.maven.AnyframeMavenLaunchDelegate";

	public static String MAVEN_MAIN_TYPE = "org.codehaus.classworlds.Launcher";

	public static String ATTR_MAVEN_HOME = "org.anyframe.ide.eclipse.core.maven.home";

	public static String ATTR_COMMAND_VO = "org.anyframe.ide.eclipse.core.maven.command.vo";

	public static String ATTR_MESSAGE_DIALOG = "org.anyframe.ide.eclipse.core.message.dialog";

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	public ILaunchConfiguration createLaunchConfiguration(String goal,
			CommandVO vo) {
		try {
			GenerateCodeMojo mm = null;
			String projectName = vo.getProjectName();
			String basedir = vo.getBasedir();
			IProject project = ProjectUtil.findProject(projectName);

			ILaunchManager launchManager = DebugPlugin.getDefault()
					.getLaunchManager();
			ILaunchConfigurationType launchConfigurationType = launchManager
					.getLaunchConfigurationType(MAVEN_LAUNCH_CONFIGURATION_TYPE);

			String executeCommand = "Executing [mvn " + goal + "] in "
					+ basedir;
			ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType
					.newInstance(null, vo.getCommand());
			PluginLoggerUtil.info(ID, "[COMMAND] " + executeCommand);

			// set attributes
			List<CommandVO> voList = new ArrayList<CommandVO>();
			voList.add(vo);
			workingCopy.setAttribute(ATTR_COMMAND_VO, voList);
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
					MAVEN_MAIN_TYPE);
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					basedir);
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					projectName);
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
					goal);

			StringBuffer sb = new StringBuffer();
			String mavenHome = CodeGeneratorActivator.getDefault()
					.getPreferenceStore()
					.getString(IdePreferencesPage.MAVEN_HOME);

			// maven.home
			mavenHome = quote(mavenHome);
			sb.append(" -Dmaven.home=").append(mavenHome);
			workingCopy.setAttribute(ATTR_MAVEN_HOME, mavenHome);

			// m2.conf
			sb.append(" -Dclassworlds.conf=").append(
					quote(mavenHome + "/bin/m2.conf"));

			// check environment variables MAVEN_OPTS
			String mavenOpts = System.getenv("MAVEN_OPTS");
			if (mavenOpts != null)
				sb.append(" ").append(quote(mavenOpts));

			String vmArgs = sb.toString();

			workingCopy
					.setAttribute(
							IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
							vmArgs);
			workingCopy.setAttribute(
					IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);

			if (vo.getCommand().equals(CommandUtil.CMD_CREATE_PROJECT)
					|| vo.getCommand().equals(
							CommandUtil.CMD_CHANGE_DB + " after creation"))
				workingCopy.setAttribute(ATTR_MESSAGE_DIALOG, false);

			IPath path = getJREContainerPath(project);
			if (path != null) {
				workingCopy
						.setAttribute(
								IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
								path.toPortableString());
			}
			return workingCopy;
		} catch (CoreException ex) {
			PluginLoggerUtil.error(ID,
					"failed to create maven launch configuration", ex);
		}
		return null;
	}

	private String quote(String string) {
		return string.indexOf(' ') > -1 ? "\"" + string + "\"" : string;
	}

	private IPath getJREContainerPath(IProject project) throws CoreException {
		if (project != null && project.hasNature(JavaCore.NATURE_ID)) {
			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				if (JavaRuntime.JRE_CONTAINER
						.equals(entry.getPath().segment(0))) {
					return entry.getPath();
				}
			}
		}
		return null;
	}

}
