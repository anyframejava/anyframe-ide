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

import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.model.QMXMLTableTreeViewer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;

/**
 * This is AbstractQueryActionDelegate class.
 * 
 * @author Junghwan Hong
 */
public abstract class AbstractQueryActionDelegate implements
		IEditorActionDelegate {
	TextSelection textSelection;
	StructuredTextEditor targetEditor;
	IAction action;
	ElementImpl impl;

	public void setActiveEditor(IAction action, IEditorPart editor) {
		if (editor != null && action != null)

			if (editor instanceof StructuredTextEditor) {
				this.targetEditor = (StructuredTextEditor) editor;
				if (textSelection != null) {
					action.setEnabled(true);
				} else {
					action.setEnabled(false);
				}
			} else {
				targetEditor = null;
				action.setEnabled(false);
			}

	}

	abstract public void run(IAction action);

	abstract public void selectionChanged(IAction arg0, ISelection arg1);

	ElementImpl getElementImplFromStructuredTextEditor() {

		IWorkbenchPage activePage = QueryManagerActivator.getDefault()
				.getActiveWorkbenchPage();
		if (activePage == null || targetEditor == null) {

			return null;
		}

		IEditorPart part = activePage.getActiveEditor();

		if (part instanceof QMEditor) {
			IStructuredSelection selection = (IStructuredSelection) targetEditor
					.getSelectionProvider().getSelection();

			QMXMLTableTreeViewer tableTreeViewer = ((QMEditor) part)
					.getTableTreeViewer();
			tableTreeViewer.setSelection(selection);

			if (tableTreeViewer != null
					&& tableTreeViewer.getSelection() instanceof TreeSelection) {
				TreeSelection treeSelected = (TreeSelection) tableTreeViewer
						.getSelection();
				Object element = treeSelected.getFirstElement();

				if (element != null && element instanceof ElementImpl) {
					impl = (ElementImpl) treeSelected.getFirstElement();

					return impl;

				}
			}

		}
		return null;
	}

	// open File in Editor
	public IEditorPart openEditor(IFile file, String editorId) {
		// Open editor on new file.
		// String editorId = "org.eclipse.jdt.ui.CompilationUnitEditor";
		IWorkbenchWindow dw = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				IWorkbenchPage page = dw.getActivePage();
				if (page != null)
					return page.openEditor(new FileEditorInput(file), editorId,
							true);

			}
		} catch (PartInitException exception) {
			return null;
		}
		return null;
	}

	public IEditorPart getQueryMgrEditorPart() {
		IWorkbenchWindow dw = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		if (dw != null) {
			IWorkbenchPage page = dw.getActivePage();
			IEditorPart part = page.getActiveEditor();
			return part;

		}

		return null;

	}

}
