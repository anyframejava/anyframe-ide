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
package org.anyframe.ide.querymanager.editors;

import org.anyframe.ide.querymanager.model.QMXMLTableTreeViewer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.wst.xml.ui.internal.tabletree.IDesignViewer;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;

/**
 * The class QMEditor is Multi Page editor which extends XMLMultiPageEditorPart
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class QMEditor extends XMLMultiPageEditorPart {

	/**
	 * Create and Add the Design Page using a registered factory
	 * 
	 * @return tableTreeViewer
	 */

	QMXMLTableTreeViewer tableTreeViewer;

	protected IDesignViewer createDesignPage() {

		tableTreeViewer = new QMXMLTableTreeViewer(getContainer());

		IEditorInput input = getEditorInput();
		IFileEditorInput ifei = (IFileEditorInput) input;
		IFile file = ifei.getFile();
		IProject project = file.getProject();
		tableTreeViewer.setFileName(file.getName());
		tableTreeViewer.setProject(project);
		tableTreeViewer.setSite(getSite());
		return tableTreeViewer;
	}

	public QMXMLTableTreeViewer getTableTreeViewer() {
		return tableTreeViewer;
	}

}
