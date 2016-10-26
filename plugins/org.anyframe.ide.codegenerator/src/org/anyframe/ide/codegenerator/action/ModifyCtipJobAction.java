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

import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.model.table.CtipDetailList;
import org.anyframe.ide.codegenerator.util.HudsonRemoteAPI;
import org.anyframe.ide.codegenerator.views.CtipView;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.jface.viewers.TreeSelection;
import org.jdom.Element;

/**
 * This is a ModifyCtipJobAction class.
 * 
 * @author junghwan.hong
 * @since 2.7.0
 * 
 */
public class ModifyCtipJobAction extends AbstractlViewAction {

	/**
	 * Constructor of NewSnapshotAction forward actionId, actionTooltipText,
	 * actionIconId to AbstractSnapshotViewAction
	 */
	private final HudsonRemoteAPI hudson = new HudsonRemoteAPI();

	/**
	 * Constructor of ModifyCtipJobAction forward actionId, actionTooltipText,
	 * actionIconId to AbstractlViewAction
	 */
	public ModifyCtipJobAction() {
		super(Message.view_ctip_action_editjob_title,
				Message.view_ctip_action_editjob_title, Message.image_edit);
	}

	/**
	 * Run this Action with message box
	 */
	public void run() {
		// open pop up window
		CtipView view = CodeGeneratorActivator.getDefault().getCtipView();
		TreeSelection selection = (TreeSelection) view.getSelectedId();

		if (selection != null) {
			String ctipUrl = selection.getPaths()[0].getFirstSegment()
					.toString();
			ctipUrl = ctipUrl.substring(ctipUrl.toLowerCase()
					.indexOf("http://"));

			CtipDetailList detailList = (CtipDetailList) selection
					.getFirstElement();

			view.openCtipModifyJobPopup(detailList, ctipUrl,
					getJobList(ctipUrl));
		}
	}

	private List<String> getJobList(String ctipUrl) {
		List<String> rtnList = new ArrayList<String>();
		hudson.setHudsonURL(ctipUrl);
		
		try {
			List<Element> jobList = hudson.getJobList();

			for (int i = 0; i < jobList.size(); i++) {
				Element elem = jobList.get(i);
				String job = elem.getChildText("name");
				rtnList.add(job);
			}
		} catch (Exception e) {
			PluginLoggerUtil.info(CodeGeneratorActivator.PLUGIN_ID, e.getMessage());
		}
		return rtnList;
	}

}
