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
package org.anyframe.ide.codegenerator.model.table;

import java.util.LinkedList;
import java.util.List;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.util.HudsonRemoteAPI;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jdom.Element;

/**
 * This is a CtipInfoContentProvider class.
 * 
 * @author junghwan.hong
 */

public class CtipInfoContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID
			+ ".views.CtipView";

	private final Object EMPTY_ELEMENT_ARRAY[] = new Object[0];

	private final HudsonRemoteAPI hudson = new HudsonRemoteAPI();

	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// this.viewer = (TreeViewer) viewer;
	}

	public Object[] getChildren(Object parent) {
		if (parent instanceof java.lang.String)
			return displayCtipChildren(parent);

		else if (parent instanceof CtipServerList) {
			return ((CtipServerList) parent).getAnyframeConfig()
					.getCtipUrlList().toArray();
		}
		// TODO Auto-generated method stub
		return EMPTY_ELEMENT_ARRAY;
	}

	public Object getParent(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren(Object parent) {
		// TODO Auto-generated method stub
		if (parent instanceof CtipDetailList)
			return false;
		else if (parent instanceof java.lang.String) {
			if (isActiveServer(parent.toString()))
				return true;
		}
		return false;
	}

	private Object[] displayCtipChildren(Object parent) {
		String hudsonUrl = parent.toString().substring(
				parent.toString().toLowerCase().indexOf("http://"));

		hudson.setHudsonURL(hudsonUrl);
		LinkedList<CtipDetailList> ctipList = new LinkedList<CtipDetailList>();
		try {
			List<Element> jobList = hudson.getJobList();
			for (int i = 0; i < jobList.size(); i++) {
				Element elem = jobList.get(i);
				Element jobConfigElement = hudson.getJobConfigXml(elem
						.getChildText("name"));
				CtipDetailList detailList = new CtipDetailList(
						elem.getChildText("name"), elem.getChildText("color"));
				detailList.setWorkSpace(hudson
						.getCustomWorkspace(jobConfigElement));
				if (elem.getChildText("name").endsWith("build"))
					detailList.setBuildType("build");
				else if (elem.getChildText("name").endsWith("report"))
					detailList.setBuildType("report");
				switch (hudson.getScmTypeMapping(jobConfigElement)) {
				case 0:
					detailList.setScmServerType("subversion");
					break;
				case 1:
					detailList.setScmServerType("cvs");
					break;
				default:
					detailList.setScmServerType("none");
					break;
				}

				detailList.setScmServerUrl(hudson.getScmURL(jobConfigElement));
				detailList.setSchedule(hudson.getSchedule(jobConfigElement));
				detailList.setOtherProject(hudson
						.getChildProject(jobConfigElement));

				ctipList.add(detailList);
			}
		} catch (Exception exception) {
			PluginLoggerUtil.error(ID, Message.view_ctip_getpjtlist_warn,
					exception);
		}

		// get Job list List<CtipServerList , CtipDetailList....

		return getArrays(ctipList);
	}

	private Object[] getArrays(List list) {
		if (list.isEmpty())
			return EMPTY_ELEMENT_ARRAY;

		return list.toArray(new Object[list.size()]);
	}

	private boolean isActiveServer(String ctipUrl) {
		hudson.setHudsonURL(ctipUrl.substring(ctipUrl.toLowerCase()
				.indexOf("http://")));
		try {
			hudson.getJobList();
		} catch (Exception ex) {
			return false;
		}
		return true;
	}
}
