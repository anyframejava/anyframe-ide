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
package org.anyframe.ide.codegenerator;

import org.anyframe.ide.codegenerator.command.vo.CommandVO;
import org.anyframe.ide.codegenerator.command.vo.CreatePJTVO;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.PostProcess;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.command.cli.util.CommandUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.common.databases.JdbcOption;
import org.anyframe.ide.common.util.ConfigXmlUtil;
import org.anyframe.ide.common.util.EncryptUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.ProjectConfig;
import org.eclipse.swt.widgets.Display;

/**
 * This is a ProjectCreationPostProcess class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class ProjectCreationPostProcess implements PostProcess {
	private CreatePJTVO vo = null;
	public static final String SLASH = ProjectUtil.SLASH;
	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	public ProjectCreationPostProcess(CommandVO vo) {
		this.vo = (CreatePJTVO) vo;
	}

	public void execute(String[] config) {

		if (config != null) {

			if (config[0].equals(CommandUtil.CMD_INSTALL)) {
				final String appLocation = vo.getBasedir();
				// get project name
				final String pjtName = vo.getProjectName();

				// modify project structure
				String projectHome = appLocation + SLASH + pjtName;
				doAfterProjectCreation(projectHome);

				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							PluginLoggerUtil.error(ID,
									Message.view_exception_timesleep, e);
						}
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								ProjectUtil.importProject(appLocation, pjtName);
								ProjectUtil.refreshProject(pjtName);
							}
						});
					}
				}).start();
			} else if (config[0].equals(CommandUtil.CMD_CHANGE_DB)) {
				// get project name
				final String pjtName = vo.getProjectName();
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							PluginLoggerUtil.error(ID,
									Message.view_exception_timesleep, e);
						}
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								ProjectUtil.refreshProject(pjtName);
							}
						});
					}
				}).start();
			}
		} else {
			if (vo.getCommand().equals(CommandUtil.CMD_CREATE_PROJECT)) {
				ProjectUtil.refreshProject(vo.getProjectName());
			}
		}
	}

	public void doAfterProjectCreation(String projectHome) {

		// 2. modify common configuration file
		try {
			String configFile = ConfigXmlUtil.getCommonConfigFile(projectHome);
			
			// common config file
			ProjectConfig projectConfig = ConfigXmlUtil.getProjectConfig(configFile);
			projectConfig.setAnyframeHome(vo.getAnyframeHome());
			projectConfig.setOffline(String.valueOf(vo.isOffline()));
			projectConfig.setPackageName(vo.getPackageName());
			projectConfig.setPjtHome(projectHome);
			projectConfig.setPjtName(vo.getProjectName());
			projectConfig.setTemplateHomePath(vo.getTemplateHome());
			projectConfig.setDatabasesPath(projectHome + CommonConstants.fileSeparator + CommonConstants.SETTING_HOME);
			projectConfig.setJdbcdriverPath(projectHome + CommonConstants.fileSeparator + CommonConstants.SETTING_HOME);

			ConfigXmlUtil.saveProjectConfig(projectConfig);
			
			// default databaase
			JdbcOption jdbcOption = ConfigXmlUtil.getDefaultDatabase(projectConfig);
			jdbcOption.setDbName(vo.getDatabaseName());
			jdbcOption.setDbType(vo.getDatabaseType());
			jdbcOption.setDefault(true);
			jdbcOption.setDialect(vo.getDatabaseDialect());
			jdbcOption.setDriverClassName(vo.getDatabaseDriver());
			jdbcOption.setDriverJar(vo.getDatabaseDriverPath());
			jdbcOption.setMvnArtifactId(vo.getDatabaseArtifactId());
			jdbcOption.setMvnGroupId(vo.getDatabaseGroupId());
			jdbcOption.setMvnVersion(vo.getDatabaseVersion());
			jdbcOption.setPassword(EncryptUtil.encrypt(vo.getDatabasePassword()));
			jdbcOption.setProjectName(vo.getProjectName());
			jdbcOption.setSchema(vo.getDatabaseSchema());
			jdbcOption.setUrl(vo.getDatabaseUrl());
			jdbcOption.setUserName(vo.getDatabaseUserId());

			jdbcOption.setRunExplainPaln(false);//default
			jdbcOption.setUseDbSpecific(false);//default
			
			ConfigXmlUtil.saveDatabase(projectHome, jdbcOption);
			
		} catch (Exception e) {
			PluginLoggerUtil.error(ID, Message.view_exception_request, e);
		}
	}
}
