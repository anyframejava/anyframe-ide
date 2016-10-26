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
package org.anyframe.ide.command.common.plugin.versioning;

import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.command.common.plugin.versioning.VersionComparator;

import junit.framework.TestCase;

/**
 * TestCase Name : VersionComparatorTest <br>
 * <br>
 * [Description] : Test for comparing version<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : check if a specific version belongs to defined
 * version range</li>
 * <li>#-2 Positive Case : get latest version under defined version range</li>
 * </ul>
 */
public class VersionComparatorTest extends TestCase {
	/**
	 * [Flow #-1] Positive Case : check if a specific version belongs to defined
	 * version range
	 */
	public void testIsMatched() {
		String sourcePluginVersionRange = "2.0.0 <= * < 4.0.1-SNAPSHOT";

		assertTrue("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "3.0.0"));
		assertTrue("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "2.0.0"));
		assertFalse("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "4.0.1"));
		assertFalse("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "4.0.2"));
		assertFalse("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "1.0.0"));

		sourcePluginVersionRange = "2.0.0 <= *";

		assertTrue("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "2.0.0"));
		assertTrue("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "3.0.0"));
		assertTrue("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "4.0.0"));
		assertFalse("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "1.0.0"));

		sourcePluginVersionRange = "* < 4.0.1-SNAPSHOT";

		assertTrue("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "1.0.0"));
		assertTrue("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "2.0.0"));
		assertTrue("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "3.0.0"));
		assertFalse("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "4.0.1"));
		assertFalse("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "4.0.2"));

		sourcePluginVersionRange = "2.0.0, 3.0.0, 4.0.0";

		assertTrue("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "3.0.0"));
		assertFalse("fail to check matched version.", VersionComparator
				.isMatched(sourcePluginVersionRange, "5.0.0"));
	}

	/**
	 * [Flow #-2] Positive Case : get latest version under defined version range
	 */
	public void testGetLatest() {
		List<String> releaseVersions = new ArrayList<String>();
		releaseVersions.add("1.0.0");
		releaseVersions.add("2.0.0");
		releaseVersions.add("3.0.0");
		releaseVersions.add("4.0.0");
		releaseVersions.add("5.0.0");

		assertEquals("fail to find latest release version.", "4.0.0",
				VersionComparator.getLatest("2.0.0<=*<4.0.1-SNAPSHOT",
						releaseVersions));
		assertEquals("fail to find latest release version.", "5.0.0",
				VersionComparator.getLatest("2.0.0<=*", releaseVersions));
		assertEquals("fail to find latest release version.", "4.0.0",
				VersionComparator
						.getLatest("*<4.0.1-SNAPSHOT", releaseVersions));
		assertEquals("fail to find latest release version.", "4.0.0",
				VersionComparator.getLatest("2.0.0,4.0.0, 3.0.0",
						releaseVersions));
	}
}
