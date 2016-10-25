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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.command.cli.util.CommandUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.eclipse.core.AnyframeIDEPlugin;
import org.anyframe.ide.eclipse.core.command.vo.CommandVO;
import org.anyframe.ide.eclipse.core.command.vo.CreateCRUDVO;
import org.anyframe.ide.eclipse.core.command.vo.CreateModelVO;
import org.anyframe.ide.eclipse.core.command.vo.CreatePJTVO;
import org.anyframe.ide.eclipse.core.command.vo.InstallPluginVO;
import org.anyframe.ide.eclipse.core.command.vo.UninstallPluginVO;
import org.anyframe.ide.eclipse.core.config.JdbcType;
import org.anyframe.ide.eclipse.core.preferences.IdePreferencesPage;
import org.anyframe.ide.eclipse.core.util.AntExecution;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.PluginUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.anyframe.ide.eclipse.core.util.XmlFileUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This is an AntCommand class.
 * @author Sooyeon Park
 */
public class AntCommand implements Command {

    public void execute(CommandVO commandVo) {

        List<String[]> antConfigList = new ArrayList<String[]>();
        String command = commandVo.getCommand();

        if (command.equals(CommandUtil.CMD_CREATE_CRUD)) {

            CreateCRUDVO vo = (CreateCRUDVO) commandVo;
            String[] args =
                {command, vo.getDomainClassName(), "-project",
                    vo.getProjectName(), "-package", vo.getPackageName(),
                    "-scope", vo.getScope(), "-project.home",
                    vo.getProjectHome(), "-anyframeHome", vo.getAnyframeHome(),
                    "-basedir", vo.getBasedir(), "-isCLIMode", "false" };
            antConfigList.add(args);

        } else if (command.equals(CommandUtil.CMD_CREATE_MODEL)) {

            CreateModelVO vo = (CreateModelVO) commandVo;
            String[] args =
                {command, "-table", vo.getTableName(), "-package",
                    vo.getPackageName(), "-project.home", vo.getProjectHome(),
                    "-anyframeHome", vo.getAnyframeHome(), "-basedir",
                    vo.getBasedir(), "-isCLIMode", "false" };
            antConfigList.add(args);

        } else if (command.equals(CommandUtil.CMD_CHANGE_DB)) {

            String[] args =
                {command, "-project.home", commandVo.getProjectHome(),
                    "-anyframeHome", commandVo.getAnyframeHome() };
            antConfigList.add(args);

        } else if (command.equals(CommandUtil.CMD_INSTALL)) {

            InstallPluginVO vo = (InstallPluginVO) commandVo;

            String[] args =
                {command, vo.getPluginNames(), "-target", vo.getBasedir(),
                    "-excludeSrc", new Boolean(vo.isExcludeSrc()).toString(),
                    "-anyframeHome", commandVo.getAnyframeHome(),
                    "-project.home", commandVo.getProjectHome(),
                    "-log4j.ignoreTCL", "true", "-isCLIMode", "false" };
            antConfigList.add(args);

        } else if (command.equals(CommandUtil.CMD_UNINSTALL)) {

            UninstallPluginVO vo = (UninstallPluginVO) commandVo;
            String[] args =
                {command, vo.getPluginNames(), "-target", vo.getBasedir(),
                    "-anyframeHome", commandVo.getAnyframeHome(),
                    "-project.home", commandVo.getProjectHome() };
            antConfigList.add(args);

        } else if (command.equals(CommandUtil.CMD_UPDATE_CATALOG)) {

            String[] args =
                {command, "-project.home", commandVo.getProjectHome(),
                    "-anyframeHome", commandVo.getAnyframeHome(), "-target",
                    commandVo.getBasedir() };
            antConfigList.add(args);

        } else if (command.equals(CommandUtil.CMD_CREATE_PROJECT)) {
            try {
                CreatePJTVO vo = (CreatePJTVO) commandVo;
                String archetypeGroudId = CommonConstants.ARCHETYPE_GROUP_ID;
                String archetypeArtifactId =
                    CommonConstants.ARCHETYPE_BASIC_ARTIFACT_ID;
                String archetypeVersion = "";

                IPreferenceStore store =
                    AnyframeIDEPlugin.getDefault().getPreferenceStore();
                if (vo.getProjectType().equals(
                    CommonConstants.PROJECT_TYPE_SERVICE)) {
                    archetypeArtifactId =
                        CommonConstants.ARCHETYPE_SERVICE_ARTIFACT_ID;
                    archetypeVersion =
                        store.getString(IdePreferencesPage.SERVICE_ARCHETYPE);
                    if (StringUtils.isEmpty(archetypeVersion))
                        archetypeVersion =
                            PluginUtil.getLatestArchetypeVersion(
                                archetypeArtifactId,
                                CommonConstants.PROJECT_BUILD_TYPE_ANT,
                                vo.getAnyframeHome(), vo.isOffline());
                } else {
                    archetypeVersion =
                        store.getString(IdePreferencesPage.BASIC_ARCHETYPE);
                    if (StringUtils.isEmpty(archetypeVersion))
                        archetypeVersion =
                            PluginUtil.getLatestArchetypeVersion(
                                archetypeArtifactId,
                                CommonConstants.PROJECT_BUILD_TYPE_ANT,
                                vo.getAnyframeHome(), vo.isOffline());
                }
                String[] projectArgs =
                    {command, "-pjttype", vo.getProjectType(), "-pjtname",
                        vo.getProjectName(), "-package", vo.getPackageName(),
                        "-anyframeHome", vo.getAnyframeHome(), "-target",
                        vo.getBasedir(), "-basedir", vo.getBasedir(),
                        "-project.home", vo.getProjectHome(),
                        "-archetypeGroudId", archetypeGroudId,
                        "-archetypeArtifactId", archetypeArtifactId,
                        "-archetypeVersion", archetypeVersion, "-offline",
                        new Boolean(vo.isOffline()).toString() };
                // for(int
                // i=0;i<projectArgs.length;i++){
                // ExceptionUtil.showException("[AntCommand] i="+i+" projectArgs[i]="+projectArgs[i],
                // IStatus.ERROR, null);
                // }
                antConfigList.add(projectArgs);

                String[] installArgs =
                    {
                        CommandUtil.CMD_INSTALL,
                        CommonConstants.CORE_PLUGIN,
                        "-target",
                        vo.getBasedir() + ProjectUtil.SLASH
                            + vo.getProjectName(),
                        "-anyframeHome",
                        vo.getAnyframeHome(),
                        "-pjthome",
                        vo.getBasedir() + ProjectUtil.SLASH
                            + vo.getProjectName(), "-package",
                        vo.getPackageName(), "-pjtname", vo.getProjectName(),
                        "-log4j.ignoreTCL", "true", "-isCLIMode", "false" };
                antConfigList.add(installArgs);

                boolean dbChangeJobEnabled = true;

                if (vo.getDatabaseType().equals("hsqldb")) {
                    java.util.List<JdbcType> jdbcTypes = null;
                    File jdbcConfigFile =
                        new File(vo.getAnyframeHome() + ProjectUtil.SLASH
                            + "ide" + ProjectUtil.SLASH + "db"
                            + ProjectUtil.SLASH + "jdbc.config");
                    if (jdbcConfigFile.exists()) {
                        try {
                            jdbcTypes =
                                (java.util.List<JdbcType>) XmlFileUtil
                                    .getObjectFromInputStream(new FileInputStream(
                                        jdbcConfigFile));
                        } catch (FileNotFoundException e) {
                            ExceptionUtil.showException(MessageUtil
                                .getMessage("editor.exception.loadjdbcconfig"),
                                IStatus.ERROR, e);
                        }
                    } else {
                        jdbcTypes =
                            (java.util.List<JdbcType>) XmlFileUtil
                                .getObjectFromInputStream(this.getClass()
                                    .getClassLoader()
                                    .getResourceAsStream("jdbc.config"));
                    }

                    for (JdbcType jdbcType : jdbcTypes) {
                        if (jdbcType.getType().equals("hsqldb")) {
                            if (vo.getDatabaseName().equals("sampledb")
                                && vo.getDatabaseUserId().equals("SA")
                                && vo.getDatabasePassword().equals("")
                                && vo.getDatabaseServer().equals("localhost")
                                && vo.getDatabasePort().equals(
                                    jdbcType.getPort())
                                && vo.getDatabaseDialect().equals(
                                    jdbcType.getDialect()[0])
                                && vo.getDatabaseDriver().equals(
                                    jdbcType.getDriver())
                                && vo.getDatabaseDriverPath().equals(
                                    vo.getAnyframeHome() + ProjectUtil.SLASH
                                        + "ide" + ProjectUtil.SLASH + "db"
                                        + ProjectUtil.SLASH + "lib"
                                        + ProjectUtil.SLASH
                                        + "hsqldb-2.0.0.jar")
                                && vo.getDatabaseSchema().equals("PUBLIC")) {
                                dbChangeJobEnabled = false;
                            }
                            break;
                        }
                    }
                }

                if (dbChangeJobEnabled) {
                    String[] changedbArgs =
                        {
                            CommandUtil.CMD_CHANGE_DB,
                            "-pjthome",
                            vo.getBasedir() + ProjectUtil.SLASH
                                + vo.getProjectName(), "-anyframeHome",
                            vo.getAnyframeHome() };
                    antConfigList.add(changedbArgs);
                }
            } catch (Exception e) {
                DialogUtil.openDetailMessageDialog(
                    MessageUtil.getMessage("ide.message.title"),
                    MessageUtil.getMessage("exception.log.createproject"),
                    e.getMessage(), MessageDialog.ERROR);

                ExceptionUtil.showException(e.getMessage(), IStatus.ERROR, e);
            }
        }

        AntExecution.runAnt(antConfigList, commandVo);
    }
}
