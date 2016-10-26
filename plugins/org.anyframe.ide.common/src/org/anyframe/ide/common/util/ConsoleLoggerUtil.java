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
package org.anyframe.ide.common.util;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;

/**
 * This is ConsoleLoggerUtil class.
 * 
 * @author Sujeong Lee
 */
public class ConsoleLoggerUtil {

	private MessageConsole messageConsole;
	private BufferedWriter bufferedWriter;
	private boolean isBWOpen = false;

	private ConsoleLoggerUtil() {
	}

	private static class ConsoleLoggerSingletonHolder {
		private static final ConsoleLoggerUtil INSTANCE = new ConsoleLoggerUtil(); // IODH
	}

	public static ConsoleLoggerUtil getLogger() {
		return ConsoleLoggerSingletonHolder.INSTANCE;
	}

	public void open() {
		// if (!isBWOpen) {
		bufferedWriter = new BufferedWriter(new OutputStreamWriter(
				getLoggingStream()));
		isBWOpen = true;
	}

	public void write(String str) {
		try {
			if (!isBWOpen) {
				open();
				isBWOpen = true;
			}
			bufferedWriter.write(str + "\n");
			bufferedWriter.flush();
		} catch (Exception ex) {
			PluginLogger.error(ex);
		}
	}

	/**
	 * ### bonobono : 임시 방편으로 죽지 않도록 수정했음.
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=136943 eclipse 3.5 부터 수정된
	 * 걸로 생각됨 => 여전함
	 */

	synchronized public void writeLargeSafe(final String str,
			final boolean startWithClear) {

		new Thread(new Runnable() {
			public void run() {
				try {
					open();
					isBWOpen = true;

					bufferedWriter.write(str + "\n");
					bufferedWriter.flush();
				} catch (Exception ex) {
					PluginLogger.error(ex);
				}
			}
		}).start();
	}

	public void clear() {
		try {
			if (messageConsole != null) {
				messageConsole.clearConsole();
			}

			if (isBWOpen) {
				bufferedWriter.close();
				isBWOpen = false;
			}
		} catch (Exception ex) {
			PluginLogger.error(ex);
		}
	}

	public void close() {
		try {
			if (isBWOpen) {
				bufferedWriter.close();
				isBWOpen = false;
			}
			// bufferedWriter.close();
		} catch (Exception ex) {
			PluginLogger.error(ex);
		}
	}

	private OutputStream getLoggingStream() {
		messageConsole = null;

		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager()
				.getConsoles();
		for (int i = 0; i < consoles.length; i++) {
			if (consoles[i].getName().equals("Anyframe Log")) {
				messageConsole = (MessageConsole) consoles[i];
				break;
			}
		}

		if (messageConsole == null) {
			messageConsole = new MessageConsole("Anyframe Log", null);
			ConsolePlugin.getDefault().getConsoleManager()
					.addConsoles(new IConsole[] { messageConsole });
		}

		ConsolePlugin.getDefault().getConsoleManager()
				.showConsoleView(messageConsole);

		return messageConsole.newOutputStream();
	}

}
