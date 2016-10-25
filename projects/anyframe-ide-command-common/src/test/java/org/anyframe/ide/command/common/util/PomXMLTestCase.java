/*   
 * Copyright 2002-2011 the original author or authors.   
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
package org.anyframe.ide.command.common.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.plugin.PluginInterceptorDependency;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

/**
 * TestCase Name : PomXMLTestCase <br>
 * <br>
 * [Description] : Test for pom.xml file handling<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : convert pom.xml file to Model object.</li>
 * <li>#-2 Positive Case : convert Model object to pom.xml file.</li>
 * <li>#-3 Positive Case : convert Model object to pom.xml file using XStream.</li>
 * </ul>
 */
public class PomXMLTestCase extends TestCase {

	/**
	 * [Flow #-1] Positive Case : convert pom.xml file to Model object.
	 * 
	 * @throws Exception
	 */
	public void testPomXMLToModelObject() throws Exception {

		File file = new File("./src/test/resources/project/sample/pom.xml");

		FileReader reader = null;
		MavenXpp3Reader mavenreader = new MavenXpp3Reader();

		reader = new FileReader(file);
		Model model = mavenreader.read(reader);
		assertEquals("fail to convert xml to Model - name.",
				"remoting", model.getName());
		assertEquals("fail to convert xml to Model - groupId.",
				"org.anyframe.plugin", model.getGroupId());
		assertEquals("fail to convert xml to Model - artifactId.",
				"anyframe-remoting-pi", model.getArtifactId());
		assertEquals("fail to convert xml to Model - version.", "1.0.0",
				model.getVersion());

	}

	/**
	 * [Flow #-2] Positive Case : convert Model object to pom.xml file.
	 * 
	 * @throws Exception
	 */
	public void testModelObjectToPomXML() throws Exception {
		Model model = new Model();
		Parent parent = new Parent();
		parent.setGroupId("org.anyframe");
		parent.setArtifactId("anyframe-common-maven-root");
		parent.setVersion("5.0.0");
		model.setParent(parent);
		model.setGroupId("org.anyframe.plugin");
		model.setArtifactId("anyframe-remoting-pi");
		model.setVersion("1.0.0");
		
		// get dependencies from plugin-build.xml
		File file = new File(
				"./src/test/resources/project/sample/plugin-build.xml");
		PluginInfo pluginInfo = (PluginInfo) FileUtil.getObjectFromXML(file);
		List<PluginInterceptorDependency> interceptorDependencies = pluginInfo
				.getInterceptor().getDependencies();
		List<Dependency> dependencies = new ArrayList<Dependency>();
		Iterator<PluginInterceptorDependency> itr = interceptorDependencies
				.iterator();
		while (itr.hasNext()) {
			PluginInterceptorDependency interceptorDependency = itr.next();
			Dependency dependency = new Dependency();
			dependency.setGroupId(interceptorDependency.getGroupId());
			dependency.setArtifactId(interceptorDependency.getArtifactId());
			dependency.setVersion(interceptorDependency.getVersion());
			dependencies.add(dependency);
		}

		// set dependencies for pom.xml
		model.setDependencies(dependencies);
		
		// write new pom.xml
		MavenXpp3Writer mavenwriter = new MavenXpp3Writer();
		FileWriter writer = new FileWriter(new File(
				"./src/test/resources/project/sample/pom-test.xml"));
		mavenwriter.write(writer, model);
	}
	
	/**
	 * [Flow #-2] Positive Case : convert Model object to pom.xml file using XStream.
	 * 
	 * @throws Exception
	 */
	public void testModelObjectToPomXMLUsingXStream() throws Exception {
		Model model = new Model();
		Parent parent = new Parent();
		parent.setGroupId("org.anyframe");
		parent.setArtifactId("anyframe-common-maven-root");
		parent.setVersion("5.0.0");
		model.setParent(parent);
		model.setGroupId("org.anyframe.plugin");
		model.setArtifactId("anyframe-remoting-pi");
		model.setVersion("1.0.0");

		// get dependencies from plugin-build.xml
		File file = new File(
				"./src/test/resources/project/sample/plugin-build.xml");
		PluginInfo pluginInfo = (PluginInfo) FileUtil.getObjectFromXML(file);
		List<PluginInterceptorDependency> interceptorDependencies = pluginInfo
				.getInterceptor().getDependencies();
		List<Dependency> dependencies = new ArrayList<Dependency>();
		Iterator<PluginInterceptorDependency> itr = interceptorDependencies
				.iterator();
		while (itr.hasNext()) {
			PluginInterceptorDependency interceptorDependency = itr.next();
			Dependency dependency = new Dependency();
			dependency.setGroupId(interceptorDependency.getGroupId());
			dependency.setArtifactId(interceptorDependency.getArtifactId());
			dependency.setVersion(interceptorDependency.getVersion());
			dependencies.add(dependency);
		}

		// set dependencies for pom.xml
		model.setDependencies(dependencies);

		FileUtil.getObjectToXML(model, new File(
				"./src/test/resources/project/sample/pom-test.xml"));
	}

}
