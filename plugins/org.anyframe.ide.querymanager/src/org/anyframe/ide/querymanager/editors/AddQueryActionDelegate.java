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

import org.anyframe.ide.querymanager.model.QMXMLTableTreeViewer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.NodeList;

/**
 * This is AddQueryActionDelegate class.
 * 
 * @author Junghwan Hong
 */
public class AddQueryActionDelegate extends AbstractQueryActionDelegate
		implements IWorkbenchWindowActionDelegate {

	static private AddQueryActionDelegate addQueryActionDelegate;

	public AddQueryActionDelegate() {
		super();
		addQueryActionDelegate = this;
	}

	public void run(IAction action) {
		if (targetEditor == null) {
			textSelection = null;
			return;
		}
		if (textSelection == null) {
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
				MessageDialog.openInformation(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(), "Modify Query",
						"Select the query to modify");
			}
			return;
		}

		impl = getElementImplFromStructuredTextEditor();

		if (impl == null) {

			return;
		}
		IEditorInput input = targetEditor.getEditorInput();
		IFileEditorInput ifei = (IFileEditorInput) input;
		IFile file = ifei.getFile();
		// Check for null
		if (file == null) {
			return;
		}

		IEditorPart part = getQueryMgrEditorPart();

		QMXMLTableTreeViewer tableTreeViewer = ((QMEditor) part)
				.getTableTreeViewer();

		NodeList queries = impl.getContainerDocument().getDocumentElement()
				.getElementsByTagName("query");
		// AddQueryWizard wizard = new AddQueryWizard(false);
		// // wizard.setData(impl.getAttribute("Id"),
		// // impl.getAttribute(name));
		// wizard.setModifyFlag(false);
		// wizard.setQueries(queries);
		// wizard.setSite(targetEditor.getSite());
		// wizard.setDocument(impl.getContainerDocument());
		// wizard.setFileName(file.getName());
		// wizard.setProject(file.getProject());
		//
		// WizardDialog dialog =
		// new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		// dialog.open();
		// Check for null
		if (tableTreeViewer != null) {
			if (impl != null)
				tableTreeViewer.setInput(impl.getContainerDocument());
		}

	}

	private boolean selectTextSelection() {

		impl = getElementImplFromStructuredTextEditor();
		if (impl == null) {
			return false;
		}

		if (impl.getTagName() != null) {

			IEditorPart part = getQueryMgrEditorPart();

			if (part instanceof QMEditor) {

				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

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

					action.setEnabled(selectTextSelection());

				} else {
					action.setEnabled(false);
				}

			} else {
				action.setEnabled(false);
			}
		}

	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow arg0) {

	}

	public static AddQueryActionDelegate getAddQueryActionDelegate() {
		return addQueryActionDelegate;
	}

}
