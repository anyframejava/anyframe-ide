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
package org.anyframe.ide.codegenerator.wizards;

import java.util.Map;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.CommandExecution;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.preferences.IdePreferencesPage;
import org.anyframe.ide.codegenerator.util.DatabaseUtil;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.usage.EventSourceID;
import org.anyframe.ide.common.usage.UsageCheckAdapter;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * This is an NewApplicationWizard class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class NewApplicationWizard extends Wizard implements INewWizard {
	private NewApplicationWizardPage applicationPage;
	private NewApplicationWizardJDBCPage jdbcPage;
	private IStructuredSelection selection;

	public NewApplicationWizard() {
	}

	@Override
	public boolean performFinish() {

		new UsageCheckAdapter(EventSourceID.CD_NEW_PROJECT);
		
		// 0. validation - common, service project name
		// case 1 : The application doesn't have the
		// common type project, it shows the message
		// below.
		// case 2 : The application has the common type
		// & the web type projects except the service
		// type project, it shows the message below.
		// case 3 : The application has the common type
		// project only, it is fine.

		// validation - get connection & schema

		Map<Object, Object> checkResult = DatabaseUtil.checkConnection("",
				jdbcPage.getDriverJar(), jdbcPage.getDriverClassName(),
				jdbcPage.getDatabaseUrl(), jdbcPage.getUseName(),
				jdbcPage.getPassword());
		if (!(Boolean) checkResult.get(DatabaseUtil.DB_CON_CHK_KEY)) {
			if (!MessageDialogUtil.confirmMessageDialog(
					Message.ide_message_title, Message.wizard_jdbc_setjdbc)) {
				return false;
			}
		} else {
			if (jdbcPage.getSchema() != null
					&& jdbcPage.getSchema().trim().length() == 0) {
				MessageDialogUtil.openMessageDialog(Message.ide_message_title,
						Message.wizard_validation_dbschema,
						MessageDialog.INFORMATION);
				return false;
			} else if (jdbcPage.getSchema().equals(
					Message.wizard_jdbc_defaultschema)) {
				MessageDialogUtil.openMessageDialog(Message.ide_message_title,
						Message.wizard_validation_dbschema,
						MessageDialog.INFORMATION);
				return false;

			} else if (jdbcPage.getSchema().equals(
					Message.wizard_jdbc_defaultschema)) {
				MessageDialogUtil.openMessageDialog(Message.ide_message_title,
						Message.wizard_validation_dbschema,
						MessageDialog.INFORMATION);
				return false;
			}
		}

		// 1. Create Application
		ApplicationData applicationData = new ApplicationData();

//		Map<String, String> urlParseResult = DatabasesSettingUtil.parseDbUrl(
//				jdbcPage.getDatabaseType(), jdbcPage.getDatabaseUrl());

		String projectHome = applicationPage.getLocation() + ProjectUtil.SLASH
				+ applicationPage.getPjtName();

		if (applicationPage.useDefaultTemplateHome()
				|| applicationPage.getTemplateHomeLocation().equals(
						Constants.DFAULT_TEMPLATE_HOME)) {
			applicationData.setPjtTemplateHome(projectHome
					+ Constants.FILE_SEPERATOR + Constants.DFAULT_TEMPLATE_HOME);
		} else {
			applicationData.setPjtTemplateHome(applicationPage
					.getTemplateHomeLocation());
		}

		if (applicationPage.isAntProject()) {
			applicationData.setAnyframeHome(applicationPage.getAnyframeHome());
			applicationData.setOffine(applicationPage.isOfflineChecked());
			// applicationData.setPjtTemplateHome(defaultTemplateHome);
		} else {
			// else
			applicationData.setPjtGroupId(applicationPage.getPjtGroupId());
			applicationData
					.setPjtArtifactId(applicationPage.getPjtArtifactId());
			applicationData.setPjtVersion(applicationPage.getPjtVersion());
			// Set driver jar dependency for pom.xml
			applicationData.setDriverGroupId(jdbcPage.getDriverGroupId());
			applicationData.setDriverArtifactId(jdbcPage.getDriverArtifactId());
			applicationData.setDriverVersion(jdbcPage.getDriverVersion());
		}

		applicationData.setLocation(applicationPage.getLocation());
		applicationData.setPjtName(applicationPage.getPjtName());
		applicationData.setAppPackage(applicationPage.getPackageName());
		applicationData.setWebTypeProject(true);
		applicationData.setAntProject(applicationPage.isAntProject());

		applicationData.setDatabaseType(jdbcPage.getDatabaseType());
		applicationData.setDatabaseName(jdbcPage.getDatabaseType());
		applicationData.setSchema(jdbcPage.getSchema().equals("No Schema")
				|| jdbcPage.getSchema().equals("<select schema>") ? ""
				: jdbcPage.getSchema());
		applicationData.setUseName(jdbcPage.getUseName());
		applicationData.setPassword(jdbcPage.getPassword());
		applicationData.setUrl(jdbcPage.getDatabaseUrl());
		applicationData.setServer("");
		applicationData.setPort("");
		applicationData.setDialect(jdbcPage.getDialect());
		applicationData.setDriverClassName(jdbcPage.getDriverClassName());
		applicationData.setDriverJar(jdbcPage.getDriverJar());

		String pjtType = CommonConstants.PROJECT_TYPE_WEB;

		applicationData.setPjtLocation(projectHome);
		String inspectionHome = System.getProperty("user.home")
				+ ProjectUtil.SLASH + ".anyframe" + ProjectUtil.SLASH
				+ "inspection";
		applicationData.setInspectionHome(inspectionHome);

		CommandExecution commandExecution = new CommandExecution();
		
		// plugin name add 13/06/26 by junghwan.hong
		commandExecution.createProject(pjtType, applicationPage.getPjtName(),
				applicationPage.getLocation(), applicationData, this.selection, applicationPage.getPluginName());

		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.setWindowTitle(Message.wizard_application_window_title);
		this.selection = selection;
	}

	@Override
	public void addPages() {
		IPreferenceStore store = CodeGeneratorActivator.getDefault()
				.getPreferenceStore();
		String buildType = store.getString(IdePreferencesPage.BUILD_TYPE);

		if (StringUtils.isNotEmpty(buildType)) {
			applicationPage = new NewApplicationWizardPage(
					Message.wizard_application_page);
			addPage(applicationPage);

			jdbcPage = new NewApplicationWizardJDBCPage(
					Message.wizard_jdbc_page);
			addPage(jdbcPage);
		}
	}

	@Override
	public IWizardPage getStartingPage() {
		if (this.getPageCount() == 0) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.wizard_application_page_preference_correct,
					MessageDialog.ERROR);
			return null;
		}
		return (IWizardPage) this.getPages()[0];
	}

}
