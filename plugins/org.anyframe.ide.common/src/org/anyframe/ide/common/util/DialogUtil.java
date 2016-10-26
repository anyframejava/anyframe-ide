package org.anyframe.ide.common.util;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public class DialogUtil {
	public static String openDirectoryDialog(Shell shell, String title, String message, String filterPath){
		DirectoryDialog directoryDialog = new DirectoryDialog(shell);
		directoryDialog.setText(title);
		directoryDialog.setMessage(message);
		
		if(!StringUtil.isEmptyOrNull(filterPath)){
			directoryDialog.setFilterPath(filterPath);
		}
		
		return directoryDialog.open();
	}
}
