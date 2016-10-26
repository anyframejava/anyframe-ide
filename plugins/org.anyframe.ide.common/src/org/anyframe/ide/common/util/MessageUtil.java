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

import org.anyframe.ide.common.CommonActivator;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * This is MessageUtil class.
 * 
 * @author Sujeong Lee
 */
public class MessageUtil {

	public static void showMessage(String message, String title) {
		MessageDialogUtil.openMessageDialog(title, message,
				MessageDialog.INFORMATION);
	}

	public static void showErrorMessage(String title, String message,
			Exception e) {
		// ErrorDialog.openError(new Shell(), title, null,
		// createStatus(IStatus.ERROR, IStatus.OK, message +
		// "\nSee Error Log for more details.", e));
		showErrorMessage(title, message, e, false);
	}

	private static void showErrorMessage(String title, String message,
			Exception e, boolean isNewDisplay) {
		if (isNewDisplay) {
			// eclipse 기동전에 발생하는 Display 객체 에러 문제로 우선은 사용하지 않음
			// ErrorDialog.openError(new Shell(new Display()), title, null,
			// createStatus(IStatus.ERROR, IStatus.OK, message +
			// "\nSee Error Log for more details.", e));
			MessageDialogUtil.openDetailMessageDialog(
					CommonActivator.PLUGIN_ID, title, message, message
							+ "\nSee Error Log for more details.\n" + e,
					MessageDialog.ERROR);
		} else {
			MessageDialogUtil.openDetailMessageDialog(
					CommonActivator.PLUGIN_ID, title, message, message
							+ "\nSee Error Log for more details.\n" + e,
					MessageDialog.ERROR);
		}
	}

	public static boolean showConfirmMessage(String title, String message) {
		return MessageDialogUtil.confirmMessageDialog(title, message);
	}

	public static String printStackTrace(Throwable e) {
		StringBuilder builder = new StringBuilder();
		builder.append(e.toString() + "\n");
		StackTraceElement astacktraceelement[] = e.getStackTrace();
		for (int i = 0; i < astacktraceelement.length; i++)
			builder.append(astacktraceelement[i].toString()).append("\n");
		return builder.toString();
	}

}
