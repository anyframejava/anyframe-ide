/*   
 * Copyright 2002-2013 the original author or authors.   
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
package org.anyframe.ide.codegenerator.command.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.codegenerator.CodeGenPostProcess;
import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.DBConfigChangePostProcess;
import org.anyframe.ide.codegenerator.ModelGenPostProcess;
import org.anyframe.ide.codegenerator.PluginInstallPostProcess;
import org.anyframe.ide.codegenerator.PluginUninstallPostProcess;
import org.anyframe.ide.codegenerator.ProjectCreationPostProcess;
import org.anyframe.ide.codegenerator.command.MavenCommand;
import org.anyframe.ide.codegenerator.command.vo.CommandVO;
import org.anyframe.ide.codegenerator.command.vo.CreatePJTVO;
import org.anyframe.ide.codegenerator.util.MavenJob;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.PostProcess;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.codegenerator.util.XmlFileUtil;
import org.anyframe.ide.command.cli.util.CommandUtil;
import org.anyframe.ide.command.maven.mojo.container.PluginContainer;
import org.anyframe.ide.common.dialog.JdbcType;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.XMLUtil;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Display;
import org.maven.ide.eclipse.internal.launch.MavenLaunchDelegate.BackgroundResourceRefresher;

/**
 * This is an AnyframeBackgroundResourceRefresher class.
 * 
 * @author Sooyeon Park
 */
public class AnyframeBackgroundResourceRefresher extends
		BackgroundResourceRefresher {
	final ILaunchConfiguration configuration;

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

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
			List<CommandVO> voList = configuration.getAttribute(
					AnyframeMavenLaunchConfiguration.ATTR_COMMAND_VO,
					new ArrayList());
			CommandVO vo = voList.get(0);
			String command = vo.getCommand();
			if (command.equals(CommandUtil.CMD_CREATE_MODEL)) {
				postProcess = new ModelGenPostProcess(vo);
			} else if (command.equals(CommandUtil.CMD_CREATE_CRUD)) {
				postProcess = new CodeGenPostProcess(vo);
			} else if (command.equals(CommandUtil.CMD_CHANGE_DB)
					|| command.equals(CommandUtil.CMD_CHANGE_DB
							+ " after creation")) {
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
					try {
						jdbcTypes = (java.util.List<JdbcType>) XmlFileUtil
								.getObjectFromInputStream(XMLUtil
										.getJdbcConfigFileInputStream());
					} catch (IOException e) {
						PluginLoggerUtil.error(CodeGeneratorActivator.PLUGIN_ID,
								Message.wizard_application_error_cannotgetjdbcconfiguration, e);
					}

					PluginContainer container = null;
					try {
						container = new PluginContainer(null);
					} catch (Exception e) {
						PluginLoggerUtil.error(ID, e.getMessage(), e);
					}
					ArchetypeGenerationRequest request = container.getRequest();

					for (JdbcType jdbcType : jdbcTypes) {

						if (jdbcType.getType().equals("hsqldb")) {

							if (pjtvo.getDatabaseUserId().equals("SA")
									&& pjtvo.getDatabasePassword().equals("")
									&& pjtvo.getDatabaseDialect().equals(
											jdbcType.getDialect()[0])
									&& pjtvo.getDatabaseDriver().equals(
											jdbcType.getDriver())
									&& pjtvo.getDatabaseDriverPath().equals(
											request.getLocalRepository()
													.getBasedir()
													+ ProjectUtil.SLASH
													+ "hsqldb"
													+ ProjectUtil.SLASH
													+ "hsqldb"
													+ ProjectUtil.SLASH
													+ "2.0.0"
													+ ProjectUtil.SLASH
													+ "hsqldb-2.0.0.jar")
									&& pjtvo.getDatabaseSchema().equals(
											"PUBLIC")
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
								PluginLoggerUtil.error(ID,
										Message.view_exception_timesleep, e);
							}
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									String mvnCommand = "anyframe:"
											+ CommandUtil.CMD_CHANGE_DB
											+ " -Dbasedir="
											+ pjtvo.getProjectHome() + "\"";
									MavenCommand launchCommand = new MavenCommand();
									try {
										pjtvo.setCommand(CommandUtil.CMD_CHANGE_DB
												+ " after creation");
										launchCommand.launchMaven(mvnCommand,
												pjtvo);
									} catch (CoreException e) {
										PluginLoggerUtil
												.error(ID,
														Message.view_exception_failtochagnedbinfo,
														e);
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
			PluginLoggerUtil.error(ID, Message.view_exception_failtorefreshresources, e);
		}
	}
}
