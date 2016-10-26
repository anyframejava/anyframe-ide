/*
 * Copyright 2007-2013 Samsung SDS Co., Ltd.
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
import org.anyframe.ide.codegenerator.model.table.CtipDetailList;
import org.anyframe.ide.codegenerator.util.HudsonRemoteAPI;
import org.anyframe.ide.codegenerator.views.CtipView;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Constructs an Action group for ctip view of Anyframe IDE Eclipse Plug-in.
 * 
 * @author junghwan.hong
 * @since 2.7.0
 * 
 */
public class CtipViewActionGroup extends ActionGroup {

	private String ctipUrl;

	private final HudsonRemoteAPI hudson = new HudsonRemoteAPI();

	/*
	 * Define Ctip view cotext menu
	 * 
	 * @param contextmenu
	 */
	public void fillContextMenu(IMenuManager contextmenu) {
		CtipView view = CodeGeneratorActivator.getDefault().getCtipView();
		TreeSelection selection = (TreeSelection) view.getSelectedId();

		if (selection == null) {
			return;
		} else if (selection.size() == 1) {
			Object select = selection.getFirstElement();

			ctipUrl = selection.getPaths()[0].getFirstSegment().toString();
			ctipUrl = ctipUrl.substring(ctipUrl.toLowerCase()
					.indexOf("http://"));

			if (select instanceof CtipDetailList) {
				// when selected ctip job
				addAction(contextmenu, new ModifyCtipJobAction());
				addAction(contextmenu, new RemoveCtipJobAction());
				contextmenu.add(new Separator());
				addAction(contextmenu, new RunCtipJobAction());
			} else if (select instanceof java.lang.String) {
				// when selected ctip server
				addAction(contextmenu, new AddCtipJobAction());
				contextmenu.add(new Separator());
				// ctip configure... configuration setting
				addAction(contextmenu, new CreateOpenCtipConfigureAction());
			}

		}
	}

	private boolean addAction(IMenuManager menu, AbstractlViewAction action) {
		if (action.isAvailable()) {
			menu.add(action);
			if (isActiveServer())
				action.setEnabled(true);
			else
				action.setEnabled(false);
			return true;
		}
		return false;
	}

	private boolean isActiveServer() {
		hudson.setHudsonURL(ctipUrl);
		try {
			hudson.getJobList();
		} catch (Exception ex) {
			return false;
		}
		return true;
	}
}
