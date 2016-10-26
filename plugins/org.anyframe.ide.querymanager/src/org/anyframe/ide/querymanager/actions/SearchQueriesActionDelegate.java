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
package org.anyframe.ide.querymanager.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

/**
 * This is SearchQueriesActionDelegate class.
 * 
 * @author Junghwan Hong
 */
public class SearchQueriesActionDelegate implements IEditorActionDelegate {

	static private SearchQueriesActionDelegate searchQueryActionDelegate;

	StructuredTextEditor targetEditor;

	public SearchQueriesActionDelegate() {
		super();
		searchQueryActionDelegate = this;
	}

	public void setActiveEditor(IAction action, IEditorPart editor) {
		if (editor instanceof StructuredTextEditor) {
			targetEditor = (StructuredTextEditor) editor;
			enableOrDisableAction(action);

		} else {

			action.setEnabled(false);
		}

	}

	public void run(IAction arg0) {
		try {
			PlatformUI
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.getActivePage()
					.showView(
							"org.anyframe.querymanager.eclipse.core.views.QueryNavigatorView");
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void selectionChanged(IAction action, ISelection arg1) {
		enableOrDisableAction(action);

	}

	private void enableOrDisableAction(IAction action) {
		if (targetEditor != null) {

			if (targetEditor.getSite().getId()
					.equals("org.eclipse.core.runtime.xml.source")) {
				action.setEnabled(true);
			}

		}
	}

	public static SearchQueriesActionDelegate getSearchQueryActionDelegate() {
		return searchQueryActionDelegate;
	}

}
