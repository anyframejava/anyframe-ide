/*
 * Copyright 2008-2011 the original author or authors.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyframe.ide.command.common.CommandException;
import org.anyframe.ide.command.common.plugin.versioning.VersionComparator;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Reader;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;

/**
 * 
 * This is an ArchetypeCatalogDataSource class. This class is for reading a
 * archetype-catalog.xml file in user.home repository
 * 
 * @plexus.component 
 *                   role="org.anyframe.ide.command.common.catalog.ArchetypeCatalogDataSource"
 * 
 * @author Jason van Zyl
 * @author modified by Soyon Lim
 */
public class ArchetypeCatalogDataSource extends AbstractLogEnabled {
	public static String ROLE = ArchetypeCatalogDataSource.class.getName();

	private String archetypeCatalogFileName = "archetype-catalog.xml";

	private String remoteArchetypeCatalog = CommonConstants.REMOTE_CATALOG_PATH
			+ "/archetype-catalog.xml";

	private ArchetypeCatalogXpp3Reader catalogReader = new ArchetypeCatalogXpp3Reader();

	/**
	 * Finds latest archetype version
	 * 
	 * @param request
	 *            information includes maven repositories in settings.xml
	 * @param archetypeArtifactId
	 *            archetype's artifactId
	 * @param buildType
	 *            build type of project ('maven' or 'ant')
	 * @param anyframeHome
	 *            anyframe home includes repositories, ant, templates, etc.
	 * @return latest archetype version
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String getLatestArchetypeVersion(ArchetypeGenerationRequest request,
			String archetypeArtifactId, String buildType, String anyframeHome)
			throws Exception {

		ArchetypeCatalog archetypeCatalog = getArchetypeCatalog(request,
				buildType, anyframeHome);

		List<Archetype> archetypes = archetypeCatalog.getArchetypes();
		List<String> versions = new ArrayList<String>();
		for (Archetype archetype : archetypes) {
			if (archetypeArtifactId.equals(archetype.getArtifactId())) {
				versions.add(archetype.getVersion());
			}
		}

		return VersionComparator.getLatest("", versions);
	}

	/**
	 * Gets Archetypes
	 * 
	 * @param request
	 *            information includes maven repositories in settings.xml
	 * @param buildType
	 *            build type of project ('maven' or 'ant')
	 * @param anyframeHome
	 *            anyframe home includes repositories, ant, templates, etc.
	 * @return Archetype objects map of which key is archetype's artifactId.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Archetype> getArchetypes(
			ArchetypeGenerationRequest request, String buildType,
			String anyframeHome) throws Exception {

		try {
			ArchetypeCatalog archetypeCatalog = getArchetypeCatalog(request,
					buildType, anyframeHome);

			Map<String, Archetype> archetypes = new HashMap<String, Archetype>();
			for (Iterator<Archetype> i = archetypeCatalog.getArchetypes()
					.iterator(); i.hasNext();) {
				Archetype archetype = (Archetype) i.next();
				archetypes.put(archetype.getArtifactId(), archetype);
			}

			return archetypes;

		} catch (Exception e) {
			if (e instanceof CommandException)
				throw e;
			throw new CommandException(
					"The specific archetype catalog does not exist.", e);
		}
	}

	/**
	 * Gets Archetypes
	 * 
	 * @param request
	 *            information includes maven repositories in settings.xml
	 * @param buildType
	 *            build type of project ('maven' or 'ant')
	 * @param anyframeHome
	 *            anyframe home includes repositories, ant, templates, etc.
	 * @return Archetype objects map of which key is '{artifactId}-{version}' of
	 *         archetype.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Archetype> getArchetypeVersions(
			ArchetypeGenerationRequest request, String buildType,
			String anyframeHome) throws Exception {
		try {
			ArchetypeCatalog archetypeCatalog = getArchetypeCatalog(request,
					buildType, anyframeHome);

			Map<String, Archetype> archetypes = new HashMap<String, Archetype>();
			for (Iterator<Archetype> i = archetypeCatalog.getArchetypes()
					.iterator(); i.hasNext();) {
				Archetype archetype = (Archetype) i.next();
				archetypes.put(archetype.getArtifactId() + "-"
						+ archetype.getVersion(), archetype);
			}

			return archetypes;

		} catch (Exception e) {
			if (e instanceof CommandException)
				throw e;
			throw new CommandException(
					"The specific archetype catalog does not exist.", e);
		}
	}

	/**
	 * 
	 * @param request
	 *            information includes maven repositories in settings.xml
	 * @param buildType
	 *            build type of project ('maven' or 'ant')
	 * @param anyframeHome
	 *            anyframe home includes repositories, ant, templates, etc.
	 * @return ArchetypeCatalog
	 * @throws Exception
	 */
	private ArchetypeCatalog getArchetypeCatalog(
			ArchetypeGenerationRequest request, String buildType,
			String anyframeHome) throws Exception {

		FileOutputStream out = null;
		InputStream in = null;

		try {
			String localRepo = "";

			if (buildType
					.equalsIgnoreCase(CommonConstants.PROJECT_BUILD_TYPE_ANT)) {
				localRepo = anyframeHome + CommonConstants.fileSeparator
						+ "repo";
			} else {
				localRepo = request.getLocalRepository().getBasedir();

				File temp = new File(localRepo);
				if (temp.exists()) {
					localRepo = temp.getParent();
				}
			}

			File archetypeCatalogFile = new File(localRepo,
					archetypeCatalogFileName);

			if (!archetypeCatalogFile.exists()) {
				int idx = remoteArchetypeCatalog.lastIndexOf("/");

				String remoteArchetypeCatalogPath = "";
				String remoteArchetypeCatalogFile = "";

				if (idx != -1) {
					remoteArchetypeCatalogPath = remoteArchetypeCatalog
							.substring(0, idx + 1);
					remoteArchetypeCatalogFile = remoteArchetypeCatalog
							.substring(idx + 1,
									remoteArchetypeCatalog.length() - 4);
				}

				// download archetype-catalog.xml to local repository
				File localArchetypeCatalogFile = new File(localRepo,
						archetypeCatalogFileName);
				localArchetypeCatalogFile.createNewFile();
				out = new FileOutputStream(localArchetypeCatalogFile);

				in = getResourceFromRemoteRepository(
						remoteArchetypeCatalogPath, remoteArchetypeCatalogFile,
						null, null);

				IOUtil.copy(in, out);
			}

			ArchetypeCatalog archetypeCatalog = readCatalog(new FileReader(
					archetypeCatalogFile));

			return archetypeCatalog;

		} catch (Exception e) {
			if (e instanceof CommandException)
				throw e;
			throw new CommandException(
					"The specific archetype catalog does not exist.", e);
		} finally {
			IOUtil.close(in);
			IOUtil.close(out);
		}
	}

	/**
	 * Read archetype-catalog.xml file from remote repository
	 * 
	 * @param url
	 *            url for remote repository
	 * @param remoteResource
	 *            resource (plugin catalog file) to read
	 * @param userName
	 *            user name to connect remote repository
	 * @param password
	 *            password to connect remote repository
	 * @return input stream of resource in remote repository
	 */
	private InputStream getResourceFromRemoteRepository(String url,
			String remoteResource, String userName, String password) {
		getLogger().debug(
				"Ready to get a resource from a remote repository. [location="
						+ url + "]");
		HttpClient client = new HttpClient();
		Credentials creds = null;
		if (userName != null && password != null) {
			creds = new UsernamePasswordCredentials(userName, password);
		}
		client.getState().setCredentials(AuthScope.ANY, creds);

		GetMethod getMethod = new GetMethod(url + "/" + remoteResource + "."
				+ CommonConstants.EXT_XML);
		InputStream is = null;
		try {
			int resultCode = client.executeMethod(getMethod);

			if (resultCode == HttpStatus.SC_OK) {
				is = getMethod.getResponseBodyAsStream();

				return is;
			} else {
				// TODO : Error? Warn?
				throw new Exception("");
			}
		} catch (Exception e) {
			IOUtil.close(is);
			getLogger().warn(
					"Reading " + remoteResource + " from " + url
							+ " is skipped. The reason is " + e.getMessage());
		}
		return null;
	}

	/**
	 * 
	 * @param reader
	 * @return
	 * @throws Exception
	 */
	protected ArchetypeCatalog readCatalog(Reader reader) throws Exception {
		try {
			return catalogReader.read(reader);
		} catch (Exception e) {
			throw new CommandException("Error reading archetype catalog.", e);
		} finally {
			IOUtil.close(reader);
		}
	}
}
