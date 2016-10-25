package org.anyframe.ide.command.common.catalog;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.anyframe.ide.command.common.AbstractCommandTest;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.maven.archetype.catalog.Archetype;

public class ArchetypeCatalogDataSourceTestCase extends AbstractCommandTest {

	public void testGetLatestArchetypeVersion() throws Exception {
		ArchetypeCatalogDataSource acd = new ArchetypeCatalogDataSource();
		String latestVersion = acd.getLatestArchetypeVersion(createRequest(""),
				"anyframe-basic-archetype",
				CommonConstants.PROJECT_BUILD_TYPE_ANT, currentPath
						+ CommonConstants.SRC_TEST_RESOURCES);

		assertThat(latestVersion, is("5.0.0.RC2"));
	}

	public void testGetArchetypes() throws Exception {
		ArchetypeCatalogDataSource acd = new ArchetypeCatalogDataSource();
		Map<String, Archetype> archetypes = acd.getArchetypes(
				createRequest(""), CommonConstants.PROJECT_BUILD_TYPE_ANT,
				currentPath + CommonConstants.SRC_TEST_RESOURCES);
		assertThat(archetypes.size(), is(3));
	}

	public void testGetArchetypeVersions() throws Exception {
		ArchetypeCatalogDataSource acd = new ArchetypeCatalogDataSource();
		Map<String, Archetype> archetypes = acd.getArchetypeVersions(
				createRequest(""), CommonConstants.PROJECT_BUILD_TYPE_ANT,
				currentPath + CommonConstants.SRC_TEST_RESOURCES);

		assertThat(archetypes.size(), is(12));
	}
}
