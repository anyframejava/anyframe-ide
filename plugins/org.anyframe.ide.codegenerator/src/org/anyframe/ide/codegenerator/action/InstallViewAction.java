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
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.views.InstallationView;

/**
 * Constructs an Action group for ctip view of Anyframe IDE Eclipse Plug-in.
 * 
 * @author junghwan.hong
 * @since 2.7.0
 * 
 */
public class InstallViewAction extends AbstractlViewAction {

	/**
	 * Constructor of InstallViewAction forward actionId, actionTooltipText,
	 * actionIconId to AbstractViewAction
	 */
	public InstallViewAction() {
		super(Message.view_installation_action_install_title,
				Message.view_installation_action_install_tooltip,
				Message.image_install);
	}

	/**
	 * Run this Action with message box
	 */
	public void run() {
		InstallationView view = CodeGeneratorActivator.getDefault().getInstallationView();
		view.installPlugin();
	}

}
