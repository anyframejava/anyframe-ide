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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.PluginUtil;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.apache.commons.collections.map.ListOrderedMap;

/**
 * This is a PluginInfoList class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class PluginInfoList {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID
			+ ".views.InstallationView";

	private final Map<String, PluginInfo> pInfoList = new ListOrderedMap();
	private final PropertiesIO projectMF;

	public PluginInfoList(PropertiesIO pio) {
		this.projectMF = pio;
		try {
			Map<String, PluginInfo> pluginList = PluginUtil.getPluginList(
					getPjtBuild(), getAnyframeHome(), getBaseDir());
			pInfoList.putAll(pluginList);
			// Iterator<String> itr =
			// pluginList.keySet().iterator();
			// while (itr.hasNext()) {
			// String pluginName = (String) itr.next();
			// PluginInfo info =
			// pluginList.get(pluginName);
			// addPluginInfo(info);
			// }
		} catch (Exception exception) {
			PluginLoggerUtil.error(ID, Message.view_exception_getpluginlist,
					exception);
		}
	}

	private String getBaseDir() throws Exception {
		String baseDir = this.projectMF.readValue(CommonConstants.PROJECT_HOME);

		return baseDir;
	}

	private String getAnyframeHome() throws Exception {
		String anyframeHome = this.projectMF
				.readValue(CommonConstants.ANYFRAME_HOME);

		return anyframeHome;
	}

	private String getPjtBuild() throws Exception {
		String pjtBuild = this.projectMF
				.readValue(CommonConstants.PROJECT_BUILD_TYPE);

		return pjtBuild;
	}

	public void addPluginInfo(PluginInfo pluginInfo) {
		pInfoList.put(pluginInfo.getName(), pluginInfo);
	}

	public Map<String, PluginInfo> getPluginInfoList() {
		return pInfoList;
	}

	public List<String> getInstalledPluginTypeList() {
		List<String> pluginTypeList = new ArrayList<String>();
		try {
			Map<String, PluginInfo> pluginList = PluginUtil
					.getInstalledPluginList(getPjtBuild(), getAnyframeHome(),
							getBaseDir());

			Iterator<String> itr = pluginList.keySet().iterator();
			while (itr.hasNext()) {
				pluginTypeList.add((String) itr.next());
			}
		} catch (Exception exception) {
			PluginLoggerUtil.error(ID,
					Message.view_exception_getinstalledpluginlist, exception);
		}

		return pluginTypeList;
	}

}
