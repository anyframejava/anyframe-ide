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
package org.anyframe.ide.codegenerator.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.launchConfigurations.AntLaunchConfigurationMessages;
import org.eclipse.ant.internal.ui.launchConfigurations.AntLaunchShortcut;
import org.eclipse.ant.internal.ui.launchConfigurations.IAntLaunchConfigurationConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.progress.IProgressService;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.project.IProjectConfigurationManager;
import org.maven.ide.eclipse.project.ResolverConfiguration;

/**
 * This is an EclipseProjectUtil class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class ProjectUtil {
	public static String anyframeHome = System.getenv("ANYFRAME_HOME");
	public static final String SLASH = CommonConstants.fileSeparator;
	public static String projectHome = "." + SLASH;
	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	protected ProjectUtil() {
		throw new UnsupportedOperationException(); // prevents
		// calls
		// from
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
		try {
			IProject project = findProject(projectName);
			MavenPlugin plugin = MavenPlugin.getDefault();
			IProgressMonitor monitor = new NullProgressMonitor();

			ResolverConfiguration configuration = new ResolverConfiguration();
			// default : false
			// configuration.setIncludeModules(false);
			configuration.setActiveProfiles("");

			IProjectConfigurationManager configurationManager = plugin
					.getProjectConfigurationManager();
			configurationManager.enableMavenNature(project, configuration,
					monitor);

		} catch (CoreException ex) {
			PluginLoggerUtil.error(ID, ex.getMessage(), ex);
		}
	}

	public static PropertiesIO getProjectProperties(IProject currentProject)
			throws Exception {
		// get application location
		String projectLocation = currentProject.getLocation().toOSString();
		return getProjectProperties(projectLocation);
	}

	public static PropertiesIO getProjectProperties(String projectLocation)
			throws Exception {
		PropertiesIO appProps = new PropertiesIO(projectLocation
				+ CommonConstants.METAINF + CommonConstants.METADATA_FILE);
		return appProps;
	}

	public static String getProjectLocation(String projectLocation)
			throws Exception {
		PropertiesIO appProps = getProjectProperties(projectLocation);

		return appProps.readValue(CommonConstants.PROJECT_HOME);
	}

	public static List<String> getWizardProjectList(PropertiesIO appProps,
			boolean isAll) throws Exception {
		String projectName = appProps.readValue(CommonConstants.PROJECT_NAME);

		List<String> modules = new ArrayList<String>();
		modules.add(projectName);

		return modules;
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
			PluginLoggerUtil.error(ID, Message.exception_log_refresh, e);
		}
	}

	public static boolean importProject(final String path,
			final String projectName) {
		File projectDir = new File(path + SLASH + projectName);
		final ProjectRecord record = new ProjectRecord(projectDir, projectName);
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath locationPath = new Path(record.getProjectSystemFile()
				.getAbsolutePath());
		record.setDescription(workspace.newProjectDescription(projectName));
		if (Platform.getLocation().isPrefixOf(locationPath)) {
			record.getDescription().setLocation(null);
		} else {
			record.getDescription().setLocation(locationPath);
		}
		final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask("CreateExistingProjectTask", 1);
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					createExistingProject(record, new SubProgressMonitor(
							monitor, 1));
					// createExistingProject(record);
				} finally {
					monitor.done();
				}
			}
		};

		IWorkbench workbench = DebugUIPlugin.getDefault().getWorkbench();
		IProgressService progressService = workbench.getProgressService();
		try {
			progressService.run(true, true, op);
		} catch (InvocationTargetException e) {
			PluginLoggerUtil.error(ID, Message.view_exception_invoked, e);
		} catch (InterruptedException e) {
			PluginLoggerUtil.error(ID, Message.view_exception_interrupt, e);
		}
		return true;
	}

	private static boolean createExistingProject(final ProjectRecord record,
			IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		String projectName = record.getProjectName();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);

		IProject[] projects = workspace.getRoot().getProjects();
		if (projects != null && projects.length > 0) {
			for (IProject p : projects) {
				if (p.getName().equals(projectName)) {
					return false;
				}
			}
		}
		record.getDescription().setName(projectName);
		try {
			monitor.beginTask("CreateProjectsTask", 100);
			project.create(record.getDescription(), new SubProgressMonitor(
					monitor, 30));
			project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(
					monitor, 70));
		} catch (CoreException e) {
			PluginLoggerUtil.error(ID, Message.exception_log_openpjt, e);
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}

		return true;
	}

	public static void cleanAndBuildProjects() {
		final boolean cleanAll = true;
		final boolean buildAll = true;

		try {
			// batching changes ensures that autobuild
			// runs after cleaning
			PlatformUI.getWorkbench().getProgressService()
					.busyCursorWhile(new WorkspaceModifyOperation() {
						protected void execute(IProgressMonitor monitor)
								throws CoreException {
							if (cleanAll)
								ResourcesPlugin.getWorkspace().build(
										IncrementalProjectBuilder.CLEAN_BUILD,
										monitor);
						}
					});
			// see if a build was requested
			if (buildAll) {
				// start an immediate workspace build
				GlobalBuildAction build = new GlobalBuildAction(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow(),
						IncrementalProjectBuilder.INCREMENTAL_BUILD);
				build.run();
			}
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof CoreException)
				MessageDialogUtil.openMessageDialog(Message.ide_message_title,
						target.getMessage(), MessageDialog.ERROR);

			// message key is not exist
			// ExceptionUtil.showException(Message.view_dialog_error_build,
			// IStatus.ERROR, e);
			PluginLoggerUtil.error(ID, Message.ide_message_title, e);

		} catch (InterruptedException e) {
			PluginLoggerUtil.error(ID, Message.exception_log_cleanpjt, e);
		}
	}

	@SuppressWarnings({ "unchecked", "restriction" })
	public static ILaunchConfiguration createDefaultLaunchConfiguration(
			IPath filePath, Properties antProps, String target) {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager
				.getLaunchConfigurationType("org.eclipse.ant.AntLaunchConfigurationType");

		String name = AntLaunchShortcut.getNewLaunchConfigurationName(filePath,
				null, null);
		try {
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(
					null, name);
			workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION,
					filePath.toString());
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER,
					"org.eclipse.ant.ui.AntClasspathProvider");
			// // set log level
			// IPreferenceStore store =
			// AnyframeIDEPlugin.getDefault().getPreferenceStore();
			// String logLevel = store.getString(IdePreferencesPage.LOG_LEVEL);
			// String logOption = "-q";
			// if (logLevel.equals(CommonConstants.LOG_LEVEL_INFO))
			// logOption = "";
			// else if (logLevel.equals(CommonConstants.LOG_LEVEL_DEBUG))
			// logOption = "-debug";
			// workingCopy.setAttribute(
			// IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, logOption);
			// workingCopy.setAttribute(
			// IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
			// logOption);

			// set default for common settings
			CommonTab tab = new CommonTab();
			tab.setDefaults(workingCopy);

			IFile file = AntUtil.getFileForLocation(filePath.toString(), null);
			workingCopy.setMappedResources(new IResource[] { file });

			// add attributes to workingCopy
			workingCopy.setAttribute(
					IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
			Map properties = workingCopy.getAttribute(
					IAntLaunchConfigurationConstants.ATTR_ANT_PROPERTIES,
					new HashMap<Object, Object>());
			properties.putAll(antProps);
			workingCopy.setAttribute(
					IAntLaunchConfigurationConstants.ATTR_ANT_PROPERTIES,
					properties);
			workingCopy.setAttribute(
					IAntLaunchConfigurationConstants.ATTR_ANT_TARGETS, target);
			return workingCopy.doSave();
		} catch (Exception e) {
			PluginLoggerUtil.error(ID, MessageFormat.format(
					AntLaunchConfigurationMessages.AntLaunchShortcut_2,
					new Object[] { filePath.toString() }), e);
		}

		return null;
	}

	protected static void reportError(String message, Throwable throwable) {
		IStatus status = null;
		if (throwable instanceof CoreException) {
			status = ((CoreException) throwable).getStatus();
		} else {
			status = new Status(IStatus.ERROR, IAntUIConstants.PLUGIN_ID, 0,
					message, throwable);
		}
		ErrorDialog
				.openError(
						AntUIPlugin.getActiveWorkbenchWindow().getShell(),
						AntLaunchConfigurationMessages.AntLaunchShortcut_Error_7,
						AntLaunchConfigurationMessages.AntLaunchShortcut_Build_Failed_2,
						status);
	}

	public static IResource getSelectedResource(ISelection selection) {
		ArrayList<Object> resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList<Object>();
			if (selection instanceof IStructuredSelection) {
				Iterator<?> elements = ((IStructuredSelection) selection)
						.iterator();
				while (elements.hasNext()) {
					Object next = elements.next();
					if (next instanceof IResource) {
						resources.add(next);
						continue;
					}
					if (next instanceof IAdaptable) {
						IAdaptable adaptable = (IAdaptable) next;
						Object adapter = adaptable.getAdapter(IResource.class);
						if (adapter instanceof IResource) {
							resources.add(adapter);
							continue;
						}
					}
				}
			}
		}

		if (resources != null && !resources.isEmpty()) {
			IResource[] result = new IResource[resources.size()];
			resources.toArray(result);
			if (result.length >= 1)
				return result[0];
		}
		return null;
	}

	public static boolean validatePath(String loc) {
		File path = new File(loc);
		return path.isAbsolute();
	}

	public static boolean existPath(String loc) {
		File path = new File(loc);
		return path.exists();
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

	public static boolean validateComment(String str) {
		String newString = str.replaceAll("\\\\u002[Ff]", "/"); //$NON-NLS-1$//$NON-NLS-2$
		newString = newString.replaceAll("\\\\u002[Aa]", "*"); //$NON-NLS-1$//$NON-NLS-2$
		return newString.indexOf("*/") < 0; //$NON-NLS-1$
	}

	public static boolean isAnyframeProject(IProject currentProject) {

		String projectLocation = currentProject.getLocation().toOSString();
		File f = new File(projectLocation + CommonConstants.METAINF
				+ CommonConstants.METADATA_FILE);
		return f.exists();
	}
}
