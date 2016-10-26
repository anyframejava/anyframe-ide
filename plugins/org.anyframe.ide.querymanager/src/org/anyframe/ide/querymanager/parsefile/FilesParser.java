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
package org.anyframe.ide.querymanager.parsefile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.QueryManagerConstants;
import org.anyframe.ide.querymanager.properties.QMPropertiesXMLUtil;
import org.anyframe.ide.querymanager.util.BuilderUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This is FilesParser class.
 * 
 * @author Surindhar.Kondoor
 * @author viswa.srikant
 */
public class FilesParser {

	// private static final Log LOGGER =
	// LogFactory.getLog(FilesParser.class);
	private HashMap allQueryIds = new HashMap();
	private HashMap daoQueryIds = new HashMap();
	private Set daoQueryIdsSet = new HashSet();
	private Set usedQueryIDLocation = new HashSet();
	// for properties page
	private ArrayList xmlFileNames = new ArrayList();
	private HashMap allQueryIdsPerFile = new HashMap();
	private HashMap duplAliasIdsMap = new HashMap();

	private ArrayList<String> mappingFiles = null;

	public void parseFiles(IProject project, IProgressMonitor monitor,
			HashMap technicalMap) {
		Collection files = null;

		ParserHelper parserHelper = new ParserHelper();
		XMLFileParser xmlFileParser = new XMLFileParser();
		JavaFileParser javaFileParser = new JavaFileParser();
		files = parserHelper.getAllFileInfo(project, project.getLocation());
		// LOGGER.debug("All Files from the project ==============  " + files);
		String projectPath = project.getLocation().toString();

		try {
			mappingFiles = getPropertiesFile(project);
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Exception occurred while getting propertyfiles.", e);
		}

		for (int i = 0; i < files.size(); i++) {
			monitor.subTask("Searching the file " + ((ArrayList) files).get(i));
			String file = (String) ((ArrayList) files).get(i);
			if (monitor.isCanceled()) {
				monitor.done();
				break;
			}
			String filePath = file.substring(projectPath.length() + 1,
					file.length());
			QMPropertiesXMLUtil util = new QMPropertiesXMLUtil();
			if (file.toUpperCase().endsWith(".XML") && isMappingXMLFile(file)) {
				monitor.subTask("Collecting the QueryIds : XML File " + file);
				Map queryIds = xmlFileParser.collectXMLQueryIds(project,
						filePath, allQueryIds, monitor);
				// if (queryIds != null && queryIds.size()!=0){
				IFile ifile = project.getFile(filePath);
				try {
					ifile.refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				String content = BuilderUtil.readFile(ifile);
				if (content
						.contains("http://www.anyframejava.org/dtd/anyframe-core-query-mapping-3.2.dtd")
						|| content
								.contains("http://www.anyframejava.org/schema/query/mapping")
						|| (queryIds != null && queryIds.size() != 0)) {
					allQueryIds.putAll(queryIds);
					xmlFileNames.add(file);
					allQueryIdsPerFile.put(file, queryIds);
					if (util.getPropertiesDupl2(project)) {
						duplAliasIdsMap.putAll(xmlFileParser
								.getDuplicateIdsMap());
					} else {
						duplAliasIdsMap.putAll(new HashMap());
					}
				}
				monitor.worked(10);
			} else if (file.toUpperCase().endsWith(".JAVA")) {
				// DAO files
				// Traverse through DAO file and get
				// the Query Ids used in this
				// file
				monitor.subTask("Collecting the QueryIds : Java File " + file);
				Map queryIdsInDaos = javaFileParser
						.collectDaoQueryIdsFromProject(project, filePath,
								daoQueryIds, monitor, technicalMap);
				monitor.worked(10);
				Set usedQueryID = javaFileParser.getUsedQueryID();
				usedQueryIDLocation = javaFileParser.getUsedQueryIDLocation();
				// store those Query Ids in Has Map
				if (daoQueryIds != null && usedQueryID != null
						&& queryIdsInDaos != null) {
					daoQueryIdsSet = usedQueryID;
					daoQueryIds.putAll(queryIdsInDaos);
				}
			}
			monitor.worked(5);
		}
	}

	public void parseFiles2(IProject project, IProgressMonitor monitor,
			HashMap technicalMap) {
		Collection files = null;

		ParserHelper parserHelper = new ParserHelper();
		XMLFileParser xmlFileParser = new XMLFileParser();
		JavaFileParser javaFileParser = new JavaFileParser();
		files = parserHelper.getAllFileInfo(project, project.getLocation());
		// LOGGER.debug("All Files from the project ==============  " + files);
		String projectPath = project.getLocation().toString();

		for (int i = 0; i < files.size(); i++) {
			monitor.subTask("Searching the file " + ((ArrayList) files).get(i));
			String file = (String) ((ArrayList) files).get(i);
			if (monitor.isCanceled()) {
				monitor.done();
				break;
			}
			String filePath = file.substring(projectPath.length() + 1,
					file.length());

			QMPropertiesXMLUtil util = new QMPropertiesXMLUtil();
			if (file.toUpperCase().endsWith(".XML")) {
				monitor.subTask("Collecting the QueryIds : XML File " + file);
				Map queryIds = xmlFileParser.collectXMLQueryIds(project,
						filePath, allQueryIds, monitor);
				// if (queryIds != null && queryIds.size()!=0){
				IFile ifile = project.getFile(filePath);
				String content = BuilderUtil.readFile(ifile);
				if (content
						.contains("http://www.anyframejava.org/dtd/anyframe-core-query-mapping-3.2.dtd")
						|| content
								.contains("http://www.anyframejava.org/schema/query/mapping")
						|| (queryIds != null && queryIds.size() != 0)) {
					allQueryIds.putAll(queryIds);
					xmlFileNames.add(file);
					allQueryIdsPerFile.put(file, queryIds);
					if (util.getPropertiesDupl2(project)) {
						duplAliasIdsMap.putAll(xmlFileParser
								.getDuplicateIdsMap());
					} else {
						duplAliasIdsMap.putAll(new HashMap());
					}
				}
				monitor.worked(10);
			} else if (file.toUpperCase().endsWith(".JAVA")) {
				// DAO files
				// Traverse through DAO file and get
				// the Query Ids used in this
				// file
				monitor.subTask("Collecting the QueryIds : Java File " + file);
				Map queryIdsInDaos = javaFileParser
						.collectDaoQueryIdsFromProject(project, filePath,
								daoQueryIds, monitor, technicalMap);
				monitor.worked(10);
				Set usedQueryID = javaFileParser.getUsedQueryID();
				// store those Query Ids in Has Map
				if (daoQueryIds != null && usedQueryID != null)
					daoQueryIdsSet = usedQueryID;
				daoQueryIds.putAll(queryIdsInDaos);
			}
			monitor.worked(5);
		}
	}

	private boolean isMappingXMLFile(String fileName) {
		for (String mappingFile : mappingFiles)
			if (fileName.endsWith(mappingFile))
				return true;
		return false;
	}

	private ArrayList<String> getPropertiesFile(IProject projects)
			throws Exception {
		Document document;

		ArrayList<String> resultList = new ArrayList<String>();
		File file = new File(projects.getProject().getLocation()
				+ Constants.FILE_SEPERATOR + Constants.SETTING_HOME + File.separator
				+ QueryManagerConstants.PROPERTY_FILE);
		
		try {
			if (file.exists()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(file);

				Element root = document.getDocumentElement();
				NodeList mappings = root.getElementsByTagName("mapping");
				for (int i = 0; i < mappings.getLength(); i++) {
					Element node = (Element) mappings.item(i);
					resultList.add(node.getTextContent());
				}
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Check DTD in XML file.", e);
		}

		// if (file.exists()) {
		// Element root = XMLUtil.readRoot(file);
		// if (root != null) {
		// Element project = root.element("project");
		// Element mappings = root.element("mappings");
		// List<Element> list = mappings.elements("mapping");
		// if (list != null) {
		// for (int i = 0; i < list.size(); i++) {
		// resultList.add(list.get(i).getData().toString());
		// }
		// }
		// }
		// }
		return resultList;
	}

	public HashMap getAllQueryIds() {
		return allQueryIds;
	}

	public HashMap getDaoQueryIds() {
		return daoQueryIds;
	}

	public ArrayList getXmlFileNames() {
		return xmlFileNames;
	}

	public Set getDaoQueryIdsSet() {
		return daoQueryIdsSet;
	}

	public Set getUsedQueryIDLocation() {
		return usedQueryIDLocation;
	}

	public HashMap getQueryIdsPerFile() {
		return allQueryIdsPerFile;
	}

	public HashMap getDuplAliasIdsMap() {
		return duplAliasIdsMap;
	}
	// usedQueryIDLocation
}
