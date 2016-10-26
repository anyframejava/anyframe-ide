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
package org.anyframe.ide.querymanager.wizards;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.messages.Message;
import org.anyframe.ide.querymanager.util.BuilderUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.xml.ui.internal.wizards.NewXMLWizard;

/**
 * The AddSQMPFileWizard class helps the developer to create new SQL XML files
 * through wizard page.And Opens in Query Manager Editor
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class AddSQMPFileWizard extends NewXMLWizard {

	// logger
	// private static final Log LOGGER = LogFactory
	// .getLog(AddSQMPFileWizard.class);

	QMNewXMLGenerator xmlgenerator;

	/**
	 * adds wizard page NewFilePage
	 */
	public void addPages() {
		// new file page
		newFilePage = new NewFilePage(fSelection);

		newFilePage.setTitle(Message.wizard_createquerymappingxmlfile_title);
		newFilePage
				.setDescription(Message.wizard_createquerymappingxmlfile);

		newFilePage.defaultName = "mapping-query";

		// .xml is default extension
		newFilePage.defaultFileExtension = "." + "xml";
		newFilePage.filterExtensions = filePageFilterExtensions;

		addPage(newFilePage);
	}

	/**
	 * This method will get executed when user click on finish button of wizard
	 * page. This method creates New SQL XML File with default content.
	 * 
	 * @return boolean
	 */
	// public boolean performFinish() {
	// boolean result = super.performFinish();
	// if (!result) return result;
	// xmlgenerator = new QueryMgrNewXMLGenerator();
	// IFile newFile = newFilePage.createNewFile();
	// try {
	// 		xmlgenerator.createEmptyXMLDocument(newFile);
	// 		openEditor(newFile);
	// }
	// catch (Exception exception) {
	// 		LOGGER.error(Message.getProperty("error.addSQMPFileWizard.creating.sql.file"),
	// 		exception);
	// }
	// 		return result;
	// }
	public boolean performFinish() {
		// boolean result = super.performFinish();
		boolean result = true;
		WizardPage currentPage = (WizardPage) getContainer().getCurrentPage();
		if (currentPage != null) {
			result = currentPage.isPageComplete();
		}
		// boolean result = super.performFinish();
		// save user options for next use
		// fNewXMLTemplatesWizardPage. .
		// .saveLastSavedPreferences();

		if (!result)
			return result;
		xmlgenerator = new QMNewXMLGenerator();
		IFile newFile = newFilePage.createNewFile();
		try {
			xmlgenerator.createEmptyXMLDocument(newFile);
			openEditor(newFile);
		} catch (Exception ex) {
			// LOGGER.error("An error occured during creating SQL File.",
			// exception);
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					Message.wizard_createsqlfile, ex);
		}
		// Add AnyFrame Builder to project.

		BuilderUtil util = new BuilderUtil();
		util.runJobToAddBuilder(newFile.getProject());
		return result;
	}

	/**
	 * This method gets called by method performFinish() which creates SQL XML
	 * file .This method opens SQL XML file in Query Manager editor
	 * 
	 * @param file
	 *            Name of file to be opened
	 */

	public void openEditor(IFile file) {
		// Open editor on new file.
		String editorId = "org.anyframe.editor.xml.MultiPageEditor";
		IWorkbenchWindow dw = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				IWorkbenchPage page = dw.getActivePage();
				if (page != null)
					page.openEditor(new FileEditorInput(file), editorId, true);
			}
		} catch (PartInitException ex) {
			// LOGGER.error("An error occured during opening DBIO Editor.", exception);
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					Message.wizard_createdbioeditor, ex);
			// editor can not open for some reason
			return;
		}

	}

	/**
	 * This method enable or disable finsh button depends on user input
	 * 
	 * @return boolean
	 */

	public boolean canFinish() {
		boolean result = false;

		IWizardPage currentPage = getContainer().getCurrentPage();
		if ((currentPage == newFilePage && generator.getGrammarURI() == null)
				|| (currentPage == selectRootElementPage)) {
			result = currentPage.isPageComplete();
		}
		return result;
	}
}
