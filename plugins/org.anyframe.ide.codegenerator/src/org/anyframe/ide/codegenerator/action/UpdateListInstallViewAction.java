/*
 * Copyright 2007-2012 Samsung SDS Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.anyframe.ide.codegenerator.action;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.views.InstallationView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

/**
 * This is an UpdateListInstallViewAction class.
 * 
 * @author junghwan.hong
 * @since 2.7.0
 * 
 */
public class UpdateListInstallViewAction extends AbstractlViewAction {

	private InstallationView view;

	/**
	 * Constructor of UpdateListInstallViewAction forward actionId,
	 * actionTooltipText, actionIconId to AbstractlViewAction
	 */
	public UpdateListInstallViewAction() {
		super(Message.view_installation_action_updatecatalog_title,
				Message.view_installation_action_updatecatalog_tooltip,
				Message.image_update);
	}

	/**
	 * Run this Action with message box
	 */
	public void run() {
		view = CodeGeneratorActivator.getDefault().getInstallationView();
		view.updateCatalogList();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		action.setEnabled(view.checkOfflineMode());
	}

}
