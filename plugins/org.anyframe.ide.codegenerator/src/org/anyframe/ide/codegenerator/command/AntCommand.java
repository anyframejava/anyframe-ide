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
package org.anyframe.ide.codegenerator.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.command.vo.CommandVO;
import org.anyframe.ide.codegenerator.command.vo.CreateCRUDVO;
import org.anyframe.ide.codegenerator.command.vo.CreateModelVO;
import org.anyframe.ide.codegenerator.command.vo.CreatePJTVO;
import org.anyframe.ide.codegenerator.command.vo.InstallPluginVO;
import org.anyframe.ide.codegenerator.command.vo.UninstallPluginVO;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.preferences.IdePreferencesPage;
import org.anyframe.ide.codegenerator.util.AntExecution;
import org.anyframe.ide.codegenerator.util.PluginUtil;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.codegenerator.util.XmlFileUtil;
import org.anyframe.ide.command.cli.util.CommandUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.dialog.JdbcType;
import org.anyframe.ide.common.properties.PropertiesSettingUtil;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.XMLUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This is an AntCommand class.
 * 
 * @author Sooyeon Park
 */
public class AntCommand implements Command {

	private IPreferenceStore store = null;
	private String logLevel = CommonConstants.LOG_LEVEL_ERROR;
	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	public AntCommand() {
		store = CodeGeneratorActivator.getDefault().getPreferenceStore();
		logLevel = store.getString(IdePreferencesPage.LOG_LEVEL).toLowerCase();
	}

	public void execute(CommandVO commandVo) {

		List<String[]> antConfigList = new ArrayList<String[]>();
		String command = commandVo.getCommand();

		if (command.equals(CommandUtil.CMD_CREATE_CRUD)) {
			CreateCRUDVO vo = (CreateCRUDVO) commandVo;
			String[] args = { command, vo.getDomainClassName(), "-project",
					vo.getProjectName(), "-package", vo.getPackageName(),
					"-scope", vo.getScope(), "-insertSampleData",
					vo.getInsertSampleData(), "-project.home",
					vo.getProjectHome(), "-anyframeHome", vo.getAnyframeHome(),
					"-basedir", vo.getBasedir(), "-isCLIMode", "false",
					"-logLevel", logLevel };
			antConfigList.add(args);

		} else if (command.equals(CommandUtil.CMD_CREATE_MODEL)) {

			CreateModelVO vo = (CreateModelVO) commandVo;
			String[] args = { command, "-table", vo.getTableName(), "-package",
					vo.getPackageName(), "-project.home", vo.getProjectHome(),
					"-anyframeHome", vo.getAnyframeHome(), "-basedir",
					vo.getBasedir(), "-isCLIMode", "false", "-logLevel",
					logLevel };
			antConfigList.add(args);

		} else if (command.equals(CommandUtil.CMD_CHANGE_DB)) {

			String[] args = { command, "-project.home",
					commandVo.getProjectHome(), "-anyframeHome",
					commandVo.getAnyframeHome(), "-logLevel", logLevel };
			antConfigList.add(args);

		} else if (command.equals(CommandUtil.CMD_INSTALL)) {

			InstallPluginVO vo = (InstallPluginVO) commandVo;

			String[] args = { command, vo.getPluginNames(), "-target",
					vo.getBasedir(), "-excludeSrc",
					new Boolean(vo.isExcludeSrc()).toString(), "-anyframeHome",
					commandVo.getAnyframeHome(), "-project.home",
					commandVo.getProjectHome(), "-log4j.ignoreTCL", "true",
					"-isCLIMode", "false", "-logLevel", logLevel };
			antConfigList.add(args);

		} else if (command.equals(CommandUtil.CMD_UNINSTALL)) {

			UninstallPluginVO vo = (UninstallPluginVO) commandVo;
			String[] args = { command, vo.getPluginNames(), "-target",
					vo.getBasedir(), "-anyframeHome",
					commandVo.getAnyframeHome(), "-project.home",
					commandVo.getProjectHome(), "-logLevel", logLevel };
			antConfigList.add(args);

		} else if (command.equals(CommandUtil.CMD_UPDATE_CATALOG)) {

			String[] args = { command, "-project.home",
					commandVo.getProjectHome(), "-anyframeHome",
					commandVo.getAnyframeHome(), "-target",
					commandVo.getBasedir(), "-logLevel", logLevel };
			antConfigList.add(args);

		} else if (command.equals(CommandUtil.CMD_CREATE_PROJECT)) {
			try {
				CreatePJTVO vo = (CreatePJTVO) commandVo;
				String archetypeGroudId = CommonConstants.ARCHETYPE_GROUP_ID;
				String archetypeArtifactId = CommonConstants.ARCHETYPE_BASIC_ARTIFACT_ID;
				String archetypeVersion = "";

				IPreferenceStore store = CodeGeneratorActivator.getDefault()
						.getPreferenceStore();
				if (vo.getProjectType().equals(
						CommonConstants.PROJECT_TYPE_SERVICE)) {
					archetypeArtifactId = CommonConstants.ARCHETYPE_SERVICE_ARTIFACT_ID;
					archetypeVersion = store
							.getString(IdePreferencesPage.SERVICE_ARCHETYPE);
					if (StringUtils.isEmpty(archetypeVersion))
						archetypeVersion = PluginUtil
								.getLatestArchetypeVersion(archetypeArtifactId,
										CommonConstants.PROJECT_BUILD_TYPE_ANT,
										vo.getAnyframeHome(), vo.isOffline());
				} else {
					archetypeVersion = store
							.getString(IdePreferencesPage.BASIC_ARCHETYPE);
					if (StringUtils.isEmpty(archetypeVersion))
						archetypeVersion = PluginUtil
								.getLatestArchetypeVersion(archetypeArtifactId,
										CommonConstants.PROJECT_BUILD_TYPE_ANT,
										vo.getAnyframeHome(), vo.isOffline());
				}
				String[] projectArgs = { command, "-pjttype",
						vo.getProjectType(), "-pjtname", vo.getProjectName(),
						"-package", vo.getPackageName(), "-anyframeHome",
						vo.getAnyframeHome(), "-target", vo.getBasedir(),
						"-basedir", vo.getBasedir(), "-project.home",
						vo.getProjectHome(), "-archetypeGroudId",
						archetypeGroudId, "-archetypeArtifactId",
						archetypeArtifactId, "-archetypeVersion",
						archetypeVersion, "-offline",
						new Boolean(vo.isOffline()).toString(), "-logLevel",
						logLevel };
				// for(int
				// i=0;i<projectArgs.length;i++){
				// ExceptionUtil.showException("[AntCommand] i="+i+" projectArgs[i]="+projectArgs[i],
				// IStatus.ERROR, null);
				// }
				antConfigList.add(projectArgs);

				String[] installArgs = {
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
						"-log4j.ignoreTCL", "true", "-isCLIMode", "false",
						"-logLevel", logLevel };
				antConfigList.add(installArgs);

				boolean dbChangeJobEnabled = true;

				if (vo.getDatabaseType().equals("hsqldb")) {
					java.util.List<JdbcType> jdbcTypes = null;
					File jdbcConfigFile = new File(PropertiesSettingUtil.JDBCDRIVERS_LOC);
					if (jdbcConfigFile.exists()) {
						try {
							jdbcTypes = (java.util.List<JdbcType>) XmlFileUtil
									.getObjectFromInputStream(new FileInputStream(
											jdbcConfigFile));
						} catch (FileNotFoundException e) {
							PluginLoggerUtil.error(ID,
									Message.view_exception_loadjdbcconfig, e);
						}
					} else {
						try {
							jdbcTypes = (java.util.List<JdbcType>) XmlFileUtil
									.getObjectFromInputStream(XMLUtil
											.getJdbcConfigFileInputStream());
						} catch (IOException e) {
							PluginLoggerUtil.error(
									CodeGeneratorActivator.PLUGIN_ID,
									Message.wizard_application_error_cannotgetjdbcconfiguration, e);
						}
					}

					for (JdbcType jdbcType : jdbcTypes) {
						if (jdbcType.getType().equals("hsqldb")) {
							if (vo.getDatabaseUserId().equals("SA")
									&& vo.getDatabasePassword().equals("")
									&& vo.getDatabaseDialect().equals(
											jdbcType.getDialect()[0])
									&& vo.getDatabaseDriver().equals(
											jdbcType.getDriver())
									&& vo.getDatabaseDriverPath().equals(
											vo.getAnyframeHome()
													+ ProjectUtil.SLASH + "ide"
													+ ProjectUtil.SLASH + "db"
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
					String[] changedbArgs = {
							CommandUtil.CMD_CHANGE_DB,
							"-pjthome",
							vo.getBasedir() + ProjectUtil.SLASH
									+ vo.getProjectName(), "-anyframeHome",
							vo.getAnyframeHome(), "-logLevel", logLevel };
					antConfigList.add(changedbArgs);
				}
			} catch (Exception e) {
				MessageDialogUtil.openDetailMessageDialog(ID,
						Message.ide_message_title,
						Message.exception_log_createproject, e.getMessage(),
						MessageDialog.ERROR);
				PluginLoggerUtil.error(ID, e.getMessage(), e);
			}
		}

		AntExecution.runAnt(antConfigList, commandVo);
	}
}
