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
import org.anyframe.ide.codegenerator.views.CtipView;
import org.anyframe.ide.common.usage.EventSourceID;
import org.anyframe.ide.common.usage.UsageCheckAdapter;
import org.eclipse.jface.viewers.TreeSelection;

/**
 * This is a RunCtipJobAction class.
 * 
 * @author junghwan.hong
 * @since 2.7.0
 * 
 */
public class RunCtipJobAction extends AbstractlViewAction {

	/**
	 * Constructor of RunCtipJobAction forward actionId, actionTooltipText,
	 * actionIconId to AbstractlViewAction
	 */
	public RunCtipJobAction() {
		super(Message.view_ctip_action_runjob_title,
				Message.view_ctip_action_runjob_tooltip, Message.image_run);
	}

	/**
	 * Run this Action with message box
	 */
	public void run() {
		new UsageCheckAdapter(EventSourceID.CD_RUN_CTIP);
		
		// run job action coding
		CtipView view = CodeGeneratorActivator.getDefault().getCtipView();
		TreeSelection selection = (TreeSelection) view.getSelectedId();

		if (selection != null) {
			String ctipUrl = selection.getPaths()[0].getFirstSegment()
					.toString();

			ctipUrl = ctipUrl.substring(ctipUrl.toLowerCase()
					.indexOf("http://"));
			view.runJob(ctipUrl);
		}

	}

}
