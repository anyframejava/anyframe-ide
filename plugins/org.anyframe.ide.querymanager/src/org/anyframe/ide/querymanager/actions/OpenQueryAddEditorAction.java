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

import org.anyframe.ide.querymanager.messages.Message;
import org.anyframe.ide.querymanager.util.AbstractQueryManagerAction;
import org.anyframe.ide.querymanager.views.QMExplorerView;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Open Query Add Editor Action. This class extends AbstractQueryManagerAction
 * class.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class OpenQueryAddEditorAction extends AbstractQueryManagerAction {

	public OpenQueryAddEditorAction() {
		super(
				Message.view_explorer_action_addqueryeditor_title,
				Message.view_explorer_action_addqueryeditor_desc,
				Message.image_explorer_addquery);
	}

	/**
	 * Run this Action
	 */
	public void run() {
		ISelection selected = (ISelection) QMExplorerView.getSelectedId();
		new OpenEditorActionHandler().setModify(false, selected);
	}

	/**
	 * Available check this method
	 */
	public boolean isAvailable() {
		// if (getView() == null) {
		// 		return false;
		// }

		return true;
	}

	class NameSorter extends ViewerSorter {
	}
}
