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
package org.anyframe.ide.codegenerator.action;

import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.HudsonRemoteAPI;
import org.anyframe.ide.codegenerator.views.CtipView;
import org.anyframe.ide.common.usage.EventSourceID;
import org.anyframe.ide.common.usage.UsageCheckAdapter;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.jface.viewers.TreeSelection;
import org.jdom.Element;

/**
 * This is a AddCtipJobAction class.
 * 
 * @author junghwan.hong
 * @since 2.7.0
 * 
 */
public class AddCtipJobAction extends AbstractlViewAction {

	private final HudsonRemoteAPI hudson = new HudsonRemoteAPI();

	/**
	 * Constructor of AddCtipJobAction forward actionId, actionTooltipText,
	 * actionIconId to AbstractlViewAction
	 */
	public AddCtipJobAction() {
		super(Message.view_ctip_action_addjob_title,
				Message.view_ctip_action_addjob_tooltip, Message.image_new);
	}

	/**
	 * Run this Action with message box
	 */
	public void run() {
		new UsageCheckAdapter(EventSourceID.CD_ADD_CTIP);
		
		// Open Pop Up window
		CtipView view = CodeGeneratorActivator.getDefault().getCtipView();
		TreeSelection selection = (TreeSelection) view.getSelectedId();

		if (selection != null) {
			String ctipUrl = selection.getPaths()[0].getFirstSegment()
					.toString();
			ctipUrl = ctipUrl.substring(ctipUrl.toLowerCase()
					.indexOf("http://"));
			view.openCtipAddJobPopup(ctipUrl, getJobList(ctipUrl));
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
