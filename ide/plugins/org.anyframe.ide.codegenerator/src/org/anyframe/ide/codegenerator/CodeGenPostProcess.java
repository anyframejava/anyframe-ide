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
package org.anyframe.ide.codegenerator;

import org.anyframe.ide.codegenerator.command.vo.CommandVO;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.PostProcess;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;

/**
 * This is a CodeGenPostProcess class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class CodeGenPostProcess implements PostProcess {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	private CommandVO vo = null;

	public CodeGenPostProcess(CommandVO vo) {
		this.vo = vo;
	}

	public void execute(String[] config) {
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (Exception exception) {
					PluginLoggerUtil.error(ID,
							Message.view_exception_timesleep, exception);
				}
				ProjectUtil.refreshProject(vo.getProjectName());
			}
		}).start();
	}

}
