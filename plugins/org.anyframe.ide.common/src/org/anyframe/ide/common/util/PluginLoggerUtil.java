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
package org.anyframe.ide.common.util;

import org.anyframe.ide.common.CommonActivator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This is PluginLoggerUtil class.
 * 
 * @author Sujeong Lee
 */
public class PluginLoggerUtil {

	public static void info(String pid, String message) {
		log(pid, IStatus.INFO, IStatus.OK, message, null);
	}

	public static void warning(String pid, String message) {
		log(pid, IStatus.WARNING, IStatus.OK, message, null);
	}

	public static void error(String pid, String message, Throwable exception) {
		log(pid, IStatus.ERROR, IStatus.OK, message, exception);
	}

	private static void log(String pid, int severity, int code, String message,
			Throwable exception) {
		log(createStatus(pid, severity, code, message, exception));
	}

	private static IStatus createStatus(String pid, int severity, int code,
			String message, Throwable exception) {
		return new Status(severity, pid, code, message, exception);
	}

	private static void log(IStatus status) {
		CommonActivator.getDefault().getLog().log(status);
	}

}
