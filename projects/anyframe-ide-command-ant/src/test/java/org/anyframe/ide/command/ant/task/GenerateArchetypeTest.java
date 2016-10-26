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
package org.anyframe.ide.command.ant.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.tools.ant.BuildException;

/**
 * TestCase Name : GenerateArchetypeTest <br>
 * <br>
 * [Description] : Test for Custom task 'GenerateArchetypeTask'<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : execute GenerateArchetypeTask</li>
 * <li>#-2 Negative Case : After generating basic archetype, try to generate
 * again</li>
 * </ul>
 * 
 * @author Soyon Lim
 */
public class GenerateArchetypeTest extends AbstractTaskTest {
	private GenerateArchetypeTask generateArchetypeTask;

	/**
	 * initialize
	 */
	public void setUp() throws Exception {
		super.setUp();

		generateArchetypeTask = new GenerateArchetypeTask();

		// 1. set ant properties for generating basic archetype
		generateArchetypeTask.setRepo(new File(".").getAbsolutePath()
				+ CommonConstants.SRC_TEST_RESOURCES + "repo");
		generateArchetypeTask.setAnyframeHome(anyframeHome);
		generateArchetypeTask.setPjtname("myproject");
		generateArchetypeTask.setPackage("anyframe");
		generateArchetypeTask.setTarget(new File("./temp").getAbsolutePath());
		generateArchetypeTask.setArchetypeGroupId("org.anyframe.archetype");
		generateArchetypeTask
				.setArchetypeArtifactId("anyframe-basic-archetype");
	}

	/**
	 * [Flow #-1] Positive Case : execute GenerateArchetypeTask.
	 * 
	 * @throws Exception
	 */
	public void testGenerateArchetype() throws Exception {
		File baseDir = new File("./temp/myproject");

		// 1. try to generate basic archetype
		generateArchetype(baseDir);

		// 2. delete a temporary directory for next test
		FileUtil.deleteDir(temporaryDir, new ArrayList<String>());
	}

	/**
	 * [Flow #-2] Negative Case : After generating basic archetype, try to
	 * generate again.
	 * 
	 * @throws Exception
	 */
	public void testGenerateArchetypeAgain() throws Exception {
		File baseDir = new File("./temp/myproject");

		// 1. try to generate basic archetype
		generateArchetype(baseDir);

		// 2. try to generate basic archetype again
		try {
			generateArchetype(baseDir);
			fail("Fail to check validation.");
		} catch (BuildException e) {
			assertEquals(
					"Fail to compare exception message.",
					"You already generated a basic or service archetype. Try anyframe -help.",
					e.getMessage());
		}

		// 3. delete a temporary directory for next test
		FileUtil.deleteDir(temporaryDir, new ArrayList<String>());
	}

	public void generateArchetype(File baseDir) throws Exception {
		generateArchetypeTask.execute();
		// 3. assert
		Map<String, PluginInfo> installedPlugins = generateArchetypeTask
				.getPluginInfoManager().getInstalledPlugins(
						baseDir.getAbsolutePath());
		assertEquals("Fail to generate a archetype.", 0, installedPlugins
				.size());

		File mainJava = new File(baseDir + "/src/main/java");
		assertTrue("Fail to generate a archetype.", mainJava.exists());

		File mainRrc = new File(baseDir + "/src/main/resources");
		assertTrue("Fail to generate a archetype.", mainRrc.exists());

		File testJava = new File(baseDir + "/src/test/java");
		assertTrue("Fail to generate a archetype.", testJava.exists());

		File testRrc = new File(baseDir + "/src/test/resources");
		assertTrue("Fail to generate a archetype.", testRrc.exists());

		File indexHtml = new File(baseDir + "/src/main/webapp", "index.html");
		assertTrue("Fail to generate a archetype.", indexHtml.exists());

		File webAppLib = new File(baseDir + "/src/main/webapp/WEB-INF/lib");

		assertTrue("Fail to generate a archetype.", !webAppLib.exists());
	}
}
