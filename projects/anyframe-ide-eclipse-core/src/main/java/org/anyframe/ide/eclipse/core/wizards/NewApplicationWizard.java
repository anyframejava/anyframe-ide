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
package org.anyframe.ide.eclipse.core.wizards;

import java.util.Map;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.eclipse.core.AnyframeIDEPlugin;
import org.anyframe.ide.eclipse.core.CommandExecution;
import org.anyframe.ide.eclipse.core.preferences.IdePreferencesPage;
import org.anyframe.ide.eclipse.core.util.AntCommandUtil;
import org.anyframe.ide.eclipse.core.util.DatabaseUtil;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * This is an NewApplicationWizard class.
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

        Map<Object, Object> checkResult =
            DatabaseUtil.checkConnection(
                "",
                jdbcPage.getDriverJar(),
                jdbcPage.getDriverClassName(),
                DatabaseUtil.getDbUrl(jdbcPage.getDatabaseType(),
                    jdbcPage.getDatabaseName(), jdbcPage.getServer(),
                    jdbcPage.getPort()), jdbcPage.getUseName(),
                jdbcPage.getPassword());
        if (!(Boolean) checkResult.get(DatabaseUtil.DB_CON_CHK_KEY)) {
            if (!DialogUtil.confirmMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("wizard.jdbc.setjdbc"))) {
                return false;
            }
        } else {
            if (jdbcPage.getSchemaCombo().getText() != null
                && jdbcPage.getSchemaCombo().getText().trim().length() == 0) {
                jdbcPage.getSchemaCombo().select(0);

                DialogUtil
                    .openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.dialog.validation.dbschema"),
                        MessageDialog.INFORMATION);
                return false;
            } else if (jdbcPage
                .getSchemaCombo()
                .getText()
                .equals(
                    MessageUtil.getMessage("editor.config.jdbc.defaultschema"))
                && jdbcPage.getSchemaCombo().getItemCount() == 1) {
                jdbcPage.getSchemaCombo().select(0);
                DialogUtil
                    .openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.dialog.validation.dbschema"),
                        MessageDialog.INFORMATION);
                return false;

            } else if (jdbcPage
                .getSchemaCombo()
                .getText()
                .equals(
                    MessageUtil.getMessage("editor.config.jdbc.defaultschema"))
                && jdbcPage.getSchemaCombo().getItemCount() > 1) {
                DialogUtil
                    .openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.dialog.validation.dbschema"),
                        MessageDialog.INFORMATION);
                return false;
            }
        }

        // 1. Create Application
        // 2. Set DB configuration

        ApplicationData applicationData = new ApplicationData();

        if (applicationPage.isAntProject()) {
            applicationData.setAnyframeHome(applicationPage.getAnyframeHome());
            applicationData.setPjtTemplateHome(applicationPage
                .getAnyframeHome() + ProjectUtil.SLASH + "templates");
            applicationData.setOffine(applicationPage.isOfflineChecked());
        } else {
            if (applicationPage.useDefaultTemplateHome()) {
                applicationData.setPjtTemplateHome(System
                    .getProperty("user.home")
                    + ProjectUtil.SLASH
                    + ".anyframe"
                    + ProjectUtil.SLASH + "templates");
            } else {
                applicationData.setPjtTemplateHome(applicationPage
                    .getTemplateHomeLocation());
            }
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
        applicationData.setWebTypeProject(applicationPage.useWebProject());
        applicationData.setAntProject(applicationPage.isAntProject());

        applicationData.setDatabaseType(jdbcPage.getDatabaseType());
        applicationData.setDatabaseName(jdbcPage.getDatabaseName());
        applicationData.setSchema(jdbcPage.getSchema().equals("No Schema")
            || jdbcPage.getSchema().equals("<select schema>") ? "" : jdbcPage
            .getSchema());
        applicationData.setUseName(jdbcPage.getUseName());
        applicationData.setPassword(jdbcPage.getPassword());
        applicationData.setServer(jdbcPage.getServer());
        applicationData.setPort(jdbcPage.getPort());
        applicationData.setDialect(jdbcPage.getDialect());
        applicationData.setDriverClassName(jdbcPage.getDriverClassName());
        applicationData.setDriverJar(jdbcPage.getDriverJar());

        String projectHome =
            applicationPage.getLocation() + ProjectUtil.SLASH
                + applicationPage.getPjtName();

        String pjtType = CommonConstants.PROJECT_TYPE_WEB;
        if (applicationPage.useServiceProject())
            pjtType = CommonConstants.PROJECT_TYPE_SERVICE;

        applicationData.setPjtLocation(projectHome);
        String inspectionHome =
            System.getProperty("user.home") + ProjectUtil.SLASH + ".anyframe"
                + ProjectUtil.SLASH + "inspection";
        applicationData.setInspectionHome(inspectionHome);

        CommandExecution commandExecution = new CommandExecution();
        commandExecution.createProject(pjtType, applicationPage.getPjtName(),
            applicationPage.getLocation(), applicationData, this.selection);

        return true;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.setWindowTitle(MessageUtil
            .getMessage("wizard.application.window.title"));
        this.selection = selection;
    }

    @Override
    public void addPages() {
        IPreferenceStore store =
            AnyframeIDEPlugin.getDefault().getPreferenceStore();
        String buildType = store.getString(IdePreferencesPage.BUILD_TYPE);

        if (StringUtils.isNotEmpty(buildType)) {
            applicationPage =
                new NewApplicationWizardPage(
                    MessageUtil.getMessage("wizard.application.page"));
            addPage(applicationPage);

            jdbcPage =
                new NewApplicationWizardJDBCPage(
                    MessageUtil.getMessage("wizard.jdbc.page"));
            addPage(jdbcPage);
        }
    }

    @Override
    public IWizardPage getStartingPage() {
        if (this.getPageCount() == 0) {
            DialogUtil.openMessageDialog(MessageUtil
                .getMessage("ide.message.title"), MessageUtil
                .getMessage("wizard.application.page.preference.correct"),
                MessageDialog.ERROR);
            return null;
        }
        return (IWizardPage) this.getPages()[0];
    }

}
