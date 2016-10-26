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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This is MessageDialogUtil class.
 * 
 * @author Sujeong Lee
 */
public class MessageDialogUtil {

	protected MessageDialogUtil() {
		// prevents calls from subclass
		throw new UnsupportedOperationException();
	}

	public static void openMessageDialog(String title, String message, int type) {
		if (type == MessageDialog.INFORMATION) {
			MessageDialog.openInformation(
					Display.getDefault().getActiveShell(), title, message);
		} else if (type == MessageDialog.ERROR) {
			MessageDialog.openError(Display.getDefault().getActiveShell(),
					title, message);
		} else if (type == MessageDialog.WARNING) {
			MessageDialog.openWarning(Display.getDefault().getActiveShell(),
					title, message);
		}
	}

	public static void openDetailMessageDialog(String pid, String title,
			String message, String detailMessage, int type) {
		MultiStatus info = getStatusWithDetailMessage(pid, message,
				detailMessage, type);
		ErrorDialog.openError(Display.getDefault().getActiveShell(), title,
				null, info);
	}

	public static boolean openDetailMessageDialogWithCancelButton(String pid,
			String title, String message, String detailMessage, int type) {
		MultiStatus info = getStatusWithDetailMessage(pid, message,
				detailMessage, type);

		ErrorDialog dialog = new ExtErrorDialog(Display.getDefault()
				.getActiveShell(), title, null, info, 0xFFFF);
		int result = dialog.open();
		if (result == Window.OK)
			return true;
		return false;
	}

	public static boolean questionMessageDialog(String title, String message) {
		if (MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
				title, message)) {
			return true;
		}
		return false;
	}

	public static boolean confirmMessageDialog(String title, String message) {
		if (MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
				title, message)) {
			return true;
		}
		return false;
	}

	// *************************************************************
	// * private methods
	// *************************************************************

	private static MultiStatus getStatusWithDetailMessage(String pid,
			final String message, final String detailMessage, final int type) {
		MultiStatus info = new MultiStatus(pid, 1, message, null);

		if (type == MessageDialog.INFORMATION) {
			info.add(new Status(IStatus.INFO, pid, 1, detailMessage, null));
		} else if (type == MessageDialog.ERROR) {
			info.add(new Status(IStatus.ERROR, pid, 1, detailMessage, null));
		} else if (type == MessageDialog.WARNING) {
			info.add(new Status(IStatus.WARNING, pid, 1, detailMessage, null));
		}
		return info;
	}

	private static class ExtErrorDialog extends ErrorDialog {

		public ExtErrorDialog(Shell parentShell, String dialogTitle,
				String message, IStatus status, int displayMask) {
			super(parentShell, dialogTitle, message, status, displayMask);
		}

		/**
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			// create OK button
			createButton(parent, IDialogConstants.OK_ID,
					IDialogConstants.OK_LABEL, true);

			// create Cancel button
			createButton(parent, IDialogConstants.CANCEL_ID,
					IDialogConstants.CANCEL_LABEL, false);

			// create Details button
			createDetailsButton(parent);
		}
	}
}
