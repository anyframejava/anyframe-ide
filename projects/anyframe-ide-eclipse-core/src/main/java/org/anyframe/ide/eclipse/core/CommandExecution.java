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
package org.anyframe.ide.eclipse.core;

import org.anyframe.ide.command.cli.util.CommandUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.eclipse.core.command.factory.AbstractCommandFactory;
import org.anyframe.ide.eclipse.core.command.factory.CommandFactory;
import org.anyframe.ide.eclipse.core.command.vo.CommandVO;
import org.anyframe.ide.eclipse.core.command.vo.CreateCRUDVO;
import org.anyframe.ide.eclipse.core.command.vo.CreateModelVO;
import org.anyframe.ide.eclipse.core.command.vo.CreatePJTVO;
import org.anyframe.ide.eclipse.core.command.vo.InstallPluginVO;
import org.anyframe.ide.eclipse.core.command.vo.UninstallPluginVO;
import org.anyframe.ide.eclipse.core.editor.CodeGenEditor;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.anyframe.ide.eclipse.core.wizards.ApplicationData;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * This is a CommandExecution class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class CommandExecution {

    public CommandExecution() {

    }

    public void createProject(String pjtType, String pjtName, String appPath,
            ApplicationData applicationData, IStructuredSelection selection) {

        // 1. make arguments
        CreatePJTVO vo = new CreatePJTVO();
        vo.setCommand(CommandUtil.CMD_CREATE_PROJECT);
        vo.setProjectName(pjtName);
        vo.setPackageName(applicationData.getAppPackage());
        vo.setProjectGroupId(applicationData.getPjtGroupId());
        vo.setProjectVersion(applicationData.getPjtVersion());
        vo.setProjectType(pjtType);
        vo.setProjectHome(appPath);
        vo.setAnyframeHome(applicationData.getAnyframeHome());
        vo.setBasedir(appPath);
        vo.setDatabaseType(applicationData.getDatabaseType());
        vo.setDatabaseName(applicationData.getDatabaseName());
        vo.setDatabaseDriverPath(applicationData.getDriverJar());
        vo.setSelection(selection);
        vo.setTemplateHome(applicationData.getPjtTemplateHome());
        vo.setInspectionHome(applicationData.getInspectionHome());
        vo.setDatabaseSchema(applicationData.getSchema());
        vo.setDatabaseUserId(applicationData.getUseName());
        vo.setDatabasePassword(applicationData.getPassword());
        vo.setDatabaseServer(applicationData.getServer());
        vo.setDatabasePort(applicationData.getPort());
        vo.setDatabaseDialect(applicationData.getDialect());
        vo.setDatabaseGroupId(applicationData.getDriverGroupId());
        vo.setDatabaseArtifactId(applicationData.getDriverArtifactId());
        vo.setDatabaseVersion(applicationData.getDriverVersion());
        vo.setDatabaseDriver(applicationData.getDriverClassName());
        vo.setOffline(applicationData.isOffine());

        // 2. run ant/maven
        String buildType =
            applicationData.isAntProject()
                ? CommonConstants.PROJECT_BUILD_TYPE_ANT
                : CommonConstants.PROJECT_BUILD_TYPE_MAVEN;
        AbstractCommandFactory factory = new CommandFactory(buildType);
        factory.newInstance().execute(vo);
    }

    public void createCRUD(String domainClassName, String srcPackage,
            String serviceProjectName, boolean createWebProject, boolean insertSampleData,
            String projectLocation) {

        // 1. get application location
        String pjtHomeLocation = "";
        PropertiesIO pio = null;
        String anyframeHomeLocation = "";
        String projectBuildType = "";
        try {
            pjtHomeLocation = ProjectUtil.getProjectLocation(projectLocation);
            pio = ProjectUtil.getProjectProperties(projectLocation);
            anyframeHomeLocation = pio.readValue(CommonConstants.ANYFRAME_HOME);
            projectBuildType =
                pio.readValue(CommonConstants.PROJECT_BUILD_TYPE);
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.loadconfig"),
                IStatus.ERROR, e);
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.exception.findconfig"),
                MessageDialog.ERROR);
        }

        String scope = CommonConstants.PROJECT_TYPE_SERVICE;
        if (createWebProject)
            scope = "all";
        String sampleData = "false";
        if (insertSampleData)
            sampleData = "true";

        // 2. make arguments
        CreateCRUDVO vo = new CreateCRUDVO();
        vo.setCommand(CommandUtil.CMD_CREATE_CRUD);
        vo.setDomainClassName(domainClassName);
        vo.setProjectName(serviceProjectName);
        vo.setPackageName(srcPackage);
        vo.setScope(scope);
        vo.setProjectHome(pjtHomeLocation);
        vo.setAnyframeHome(anyframeHomeLocation);
        vo.setBasedir(pjtHomeLocation);
        vo.setInsertSampleData(sampleData);

        // 3. run ant/maven
        AbstractCommandFactory factory = new CommandFactory(projectBuildType);
        factory.newInstance().execute(vo);
    }

    /**
     * Build configuration of mapping
     */
    public void createModel(String tableName, String srcPackage,
            String projectLocation) throws Exception {
        // 1. get project location, package name
        String pjtHomeLocation = "";
        PropertiesIO pio = null;
        String anyframeHomeLocation = "";
        String projectBuildType = "";
        String projectName = "";
        try {
            pjtHomeLocation = ProjectUtil.getProjectLocation(projectLocation);
            pio = ProjectUtil.getProjectProperties(projectLocation);
            anyframeHomeLocation = pio.readValue(CommonConstants.ANYFRAME_HOME);
            projectBuildType =
                pio.readValue(CommonConstants.PROJECT_BUILD_TYPE);
            projectName = pio.readValue(CommonConstants.PROJECT_NAME);
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.loadconfig"),
                IStatus.ERROR, e);
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.exception.findconfig"),
                MessageDialog.ERROR);
        }

        // 2. make arguments
        CreateModelVO vo = new CreateModelVO();
        vo.setCommand(CommandUtil.CMD_CREATE_MODEL);
        vo.setTableName(tableName);
        vo.setPackageName(srcPackage);
        vo.setProjectHome(pjtHomeLocation);
        vo.setAnyframeHome(anyframeHomeLocation);
        vo.setBasedir(pjtHomeLocation);
        vo.setProjectName(projectName);

        // 3. run ant
        AbstractCommandFactory factory = new CommandFactory(projectBuildType);
        factory.newInstance().execute(vo);
    }

    public void changeDBConfig(String projectLocation) {

        // 1. get project location
        String pjtHomeLocation = "";
        PropertiesIO pio = null;
        String anyframeHomeLocation = "";
        String projectBuildType = "";
        String projectName = "";
        try {
            pjtHomeLocation = ProjectUtil.getProjectLocation(projectLocation);
            pio = ProjectUtil.getProjectProperties(projectLocation);
            anyframeHomeLocation = pio.readValue(CommonConstants.ANYFRAME_HOME);
            projectBuildType =
                pio.readValue(CommonConstants.PROJECT_BUILD_TYPE);
            projectName = pio.readValue(CommonConstants.PROJECT_NAME);
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.loadconfig"),
                IStatus.ERROR, e);
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.exception.findconfig"),
                MessageDialog.ERROR);
        }

        // 2. make arguments
        CommandVO vo = new CommandVO();
        vo.setCommand(CommandUtil.CMD_CHANGE_DB);
        vo.setProjectHome(pjtHomeLocation);
        vo.setAnyframeHome(anyframeHomeLocation);
        vo.setBasedir(pjtHomeLocation);
        vo.setProjectName(projectName);

        // 3. run ant
        AbstractCommandFactory factory = new CommandFactory(projectBuildType);
        factory.newInstance().execute(vo);
    }

    public void installPlugins(String pluginNames, boolean installSample,
            String projectLocation, CodeGenEditor ideEditor) {
        try {
            // 1. get project location
            String pjtHomeLocation = "";
            PropertiesIO pio = null;
            String anyframeHomeLocation = "";
            String projectBuildType = "";
            String projectName = "";
            try {
                pjtHomeLocation =
                    ProjectUtil.getProjectLocation(projectLocation);
                pio = ProjectUtil.getProjectProperties(projectLocation);
                anyframeHomeLocation = pio.readValue(CommonConstants.ANYFRAME_HOME);
                projectBuildType =
                    pio.readValue(CommonConstants.PROJECT_BUILD_TYPE);
                projectName = pio.readValue(CommonConstants.PROJECT_NAME);
            } catch (Exception e) {
                ExceptionUtil.showException(
                    MessageUtil.getMessage("editor.exception.loadconfig"),
                    IStatus.ERROR, e);
                DialogUtil.openMessageDialog(
                    MessageUtil.getMessage("ide.message.title"),
                    MessageUtil.getMessage("editor.exception.findconfig"),
                    MessageDialog.ERROR);
            }

            // 2. make arguments
            InstallPluginVO vo = new InstallPluginVO();
            vo.setCommand(CommandUtil.CMD_INSTALL);
            vo.setProjectHome(pjtHomeLocation);
            vo.setAnyframeHome(anyframeHomeLocation);
            vo.setBasedir(pjtHomeLocation);
            vo.setPluginNames(pluginNames);
            vo.setExcludeSrc(!installSample);
            vo.setIdeEditor(ideEditor);
            vo.setProjectName(projectName);

            // 3. run ant
            AbstractCommandFactory factory =
                new CommandFactory(projectBuildType);
            factory.newInstance().execute(vo);

        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.installpluginlist"),
                IStatus.ERROR, e);
        }
    }

    public void uninstallPlugins(String pluginNames, boolean uninstallSample,
            String projectLocation, CodeGenEditor ideEditor) {
        try {
            // 1. get project location
            String pjtHomeLocation = "";
            PropertiesIO pio = null;
            String anyframeHomeLocation = "";
            String projectBuildType = "";
            String projectName = "";
            try {
                pjtHomeLocation =
                    ProjectUtil.getProjectLocation(projectLocation);
                pio = ProjectUtil.getProjectProperties(projectLocation);
                anyframeHomeLocation = pio.readValue(CommonConstants.ANYFRAME_HOME);
                projectBuildType =
                    pio.readValue(CommonConstants.PROJECT_BUILD_TYPE);
                projectName = pio.readValue(CommonConstants.PROJECT_NAME);
            } catch (Exception e) {
                ExceptionUtil.showException(
                    MessageUtil.getMessage("editor.exception.loadconfig"),
                    IStatus.ERROR, e);
                DialogUtil.openMessageDialog(
                    MessageUtil.getMessage("ide.message.title"),
                    MessageUtil.getMessage("editor.exception.findconfig"),
                    MessageDialog.ERROR);
            }

            // 2. make arguments
            UninstallPluginVO vo = new UninstallPluginVO();
            vo.setCommand(CommandUtil.CMD_UNINSTALL);
            vo.setProjectHome(pjtHomeLocation);
            vo.setAnyframeHome(anyframeHomeLocation);
            vo.setBasedir(pjtHomeLocation);
            vo.setPluginNames(pluginNames);
            vo.setIdeEditor(ideEditor);
            vo.setProjectName(projectName);

            // 3. run ant
            AbstractCommandFactory factory =
                new CommandFactory(projectBuildType);
            factory.newInstance().execute(vo);

        } catch (Exception e) {
            ExceptionUtil
                .showException(MessageUtil
                    .getMessage("editor.exception.uninstallepluginlist"),
                    IStatus.ERROR, e);
        }
    }

    public void updateCatalog(String projectLocation) {
        try {
            // 1. get project location
            String pjtHomeLocation =
                ProjectUtil.getProjectLocation(projectLocation);
            PropertiesIO pio =
                ProjectUtil.getProjectProperties(projectLocation);
            String projectBuildType =
                pio.readValue(CommonConstants.PROJECT_BUILD_TYPE);
            String anyframeHomeLocation = pio.readValue(CommonConstants.ANYFRAME_HOME);
            String projectName = pio.readValue(CommonConstants.PROJECT_NAME);
            // 2. make arguments
            CommandVO vo = new CommandVO();
            vo.setCommand(CommandUtil.CMD_UPDATE_CATALOG);
            vo.setProjectHome(pjtHomeLocation);
            vo.setAnyframeHome(anyframeHomeLocation);
            vo.setBasedir(pjtHomeLocation);
            vo.setProjectName(projectName);

            // 3. run ant
            AbstractCommandFactory factory =
                new CommandFactory(projectBuildType);
            factory.newInstance().execute(vo);

        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.getpluginlist"),
                IStatus.ERROR, e);
        }
    }
}
