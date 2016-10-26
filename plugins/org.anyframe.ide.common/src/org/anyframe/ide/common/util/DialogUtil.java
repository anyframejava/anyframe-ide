package org.anyframe.ide.common.util;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

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
	
	public static Rectangle center(int width, int height){
		return new Rectangle(getX(width), getY(height), width, height);
	}
	
	private static int getX(int width){
		Rectangle bounds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getBounds();
		return bounds.x + (bounds.width - width) / 2;
	}
	
	private static int getY(int height) {
		Rectangle bounds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getBounds();
		return bounds.y + (bounds.height - height) / 2;
	}
}
