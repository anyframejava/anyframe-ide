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
package org.anyframe.ide.eclipse.core.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.anyframe.ide.command.cli.CLIAntRunner;
import org.anyframe.ide.command.cli.util.CommandUtil;
import org.anyframe.ide.command.cli.util.Messages;
import org.anyframe.ide.command.cli.util.PluginConstants;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

/**
 * This is a AntCommandUtil class.
 * @author Sooyeon Park
 */
public class AntCommandUtil extends org.anyframe.ide.command.cli.CLIAntRunner {

    public static String anyframeHome = System.getenv("ANYFRAME_HOME");
    public static String projectHome = "." + SLASH;

    public static void setProjectHome(String projectHome) {
        AntCommandUtil.projectHome = projectHome;
        CLIAntRunner.projectHome = projectHome;
    }

    public static Map<String, Object> getAntInformation(String[] args) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {

            // prepare ant properties
            AntCommandUtil antcommand = new AntCommandUtil();
            Properties antProperties = setRequiredOptions(args);
            String projectHome =
                antProperties.getProperty(CommonConstants.PROJECT_HOME);
            setProjectHome(projectHome);

            antcommand.prepare(args);

            String command = args[0];
            Path buildXml = null;
            Properties properties = null;

            // TODO 나중에 비교로직 삭제 필요 (gen anyframe
            // command 비교 필요없음)
            if (!command.equals(CommandUtil.CMD_CREATE_PROJECT)
                && CommandUtil.containsCommand(command)) {

                properties =
                    CLIAntRunner.getAntProperties(command, null, null, args);
                properties.setProperty(CommonConstants.PROJECT_HOME,
                    projectHome);

                buildXml =
                    new Path(anyframeHome + SLASH + "ide" + SLASH + "cli"
                        + SLASH + "scripts" + SLASH + "plugin-install.xml");
            } else {
                properties =
                    AntCommandUtil.getAntProperties(command, null, null, args);
                String buildXmlFileName = CommandUtil.getBuildXmlFile(command);
                buildXml =
                    new Path(anyframeHome + SLASH + "ide" + SLASH + "cli"
                        + SLASH + "scripts" + SLASH + buildXmlFileName);
            }
            result.put("buildfile", buildXml);
            result.put("antProps", properties);
            result.put("target", command);

            // print ant command
            // String goal = command + " " +
            // properties.;
            // String executeCommand =
            // "Executing [anyframe " + goal + "]";
            //
            // ExceptionUtil.showException("[COMMAND] "
            // + executeCommand,
            // IStatus.INFO, null);

        } catch (Exception e) {
            ExceptionUtil.showException(Messages.EXCEPTION_ERROR,
                IStatus.ERROR, e);
        }

        return result;
    }

    public void prepare(String[] args) throws Exception {
        // in case of no arguments
        if (args == null || args.length == 0) {
            ExceptionUtil.showException(Messages.NO_ARGS, IStatus.ERROR, null);
            return;
        }

        String command = args[0];
        if (!checkCommand(command))
            return;

        if (!command.equals(CommandUtil.CMD_CREATE_PROJECT)
            && CommandUtil.containsCommand(command)) {
            doPrepare(args);
        }
    }

    public void doPrepare(String[] args) throws Exception {

        // [Check #1] in case of no arguments
        if (args == null || args.length == 0) {
            ExceptionUtil.showException(Messages.NO_ARGS, IStatus.ERROR, null);
        }

        String command = args[0];
        // [Check #2] in case, command doesn't exist or
        // command is '-help'
        if (!checkIsCommandNotFound(command, args))
            return;

        // [Check #3] in case, ide command executed
        // without project.mf
        if (!checkIsValidGenCommand(command, args))
            return;

        // [Check #4] in case, anyframe command
        // executed with proper
        // project.type config
        if (!checkIsValidCoreCommand(command, args))
            return;
    }

    private boolean checkIsCommandNotFound(String command, String[] args) {

        if (!CommandUtil.containsCommandBuildXml(command)
            && !CommandUtil.containsCommand(command)) {

            if (command.equals(CommandUtil.CMD_HELP)) {
                if (args.length < 3) {
                    ExceptionUtil.showException(Messages.ANT_HELP,
                        IStatus.INFO, null);
                } else {

                    if (Messages.ANT_HELP_MESSAGES_BY_COMMAND
                        .containsKey(args[2].trim())) {
                        String commandMessage =
                            Messages.ANT_HELP_MESSAGES_BY_COMMAND.get(args[2]
                                .trim());
                        ExceptionUtil.showException(commandMessage,
                            IStatus.INFO, null);
                    } else {
                        ExceptionUtil.showException(
                            "No description found for name: " + args[2].trim(),
                            IStatus.INFO, null);
                    }
                }
            } else {
                ExceptionUtil.showException(Messages.WRONG_ARGS, IStatus.ERROR,
                    null);
            }
            return false;
        }

        return true;
    }

    private boolean checkIsValidCoreCommand(String command, String[] args)
            throws Exception {

        if (!CommandUtil.containsCommand(command)
            || command.equals(CommandUtil.CMD_CREATE_PROJECT)
            || command.equals(CommandUtil.CMD_LIST)) {
            return true;
        }

        String metadataDir = projectHome + SLASH + META_INF;
        if (projectHome.endsWith(SLASH))
            metadataDir = projectHome + META_INF;
        File metadata = new File(metadataDir);
        if (!metadata.exists() || !metadata.isDirectory()) {
            ExceptionUtil.showException(Messages.NOT_SUPPORTED_FOLDER_ERROR,
                IStatus.ERROR, null);
            return false;
        }

        if (!isValidProjectTypeInBuildPropertiesFile(command)) {
            ExceptionUtil.showException(Messages.NOT_SUPPORTED_ERROR,
                IStatus.ERROR, null);
            return false;
        }

        if (!checkArgs(args))
            return false;

        return true;
    }

    private static boolean checkArgs(String[] args) {
        // in case of no arguments
        if (args == null || args.length == 0) {
            ExceptionUtil.showException(Messages.NO_ARGS, IStatus.ERROR, null);
            return false;
        }

        if (!checkCommand(args))
            return false;

        String pluginName = "";
        int idx = 1;

        if (CommandUtil.containsPluginNameNecessaryCommand(args[0])) {
            // in case, commands necessary pluginName
            // (eg. uninstall,
            // install-lib, uninstall-lib)
            if (args.length < 2 || args[1].substring(0, 1).equals("-")) {
                ExceptionUtil.showException(Messages.WRONG_PLUGIN_NAME,
                    IStatus.ERROR, null);
                return false;

            } else {
                pluginName = args[1];
                idx = 2;
            }

        } else if (CommandUtil.containsPluginNameOptionalCommand(args[0])) {
            // in case, commands optional pluginName
            // (eg.install)
            if (args.length < 2 || args[1].substring(0, 1).equals("-")) {
                idx = 1;

            } else {
                pluginName = args[1];
                idx = 2;
            }
        }

        if (!validateArgs(args, idx))
            return false;

        System.setProperty("name", pluginName);
        return true;
    }

    private boolean isValidProjectTypeInBuildPropertiesFile(String command) {
        PropertiesIO pio = getProjectManifest();

        if (pio == null
            || pio.readValue(PluginConstants.PROJECT_TYPE).equals(
                PluginConstants.PROJECT_TYPE_SERVICE)) {
            if (command.equals(CommandUtil.CMD_RUN)) {
                ExceptionUtil.showException(Messages.NOT_SUPPORTED_COMMAND,
                    IStatus.ERROR, null);
                return false;
            }
        }

        return true;
    }

    private static PropertiesIO getProjectManifest() {
        String fileFolderPath = projectHome;
        if (!projectHome.endsWith(SLASH)) {
            fileFolderPath += SLASH;
        }

        PropertiesIO pio = null;
        try {
            pio =
                new PropertiesIO(fileFolderPath + "META-INF" + SLASH
                    + "project.mf");
        } catch (Exception e) {
            // ignore exception
        }
        return pio;
    }

    private static boolean checkCommand(String[] args) {
        // in case, args[0] doesn't exist or args[0] is
        // '-help'
        if (!CommandUtil.containsCommand(args[0])) {

            if (args[0].equals(CommandUtil.CMD_HELP)) {
                if (args.length < 3) {
                    ExceptionUtil.showException(Messages.ANT_HELP,
                        IStatus.INFO, null);
                } else {
                    if (Messages.ANT_HELP_MESSAGES_BY_COMMAND
                        .containsKey(args[2].trim())) {
                        String commandMessage =
                            Messages.ANT_HELP_MESSAGES_BY_COMMAND.get(args[2]
                                .trim());
                        ExceptionUtil.showException(commandMessage,
                            IStatus.INFO, null);
                    } else {
                        ExceptionUtil.showException(
                            "No description found for name: " + args[2].trim(),
                            IStatus.INFO, null);
                    }
                }
            } else {
                ExceptionUtil.showException(Messages.WRONG_ARGS, IStatus.ERROR,
                    null);
            }

            return false;
        }
        return true;
    }

    private static boolean validateArgs(String[] args, int idx) {
        // in case, argument pair is defined wrongly
        if ((args.length - idx) % 2 == 1) {
            ExceptionUtil.showException(Messages.WRONG_ARG_PAIR, IStatus.ERROR,
                null);
            return false;
        }

        return true;
    }

    private boolean checkIsValidGenCommand(String command, String[] args)
            throws Exception {

        if (CommandUtil.containsCommandBuildXml(command)
            && !command.equals(CommandUtil.CMD_CREATE_PROJECT)) {

            String projectmf =
                projectHome + SLASH + META_INF + SLASH + PROJECT_MF;
            if (projectHome.endsWith(SLASH))
                projectmf = projectHome + META_INF + SLASH + PROJECT_MF;

            File appPropertiesFile = new File(projectmf);
            if (!appPropertiesFile.exists()) {
                ExceptionUtil.showException(Messages.INSIDE_PROJECT,
                    IStatus.ERROR, null);
                return false;
            }
        }

        if (args[0].equals(CommandUtil.CMD_CREATE_CRUD)) {
            if (!checkIsValidCrudCommand(args))
                return false;
        }

        if (args[0].equals(CommandUtil.CMD_CREATE_MODEL)) {
            if (!checkIsValidGenModelCommand(args))
                return false;
        }

        return true;
    }

    private boolean checkIsValidGenModelCommand(String[] args) throws Exception {
        if (args.length < 1) {
            ExceptionUtil.showException(Messages.NO_ARGS,
                IStatus.ERROR, null);
            return false;
        }
        return true;
    }

    private boolean checkIsValidCrudCommand(String[] args) {
        if (args.length < 2 || args[1].substring(0, 1).equals("-")) {
            ExceptionUtil.showException(Messages.NO_ARGS,
                IStatus.ERROR, null);
            return false;
        }
        return true;
    }

    private static boolean checkCommand(String command) {
        // in case, command doesn't exist or command is
        // '-help'
        if (!CommandUtil.containsCommand(command)
            && !CommandUtil.containsCommandBuildXml(command)) {
            if (command.equals(CommandUtil.CMD_HELP))
                ExceptionUtil
                    .showException(Messages.ANT_HELP, IStatus.OK, null);
            else
                ExceptionUtil.showException(Messages.WRONG_ARGS, IStatus.INFO,
                    null);
            return false;
        }
        return true;
    }

    public static Properties getAntProperties(String target, String buildXml,
            String[] properties, String[] args) throws Exception {
        Properties antProperties = setRequiredOptions(args);

        // check entity property
        if (args[0].equals(CommandUtil.CMD_CREATE_CRUD)) {
            if (args.length < 2) {
                ExceptionUtil.showException(Messages.NO_ARGS, IStatus.ERROR,
                    null);
                return null;
            }
            antProperties.setProperty("entity", args[1]);
        }

        String pluginType = "";
        if (args[0].equals(CommandUtil.CMD_CREATE_CRUD)) {
            pluginType = args[1];
        } else
            pluginType = CommonConstants.CORE_PLUGIN;
        antProperties.setProperty("pluginType", pluginType);
        antProperties.setProperty("pluginName", pluginType);

        return antProperties;
    }

    private static Properties setRequiredOptions(String[] args) {
        Properties antProperties = new Properties();
        String basedirFromArgs = "";

        int i =
            (args[0].equals(CommandUtil.CMD_CREATE_CRUD)
                || CommandUtil.containsPluginNameNecessaryCommand(args[0]) || CommandUtil
                .containsPluginNameOptionalCommand(args[0])) ? 2 : 1;
        for (; i < args.length; i = i + 2) {
            String option = args[i];

            if (!option.substring(0, 1).equals("-")) {
                ExceptionUtil.showException(Messages.WRONG_ARGS_IGNORED,
                    IStatus.INFO, null);
                continue;
            }

            if (option.equals("-anyframeHome"))
                anyframeHome = args[i + 1];
            if (option.equals("-pjthome") || option.equals("-project.home"))
                projectHome = args[i + 1];
            if (option.equals("-basedir"))
                basedirFromArgs = args[i + 1];

            antProperties.setProperty(args[i].substring(1), args[i + 1]);
        }

        // set env ant properties
        if (anyframeHome == null || anyframeHome.length() == 0) {
            PropertiesIO pio = getAppProperties(projectHome);
            anyframeHome = pio.readValue(CommonConstants.ANYFRAME_HOME);
        }
        String basedir = ".";
        if (projectHome.equals("." + SLASH)
            && args[0].equals(CommandUtil.CMD_CREATE_PROJECT))
            basedir = anyframeHome + SLASH + "applications";
        else if (!projectHome.equals("." + SLASH)) {
            basedir = projectHome;
            antProperties
                .setProperty(CommonConstants.PROJECT_HOME, projectHome);
        }

        System.setProperty("anyframeHome", anyframeHome);

        antProperties.setProperty("anyframeHome", anyframeHome);
        antProperties.setProperty("ant.home", anyframeHome + SLASH + "ide"
            + SLASH + CommonConstants.PROJECT_BUILD_TYPE_ANT);
        if (basedirFromArgs.length() > 0)
            basedir = basedirFromArgs;
        antProperties.setProperty("basedir", basedir);

        return antProperties;
    }

    private static PropertiesIO getAppProperties(String projectHome) {
        try {
            return new PropertiesIO(projectHome + CommonConstants.METAINF
                + CommonConstants.METADATA_FILE);
        } catch (Exception e) {
            ExceptionUtil.showException(Messages.NO_ARGS, IStatus.ERROR, e);
            return null;
        }
    }
}
