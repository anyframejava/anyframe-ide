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
package org.anyframe.ide.querymanager.actions;

import org.anyframe.ide.querymanager.editors.QMEditor;
import org.anyframe.ide.querymanager.editors.QueryEditor;
import org.anyframe.ide.querymanager.messages.Message;
import org.anyframe.ide.querymanager.model.EditorInput;
import org.anyframe.ide.querymanager.util.AbstractQueryManagerAction;
import org.anyframe.ide.querymanager.views.QMExplorerView;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Link with mapping-xml file and Query Explorer View. This class extends
 * AbstractQueryManagerAction class.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class LinkEditorAction extends AbstractQueryManagerAction {
	/**
	 * Constructor of RefreshAction forward actionId, actionTooltipText,
	 * actionIconId to AbstractSnapshotViewAction
	 */
	public LinkEditorAction() {
		// super("Link in Editor", "Link in Editor",
		// "images/page_white_link.png");
		super(
				Message.view_explorer_action_linkeditor_title,
				Message.view_explorer_action_linkeditor_desc,
				Message.image_explorer_linkeditor);
	}

	/**
	 * Run this Action
	 */
	public void run() {
		int editorType = -1;
		// [0]QMEditor
		// [1]QueryEditor-Title(QueryID)
		// [2]QueryEditor-Title(AddQuery)
		// [-1]else

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		if (page.getActiveEditor() != null) {

			String title = page.getActiveEditor().getTitle();
			String activeEditorFullPath = ""; //$NON-NLS-1$
			if (page.getActiveEditor() instanceof QMEditor) {
				activeEditorFullPath = page.getActiveEditor().getTitleToolTip();
				editorType = 0;
			} else if (page.getActiveEditor() instanceof QueryEditor) {
				IFile ifile = ((EditorInput) page.getActiveEditor()
						.getEditorInput()).getFile();
				activeEditorFullPath = ifile.toString().substring(2);

				if (page.getActiveEditor()
						.getTitle()
						.startsWith(
								Message.editor_querymanager_message_addquerytitle_prefix)) {
					editorType = 2;
				} else {
					editorType = 1;
				}
			} else {
				activeEditorFullPath = ""; //$NON-NLS-1$
				editorType = -1;
			}

			String project = ""; //$NON-NLS-1$
			String fullPath = ""; //$NON-NLS-1$

			int projectSeperator = activeEditorFullPath.indexOf("/"); //$NON-NLS-1$

			if (!activeEditorFullPath.equals("") && projectSeperator != -1) { //$NON-NLS-1$
				project = activeEditorFullPath.substring(0, projectSeperator);
				fullPath = activeEditorFullPath.substring(projectSeperator + 1);

				TreeViewer viewer = QMExplorerView.viewer;
				viewer.collapseAll();
				Tree tree = viewer.getTree();
				TreeItem[] projectItems = tree.getItems();
				for (int i = 0; i < projectItems.length; i++) {
					if (projectItems[i].getText().equals(project)) {
						viewer.expandToLevel(projectItems[i].getData(), 1);
						TreeItem[] fileItems = projectItems[i].getItems();
						for (int j = 0; j < fileItems.length; j++) {
							int start = fileItems[j].getText().indexOf("["); //$NON-NLS-1$
							int end = fileItems[j].getText().indexOf("]"); //$NON-NLS-1$

							String file = fileItems[j].getText().substring(0,
									start - 1);
							String path = fileItems[j].getText().substring(
									start + 1, end);
							String sumPath = path + "/" + file; //$NON-NLS-1$

							if (sumPath.endsWith(fullPath)) {
								viewer.expandToLevel(fileItems[j].getData(), 1);
								if (editorType == -1) {
								} else if (editorType == 0) {
									tree.setSelection(fileItems[j]);
								} else if (editorType == 1) {
									TreeItem[] idItems = fileItems[j]
											.getItems();
									for (int n = 0; n < idItems.length; n++) {
										if (idItems[n].getText()
												.equalsIgnoreCase(title)) {
											tree.setSelection(idItems[n]);
											break;
										}
									}
								} else if (editorType == 2) {
									String editorQueryID = ((QueryEditor) page
											.getActiveEditor()).getQueryPage()
											.getStrQueryIdText();
									TreeItem[] idItems = fileItems[j]
											.getItems();
									for (int n = 0; n < idItems.length; n++) {
										if (idItems[n]
												.getText()
												.equalsIgnoreCase(editorQueryID)) {
											tree.setSelection(idItems[n]);
											break;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Available check this method
	 */
	public boolean isAvailable() {
		// if (getView() == null) {
		// return false;
		// }

		return true;
	}

	class NameSorter extends ViewerSorter {
	}
}
