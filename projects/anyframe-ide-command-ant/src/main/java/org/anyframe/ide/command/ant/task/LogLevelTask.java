/*   
 * Copyright 2008-2012 the original author or authors.   
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
package org.anyframe.ide.command.ant.task;

import java.util.Iterator;
import java.util.Vector;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Echo.EchoLevel;

/**
 * This is a LogLevelTask class. This task is for setting log level in ant build
 * script.
 * 
 * @author Sooyeon Park
 */
public class LogLevelTask extends Task {

	private int logLevel = -1;

	/**
	 * main method for executing LogLevelTask this task is executed when you
	 * write log tag in ant build script
	 */
	@SuppressWarnings("unchecked")
	public void execute() {
		try {
			if (logLevel == -1) {
				throw new BuildException("You should specify log level.");
			}
			Vector<BuildListener> listeners = this.getProject()
					.getBuildListeners();
			Iterator<BuildListener> itr = listeners.iterator();
			while (itr.hasNext()) {
				BuildListener listener = itr.next();
				if (listener instanceof BuildLogger)
					((BuildLogger) listener).setMessageOutputLevel(logLevel);
			}
		} catch (Exception e) {
			log("Fail to execute LogLevelTask", e, Project.MSG_ERR);
			throw new BuildException(e.getMessage());
		}
	}

	/**
	 * set log level
	 * 
	 * @param echoLevel
	 *            echo log level
	 */
	public void setLevel(EchoLevel echoLevel) {
		String level = echoLevel.getValue();
		if (level.equalsIgnoreCase(CommonConstants.LOG_LEVEL_ERROR)) {
			logLevel = Project.MSG_ERR;
		} else if (level.equalsIgnoreCase(CommonConstants.LOG_LEVEL_DEBUG)) {
			logLevel = Project.MSG_DEBUG;
		} else {
			logLevel = Project.MSG_INFO;
		}
	}
}
