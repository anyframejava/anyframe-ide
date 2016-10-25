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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.anyframe.ide.command.common.CommandException;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Reader;
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
public class ArchetypeCatalogDataSource {
	public static String ROLE = ArchetypeCatalogDataSource.class.getName();

	private String archetypeCatalogFileName = "archetype-catalog.xml";

	private String remoteArchetypeCatalog = CommonConstants.REMOTE_CATALOG_PATH
			+ "/archetype-catalog.xml";

	private ArchetypeCatalogXpp3Reader catalogReader = new ArchetypeCatalogXpp3Reader();

	public String getLatestArchetypeVersion(ArchetypeGenerationRequest request,
			String archetypeArtifactId, String buildType, String home)
			throws Exception {
		Map<String, Archetype> archetypes = getArchetypes(request, buildType,
				home);
		if (archetypes.containsKey(archetypeArtifactId)) {
			return archetypes.get(archetypeArtifactId).getVersion();
		}
		return null;
	}

	public Map<String, Archetype> getArchetypes(
			ArchetypeGenerationRequest request, String buildType,
			String anyframeHome) throws Exception {

		ArchetypeCatalog archetypeCatalog = null;

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

				HttpClient client = new HttpClient();
				client.getState().setCredentials(AuthScope.ANY, null);

				GetMethod getMethod = new GetMethod(remoteArchetypeCatalogPath
						+ "/" + remoteArchetypeCatalogFile + "."
						+ CommonConstants.EXT_XML);
				int resultCode = client.executeMethod(getMethod);

				// download archetype-catalog.xml to local repository
				File localArchetypeCatalogFile = new File(localRepo,
						archetypeCatalogFileName);
				localArchetypeCatalogFile.createNewFile();

				if (resultCode == HttpStatus.SC_OK) {
					InputStream in = getMethod.getResponseBodyAsStream();

					FileOutputStream out = new FileOutputStream(
							localArchetypeCatalogFile);

					IOUtil.copy(in, out);
				}
			}

			archetypeCatalog = readCatalog(new FileReader(archetypeCatalogFile));

			Map<String, Archetype> archetypes = new HashMap();
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

	public Map<String, Archetype> getArchetypeVersions(
			ArchetypeGenerationRequest request, String buildType,
			String anyframeHome) throws Exception {
		ArchetypeCatalog archetypeCatalog = null;

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

				HttpClient client = new HttpClient();
				client.getState().setCredentials(AuthScope.ANY, null);

				GetMethod getMethod = new GetMethod(remoteArchetypeCatalogPath
						+ "/" + remoteArchetypeCatalogFile + "."
						+ CommonConstants.EXT_XML);
				int resultCode = client.executeMethod(getMethod);

				// download archetype-catalog.xml to local repository
				File localArchetypeCatalogFile = new File(localRepo,
						archetypeCatalogFileName);
				localArchetypeCatalogFile.createNewFile();

				if (resultCode == HttpStatus.SC_OK) {
					InputStream in = getMethod.getResponseBodyAsStream();

					FileOutputStream out = new FileOutputStream(
							localArchetypeCatalogFile);

					IOUtil.copy(in, out);					
				}
			}

			archetypeCatalog = readCatalog(new FileReader(
					archetypeCatalogFile));
			
			Map<String, Archetype> archetypes = new HashMap();
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
