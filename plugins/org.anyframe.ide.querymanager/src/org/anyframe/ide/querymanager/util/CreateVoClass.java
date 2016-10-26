/*
 * Copyright 2008-2013 the original author or authors.
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
package org.anyframe.ide.querymanager.util;

import java.io.IOException;
import java.io.InputStream;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.messages.Message;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * the class CreateVoClass creates VO Class if it doesnt Exist
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class CreateVoClass {

	private IFile file;

	// AddQueryWizardPage addQueryWizardPage;
	//
	// private static final Logger LOGGER = Logger.getLogger(AddQueryWizardPage.class);

	/**
	 * creates VO Class if it doesnt Exist
	 * 
	 * @param project
	 *            Project Object to which SQL query XML belongs
	 * @param path
	 *            VO class path
	 * @param fileName
	 *            VO class name
	 * @param voClassInputStream
	 *            VO class input stream
	 * @param isVoexist
	 *            Indicates wether VO exist or not
	 * @return boolean
	 */
	public boolean createVO(IProject project, String path, String fileName,
			InputStream voClassInputStream, boolean isVoexist, String classType)
			throws CoreException {

		if (!path.equals("") && path != null) {
			// Create package/folder structer if it doesnt exist
			createFolder(project, path);
		}
		final IFile file = project.getFile(new Path(path + "/" + fileName));
		setFile(file);
		try {
			// check whether VO class exist or not
			if (file.exists()) {
				String msg = "org.anyframe.querymanager.eclipse.core.tableMappingWizard.vo.class";
				if ("RMC".equals(classType))
					msg = "org.anyframe.querymanager.eclipse.core.tableMappingWizard.rmc.class";
				// if (openConfirmationDailogue(Message.getProperty(msg)
				// 		+ " " +file.getName() + " "))
				// {
				// 		file.setContents(voClassInputStream, true, false, null);
				// 		return true;
				// }
				else
					return false;
			} else if (!file.exists() && isVoexist) {

			} else {
				// VO Not Exist and create new VO class
				file.create(voClassInputStream, false, null);
				return true;
			}
			voClassInputStream.close();
		} catch (IOException e) {
		} finally {
			try {
				voClassInputStream.close();
			} catch (IOException e) {
			}
		}
		return false;
	}

	public boolean createClass(IProject project, String path, String fileName,
			InputStream voClassInputStream, boolean isVoexist)
			throws CoreException {
		if (!path.equals("") && path != null) {
			// Create package/folder structer if it doesnt exist
			createFolder(project, path);
		}
		final IFile file = project.getFile(new Path(path + "/" + fileName));
		setFile(file);
		try {
			// check whether VO class exist or not
			if (file.exists()) {

				file.setContents(voClassInputStream, true, false, null);
				return true;

			} else if (!file.exists() && isVoexist) {

			} else {
				// VO Not Exist and create new VO class
				file.create(voClassInputStream, false, null);
				return true;
			}
			voClassInputStream.close();
		} catch (IOException e) {
		} finally {
			try {
				voClassInputStream.close();
			} catch (IOException e) {
			}
		}
		return false;
	}

	/**
	 * creates VO Class if it doesnt Exist
	 * 
	 * @param project
	 *            Project Object to which SQL query XML belongs
	 * @param path
	 *            VO class path
	 * @param fileName
	 *            VO class name
	 * @param voClassInputStream
	 *            VO class input stream
	 * @param isVoexist
	 *            Indicates wether VO exist or not
	 * @return boolean
	 */
	public boolean createTableMappinVO(IProject project, String path,
			String fileName, InputStream voClassInputStream, boolean isVoexist)
			throws CoreException {

		if (!path.equals("") && path != null) {
			// Create package/folder structer if it doesnt exist
			createFolder(project, path);
		}
		final IFile file = project.getFile(new Path(path + "/" + fileName));
		setFile(file);

		try {
			// check whether VO class exist or not
			if (file.exists() && isVoexist) {

				// VO Exist .OVerwrite it
				file.setContents(voClassInputStream, true, false, null);
				voClassInputStream.close();
				return true;

			} else if (file.exists() && !isVoexist) {
				// VO Exist.Dont do anything
				voClassInputStream.close();
				return false;
			} else if (isVoexist) {
				try {
					file.setContents(voClassInputStream, true, false, null);
					voClassInputStream.close();
				} catch (CoreException exception) {
					// LOGGER.error(exception.getMessage());
					PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
							Message.exception_sqlquery,
							exception);

				}

				return true;
			} else {

				try {
					// VO Not Exist and create new VO class
					file.create(voClassInputStream, false, null);
					openEditor(file);
					voClassInputStream.close();
					return true;
				} catch (ResourceException exception) {

					return false;
				}

			}

		} catch (IOException e) {
		}
		return false;
	}

	public void openEditor(IFile file) {
		// Open editor on new file.
		String editorId = "org.eclipse.jdt.ui.CompilationUnitEditor";
		if (file.getFileExtension().toLowerCase().equals("xml"))
			editorId = "org.anyframe.editor.xml.MultiPageEditor";
		IWorkbenchWindow dw = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				IWorkbenchPage page = dw.getActivePage();
				if (page != null) {
					IEditorPart editorPart = page.openEditor(
							new FileEditorInput(file), editorId, true);

				}

			}
		} catch (PartInitException exception) {
			return;
		}

	}

	/**
	 * Create package/folder structer if it doesnt exist
	 * 
	 * @param project
	 *            Project Object to which SQL query XML belongs
	 * @param path
	 *            VO class path
	 */
	public void createFolder(IProject project, String path)
			throws CoreException {

		IFolder voClassFolder = null;
		String[] pathArr = path.split("/");
		for (int i = 0; i < pathArr.length; i++) {
			if (i == 0)
				path = pathArr[i];
			else
				path = path + "/" + pathArr[i];
			voClassFolder = project.getFolder(path);
			if (!voClassFolder.exists()) {
				voClassFolder.create(false, true, null);
			}
		}
	}

	/**
	 * Get the absoulte path of VO class
	 * 
	 * @param srcFolder
	 *            VO class Source folder
	 * @param packageName
	 *            Vo class package name
	 * @param voClassName
	 *            VO class name
	 * @return absolutePathOfVoClass absoulte path of VO class
	 */
	public String getAbsolutePathOfVoClass(String srcFolder,
			String packageName, String voClassName) {

		packageName = packageName.replaceAll("\\.", "/");

		if (packageName.trim().equals(""))
			packageName = "";
		else
			packageName = "/" + packageName;

		String absolutePathOfVoClass = null;

		absolutePathOfVoClass = srcFolder + packageName + "/" + voClassName;
		// + "VO.java";

		return absolutePathOfVoClass;
	}

	public IFile getFile() {
		return file;
	}

	public void setFile(IFile file) {
		this.file = file;
	}

	public boolean openConfirmationDailogue(String voClassName) {
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		// String title =
		// 		Message.anyframe_querymanager_eclipse_core_tableMappingWizard_openDialogConfirm;
		// String message = voClassName + " " +
		// 		Message.anyframe_querymanager_eclipse_core_tableMappingWizard_openDialogMsg;
		String title = null;
		String message = null;
		if (MessageDialog.openConfirm(parent, title, message))
			return true;
		return false;

	}

}
