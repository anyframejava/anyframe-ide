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

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.CodeGeneratorConstants;
import org.anyframe.ide.codegenerator.config.AnyframeConfig;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.XmlFileUtil;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PartInitException;

/**
 * This is a CtipInfoList class.
 * 
 * @author junghwan.hong
 * 
 */
public class CtipServerList {
	public static final String ID = CodeGeneratorActivator.PLUGIN_ID
			+ ".views.CtipView";

	private final IProject project;

	private AnyframeConfig anyframeConfig;

	private final IFile fileCtipConfig;

	/**
	 * CtipInfoList Constructor
	 * 
	 * @param project
	 */
	public CtipServerList(IProject project) {
		this.project = project;
		this.fileCtipConfig = project.getFile(Constants.SETTING_HOME
				+ Constants.FILE_SEPERATOR + CodeGeneratorConstants.CONFIG_FILE);

		if (!fileCtipConfig.exists())
			createAnyframeConfig(fileCtipConfig);
		getCtipConfig();
	}

	public AnyframeConfig getAnyframeConfig() {
		return anyframeConfig;
	}

	public void saveOnlyAnyframeConfigFile() {
		XmlFileUtil.saveObjectToXml(anyframeConfig, fileCtipConfig, project);
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			PluginLoggerUtil.info(CodeGeneratorActivator.PLUGIN_ID, e.getMessage());
		}
	}

	private void getCtipConfig() {

		try {
			IFile file = project.getFile(Constants.SETTING_HOME
					+ Constants.FILE_SEPERATOR
					+ CodeGeneratorConstants.CONFIG_FILE);
			loadAnyframeConfig(file);
		} catch (PartInitException exception) {
			PluginLoggerUtil.error(ID, Message.view_exception_conf, exception);
		}
	}

	private void loadAnyframeConfig(IFile file) throws PartInitException {
		if (!file.getName().equals(CodeGeneratorConstants.CONFIG_FILE)) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.wizard_error_selectedproject,
					MessageDialog.ERROR);

			throw new PartInitException(Message.view_exception_init);
		} else {
			this.anyframeConfig = (AnyframeConfig) XmlFileUtil
					.getObjectFromXml(project.getLocation()
							+ Constants.FILE_SEPERATOR + Constants.SETTING_HOME
							+ Constants.FILE_SEPERATOR
							+ CodeGeneratorConstants.CONFIG_FILE);
		}
	}

	private void createAnyframeConfig(IFile fileAnyframeConfig) {
		try {
			this.anyframeConfig = new AnyframeConfig();
			anyframeConfig.setPjtName(project.getName());

			// XmlFileUtil.saveObjectToXml(this.anyframeConfig,
			// fileAnyframeConfig
			// .getLocation().toOSString());

			XmlFileUtil.saveObjectToXml(this.anyframeConfig,
					fileAnyframeConfig, project);

		} catch (Exception exception) {
			PluginLoggerUtil.error(ID, Message.view_exception_conf, exception);
		}
	}

}
