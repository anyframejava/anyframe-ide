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
package org.anyframe.ide.querymanager.editors;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.model.QMXMLTableTreeViewer;
import org.anyframe.ide.querymanager.parsefile.ParserHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;

/**
 * This is OpenVOClassInJavaEditor class.
 * 
 * @author Ganga.Bhavani
 * @author Pavanesh
 */
public class OpenVOClassInJavaEditor extends AbstractQueryActionDelegate {

	static private OpenVOClassInJavaEditor openVOQueryActionDelegate;

	String selectedText;

	private String className;

	private IFile file;

	public OpenVOClassInJavaEditor() {
		super();
		openVOQueryActionDelegate = this;
	}

	public void run(IAction action) {

		// get all the files of this project.
		IWorkbenchPage activePage = QueryManagerActivator.getDefault()
				.getActiveWorkbenchPage();
		IEditorPart part = activePage.getActiveEditor();

		IEditorInput input = targetEditor.getEditorInput();
		IFileEditorInput ifei = (IFileEditorInput) input;
		file = ifei.getFile();

		if (file == null) {
			return;
		}

		if (part instanceof QMEditor) {

			QMXMLTableTreeViewer tableTreeViewer = ((QMEditor) part)
					.getTableTreeViewer();
			if (tableTreeViewer != null
					&& tableTreeViewer.getSelection() instanceof TreeSelection) {
				TreeSelection treeSelected = (TreeSelection) tableTreeViewer
						.getSelection();
				Object element = treeSelected.getFirstElement();
				if (element != null && element instanceof ElementImpl) {
					ElementImpl impl = (ElementImpl) treeSelected
							.getFirstElement();
					className = impl.getAttribute("class");

				} else if (element != null && element instanceof AttrImpl) {
					AttrImpl impl = (AttrImpl) treeSelected.getFirstElement();
					className = impl.getValue();

				}
			}
		}

		if (className == null) {
			return;
		}
		selectedText = className.replace('.', '/');

		ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getShell());
		try {
			dialog.run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor arg0)
						throws InvocationTargetException, InterruptedException {
					// BuilderHelper builderHelper = new BuilderHelper();
					IProject project = file.getProject();
					Collection files = (Collection) ParserHelper.getInstance()
							.getAllFileInfo(project, project.getLocation());
					Iterator filesItr = files.iterator();
					String projectPath = project.getLocation().toString();
					boolean fileExists = false;
					while (filesItr.hasNext()) {
						String fileFromList = filesItr.next().toString();
						if (fileFromList.indexOf(selectedText) > 1) {
							String filePath = fileFromList.substring(
									projectPath.length() + 1,
									fileFromList.length());
							IFile fileToOpen = project.getFile(filePath);
							if (fileToOpen.getFileExtension().equals("java")) {
								openEditor(fileToOpen,
										"org.eclipse.jdt.ui.CompilationUnitEditor");
								fileExists = true;
								break;
							}

						}

					}
					if (!fileExists) {
						MessageDialog.openWarning(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(),
								"VO Class does not exist",
								"Selected VO Class \"" + selectedText
										+ "\" does not exist.");
					}

				}
			});
		} catch (InvocationTargetException e) {
			return;
		} catch (InterruptedException e) {
			return;
		}

		selectedText = null;
	}

	public IEditorPart openEditor(IFile file, String editorId) {
		// Open editor on new file.
		// String editorId = ;
		IWorkbenchWindow dw = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				IWorkbenchPage page = dw.getActivePage();
				if (page != null) {
					page.openEditor(new FileEditorInput(file), editorId, true);
					return page.getActiveEditor();
				}

			}
		} catch (PartInitException exception) {
			return null;
		}
		return null;
	}

	public static OpenVOClassInJavaEditor getOpenVOQueryActionDelegate() {
		return openVOQueryActionDelegate;
	}

	// private boolean selectTextSelection() {
	//
	// 		impl = getElementImplFromStructuredTextEditor();
	// 		if (impl == null) {
	// 			return false;
	// 		}
	//
	// 		if (impl.getTagName() != null
	// 			&& (impl.getTagName().equals("table") || impl.getTagName().equals(
	// 			"result"))) {
	//
	// 				IEditorPart part = getQueryMgrEditorPart();
	//
	// 				if (part instanceof QMEditor) {
	//
	// 				return true;
	// 				} else {
	// 				return false;
	// 				}
	// 		} else {
	// 			return false;
	// 		}
	//
	// }

	public void selectionChanged(IAction action, ISelection selection) {

		this.action = action;
		this.action.setEnabled(false);

		if (targetEditor == null || selection == null) {

			return;
		} else {
			textSelection = null;
			if (selection instanceof ITextSelection) {
				textSelection = (TextSelection) selection;

				if (textSelection != null) {
					action.setEnabled(true);
				} else {
					action.setEnabled(false);
				}
			} else {
				action.setEnabled(false);
			}
		}

	}
}
