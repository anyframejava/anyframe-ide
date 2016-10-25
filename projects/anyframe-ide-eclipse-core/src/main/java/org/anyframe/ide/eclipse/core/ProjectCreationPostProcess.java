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
package org.anyframe.ide.eclipse.core;

import org.anyframe.ide.command.cli.util.CommandUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.eclipse.core.command.vo.CommandVO;
import org.anyframe.ide.eclipse.core.command.vo.CreatePJTVO;
import org.anyframe.ide.eclipse.core.util.DatabaseUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.PostProcess;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;

/**
 * This is a ProjectCreationPostProcess class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class ProjectCreationPostProcess implements PostProcess {
    private CreatePJTVO vo = null;
    public static String SLASH = ProjectUtil.SLASH;

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
                            ExceptionUtil.showException(MessageUtil
                                .getMessage("editor.exception.timesleep"),
                                IStatus.ERROR, e);
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
                            ExceptionUtil.showException(MessageUtil
                                .getMessage("editor.exception.timesleep"),
                                IStatus.ERROR, e);
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

        // 2. modify /.metadata/project.mf file
        try {
            PropertiesIO appProps =
                new PropertiesIO(projectHome + CommonConstants.METAINF
                    + CommonConstants.METADATA_FILE);
            appProps.setProperty(CommonConstants.PROJECT_TEMPLATE_HOME,
                vo.getTemplateHome());

            // save jdbc information
            appProps.setProperty(CommonConstants.DB_DRIVER_PATH,
                vo.getDatabaseDriverPath());
            appProps.setProperty(CommonConstants.DB_TYPE, vo.getDatabaseType());
            appProps.setProperty(CommonConstants.DB_NAME, vo.getDatabaseName());
            appProps.setProperty(CommonConstants.DB_SCHEMA,
                vo.getDatabaseSchema());
            appProps.setProperty(CommonConstants.DB_USERNAME,
                vo.getDatabaseUserId());
            appProps.setProperty(CommonConstants.DB_PASSWORD,
                vo.getDatabasePassword());
            appProps.setProperty(CommonConstants.DB_SERVER,
                vo.getDatabaseServer());
            appProps.setProperty(CommonConstants.DB_PORT, vo.getDatabasePort());
            appProps.setProperty(CommonConstants.DB_DIALECT,
                vo.getDatabaseDialect());
            appProps.setProperty(CommonConstants.DB_DRIVER_CLASS,
                vo.getDatabaseDriver());

            String url =
                DatabaseUtil.getDbUrl(vo.getDatabaseType(),
                    vo.getDatabaseName(), vo.getDatabaseServer(),
                    vo.getDatabasePort());
            appProps.setProperty(CommonConstants.DB_URL, url);

            if (appProps.readValue(CommonConstants.PROJECT_BUILD_TYPE).equals(
                CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
                appProps.setProperty(CommonConstants.DB_GROUPID,
                    vo.getDatabaseGroupId());
                appProps.setProperty(CommonConstants.DB_ARTIFACTID,
                    vo.getDatabaseArtifactId());
                appProps.setProperty(CommonConstants.DB_VERSION,
                    vo.getDatabaseVersion());
            }

            appProps.write();
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.request"),
                IStatus.ERROR, e);
        }
    }
}
