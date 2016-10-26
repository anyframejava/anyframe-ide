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
package org.anyframe.ide.querymanager.perspective;

import org.anyframe.ide.querymanager.views.QMExplorerView;
import org.anyframe.ide.querymanager.views.QMResultsView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Provides an Query Manager perspective for this plugin.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class QMPluginPerspective implements IPerspectiveFactory {

	/**
	 * Creates the default initial layout for this plugin. This method fufills
	 * the contract for the IPerspectiveFactory interface
	 * 
	 * @param layout
	 *            IPageLayout
	 */
	public void createInitialLayout(IPageLayout layout) {
		defineLayout(layout);
		defineAction(layout);

	}

	/**
	 * @param layout
	 *            IPageLayout
	 */
	private void defineAction(IPageLayout layout) {
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards."
				+ "NewPackageCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards"
				+ ".NewClassCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards"
				+ ".NewInterfaceCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards"
				+ ".NewEnumCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards."
				+ "NewAnnotationCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards."
				+ "NewSourceFolderCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards."
				+ "NewSnippetFileCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui." + "wizards.new.folder"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui." + "wizards.new.file"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui." + "editors.wizards."
				+ "UntitledTextFileWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt." + "junit.wizards."
				+ "NewTestCaseCreationWizard");
		layout.addNewWizardShortcut("org.eclipse.ui." + "wizards.new.folder");
		layout.addNewWizardShortcut("org.eclipse.ui." + "wizards.new.file");

		layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView");
//		layout.addShowViewShortcut("net.sourceforge.sqlexplorer.plugin.views."
//				+ "DatabaseStructureView");
		layout.addShowViewShortcut(QMExplorerView.ID);
		layout.addShowViewShortcut(QMResultsView.ID);
	}

	/**
	 * Controls the physical default layout of the perspective
	 * 
	 * @param layout
	 *            IPageLayout
	 */
	private void defineLayout(IPageLayout layout) {

		layout.setEditorAreaVisible(true);
		String editorArea = layout.getEditorArea();

		IFolderLayout topLeft = layout.createFolder("topLeft",
				IPageLayout.LEFT, 0.20f, editorArea);
		topLeft.addView(QMExplorerView.ID);
		topLeft.addView("org.eclipse.jdt.ui.PackageExplorer");
//		topLeft.addView("net.sourceforge.sqlexplorer.connections.ConnectionsView");

		IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT,
				0.80f, editorArea);
		right.addView(IPageLayout.ID_OUTLINE);
//		right.addView("net.sourceforge.sqlexplorer.plugin.views."
//				+ "DatabaseStructureView");

		IFolderLayout bottomRight = layout.createFolder("bottomRight",
				IPageLayout.BOTTOM, 0.90f, "right");
		bottomRight.addView(IPageLayout.ID_PROGRESS_VIEW);

		IFolderLayout bottom = layout.createFolder("bottom",
				IPageLayout.BOTTOM, 0.70f, editorArea);

		bottom.addView(QMResultsView.ID);
		bottom.addView("org.eclipse.pde.runtime.LogView");
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
//		bottom.addView("net.sourceforge.sqlexplorer."
//				+ "plugin.views.DataPreviewView");
//		bottom.addView("net.sourceforge.sqlexplorer."
//				+ "plugin.views.DatabaseDetailView");

	}

}
