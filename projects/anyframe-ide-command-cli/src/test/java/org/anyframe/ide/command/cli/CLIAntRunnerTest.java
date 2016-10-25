/*
 * Copyright 2002-2008 the original author or authors.
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
import java.util.Properties;

import org.anyframe.ide.command.cli.CLIAntRunner;
import org.anyframe.ide.command.cli.util.PluginConstants;

import junit.framework.TestCase;

/**
 * TestCase Name : CLIAntRunnerTest <br>
 * <br>
 * [Description] : Test for CLIAntRunner<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : check if command arguments ('pluginName') is valid</li>
 * <li>#-2 Positive Case : check if command arguments('options') is valid</li>
 * <li>#-3 Positive Case : get ant properties</li>
 * </ul>
 */
public class CLIAntRunnerTest extends TestCase {
	CLIAntRunner runner;

	/**
	 * initialize
	 */
	public void setUp() throws Exception {
		runner = new CLIAntRunner();
		runner.anyframeHome = "";
		runner.projectHome = "./src/test/resources";
	}

	/**
	 * [Flow #-1] Positive Case : check if command arguments ('pluginName') is
	 * valid.
	 * 
	 * @throws Exception
	 */
	public void testPrepare() throws Exception {
		// 1. execute CLIAntRunner with 'create-project' command
		String[] args = new String[] { "create-project" };
		runner.prepare(args);		

		// 2. execute CLIAntRunner with 'install' command
		args = new String[] { "install", PluginConstants.FOUNDATION_PLUGIN };
		runner.prepare(args);

		assertEquals("Fail to check no arguments.", PluginConstants.FOUNDATION_PLUGIN, System
				.getProperty("pluginName"));
	}

	/**
	 * [Flow #-2] Positive Case : check if command arguments('options') is
	 * valid.
	 * 
	 * @throws Exception
	 */
	public void testMakeAntCommand() throws Exception {
		String[] antArgs = runner.makeAntCommand("install", new String[] {
				"hibernate", "-package", "anyframe" });

		assertEquals("Fail to compare input args to output.", "install",
				antArgs[2]);
		assertEquals("Fail to check target.", new File(".").getAbsolutePath(),
				System.getProperty("target"));
		assertEquals("Fail to check package.", "anyframe", System
				.getProperty("package"));
	}

	/**
	 * [Flow #-3] Positive Case : get ant properties.
	 * 
	 * @throws Exception
	 */
	public void testGetAntProperties() throws Exception {
		Properties properties = runner.getAntProperties("install", "",
				new String[] { "" }, new String[] { "install", "hibernate",
						"-package", "anyframe", "-repoType", "local" });

		assertEquals("Fail to get ant properties - package.", "anyframe",
				properties.get("package"));
		assertEquals("Fail to get ant properties - repoType.", "local",
				properties.get("repoType"));
	}
}

