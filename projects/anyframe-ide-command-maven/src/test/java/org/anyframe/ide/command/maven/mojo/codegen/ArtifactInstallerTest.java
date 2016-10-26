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
package org.anyframe.ide.command.maven.mojo.codegen;

import java.io.File;

import junit.framework.TestCase;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.console.ConsoleLogger;

/**
 * This is an ArtifactInstallerTest class.
 * 
 * @author Sooyeon Park
 */
public class ArtifactInstallerTest extends TestCase {

	public void setUp() {
		// System.setProperty(CommonConstants.ANYFRAME_HOME,
		// "./src/test/resources/");

		String temp = new File(".").getAbsolutePath()
				+ "/src/test/resources/.temp";
		File tempFolder = new File(temp);
		if (!tempFolder.exists()) {
			tempFolder.mkdirs();
		}
	}

	// TODO : .temp/*.* -> service/*.* : check (service/*.*)
	public void testArtifactInstaller() throws Exception {

		MavenProject project = new MavenProject();

		project.setPackaging("jar");
		project.setGroupId("com.sds.emp");
		project.getProperties().setProperty(CommonConstants.WEB_FRAMEWORK,
				"spring");
		project.getProperties().setProperty(
				CommonConstants.APP_DAOFRAMEWORK_TYPE, "query");
		project.getProperties().setProperty("pjt.name", "services");
		project.getProperties().setProperty("template.type", "default");

//		ArtifactInstaller installer = new ArtifactInstaller(project,
//				"com.sds.domain", "Categories", "./src/test/resources/.temp/",
//				"./src/test/java/service/", false,
//				new File(".").getAbsolutePath()
//						+ "/src/test/resources/templates/");
//
//		installer
//				.setLog(new DefaultLog(new ConsoleLogger(0, "artifactLogger")));
//		installer.execute();
//
//		assertEquals(false, installer.isGenericCore());
//
//		System.out.println(installer.getPojoNameLower());
//		System.out.println(installer.getDestinationDirectory());
//		System.out.println(installer.getSourceDirectory());

	}
}