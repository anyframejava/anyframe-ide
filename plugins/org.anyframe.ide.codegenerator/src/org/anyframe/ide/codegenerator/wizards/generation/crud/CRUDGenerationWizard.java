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
package org.anyframe.ide.codegenerator.wizards.generation.crud;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.CommandExecution;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.maven.mojo.codegen.SourceCodeChecker;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.usage.EventSourceID;
import org.anyframe.ide.common.usage.UsageCheckAdapter;
import org.anyframe.ide.common.util.ConfigXmlUtil;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.ProjectConfig;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * This is an GenerationWizard class.
 * 
 * @author Sujeong Lee
 */
public class CRUDGenerationWizard extends Wizard implements INewWizard {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	private IProject project;
	private ProjectConfig projectConfig = null;
	private IStructuredSelection selection;

	private CRUDGenerationWizardPage crudGenPage;

	private CommandExecution commandExecution;

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.setWindowTitle(Message.wizard_generation_crud_title);
		setDefaultPageImageDescriptor(JavaPluginImages.DESC_WIZBAN_NEWCLASS);

		this.selection = selection;
		project = getProject();

		try {
			String configFile = ConfigXmlUtil.getCommonConfigFile(project.getLocation().toOSString());
			projectConfig = ConfigXmlUtil.getProjectConfig(configFile);
		} catch (Exception e) {
			PluginLoggerUtil.warning(ID, Message.wizard_error_properties);
		}
	}

	public void addPages() {
		crudGenPage = new CRUDGenerationWizardPage(project, Message.wizard_generation_crud_title);
		addPage(crudGenPage);
	}

	public IWizardPage getStartingPage() {
		if (selection.isEmpty() || project == null) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.wizard_error_load_properties, MessageDialog.WARNING);

			return null;
		}

		return (IWizardPage) this.getPages()[0];
	}

	@Override
	public boolean performFinish() {
		new UsageCheckAdapter(EventSourceID.CD_GENERATE_CRUD);
		
		commandExecution = new CommandExecution();

		return generateCRUDCodes();
	}
	
	private boolean generateCRUDCodes() {

		CRUDGenerationWizardPage page = (CRUDGenerationWizardPage) this.getPage(Message.wizard_generation_crud_title);
		String selectedDomain = page.getSelectedDomain();
		String selectedPackage = page.getPackage();
		String selectedTemplateType = page.getTemplateType();

		if (selectedDomain == null || "".equals(selectedDomain)) {
			return false;
		}

		if (MessageDialogUtil.confirmMessageDialog(Message.ide_message_title, Message.wizard_generation_crud_confirm)) {

			SourceCodeChecker sourceCodeChecker = new SourceCodeChecker();

			String projectHome = projectConfig.getPjtHome();
			String basePackage = projectConfig.getPackageName();
			String templateHome = projectConfig.getTemplatePath(Constants.PROJECT_NAME_CODE_GENERATOR);
			String scope = page.getGenWebSource() ? "all" : CommonConstants.PROJECT_TYPE_SERVICE;

			String domainName = selectedDomain.substring(selectedDomain.lastIndexOf(".") + 1);

			boolean isContinue = true;

			try {
				String errorMessage = sourceCodeChecker.checkExistingCrud(false, null, selectedTemplateType, templateHome, projectHome, basePackage,
						selectedPackage, domainName, scope);
				// If existing file has found.
				if (errorMessage != null) {
					if (MessageDialogUtil.confirmMessageDialog(Message.ide_message_title, errorMessage + "\n" + Message.wizard_confirm_overwrite)) {
						isContinue = true;
					} else {
						isContinue = false;
					}
				} else {
					isContinue = true;
				}
			} catch (Exception e) {
				PluginLoggerUtil.error(ID, Message.view_exception_checkoverwrite, e);
			}

			if (isContinue) {
				try {
					commandExecution.createCRUD(selectedDomain, selectedPackage, project.getName(), selectedTemplateType, page.getGenWebSource(), page.getSampleData(),
							project.getLocation().toOSString());
				} catch (Exception e) {
					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.wizard_codegen_createcode, MessageDialog.ERROR);
					PluginLoggerUtil.error(ID, Message.wizard_codegen_createcode, e);
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private IProject getProject() {
		Object obj = selection.getFirstElement();
		if (obj instanceof IProject) {
			return (IProject) obj;
		}
		if (obj instanceof IFolder) {
			return ((IFolder) obj).getProject();
		}
		if (obj instanceof IFile) {
			return ((IFile) obj).getProject();
		}
		if (obj instanceof IJavaProject) {
			return ((IJavaProject) obj).getProject();
		}
		if (obj instanceof IJavaElement) {
			return ((IJavaElement) obj).getJavaProject().getProject();
		}
		if (obj instanceof IPackageFragmentRoot) {
			return ((IPackageFragmentRoot) obj).getJavaProject().getProject();
		}
		if (obj instanceof IPackageFragment) {
			return ((IPackageFragment) obj).getJavaProject().getProject();
		}
		return null;
	}

}