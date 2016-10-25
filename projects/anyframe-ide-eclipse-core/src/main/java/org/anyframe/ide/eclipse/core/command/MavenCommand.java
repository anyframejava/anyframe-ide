/*   
 * Copyright 2008-2011 the original author or authors.   
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
package org.anyframe.ide.eclipse.core.command;

import org.anyframe.ide.command.cli.util.CommandUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.command.maven.mojo.container.PluginContainer;
import org.anyframe.ide.eclipse.core.AnyframeIDEPlugin;
import org.anyframe.ide.eclipse.core.ProjectCreationPostProcess;
import org.anyframe.ide.eclipse.core.command.maven.AnyframeMavenLaunchConfiguration;
import org.anyframe.ide.eclipse.core.command.vo.CommandVO;
import org.anyframe.ide.eclipse.core.command.vo.CreateCRUDVO;
import org.anyframe.ide.eclipse.core.command.vo.CreateModelVO;
import org.anyframe.ide.eclipse.core.command.vo.CreatePJTVO;
import org.anyframe.ide.eclipse.core.command.vo.InstallPluginVO;
import org.anyframe.ide.eclipse.core.command.vo.UninstallPluginVO;
import org.anyframe.ide.eclipse.core.preferences.IdePreferencesPage;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.PluginUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.downloader.Downloader;
import org.apache.maven.archetype.generator.ArchetypeGenerator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This is a MavenCommand class.
 * @author Sooyeon Park
 */
public class MavenCommand implements Command {

    private IPreferenceStore store = null;
    private String logLevel = CommonConstants.LOG_LEVEL_ERROR;
    private String logOption = "-q";

    public MavenCommand() {
        store = AnyframeIDEPlugin.getDefault().getPreferenceStore();
        logLevel = store.getString(IdePreferencesPage.LOG_LEVEL);
        if (logLevel.equals(CommonConstants.LOG_LEVEL_INFO))
            logOption = "";
        else if (logLevel.equals(CommonConstants.LOG_LEVEL_DEBUG))
            logOption = "--debug";
    }

    public void execute(CommandVO commandVo) {
        try {
            String command = commandVo.getCommand();
            String mvnCommand = "anyframe:" + command;

            if (command.equals(CommandUtil.CMD_CREATE_CRUD)) {

                CreateCRUDVO vo = (CreateCRUDVO) commandVo;
                mvnCommand +=
                    " -Dentity=" + vo.getDomainClassName() + " -Dpackage="
                        + vo.getPackageName() + " -Dprojecthome="
                        + vo.getProjectHome() + " -Dscope=" + vo.getScope()
                        + " -Dbasedir=" + vo.getBasedir()
                        + " -DisCLIMode=false " + logOption;
                launchMaven(mvnCommand, vo);
            } else if (command.equals(CommandUtil.CMD_CREATE_MODEL)) {

                CreateModelVO vo = (CreateModelVO) commandVo;
                mvnCommand +=
                    " -Dtable=" + vo.getTableName() + " -Dpackage="
                        + vo.getPackageName() + " -Dbasedir=" + vo.getBasedir()
                        + " -DisCLIMode=false " + logOption;
                launchMaven(mvnCommand, vo);
            } else if (command.equals(CommandUtil.CMD_CHANGE_DB)) {

                mvnCommand +=
                    " -Dbasedir=" + commandVo.getBasedir() + " " + logOption;
                launchMaven(mvnCommand, commandVo);
            } else if (command.equals(CommandUtil.CMD_INSTALL)) {

                InstallPluginVO vo = (InstallPluginVO) commandVo;
                mvnCommand +=
                    " -Dname=" + vo.getPluginNames() + " -Dpackage="
                        + vo.getPackageName() + " -Dbasedir=" + vo.getBasedir()
                        + " -DisCLIMode=false" + " -DexcludeSrc="
                        + new Boolean(vo.isExcludeSrc()).toString() + " "
                        + logOption;
                launchMaven(mvnCommand, vo);
            } else if (command.equals(CommandUtil.CMD_UNINSTALL)) {
                UninstallPluginVO vo = (UninstallPluginVO) commandVo;
                mvnCommand +=
                    " -Dname=" + vo.getPluginNames() + " -Dbasedir="
                        + vo.getBasedir() + " " + logOption;
                launchMaven(mvnCommand, vo);
            } else if (command.equals(CommandUtil.CMD_UPDATE_CATALOG)) {

                mvnCommand +=
                    " -Dbasedir=" + commandVo.getBasedir() + " " + logOption;
                launchMaven(mvnCommand, commandVo);
            } else if (command.equals(CommandUtil.CMD_CREATE_PROJECT)) {
                CreatePJTVO vo = (CreatePJTVO) commandVo;
                createProject(vo);
            }
        } catch (Exception e) {
            ExceptionUtil.showException(e.getMessage(), IStatus.ERROR, e);
        }
    }

    public void launchMaven(String mvnCommand, CommandVO vo)
            throws CoreException {
        AnyframeMavenLaunchConfiguration conf =
            new AnyframeMavenLaunchConfiguration();

        ILaunchConfiguration launchConfiguration =
            conf.createLaunchConfiguration(mvnCommand, vo);

        DebugUITools.launch(launchConfiguration, "run");
    }

    private void createProject(final CreatePJTVO vo) {
        try {
            // 1. create archetype
            String basedir = vo.getBasedir();
            String archetypeGroudId = CommonConstants.ARCHETYPE_GROUP_ID;
            String archetypeArtifactId =
                CommonConstants.ARCHETYPE_BASIC_ARTIFACT_ID;
            String archetypeVersion = "";

            if (vo.getProjectType()
                .equals(CommonConstants.PROJECT_TYPE_SERVICE)) {
                archetypeArtifactId =
                    CommonConstants.ARCHETYPE_SERVICE_ARTIFACT_ID;
                archetypeVersion =
                    store.getString(IdePreferencesPage.SERVICE_ARCHETYPE);
                if (StringUtils.isEmpty(archetypeVersion))
                    archetypeVersion =
                        PluginUtil.getLatestArchetypeVersion(
                            archetypeArtifactId,
                            CommonConstants.PROJECT_BUILD_TYPE_MAVEN, null,
                            false);
            } else {
                archetypeVersion =
                    store.getString(IdePreferencesPage.BASIC_ARCHETYPE);
                if (StringUtils.isEmpty(archetypeVersion))
                    archetypeVersion =
                        PluginUtil.getLatestArchetypeVersion(
                            archetypeArtifactId,
                            CommonConstants.PROJECT_BUILD_TYPE_MAVEN, null,
                            false);
            }

            String artifactGroupdId = vo.getProjectGroupId();
            String artifactArtifactId = vo.getProjectName();
            String artifactVersion = vo.getProjectVersion();

            PluginContainer container = new PluginContainer(null);
            ArchetypeGenerationRequest request = container.getRequest();

            // download archetype
            Downloader downloader =
                (Downloader) container.lookup(Downloader.class.getName());
            downloader.download(archetypeGroudId, archetypeArtifactId,
                archetypeVersion, null, request.getLocalRepository(),
                request.getRemoteArtifactRepositories());

            // generate from archetype
            request.setArchetypeGroupId(archetypeGroudId);
            request.setArchetypeArtifactId(archetypeArtifactId);
            request.setArchetypeVersion(archetypeVersion);
            request.setGroupId(artifactGroupdId);
            request.setArtifactId(artifactArtifactId);
            request.setVersion(artifactVersion);
            request.setPackage(vo.getPackageName());
            request.setInteractiveMode(false);
            request.setOutputDirectory(vo.getBasedir());

            ArchetypeGenerationResult result = new ArchetypeGenerationResult();
            ArchetypeGenerator generator =
                (ArchetypeGenerator) container.lookup(ArchetypeGenerator.class
                    .getName());
            generator.generateArchetype(request, result);
            Exception cause = result.getCause();
            if (cause != null) {
                String msg =
                    "Unable to create project from archetype "
                        + archetypeArtifactId;
                throw new CoreException(new Status(IStatus.ERROR,
                    AnyframeIDEPlugin.ID, -1, msg, cause));
            }

            // 2. import project
            ProjectUtil.importProject(basedir, artifactArtifactId);
            // 3. add maven project nature
            ProjectUtil.enableNature(artifactArtifactId);

            // 4. modify project.home, db.lib
            String projectHome =
                vo.getBasedir() + ProjectUtil.SLASH + artifactArtifactId;
            String properties =
                projectHome + CommonConstants.METAINF
                    + CommonConstants.METADATA_FILE;
            PropertiesIO appProps = new PropertiesIO(properties);
            appProps.setProperty(CommonConstants.PROJECT_HOME, projectHome);
            appProps.write();

            // 5. modify db information
            ProjectCreationPostProcess postProcess =
                new ProjectCreationPostProcess(vo);
            postProcess.doAfterProjectCreation(projectHome);

            // 6. install core plugin
            String mvnCommand =
                "anyframe:install -Dname=core -Dpackage=" + vo.getPackageName()
                    + " -DisCLIMode=false -Dbasedir=" + projectHome
                    + " -DtemplateHome=\"" + vo.getTemplateHome() + "\""
                    + " -DinspectionHome=\"" + vo.getInspectionHome() + "\" "
                    + logOption;

            vo.setBasedir(projectHome);
            launchMaven(mvnCommand, vo);

        } catch (Exception e) {
            DialogUtil.openDetailMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("exception.log.createproject"),
                e.getMessage(), MessageDialog.ERROR);

            ExceptionUtil.showException(e.getMessage(), IStatus.ERROR, e);
        }
    }
}
