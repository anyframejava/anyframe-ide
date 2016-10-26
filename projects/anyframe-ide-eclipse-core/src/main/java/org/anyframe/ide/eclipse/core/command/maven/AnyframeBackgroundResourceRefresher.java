/*   
 * Copyright 2002-2012 the original author or authors.   
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
package org.anyframe.ide.eclipse.core.command.maven;

import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.command.cli.util.CommandUtil;
import org.anyframe.ide.command.maven.mojo.container.PluginContainer;
import org.anyframe.ide.eclipse.core.CodeGenPostProcess;
import org.anyframe.ide.eclipse.core.DBConfigChangePostProcess;
import org.anyframe.ide.eclipse.core.ModelGenPostProcess;
import org.anyframe.ide.eclipse.core.PluginInstallPostProcess;
import org.anyframe.ide.eclipse.core.PluginUninstallPostProcess;
import org.anyframe.ide.eclipse.core.ProjectCreationPostProcess;
import org.anyframe.ide.eclipse.core.command.MavenCommand;
import org.anyframe.ide.eclipse.core.command.vo.CommandVO;
import org.anyframe.ide.eclipse.core.command.vo.CreatePJTVO;
import org.anyframe.ide.eclipse.core.config.JdbcType;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MavenJob;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.PostProcess;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.anyframe.ide.eclipse.core.util.XmlFileUtil;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Display;
import org.maven.ide.eclipse.internal.launch.MavenLaunchDelegate.BackgroundResourceRefresher;

/**
 * This is an AnyframeBackgroundResourceRefresher
 * class.
 * @author Sooyeon Park
 */
public class AnyframeBackgroundResourceRefresher extends
        BackgroundResourceRefresher {
    final ILaunchConfiguration configuration;

    public AnyframeBackgroundResourceRefresher(
            ILaunchConfiguration configuration, ILaunch launch) {
        super(configuration, launch);
        this.configuration = configuration;
    }

    protected void processResources() {
        super.processResources();

        try {
            final MavenJob job = new MavenJob("Refreshing resources...");
            PostProcess postProcess = null;
            List<CommandVO> voList =
                configuration.getAttribute(
                    AnyframeMavenLaunchConfiguration.ATTR_COMMAND_VO,
                    new ArrayList());
            CommandVO vo = voList.get(0);
            String command = vo.getCommand();
            if (command.equals(CommandUtil.CMD_CREATE_MODEL)) {
                postProcess = new ModelGenPostProcess(vo);
            } else if (command.equals(CommandUtil.CMD_CREATE_CRUD)) {
                postProcess = new CodeGenPostProcess(vo);
            } else if (command.equals(CommandUtil.CMD_CHANGE_DB)
                || command
                    .equals(CommandUtil.CMD_CHANGE_DB + " after creation")) {
                postProcess = new DBConfigChangePostProcess(vo);
            } else if (command.equals(CommandUtil.CMD_INSTALL)) {
                postProcess = new PluginInstallPostProcess(vo);
            } else if (command.equals(CommandUtil.CMD_UNINSTALL)) {
                postProcess = new PluginUninstallPostProcess(vo);
            } else if (command.equals(CommandUtil.CMD_CREATE_PROJECT)) {
                // 7. execute change-db command
                final CreatePJTVO pjtvo = (CreatePJTVO) vo;

                boolean dbChangeJobEnabled = true;

                if (pjtvo.getDatabaseType().equals("hsqldb")) {
                    java.util.List<JdbcType> jdbcTypes = null;
                    jdbcTypes =
                        (java.util.List<JdbcType>) XmlFileUtil
                            .getObjectFromInputStream(this.getClass()
                                .getClassLoader()
                                .getResourceAsStream("jdbc.config"));

                    PluginContainer container = null;
                    try {
                        container = new PluginContainer(null);
                    } catch (Exception e) {
                        ExceptionUtil.showException(e.getMessage(),
                            IStatus.ERROR, e);
                    }
                    ArchetypeGenerationRequest request = container.getRequest();

                    for (JdbcType jdbcType : jdbcTypes) {

                        if (jdbcType.getType().equals("hsqldb")) {

                            if (pjtvo.getDatabaseName().equals("sampledb")
                                && pjtvo.getDatabaseUserId().equals("SA")
                                && pjtvo.getDatabasePassword().equals("")
                                && pjtvo.getDatabaseServer()
                                    .equals("localhost")
                                && pjtvo.getDatabasePort().equals(
                                    jdbcType.getPort())
                                && pjtvo.getDatabaseDialect().equals(
                                    jdbcType.getDialect()[0])
                                && pjtvo.getDatabaseDriver().equals(
                                    jdbcType.getDriver())
                                && pjtvo.getDatabaseDriverPath().equals(
                                    request.getLocalRepository().getBasedir()
                                        + ProjectUtil.SLASH + "hsqldb"
                                        + ProjectUtil.SLASH + "hsqldb"
                                        + ProjectUtil.SLASH + "2.0.0"
                                        + ProjectUtil.SLASH
                                        + "hsqldb-2.0.0.jar")
                                && pjtvo.getDatabaseSchema().equals("PUBLIC")
                                && pjtvo.getDatabaseGroupId().equals(
                                    jdbcType.getDriverGroupId())
                                && pjtvo.getDatabaseArtifactId().equals(
                                    jdbcType.getDriverArtifactId())
                                && pjtvo.getDatabaseVersion().equals(
                                    jdbcType.getDriverVersion())) {
                                dbChangeJobEnabled = false;
                            }
                            break;
                        }
                    }
                }

                if (dbChangeJobEnabled) {
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                ExceptionUtil.showException(MessageUtil
                                    .getMessage("editor.exception.timesleep"),
                                    IStatus.ERROR, e);
                            }
                            Display.getDefault().asyncExec(new Runnable() {
                                public void run() {
                                    String mvnCommand =
                                        "anyframe:" + CommandUtil.CMD_CHANGE_DB
                                            + " -Dbasedir="
                                            + pjtvo.getProjectHome() + "\"";
                                    MavenCommand launchCommand =
                                        new MavenCommand();
                                    try {
                                        pjtvo
                                            .setCommand(CommandUtil.CMD_CHANGE_DB
                                                + " after creation");
                                        launchCommand.launchMaven(mvnCommand,
                                            pjtvo);
                                    } catch (CoreException e) {
                                        ExceptionUtil.showException(
                                            "failed to change db information",
                                            IStatus.ERROR, e);
                                    }
                                }
                            });
                        }
                    }).start();
                }
                postProcess = new ProjectCreationPostProcess(vo);
            }
            job.setPostProcess(postProcess);
            job.schedule();
        } catch (CoreException e) {
            ExceptionUtil.showException("failed to refresh resources.",
                IStatus.ERROR, e);
        }
    }
}
