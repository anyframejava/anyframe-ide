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
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.anyframe.ide.command.common.plugin.DependentPlugin;
import org.anyframe.ide.command.common.plugin.Exclude;
import org.anyframe.ide.command.common.plugin.Fileset;
import org.anyframe.ide.command.common.plugin.Include;
import org.anyframe.ide.command.common.plugin.PluginBuild;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.plugin.PluginInterceptor;
import org.anyframe.ide.command.common.plugin.PluginResource;

/**
 * TestCase Name : PluginBuildXMLTestCase <br>
 * <br>
 * [Description] : Test for plugin-build.xml file handling<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : convert plugin-build.xml file to PluginInfo object.</li>
 * <li>#-2 Positive Case : convert PluginInfo object to plugin-build.xml file.</li>
 * </ul>
 */
public class PluginBuildXMLTestCase extends TestCase {

	/**
	 * [Flow #-1] Positive Case : convert plugin-build.xml file to PluginInfo object.
	 * 
	 * @throws Exception
	 */
	public void testPluginBuildXMLToPluginObject() throws Exception {

		File file = new File(
				"./src/test/resources/project/sample/plugin-build.xml");
		PluginInfo pluginInfo = (PluginInfo) FileUtil.getObjectFromXML(file);

		assertEquals("fail to convert xml to plugin object - name.",
				"remoting", pluginInfo.getName());
		assertEquals("fail to convert xml to plugin object - groupId.",
				"org.anyframe.plugin", pluginInfo.getGroupId());
		assertEquals("fail to convert xml to plugin object - artifactId.",
				"anyframe-remoting-pi", pluginInfo.getArtifactId());
		assertEquals("fail to convert xml to plugin object - version.",
				"1.0.0", pluginInfo.getVersion());
		assertEquals("fail to convert xml to plugin object - dependent plugins.",
				"1.0.0>=*", pluginInfo.getDependentPlugins().get(0).getVersion());
		assertTrue("fail to convert xml to plugin object - samples.",
				pluginInfo.hasSamples());
	}

	/**
	 * [Flow #-2] Positive Case : convert PluginInfo object to plugin-build.xml file.
	 * 
	 * @throws Exception
	 */
	public void testPluginObjectToPluginBuildXML() throws Exception {

		PluginInfo pluginInfo = new PluginInfo();
		pluginInfo.setName("test");
		pluginInfo.setDescription("test plugin");
		pluginInfo.setGroupId("org.anyframe.plugin");
		pluginInfo.setArtifactId("anyframe-remoting-pi");
		pluginInfo.setVersion("1.0.0");
		PluginInterceptor interceptor = new PluginInterceptor();
		interceptor.setClassName("test.intercepter.TestInterceptor");
		pluginInfo.setInterceptor(interceptor);
		List<DependentPlugin> dependentPlugins = new ArrayList<DependentPlugin>();
		DependentPlugin dependency = new DependentPlugin();
		dependency.setName("core");
		dependency.setVersion("1.0.0 > *");
		dependentPlugins.add(dependency);
		dependency = new DependentPlugin();
		dependency.setName("cxf");
		dependency.setVersion("1.0.0 > *");
		dependentPlugins.add(dependency);
		pluginInfo.setDependentPlugins(dependentPlugins);
		
		List<Fileset> filesets = new ArrayList<Fileset>();
		Fileset fileset = new Fileset();
		fileset.setDir("src/main/java");
		
		List<Include> filesetIncludes = new ArrayList<Include>();
		Include filesetInclude = new Include();
		filesetInclude.setName("**/*.java");
		filesetIncludes.add(filesetInclude);
		filesetInclude = new Include();
		filesetInclude.setName("**/*.java1");
		filesetIncludes.add(filesetInclude);
		fileset.setIncludes(filesetIncludes);
		
		List<Exclude> filesetExcludes = new ArrayList<Exclude>();
		Exclude filesetExclude = new Exclude();
		filesetExclude.setName("**/*.java");
		filesetExcludes.add(filesetExclude);
		filesetExclude = new Exclude();
		filesetExclude.setName("**/*.java1");
		filesetExcludes.add(filesetExclude);
		fileset.setExcludes(filesetExcludes);
		
		filesets.add(fileset);
		
		PluginBuild build = new PluginBuild();
		build.setFilesets(filesets);
		pluginInfo.setBuild(build);	
		
		List<PluginResource> resources = new ArrayList<PluginResource>();
		PluginResource resource = new PluginResource();
		resource.setDir("src/main/java");
		resource.setFiltered(true);
		
		List<Include> includes = new ArrayList<Include>();
		Include include = new Include();
		include.setName("**/*.java");
		includes.add(include);
		include = new Include();
		include.setName("**/*.java1");
		includes.add(include);
		
		resource.setIncludes(includes);
		resources.add(resource);
		
		pluginInfo.setResources(resources);

		FileUtil.getObjectToXML(pluginInfo, new File(
				"./src/test/resources/project/sample/plugin-build-test.xml"));
	}

}
