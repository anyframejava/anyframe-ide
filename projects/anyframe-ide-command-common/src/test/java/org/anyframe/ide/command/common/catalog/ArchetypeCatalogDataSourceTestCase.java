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
package org.anyframe.ide.command.common.catalog;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.anyframe.ide.command.common.AbstractCommandTest;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.maven.archetype.catalog.Archetype;

/**
 * TestCase Name : ArchetypeCatalogDataSourceTestCase <br>
 * <br>
 * [Description] : Test for archetype catalog handling<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : get latest archetype version</li>
 * <li>#-2 Positive Case : get latest archetype</li>
 * <li>#-3 Positive Case : get latest archetype versions</li>
 * </ul>
 */
public class ArchetypeCatalogDataSourceTestCase extends AbstractCommandTest {

	/**
	 * [Flow #-1] Positive Case : get latest archetype version
	 * 
	 * @throws Exception
	 */
	public void testGetLatestArchetypeVersion() throws Exception {
		ArchetypeCatalogDataSource acd = new ArchetypeCatalogDataSource();
		String latestVersion = acd.getLatestArchetypeVersion(createRequest(""),
				"anyframe-basic-archetype",
				CommonConstants.PROJECT_BUILD_TYPE_ANT, currentPath
						+ CommonConstants.SRC_TEST_RESOURCES);

		assertThat(latestVersion, is("5.0.0.RC2"));
	}

	/**
	 * [Flow #-2] Positive Case : get latest archetype
	 * 
	 * @throws Exception
	 */
	public void testGetArchetypes() throws Exception {
		ArchetypeCatalogDataSource acd = new ArchetypeCatalogDataSource();
		Map<String, Archetype> archetypes = acd.getArchetypes(
				createRequest(""), CommonConstants.PROJECT_BUILD_TYPE_ANT,
				currentPath + CommonConstants.SRC_TEST_RESOURCES);
		assertThat(archetypes.size(), is(3));
	}

	/**
	 * [Flow #-3] Positive Case : get latest archetype versions
	 * 
	 * @throws Exception
	 */
	public void testGetArchetypeVersions() throws Exception {
		ArchetypeCatalogDataSource acd = new ArchetypeCatalogDataSource();
		Map<String, Archetype> archetypes = acd.getArchetypeVersions(
				createRequest(""), CommonConstants.PROJECT_BUILD_TYPE_ANT,
				currentPath + CommonConstants.SRC_TEST_RESOURCES);

		assertThat(archetypes.size(), is(12));
	}
}
