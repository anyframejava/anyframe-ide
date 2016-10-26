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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.build.Location;
import org.anyframe.ide.querymanager.build.QMMarkerHelper;
import org.anyframe.ide.querymanager.dialogs.QueryIdDialog;
import org.anyframe.ide.querymanager.model.QMXMLTableTreeViewer;
import org.anyframe.ide.querymanager.model.QueryIdModel;
import org.anyframe.ide.querymanager.views.QMExplorerView;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;

/**
 * Open Query Mapping XML file from Java DAO class.
 *
 * @author Sujeong Lee
 * @since 2.2.0
 */
public class OpenClassInJavaEditor extends AbstractQueryActionDelegate {

	static private OpenClassInJavaEditor openClassInJavaEditor;
	private IFile file;
	String selectedText;
	private String className;

	public OpenClassInJavaEditor() {
		super();
		openClassInJavaEditor = this;
	}

	@Override
	public void run(IAction action) {

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
		selectedText = className;
		IProject project = file.getProject();

		try {
			file.deleteMarkers(QMMarkerHelper.QUERYNAV_DBLCLK_BOOK_MARKER_ID,
					false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			return;
		}

		HashMap daoLocationMap = QMExplorerView.getUsedQueryIDsLocationMap();
		HashSet projectDaoSet = (HashSet) daoLocationMap.get(project.getName());
		Iterator itr = projectDaoSet.iterator();
		boolean fileExists = false;
		ArrayList selectedTextList = new ArrayList();
		// Location loc = (Location) next;
		Location loc = new Location();
		QueryIdModel.getQueryIdList().clear();
		QueryIdModel queryIdModel = null;

		HashSet usedLoc = new HashSet();
		while (itr.hasNext()) {
			Object next = itr.next();
			loc = (Location) next;

			if (loc.getKey().equals(selectedText)) {
				if (isContain(usedLoc, loc)) {
				} else {
					usedLoc.add(loc);
					selectedTextList.add(loc);

					queryIdModel = new QueryIdModel();
					queryIdModel.setProjectName(project.getName());
					queryIdModel.setQueryFile(loc.getFile().getName());
					queryIdModel.setFilePath(loc
							.getFile()
							.getFullPath()
							.toString()
							.substring(
									0,
									loc.getFile().getFullPath().toString()
											.lastIndexOf('/') + 1));
					queryIdModel.setLoc(loc);
					queryIdModel.addToQueryIdModelList(queryIdModel);
				}
			}
		}

		if (selectedTextList.size() > 0) {
			if (selectedTextList.size() == 1) {
				loc = (Location) selectedTextList.get(0);
				IMarker marker = createMarker(file, loc.getCharStart(),
						loc.getCharEnd(),
						QMMarkerHelper.QUERYNAV_DBLCLK_BOOK_MARKER_ID,
						"Opening the Query from Query XML file.");

				IEditorPart editor = openEditor(loc.getFile(),
						"org.eclipse.jdt.ui.CompilationUnitEditor");

				IDE.gotoMarker(editor, marker);
			} else {
				QueryIdDialog queryIdDialog = new QueryIdDialog(PlatformUI
						.getWorkbench().getDisplay().getActiveShell(),
						selectedText);
				if (queryIdDialog.open() == Dialog.OK) {
					String queryFile = queryIdDialog.getQueryFile();
					String filePath = queryIdDialog.getFilePath();
					Iterator it = selectedTextList.iterator();
					while (it.hasNext()) {
						loc = (Location) it.next();
						Location selectLoc = queryIdDialog.getLoc();

						if (loc.getCharStart() == selectLoc.getCharStart()
								&& loc.getCharEnd() == selectLoc.getCharEnd()) {
							break;
						}
					}
					IMarker marker = createMarker(file, loc.getCharStart(),
							loc.getCharEnd(),
							QMMarkerHelper.QUERYNAV_DBLCLK_BOOK_MARKER_ID,
							"Opening the Query from Query XML file.");

					IEditorPart editor = openEditor(loc.getFile(),
							"org.eclipse.jdt.ui.CompilationUnitEditor");

					IDE.gotoMarker(editor, marker);
				}
			}
		} else {
			MessageDialog.openWarning(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					"Java Class does not exist", "Selected Query ID \""
							+ selectedText
							+ "\" does not used in any java file.");
		}
	}

	private boolean isContain(HashSet usedLoc, Location loc) {
		boolean result = false;
		if (usedLoc != null) {
			Iterator itr = usedLoc.iterator();
			while (itr.hasNext()) {
				Object next = itr.next();
				Location uLoc = (Location) next;

				if (uLoc.getKey().equalsIgnoreCase(loc.getKey())
						&& uLoc.getFile().getName()
								.equalsIgnoreCase(loc.getFile().getName())
						&& uLoc.getCharStart() == loc.getCharStart()
						&& uLoc.getCharEnd() == loc.getCharEnd()) {
					result = true;
					break;
				} else {
					result = false;
				}
			}
			return result;
		} else {
			result = false;
			return result;
		}
	}

	// });
	// } catch (InvocationTargetException e) {
	// 		return;
	// } catch (InterruptedException e) {
	// 		return;
	// }
	//
	// selectedText = null;
	// }

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.action = action;
		this.action.setEnabled(false);

		if (targetEditor == null || selection == null) {

			return;
		} else {
			textSelection = null;
			if (selection instanceof ITextSelection) {
				textSelection = (TextSelection) selection;
				if (textSelection.getLength() != 0) {
					action.setEnabled(true);
				} else {
					action.setEnabled(false);
				}
			} else {
				action.setEnabled(false);
			}
		}
	}

	public static IMarker createMarker(IFile file, int charStart, int charEnd,
			String type, String msg) {
		IMarker marker = null;
		try {
			marker = file.createMarker(type);

			// marker.setAttribute(IMarker.MESSAGE, msg);
			marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			marker.setAttribute(IMarker.CHAR_START, charStart);
			marker.setAttribute(IMarker.CHAR_END, charEnd);
			// marker.setAttribute(IMarker.SEVERITY, IMarker.BOOKMARK);
		} catch (CoreException e) {
		}
		return marker;
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

}
