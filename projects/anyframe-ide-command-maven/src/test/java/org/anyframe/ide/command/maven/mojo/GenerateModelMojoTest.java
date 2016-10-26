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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.maven.project.MavenProject;

/**
 * This is a GenerateModelMojoTest class.
 * 
 * @author Sooyeon Park
 */
public class GenerateModelMojoTest extends AbstractMojoTest{

	File baseDir = new File("./src/test/resources/project/sample");

	protected void setUp() throws Exception {
		super.setUp();

		if (new File(baseDir, "/src/main/java").exists()) {
			FileUtil.deleteDir(new File(baseDir, "/src/main/java"));
		}
		System.setProperty("disableInstallation", "true");
	}

	public void testModelGeneration() throws Exception {
		makeConfigFile(baseDir.getAbsolutePath());
		
		GenerateModelMojo modelGen = new GenerateModelMojo();

		modelGen.setPluginInfoManager(super.pluginInfoManager);
		modelGen.setBaseDir(baseDir);
		modelGen.setProjectHome("./src/test/resources/project/sample");
		modelGen.setMavenProject(new MavenProject());
		modelGen.setTable("CATEGORIES,FORUMS");
		modelGen.setTemplateHome(new File("./src/test/resources/templates/").getAbsolutePath());

		modelGen.execute();
		assertGeneration();
	}

	private void assertGeneration() throws Exception {
		String domainFile = baseDir.getAbsolutePath()
				+ "/src/main/java/com/sds/domain/Categories.java";
		
		assertTrue("Fail to generate domain file.", new File(domainFile)
				.exists());
	}
	
	private void makeConfigFile(String baseDir) throws Exception {
		File file = new File(baseDir + CommonConstants.fileSeparator + CommonConstants.SETTING_HOME + CommonConstants.fileSeparator
				+ CommonConstants.COMMON_CONFIG_XML_FILE);

		List<String> lines = new ArrayList<String>();

		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = in.readLine();
		while (line != null) {
			if (line.indexOf("<databases>") > 0) {
				line = "\t\t<databases>" + baseDir + CommonConstants.fileSeparator + CommonConstants.SETTING_HOME + "</databases>";
			}
			lines.add(line);
			line = in.readLine();
		}
		in.close();

		// now, write the file again with the changes
		PrintWriter out = new PrintWriter(file);
		for (String l : lines)
			out.println(l);
		out.close();
	}

}
