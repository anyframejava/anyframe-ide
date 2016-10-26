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

import org.anyframe.ide.querymanager.model.FileInfoVO;
import org.anyframe.ide.querymanager.model.QueryTreeVO;
import org.anyframe.ide.querymanager.util.AbstractQueryManagerAction;
import org.anyframe.ide.querymanager.views.QMExplorerView;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Constructs an Action group for Query Explorer view of Anyframe Query Manager
 * Eclipse Plug-in.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class QMExplorerViewActionGroup extends ActionGroup {
	public void fillContextMenu(IMenuManager contextmenu) {
		TreeSelection selection = (TreeSelection) QMExplorerView
				.getSelectedId();
		if (selection == null) {
			return;
		} else if (selection.size() == 1) {
			Object select = selection.getFirstElement();
			if (select instanceof QueryTreeVO) {
				// addAction(contextmenu, new OpenDBSettingDialogAction());
			} else if (select instanceof FileInfoVO) {
				addAction(contextmenu, new OpenQueryAddEditorAction());
			} else {
				addAction(contextmenu, new OpenQueryModifyEditorAction());
				contextmenu.add(new Separator());
				addAction(contextmenu, new RemoveQueryAction());
			}
		} else {
			int count = 0;
			for (int i = 0; i < selection.size(); i++) {
				if (selection.toArray()[i] instanceof QueryTreeVO
						|| selection.toArray()[i] instanceof FileInfoVO) {
				} else {
					count++;
				}
			}
			if (count == selection.size() && count != 0) {
				addAction(contextmenu, new RemoveQueryAction());
			}
			if (count == 0) {
				return;
			}

		}

	}

	private boolean addAction(IMenuManager menu,
			AbstractQueryManagerAction action) {
		if (action.isAvailable()) {
			menu.add(action);
			action.setEnabled(true);
			return true;
		}
		return false;
	}
}
