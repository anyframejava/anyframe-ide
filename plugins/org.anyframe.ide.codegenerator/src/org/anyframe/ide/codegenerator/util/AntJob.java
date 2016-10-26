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
package org.anyframe.ide.codegenerator.util;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;

/**
 * This is an AntJob class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class AntJob extends Job {

	private List<String[]> antConfigList;
	private PostProcess postProcess;
	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	public AntJob(final String type, final List<String[]> antConfigList) {
		super(type);
		this.antConfigList = antConfigList;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		try {
			monitor.beginTask(DebugUIMessages.DebugUITools_3,
					antConfigList.size() + 1);

			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;

			for (String[] config : antConfigList) {

				Map<String, Object> antInfo = AntCommandUtil
						.getAntInformation(config);

				ILaunchConfiguration configuration = ProjectUtil
						.createDefaultLaunchConfiguration(
								(IPath) antInfo.get("buildfile"),
								(Properties) antInfo.get("antProps"),
								(String) antInfo.get("target"));

				ILaunch launchResult = DebugUIPlugin.buildAndLaunch(
						configuration, ILaunchManager.RUN_MODE, monitor);
				
				executePostProcess(config);
				configuration.delete();
				monitor.worked(1);
			}
			monitor.worked(1);
		} catch (CoreException e) {
			PluginLoggerUtil.error(ID, Message.exception_log_antconfig, e);
		} catch (Exception e) {
			PluginLoggerUtil.error(ID, Message.exception_log_antconfig, e);
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	public void setPostProcess(PostProcess postProcess) {
		this.postProcess = postProcess;
	}

	public void executePostProcess(String[] config) {
		if (postProcess != null)
			postProcess.execute(config);
	}
}
