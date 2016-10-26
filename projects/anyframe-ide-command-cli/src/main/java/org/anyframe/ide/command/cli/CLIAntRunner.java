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
package org.anyframe.ide.command.cli;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.anyframe.ide.command.cli.util.CommandUtil;
import org.anyframe.ide.command.cli.util.Messages;
import org.anyframe.ide.command.cli.util.PluginConstants;
import org.anyframe.ide.command.cli.util.PropertiesIO;
import org.apache.tools.ant.launch.Launcher;

/**
 * This is a CLIAntRunner class.
 * 
 * @author Sooyeon Park
 */
public class CLIAntRunner {

	public static String anyframeHome = System.getenv("ANYFRAME_HOME");
	public static String SLASH = System.getProperty("file.separator");
	public static String applicationHome = "." + SLASH;
	public static String projectHome = "." + SLASH;
	public static String META_INF = "META-INF";
	public static String PROJECT_MF = "project.mf";

	public static void main(String[] args) {
		try {
			System.out.println(Messages.WELCOME + anyframeHome + "\n");

			CLIAntRunner runner = new CLIAntRunner();
			runner.prepare(args);
			runner.execute(args);

		} catch (Exception e) {
			System.err.println(Messages.EXCEPTION_ERROR);
		}
	}

	public void prepare(String[] args) throws Exception {

		// [Check #1] in case of no arguments
		if (args == null || args.length == 0) {
			System.err.println(Messages.NO_ARGS);
			System.exit(0);
		}

		String command = args[0];
		// [Check #2] in case, command doesn't exist or command is '-help'
		checkIsCommandNotFound(command, args);

		// [Check #3] in case, ide command executed without project.mf
		checkIsValidGenCommand(command, args);

		// [Check #4] in case, anyframe command executed with proper
		// project.type config
		checkIsValidCoreCommand(command, args);
	}

	public void execute(String[] args) throws Exception {
		String command = args[0];
		if (!command.equals(CommandUtil.CMD_CREATE_PROJECT)
				&& CommandUtil.containsCommand(command)) {
			redirectToParentExecute(args, command);
			return;
		}

		String buildXmlFile = CommandUtil.getBuildXmlFile(command);
		String[] argArgs = makeAntCommand(buildXmlFile, command, args);
		setSystemPropertyBeforeExecute(command, args);
		Launcher.main(argArgs);
	}

	private void redirectToParentExecute(String[] args, String command)
			throws IOException, Exception {
		// set project home path
		projectHome = new File(".").getCanonicalPath();
		// Set default property value
		// If there are same args options, default value will be overwritten
		// while processing super.execute()
		if (CommandUtil.CMD_INSTALL.equals(command)
				|| CommandUtil.CMD_UNINSTALL.equals(command)) {
			System.setProperty("project.home", projectHome);
			System.setProperty("log4j.ignoreTCL", "true");
			PropertiesIO pio = getProjectManifest();

			System.setProperty("project.home", projectHome);
			System.setProperty("pjtname", pio
					.readValue(PluginConstants.PROJECT_NAME));
			System.setProperty("package", pio
					.readValue(PluginConstants.PACKAGE_NAME));
			System.setProperty("metadata", ".");
		}

		Launcher.main(makeAntCommand(args[0], args));
	}

	private void checkIsCommandNotFound(String command, String[] args) {

		if (!CommandUtil.containsCommandBuildXml(command)
				&& !CommandUtil.containsCommand(command)) {
			
			if (command.equals(CommandUtil.CMD_HELP)) {
				if (args.length < 3) {
					System.out.println(Messages.ANT_HELP);
				} else {

					if (Messages.ANT_HELP_MESSAGES_BY_COMMAND.containsKey(args[2].trim())) {
						String commandMessage = Messages.ANT_HELP_MESSAGES_BY_COMMAND
								.get(args[2].trim());

						System.out.println(commandMessage);
					} else {
						System.out.println("No description found for name: " + args[2].trim());
					}
				}
			} else {
				System.err.println(Messages.WRONG_ARGS);
			}
			System.exit(0);
		}
	}

	private void checkIsValidCoreCommand(String command, String[] args)
			throws Exception {

		if (!CommandUtil.containsCommand(command)
				|| command.equals(CommandUtil.CMD_CREATE_PROJECT)
				|| command.equals(CommandUtil.CMD_LIST)) {
			return;
		}

		String metadataDir = projectHome + SLASH + META_INF;
		if (projectHome.endsWith(SLASH))
			metadataDir = projectHome + META_INF;
		File metadata = new File(metadataDir);
		if (!metadata.exists() || !metadata.isDirectory()) {
			System.err.println(Messages.NOT_SUPPORTED_FOLDER_ERROR);
			System.exit(0);
		}

		if (!isValidProjectTypeInBuildPropertiesFile(command)) {
			System.err.println(Messages.NOT_SUPPORTED_ERROR);
			System.exit(0);
		}

		checkArgs(args);
	}

	private boolean isValidProjectTypeInBuildPropertiesFile(String command) {
		PropertiesIO pio = getProjectManifest();

		if (pio == null
				|| pio.readValue(PluginConstants.PROJECT_TYPE).equals(
						PluginConstants.PROJECT_TYPE_SERVICE)) {
			if (command.equals(CommandUtil.CMD_RUN)) {
				System.err.println(Messages.NOT_SUPPORTED_COMMAND);
				return false;
			}
		}

		return true;
	}

	private void checkIsValidGenCommand(String command, String[] args)
			throws Exception {

		if (CommandUtil.containsCommandBuildXml(command)
				&& !command.equals(CommandUtil.CMD_CREATE_PROJECT)) {

			String projectmf = projectHome + SLASH + META_INF + SLASH
					+ PROJECT_MF;
			if (projectHome.endsWith(SLASH))
				projectmf = projectHome + META_INF + SLASH + PROJECT_MF;

			File appPropertiesFile = new File(projectmf);
			if (!appPropertiesFile.exists()) {
				System.err.println(Messages.INSIDE_PROJECT);
				System.exit(0);
			}
		}

		if (args[0].equals(CommandUtil.CMD_CREATE_CRUD)) {
			checkIsValidCrudCommand(args);
		}

		if (args[0].equals(CommandUtil.CMD_CREATE_MODEL)) {
			checkIsValidGenModelCommand(args);
		}
	}

	private void checkIsValidGenModelCommand(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println(Messages.NO_ARGS);
			System.exit(0);
		}
	}

	private void checkIsValidCrudCommand(String[] args) {
		if (args.length < 2 || args[1].substring(0, 1).equals("-")) {
			System.err.println(Messages.NO_ARGS);
			System.exit(0);
		}
	}

	private static String[] makeAntCommand(String buildXmlFile,
			String targetName, String[] args) throws Exception {

		String[] antArgs = new String[3];
		antArgs[0] = "-f";
		antArgs[1] = anyframeHome + SLASH + "ide" + SLASH + "cli" + SLASH
				+ "scripts" + SLASH + buildXmlFile;
		antArgs[2] = targetName;

		return antArgs;
	}

	private static void setSystemPropertyBeforeExecute(String targetName,
			String[] args) {
		setSystemPropertyWithCmdLineArgs(args);

		if (args[0].equals(CommandUtil.CMD_CREATE_CRUD)) {
			if (args.length < 2 || args[1].substring(0, 1).equals("-")) {
				System.err.println(Messages.NO_ARGS);
				System.exit(0);
			}
			System.setProperty("entity", args[1]);
		}

		System.setProperty("ant.home", anyframeHome + SLASH + "ide" + SLASH
				+ "ant");
		System.setProperty("basedir", getProjectBaseDir(targetName));
		System.setProperty("anyframeHome", anyframeHome);
		System.setProperty("project.home", getProjectHomeDir(targetName));
		if (System.getProperty("target") == null) {
			System.setProperty("target", new File(".").getAbsolutePath());
		}
	}

	private static String getProjectBaseDir(String targetName) {
		String basedir = ".";
		if (applicationHome.equals("." + SLASH)
				&& targetName.equals(CommandUtil.CMD_CREATE_PROJECT)) {
			basedir = anyframeHome + SLASH + "applications";
			System.setProperty("target", basedir);
		} else if (!applicationHome.equals("." + SLASH)) {
			File applicationHomeFile = new File(applicationHome);

			if (!applicationHomeFile.exists()) {
				applicationHomeFile.mkdir();
			} else {
				if (!applicationHomeFile.isDirectory()) {
					System.err.println(Messages.WRONG_DIR_PATH);
					System.exit(0);
				}
			}

			basedir = applicationHome;
			System.setProperty("target", basedir);
		}
		return basedir;
	}

	private static String getProjectHomeDir(String targetName) {
		String pjtHome = ".";

		if (!projectHome.equals("." + SLASH)) {
			pjtHome = projectHome;
		} else if (projectHome.equals("." + SLASH)
				&& targetName.equals(CommandUtil.CMD_CREATE_PROJECT)) {
			if (applicationHome.equals("." + SLASH))
				pjtHome = anyframeHome + SLASH + "applications";
			else
				pjtHome = applicationHome;
		} else if (projectHome.equals("." + SLASH)) {
			pjtHome = new File(".").getAbsolutePath();
		}
		return pjtHome;
	}

	private static void setSystemPropertyWithCmdLineArgs(String[] args) {
		int i = (args[0].equals(CommandUtil.CMD_CREATE_CRUD)) ? 2 : 1;
		for (; i < args.length; i++) {
			String option = args[i];
			if (!option.substring(0, 1).equals("-")) {
				System.out.println(Messages.WRONG_ARGS_IGNORED);
				continue;
			}
			// check anyframe home, application home, project home
			if (option.equals("-anyframeHome") && args[i + 1].length() > 0) {
				anyframeHome = args[i + 1];
			}
			if (option.equals("-apphome")) {
				applicationHome = args[i + 1];
			}
			if (option.equals("-pjthome")) {
				projectHome = args[i + 1];
			}

			if (option.equals("-clean")) {
				System.setProperty(args[i].substring(1), "true");

			} else if (option.equals("-war")) {
				System.setProperty(args[i].substring(1), "true");

			} else {
				System.setProperty(args[i].substring(1), args[i + 1]);
				i++;
			}
		}
	}

	private static PropertiesIO getProjectManifest() {
		String fileFolderPath = projectHome;
		if (!projectHome.endsWith(SLASH)) {
			fileFolderPath += SLASH;
		}

		PropertiesIO pio = null;
		try {
			pio = new PropertiesIO(fileFolderPath + "META-INF" + SLASH
					+ "project.mf");
		} catch (Exception e) {
			// ignore exception
		}
		return pio;
	}

	public static String[] makeAntCommand(String targetName, String[] args)
			throws Exception {
		String[] antArgs = new String[3];
		antArgs[0] = "-f";
		antArgs[1] = anyframeHome + SLASH + "ide" + SLASH + "cli" + SLASH
				+ "scripts" + System.getProperty("file.separator")
				+ "plugin-install.xml";
		antArgs[2] = targetName;
		// antArgs[3] = "-quiet";

		System.setProperty("anyframeHome", anyframeHome);
		System.setProperty("target", new File(".").getAbsolutePath());

		int i = 1;

		if (CommandUtil.containsPluginNameNecessaryCommand(args[0])
				|| (CommandUtil.containsPluginNameOptionalCommand(args[0]) && (args.length > 1 && !args[1]
						.substring(0, 1).equals("-")))) {
			i = 2;
		}

		for (; i < args.length; i = i + 2) {
			if (!args[i].substring(0, 1).equals("-")) {
				System.out.println(Messages.WRONG_ARGS_IGNORED);
				continue;
			}
			System.setProperty(args[i].substring(1), args[i + 1]);
		}

		return antArgs;
	}

	public static Properties getAntProperties(String target, String buildXml,
			String[] properties, String[] args) throws Exception {
		checkArgs(args);
		Properties antProperties = new Properties();

		antProperties.setProperty("anyframeHome", System
				.getProperty("anyframeHome"));

		int i = 1;
		if (CommandUtil.containsPluginNameNecessaryCommand(args[0])
				|| (CommandUtil.containsPluginNameOptionalCommand(args[0]) && !args[1]
						.substring(0, 1).equals("-"))) {
			i = 2;
		}

		for (; i < args.length; i = i + 2) {
			if (!args[i].substring(0, 1).equals("-")) {
				System.out.println(Messages.WRONG_ARGS_IGNORED);
				continue;
			}
			antProperties.setProperty(args[i].substring(1), args[i + 1]);
		}

		return antProperties;
	}

	private static void checkArgs(String[] args) {
		// in case of no arguments
		if (args == null || args.length == 0) {
			System.err.println(Messages.NO_ARGS);
			System.exit(0);
		}

		checkCommand(args);

		String pluginName = "";
		int idx = 1;

		if (CommandUtil.containsPluginNameNecessaryCommand(args[0])) {
			// in case, commands necessary pluginName (eg. uninstall,
			// install-lib, uninstall-lib)
			if (args.length < 2 || args[1].substring(0, 1).equals("-")) {
				System.err.println(Messages.WRONG_PLUGIN_NAME);
				System.exit(0);

			} else {
				pluginName = args[1];
				idx = 2;
			}

		} else if (CommandUtil.containsPluginNameOptionalCommand(args[0])) {
			// in case, commands optional pluginName (eg.install)
			if (args.length < 2 || args[1].substring(0, 1).equals("-")) {
				idx = 1;

			} else {
				pluginName = args[1];
				idx = 2;
			}
		}

		validateArgs(args, idx);

		System.setProperty("name", pluginName);
	}

	private static void checkCommand(String[] args) {
		// in case, args[0] doesn't exist or args[0] is '-help'
		if (!CommandUtil.containsCommand(args[0])) {
			
			if (args[0].equals(CommandUtil.CMD_HELP)) {
				if (args.length < 3) {
					System.out.println(Messages.ANT_HELP);
					
				} else {
					if (Messages.ANT_HELP_MESSAGES_BY_COMMAND.containsKey(args[2].trim())) {
						String commandMessage = Messages.ANT_HELP_MESSAGES_BY_COMMAND
								.get(args[2].trim());

						System.out.println(commandMessage);
					} else {
						System.out.println("No description found for name: " + args[2].trim());
					}
				}
			} else {
				System.err.println(Messages.WRONG_ARGS);
			}
			
			System.exit(0);
		}
	}

	private static void validateArgs(String[] args, int idx) {
		// in case, argument pair is defined wrongly
		if ((args.length - idx) % 2 == 1) {
			System.err.println(Messages.WRONG_ARG_PAIR);
			System.exit(0);
		}
	}
}
