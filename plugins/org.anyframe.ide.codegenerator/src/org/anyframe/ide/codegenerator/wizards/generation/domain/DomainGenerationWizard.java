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
package org.anyframe.ide.codegenerator.wizards.generation.domain;

import java.sql.Connection;
import java.sql.SQLException;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.CommandExecution;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.command.maven.mojo.codegen.SourceCodeChecker;
import org.anyframe.ide.common.util.EDPUtil;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
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
public class DomainGenerationWizard extends Wizard implements INewWizard {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	private IProject project;
	private PropertiesIO pjtProps = null;
	private IStructuredSelection selection;

	private DomainGenerationWizardPage domainGenPage;

	private CommandExecution commandExecution;

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.setWindowTitle(Message.wizard_generation_domain_title);
		setDefaultPageImageDescriptor(JavaPluginImages.DESC_WIZBAN_NEWCLASS);

		this.selection = selection;
		project = getProject();

		try {
			pjtProps = ProjectUtil.getProjectProperties(project);
		} catch (Exception e) {
			PluginLoggerUtil.warning(ID, Message.wizard_error_properties);
		}
	}

	public void addPages() {
		domainGenPage = new DomainGenerationWizardPage(project,
				Message.wizard_generation_domain_title);
		addPage(domainGenPage);
	}

	public IWizardPage getStartingPage() {
		if (selection.isEmpty() || project == null) {
			MessageDialogUtil
					.openMessageDialog(Message.ide_message_title,
							Message.wizard_error_load_properties,
							MessageDialog.WARNING);

			return null;
		}

		return (IWizardPage) this.getPages()[0];
	}

	@Override
	public boolean performFinish() {
		commandExecution = new CommandExecution();

		DomainGenerationWizardPage page = (DomainGenerationWizardPage) this
				.getPage(Message.wizard_generation_domain_title);
		String domainGen_basePackage = page.getPackageText().getText();
		String domainGen_tablesWithComma = page.getTableNamesWithComma();

		if (domainGen_tablesWithComma.equals("")) {
			return false;
		}

		if (MessageDialogUtil.confirmMessageDialog(Message.ide_message_title,
				Message.wizard_generation_domain_confirm)) {
			generateDomainClasses(domainGen_basePackage,
					domainGen_tablesWithComma);
			return true;
		}

		return false;
	}

	private void generateDomainClasses(String domainGen_basePackage,
			String domainGen_tablesWithComma) {
		Connection conn = null;
		try {
			conn = EDPUtil.getConnection(project,
					pjtProps.readValue(CommonConstants.DB_NAME));
		} catch (Exception e) {
			PluginLoggerUtil.error(ID, Message.view_exception_getconnection, e);
		} finally {
			try {
				EDPUtil.close(conn);
			} catch (SQLException e) {
				PluginLoggerUtil.error(ID,
						Message.view_exception_closeconnection, e);
			}
		}

		if (!domainGen_tablesWithComma.equals("")) {
			if (conn == null) {
				MessageDialogUtil.openMessageDialog(Message.ide_message_title,
						Message.wizard_error_checkdb, MessageDialog.WARNING);
			} else {
				try {
					SourceCodeChecker sourceCodeChecker = new SourceCodeChecker();

					String errorMessage = sourceCodeChecker.checkExistingModel(
							false, null, pjtProps,
							pjtProps.readValue(CommonConstants.PROJECT_HOME),
							domainGen_basePackage, domainGen_tablesWithComma);

					if (errorMessage != null) {
						if (!MessageDialogUtil
								.confirmMessageDialog(
										Message.ide_message_title,
										errorMessage
												+ "\n"
												+ Message.wizard_confirm_domain_overwrite))
							return;
					}
				} catch (Exception e) {
					PluginLoggerUtil.error(ID,
							Message.view_exception_domain_checkoverwrite, e);
				}

				try {
					commandExecution.createModel(domainGen_tablesWithComma,
							domainGen_basePackage, project.getLocation()
									.toOSString());
				} catch (Exception e) {
					MessageDialogUtil.openMessageDialog(
							Message.ide_message_title,
							Message.wizard_error_createmodel,
							MessageDialog.ERROR);
					PluginLoggerUtil.error(ID,
							Message.wizard_error_createmodel, e);
				}
			}
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