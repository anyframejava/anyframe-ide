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
package org.anyframe.ide.command.maven.mojo;

import org.anyframe.ide.command.cli.util.Messages;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This is an HelpMojo class. This mojo is for showing how to use anyframe
 * custom mojos based on Maven
 * 
 * @goal help
 * @author Soyon Lim
 */
public class HelpMojo extends AbstractMojo {

	/**
	 * The command name.
	 * 
	 * @parameter expression="${command}"
	 */
	private String command = "";

	/**
	 * main method for executing HelpMojo. This mojo is executed when you input
	 * 'mvn anyframe:help'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			if (StringUtils.isEmpty(command)) {
				System.out.println(Messages.MAVEN_HELP);
			} else {

				if (Messages.MAVEN_HELP_MESSAGES_BY_COMMAND
						.containsKey(command)) {
					String commandMessage = Messages.MAVEN_HELP_MESSAGES_BY_COMMAND
							.get(command);

					System.out.println(commandMessage);
				} else {
					System.out.println("No description found for name: "
							+ command);
				}
			}

		} catch (Exception ex) {
			getLog().error(
					"Fail to execute HelpMojo. The reason is '"
							+ ex.getMessage() + "'.");
			throw new MojoFailureException(null);
		}
	}
}
