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
package org.anyframe.ide.querymanager.actions;

import java.io.File;

import org.anyframe.ide.querymanager.messages.Message;
import org.anyframe.ide.querymanager.model.FileInfoVO;
import org.anyframe.ide.querymanager.util.AbstractQueryManagerAction;
import org.anyframe.ide.querymanager.views.QMExplorerView;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Open Query XML Editor action in the Query Explorer view. This class extends
 * AbstractQueryManagerAction class.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class OpenQueryXMLEditorAction extends AbstractQueryManagerAction {
	public OpenQueryXMLEditorAction() {
		super(
				Message.view_explorer_action_openxmleditor_title,
				Message.view_explorer_action_openxmleditor_desc,
				Message.image_explorer_open_queryeditor);
	}

	/**
	 * Run this Action
	 */
	public void run() {

		// Object tt = QMExplorerView.getSelected();
		File file = new File(
				((FileInfoVO) QMExplorerView.getSelected()).getPath());
		IPath path = new Path(file.getAbsolutePath());
		IFile iFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFileForLocation(path);
		QMExplorerView.openEditor(iFile);

		// Object ob1 = (Map) ((FileInfoVO) QMExplorerView.getSelected()).getQueryId();
		// Object ob2 = ((Map) ((FileInfoVO) QMExplorerView.getSelected()).getQueryId()).keySet().iterator().next();
		// Object object = ((Map) ob1).get(ob2);
		//
		// Location location = (Location) object;
		// QMExplorerView.openEditor(location.getFile());

	}

	/**
	 * Available check this method
	 */
	public boolean isAvailable() {
		// if (getView() == null) {
		// 		return false;
		// }

		return true;
	}

	class NameSorter extends ViewerSorter {
	}
}
