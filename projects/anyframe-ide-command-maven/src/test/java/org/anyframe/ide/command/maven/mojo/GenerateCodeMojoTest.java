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

/**
 * This is a GenerateCodeMojoTest class.
 * 
 * @author Sooyeon Park
 */
public class GenerateCodeMojoTest extends AbstractMojoTest {

	String destinationDirectory = new File(".").getAbsolutePath() + "/generation/";

	protected void setUp() throws Exception {
		super.setUp();

		if (new File(destinationDirectory).exists()) {
			FileUtil.deleteDir(new File(destinationDirectory));
		}

		System.setProperty("modelpackage", "com.sds.domain");
		System.setProperty("type", "pojo");
		System.setProperty("disableInstallation", "true");
		System.setProperty("entity.check", "false");
	}

	public void testCodeGeneration() throws Exception {
		String packageName = "com.sds.service";

		GenerateCodeMojo codeGenerator = new GenerateCodeMojo();

		codeGenerator.setPluginInfoManager(super.pluginInfoManager);
		codeGenerator.setBaseDir(new File("./src/test/resources/project/sample"));
		
		makeConfigFile(new File("./src/test/resources/project/sample").getAbsolutePath());

		codeGenerator.setProjectHome(new File("./src/test/resources/project/sample").getAbsolutePath());
		codeGenerator.setEntity("Forums");
		codeGenerator.setScope("all");
		codeGenerator.setTemplateType("query");
		codeGenerator.setPackageName(packageName);
		codeGenerator.setTemplateHome(new File(".").getAbsolutePath() + "/src/test/resources/templates/");

		File destination = new File(destinationDirectory);
		if (!destination.exists()) {
			destination.mkdirs();
		}

		codeGenerator.execute();

		assertGeneration();
	}

	private void assertGeneration() throws Exception {
//		String serviceFile = destinationDirectory + "/src/main/java/com/sds/service/service/ForumsService.java";
//		assertTrue("Fail to generate service file.", new File(serviceFile).exists());
//
//		String mappingFile = destinationDirectory + "/src/main/resources/sql/mapping-query-forums.xml";
//		assertTrue("Fail to generate mapping xml file.", new File(mappingFile).exists());
//
//		String jspFile = destinationDirectory + "/src/main/webapp/WEB-INF/jsp/generation/forums/form.jsp";
//		assertTrue("Fail to generate jsp file.", new File(jspFile).exists());
//
//		String testFile = destinationDirectory + "/src/test/java/com/sds/service/service/ForumsServiceTest.java";
//		assertTrue("Fail to generate test file.", new File(testFile).exists());
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
