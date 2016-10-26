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
package org.anyframe.ide.querymanager.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.build.BuilderHelper;
import org.anyframe.ide.querymanager.build.Location;
import org.anyframe.ide.querymanager.dialogs.QueryIdDialog;
import org.anyframe.ide.querymanager.messages.Message;
import org.anyframe.ide.querymanager.model.QueryIdModel;
import org.anyframe.ide.querymanager.preferences.AnyframePreferencePage;
import org.anyframe.ide.querymanager.preferences.PreferencesHelper;
import org.anyframe.ide.querymanager.util.AnyframeJarLoader;
import org.anyframe.ide.querymanager.util.BuilderUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

/**
 * This is OpenQueryIdInEditor class.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class OpenQueryIdInEditor implements IEditorActionDelegate,
		IWorkbenchWindowActionDelegate {

	Boolean isAbstractDAO = false;

	IEditorPart targetEditor;
	TextSelection textSelection;

	String selectedText;

	Location loc;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}

	public void run(IAction action) {

		// get all the queryIds of this project.
		IWorkbenchPage activePage = QueryManagerActivator.getDefault()
				.getActiveWorkbenchPage();
		IEditorPart part = activePage.getActiveEditor();
		IEditorInput input = part.getEditorInput();
		IFileEditorInput ifei = (IFileEditorInput) input;
		IFile file = ifei.getFile();
		IProject project = file.getProject();
		// Find QS or EQS

		// IProject[] projects =
		// ResourcesPlugin.getWorkspace().getRoot().getProjects();
		ArrayList queryIdInFiles = new ArrayList();
		QueryIdModel queryIdModel = null;
		QueryIdModel.getQueryIdList().clear();
		// get the selected text
		selectedText = textSelection == null ? "" : textSelection.getText();
		// int startLine = textSelection.getStartLine();
		// int endLine = textSelection.getEndLine();
		String content = BuilderUtil.readFile(file);
		String serviceType = findQSOrEqS(content);
		// String lineText = content.substring(startLine, endLine);
		String lineInformation = getTheLineInformation(content,
				textSelection.getOffset());

		// We have the line information.
		// Find out which method's query id we are highlighting.

		// 1. Query Servcie methods.
		// 2 AbstractDAO methods.
		String queryId = "";

		// 1. Find IQueryService
		boolean methodFound = false;

		String qsVar = "";

		if (!methodFound) {
			HashMap methodMap2 = BuilderUtil.getQsMethodMap();
			final String importClassFullyQualName = "org.anyframe.query.dao.AbstractDao";
			if (content.indexOf(importClassFullyQualName) != -1) {
				isAbstractDAO = true;
			} else {
				isAbstractDAO = false;
			}
			final String className = "AbstractDao";
			// BuilderHelper helper = new BuilderHelper();
			qsVar = BuilderUtil.getVarName(content, importClassFullyQualName,
					className);
			methodMap2 = BuilderUtil
					.appendVarToMethods(methodMap2, qsVar + ".");
			// methodMap.put("getQueryService()", "getQueryService()");

			Iterator methodItr2 = methodMap2.keySet().iterator();
			qsWhile: while (methodItr2.hasNext()) {
				String methodName = methodItr2.next().toString();
				if (lineInformation.indexOf(methodMap2.get(methodName)
						.toString()) > 0) {
					// prefixToken = preferencesMap.get(methodName).toString();
					queryId = textSelection.getText();
					methodFound = true;
					break qsWhile;
				}
			}
		}

		// Check whether technical service query Id......
		if (!methodFound) {
			AnyframeJarLoader loader = new AnyframeJarLoader();
			HashMap classMethodsMap = null;
			classMethodsMap = loader
					.getRuntimeProjectTechnicalServicesDetails(file
							.getProject());
			HashMap methodMap = new HashMap();
			HashMap methMapHolder = new HashMap();

			if (classMethodsMap != null && classMethodsMap.size() > 0) {
				Iterator classMethodMapIterator = classMethodsMap.keySet()
						.iterator();
				while (classMethodMapIterator.hasNext()) {
					// String className =
					// classMethodMapIterator.next().toString();
					final String importClassFullyQualName = classMethodMapIterator
							.next().toString();
					final String className = importClassFullyQualName
							.substring(importClassFullyQualName
									.lastIndexOf('.') + 1);
					qsVar = BuilderUtil.getVarName(content,
							importClassFullyQualName, className);
					if (qsVar != null) {
						Collection methodCollection = (Collection) classMethodsMap
								.get(importClassFullyQualName);
						Iterator methodCollectionItr = methodCollection
								.iterator();
						while (methodCollectionItr.hasNext()) {
							String methName = methodCollectionItr.next()
									.toString();
							methMapHolder.put(methName, methName);
						}
						// break;
					}
					methMapHolder = BuilderUtil.appendVarToMethods(
							methMapHolder, qsVar + ".");
					methMapHolder.put(className + ".", className + ".");
					methodMap.putAll(methMapHolder);
				}
				// got the class and methods.
				// methodMap = BuilderUtil.appendVarToMethods(methodMap, qsVar + ".");
				// methodMap.put(className+".", className+".");
			}
			Iterator methodItr = methodMap.keySet().iterator();
			while (methodItr.hasNext()) {
				String methodName = methodItr.next().toString();
				if (lineInformation.indexOf(methodMap.get(methodName)
						.toString()) > 0) {
					// prefixToken = preferencesMap.get(methodName).toString();
					methodFound = true;
					break;
				}
			}
		}
		if (!methodFound && lineInformation.indexOf("getQueryService().") > 0) {
			// queryId = textSelection.getText();
			methodFound = true;
		}
		if (methodFound)
			queryId = textSelection.getText();
		// If we have found the method till now, it means it is a query servcie
		// method
		if (!methodFound) {
			String prefixToken = null;
			String suffixToken = null;
			PreferencesHelper preferencesHelper = PreferencesHelper
					.getPreferencesHelper();
			HashMap preferencesMap = new HashMap();
			preferencesMap = preferencesHelper
					.populateHashMapWithPreferences(preferencesMap);
			HashMap mehtodMap = preferencesHelper
					.populateHashMapWithAbstractDAOMethodNames();
			String createMethod = mehtodMap.get(
					AnyframePreferencePage.CREATE_PREFIX_ID).toString();
			String findMethod = mehtodMap.get(
					AnyframePreferencePage.FIND_PREFIX_ID).toString();
			String removeMethod = mehtodMap.get(
					AnyframePreferencePage.REMOVE_PREFIX_ID).toString();
			String updateMethod = mehtodMap.get(
					AnyframePreferencePage.UPDATE_PREFIX_ID).toString();
			String findByPkMethod = mehtodMap.get(
					AnyframePreferencePage.FIND_BYPK_SUFFIX_ID).toString();
			String findListMethod = mehtodMap.get(
					AnyframePreferencePage.FIND_LIST_SUFFIX_ID).toString();

			if (lineInformation.indexOf(findByPkMethod) > 0) {
				methodFound = true;
				prefixToken = preferencesMap.get(
						AnyframePreferencePage.FIND_PREFIX_ID).toString();
				suffixToken = preferencesMap.get(
						AnyframePreferencePage.FIND_BYPK_SUFFIX_ID).toString();
			} else if (lineInformation.indexOf(findListMethod) > 0) {
				methodFound = true;
				prefixToken = preferencesMap.get(
						AnyframePreferencePage.FIND_PREFIX_ID).toString();
				suffixToken = preferencesMap.get(
						AnyframePreferencePage.FIND_LIST_SUFFIX_ID).toString();
			} else if (lineInformation.indexOf(createMethod) > 0) {
				methodFound = true;
				prefixToken = preferencesMap.get(
						AnyframePreferencePage.CREATE_PREFIX_ID).toString();
				suffixToken = null;
			} else if (lineInformation.indexOf(findMethod) > 0) {
				methodFound = true;
				prefixToken = preferencesMap.get(
						AnyframePreferencePage.FIND_PREFIX_ID).toString();
				suffixToken = null;
			} else if (lineInformation.indexOf(removeMethod) > 0) {
				methodFound = true;
				prefixToken = preferencesMap.get(
						AnyframePreferencePage.REMOVE_PREFIX_ID).toString();
				suffixToken = null;
			} else if (lineInformation.indexOf(updateMethod) > 0) {
				methodFound = true;
				prefixToken = preferencesMap.get(
						AnyframePreferencePage.UPDATE_PREFIX_ID).toString();
				suffixToken = null;
			} else {
				prefixToken = null;
				suffixToken = null;
			}
			if (methodFound) {
				queryId = textSelection.getText();
				if (isAbstractDAO) {
					if (prefixToken != null)
						queryId = prefixToken + queryId;
					if (suffixToken != null)
						queryId = queryId + suffixToken;
				} else {
				}
			}

		}

		if (methodFound)
			selectedText = queryId;
		else
			selectedText = textSelection.getText();
		// get the selected text
		// selectedText = textSelection == null ? "" :
		// textSelection.getText();
		// IMarker marker = null;
		// if (projects.length > 0) {
		Location loc = null;
		// for (int i = 0; i < projects.length; i++) {
		HashMap queryIds = null;
		// try {
		// if (projects[i].isOpen()
		// && projects[i].hasNature(JavaCore.NATURE_ID)) {
		try {
			queryIds = BuilderHelper.getInstance()
					.collectAllQueryIdsForProject(project);
			if (queryIds.get(selectedText) instanceof ArrayList) {
				Collection locCollection = (ArrayList) queryIds
						.get(selectedText);
				Iterator itr = locCollection.iterator();
				while (itr.hasNext() && locCollection.size() > 1) {
					loc = (Location) itr.next();
					String thisServiceType = loc.getServiceType();
					if (thisServiceType == null)
						thisServiceType = "queryService";
					if (serviceType.equalsIgnoreCase(thisServiceType)) {
						queryIdInFiles.add(loc);
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
						queryIdModel.addToQueryIdModelList(queryIdModel);
						// marker = createMarker(loc, IMarker.BOOKMARK,
						// 		"Duplictae Query Id:"+loc.key+"exists in the project");
					}
				}
			}
		} catch (NullPointerException npe) {
			// continue;
		} catch (Exception e) {
			// continue;
		}
		// }
		// } catch (CoreException e) {
		// 		System.out.println(" Project " + project.getName()
		// 			+ "is closed or it is not a Java Project.");
		// }
		if (queryIds != null && queryIds.get(selectedText) != null) {
			try {
				loc = queryIds.get(selectedText) instanceof Location ? (Location) queryIds
						.get(selectedText) : null;
				String thisServiceType = loc.getServiceType();
				if (thisServiceType == null)
					thisServiceType = "queryService";
				if (loc != null
						&& serviceType.equalsIgnoreCase(thisServiceType)) {
					queryIdInFiles.add(loc);
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
					queryIdModel.addToQueryIdModelList(queryIdModel);
				}
			} catch (NullPointerException npe) {
				// continue;
			}
		}
		// }
		if (queryIdInFiles.size() > 1) {
			QueryIdDialog queryIdDialog = new QueryIdDialog(PlatformUI
					.getWorkbench().getDisplay().getActiveShell(), selectedText);
			if (queryIdDialog.open() == Dialog.OK) {
				String queryFile = queryIdDialog.getQueryFile();
				String filePath = queryIdDialog.getFilePath();
				for (Iterator itr = queryIdInFiles.iterator(); itr.hasNext();) {
					loc = (Location) itr.next();
					if (queryFile.equals(loc.getFile().getName())
							&& filePath.equals(loc
									.getFile()
									.getFullPath()
									.toString()
									.substring(
											0,
											loc.getFile().getFullPath()
													.toString()
													.lastIndexOf('/') + 1)))
						break;
				}
				this.loc = loc;
				openInEditor(loc.file);
			}
		} else
			openInEditor(file);
		// } else {
		// 		openInEditor(file);
		// }
	}

	private String getTheLineInformation(String content, int offset) {
		int movingPointer = offset;
		while (true) {
			if (content.charAt(movingPointer) != '\n' && movingPointer > -1)
				movingPointer--;
			else
				break;
		}
		int startPos = movingPointer;
		int endLine = content.indexOf(";", startPos + 2);
		String lineInfo = content.substring(startPos, endLine);
		return lineInfo;
	}

	// check whether the file is QueryService file or
	// extended query service file
	private String findQSOrEqS(String content) {
		if (content.indexOf("extends AbstractDao") > 0) {
			return "extendedQueryService";
		} else {
			return "queryService";
		}
	}

	private void openInEditor(IFile file) {

		IProject project = file.getProject();

		HashMap queryIds = BuilderHelper.getInstance()
				.collectAllQueryIdsForProject(project);

		// get the selected text
		// String selectedText = textSelection == null ? "" :
		// 		textSelection.getText();
		clearMarkers(project, IMarker.BOOKMARK);

		// check whether this is a query Id
		Location loc = null;

		IMarker marker = null;
		if (queryIds.get(selectedText) != null) {
			loc = queryIds.get(selectedText) instanceof Location ? (Location) queryIds
					.get(selectedText) : null;
			if (loc == null) {
				Collection locCollection = (ArrayList) queryIds
						.get(selectedText);
				Iterator itr = locCollection.iterator();
				while (itr.hasNext()) {
					loc = (Location) itr.next();
					marker = createMarker(loc, IMarker.BOOKMARK,
							Message.build_duplicatequeryid_one + loc.key
									+ Message.build_duplicatequeryid_two);
				}
				if (this.loc != null)
					marker = createMarker(this.loc, IMarker.BOOKMARK,
							this.loc.key);
				openEditor(file);
			} else {
				marker = createMarker(loc, IMarker.BOOKMARK, loc.key);
				// open the editor
				openEditor(loc.file);
			}

			// show the location of query id.
			IWorkbenchPage activePage1 = QueryManagerActivator.getDefault()
					.getActiveWorkbenchPage();
			if (activePage1.getActiveEditor() != null)
				IDE.gotoMarker(activePage1.getActiveEditor(), marker);
		} else {
			IWorkbenchPage activePage1 = QueryManagerActivator.getDefault()
					.getActiveWorkbenchPage();
			// no query id with the selected text.
			// show error dialog.
			MessageDialog.openWarning(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					Message.exception_queryidnotexist,
					Message.exception_selectedqueryidnotexist_one + selectedText
							+ Message.exception_selectedqueryidnotexist_two);
		}
	}

	private void clearMarkers(IProject project, String type) {
		try {
			project.deleteMarkers(type, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			// do not do anything.
		}
	}

	private IMarker createMarker(Location loc, String type, String msg) {
		IMarker marker = null;
		try {
			marker = loc.file.createMarker(type);

			// marker.setAttribute(IMarker.MESSAGE, msg);
			marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			marker.setAttribute(IMarker.CHAR_START, loc.charStart);
			marker.setAttribute(IMarker.CHAR_END, loc.charEnd);
			// marker.setAttribute(IMarker.SEVERITY, IMarker.MESSAGE);
			// marker.setAttribute(IMarker., value)
			// marker.setAttribute(IMarker.LINE_NUMBER, 5);
			// IDE.gotoMarker(activePage1.getActiveEditor(), marker);
		} catch (CoreException e) {
		}
		return marker;
	}

	public void openEditor(IFile file) {
		// Open editor on new file.
		String editorId = "org.anyframe.editor.xml.MultiPageEditor";
		IWorkbenchWindow dw = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				IWorkbenchPage page = dw.getActivePage();
				if (page != null)
					page.openEditor(new FileEditorInput(file), editorId, true);

			}
		} catch (PartInitException exception) {
			return;
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		try {
			if (selection != null)
				textSelection = (TextSelection) selection;
		} catch (Exception e) {
			// do not do anything.
		}
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub

	}

}
