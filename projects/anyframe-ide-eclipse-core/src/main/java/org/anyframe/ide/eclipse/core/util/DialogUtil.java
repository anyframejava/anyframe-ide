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
package org.anyframe.ide.eclipse.core.util;

import org.anyframe.ide.eclipse.core.AnyframeIDEPlugin;
import org.anyframe.ide.eclipse.core.dialog.ExtErrorDialog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;


/**
 * This is an DialogUtil class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class DialogUtil {

    protected DialogUtil() {
        throw new UnsupportedOperationException(); // prevents
        // calls
        // from
        // subclass
    }

    public static void openMessageDialog(final String title,
            final String message, final int type) {
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

    public static void openDetailMessageDialog(final String title,
            final String message, final String detailMessage, final int type) {
        MultiStatus info =
            getStatusWithDetailMessage(message, detailMessage, type);

        ErrorDialog.openError(Display.getDefault().getActiveShell(), title,
            null, info);

    }

    public static boolean openDetailMessageDialogWithCancelButton(
            final String title, final String message,
            final String detailMessage, final int type) {
        MultiStatus info =
            getStatusWithDetailMessage(message, detailMessage, type);

        ExtErrorDialog dialog =
            new ExtErrorDialog(Display.getDefault().getActiveShell(), title,
                null, info, 0xFFFF);
        int result = dialog.open();
        if (result == Window.OK)
            return true;
        return false;
    }

    public static boolean confirmMessageDialog(final String title,
            final String message) {
        if (MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
            title, message)) {
            return true;
        }
        return false;
    }

    // *************************************************************
    // * private methods
    // *************************************************************

    private static MultiStatus getStatusWithDetailMessage(final String message,
            final String detailMessage, final int type) {
        final String PID = AnyframeIDEPlugin.ID;
        MultiStatus info = new MultiStatus(PID, 1, message, null);

        if (type == MessageDialog.INFORMATION) {
            info.add(new Status(IStatus.INFO, PID, 1, detailMessage, null));
        } else if (type == MessageDialog.ERROR) {
            info.add(new Status(IStatus.ERROR, PID, 1, detailMessage, null));
        } else if (type == MessageDialog.WARNING) {
            info.add(new Status(IStatus.WARNING, PID, 1, detailMessage, null));
        }
        return info;
    }
}
