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
package org.anyframe.ide.querymanager.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.QueryManagerConstants;
import org.anyframe.ide.querymanager.build.BuilderHelper;
import org.anyframe.ide.querymanager.build.Location;
import org.anyframe.ide.querymanager.build.QMMarkerHelper;
import org.anyframe.ide.querymanager.messages.MessagePropertiesLoader;
import org.anyframe.ide.querymanager.model.FileInfoVO;
import org.anyframe.ide.querymanager.model.QueryTreeVO;
import org.anyframe.ide.querymanager.properties.QMPropertiesXMLUtil;
import org.anyframe.ide.querymanager.util.AnyframeJarLoader;
import org.anyframe.ide.querymanager.util.BuilderUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Binding query ids, files and projects
 * 
 * @author raveendra
 * @author Sujeong Lee
 */
public class QueryExplorerHelper {

	private Set daoSet = new HashSet();
	private HashMap daoMap = new HashMap();
	private HashMap duplicateIds = new HashMap();
	private HashMap duplAliasIdsMap = new HashMap();
	private HashMap usedQueryIDsLocationMap = new HashMap();

	public Set getDaoSet() {
		return daoSet;
	}

	public HashMap getDaoMap() {
		return daoMap;
	}

	public HashMap getDuplicateIds() {
		return duplicateIds;
	}

	public HashMap getDuplAliasIdsMap() {
		return duplAliasIdsMap;
	}

	public HashMap getUsedQueryIDsLocationMap() {
		return usedQueryIDsLocationMap;
	}

	public HashMap getAllQueriesForAllProjects(IProgressMonitor monitor) {
		HashMap projectMap = new HashMap();
		// Go through all the projects and collect all
		// the Query Ids.
		// Get All the projects
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		AnyframeJarLoader loader = new AnyframeJarLoader();
		// Collect Technical Service Details here and pass to
		// searchQueryIdsForProject
		BuilderHelper builder = BuilderHelper.getInstance();
		outer: for (int i = 0; i < projects.length; i++) {
			try {

				if (monitor.isCanceled()) {
					break;
				}
				if (projects[i].isOpen()
						&& projects[i]
								.hasNature("org.anyframe.ide.querymanager.bulld.AnyframeNature")) {
					String pjtName = projects[i].getName();
					HashMap map = builder
							.collectAllQueryIdsForBuilder(projects[i]);
					// if (builder.duplicateIds != null
					// &&
					// builder.duplicateIds.get(pjtName)
					// != null) {
					// map.putAll((HashMap)
					// builder.duplicateIds.get(pjtName));
					// }
					if (map != null && map.size() > 0)
						projectMap.put(pjtName, map);
				}
			} catch (Exception projectReadingException) {
				// If for any reason exception occurs
				// in
				// reading one project, that may be in
				// an unstable state.
				// If so, continue with the next
				// project, so that the Functionality
				// is not disturbed only because of
				// some unknown error in a project.
				continue outer;
			}
		} // outer
		// Now we have all the Query Ids.
		return projectMap;
	}

	public HashMap getAllQueriesForAllProjectsFromProperties(
			IProgressMonitor monitor) {

		HashMap projectMap = new HashMap();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		AnyframeJarLoader loader = new AnyframeJarLoader();
		BuilderHelper builder = BuilderHelper.getInstance();
		outer: for (int i = 0; i < projects.length; i++) {
			try {

				if (monitor.isCanceled()) {
					break;
				}
				if (projects[i].isOpen()
						&& projects[i]
								.hasNature("org.anyframe.ide.querymanager.build.AnyframeNature")) {
					String pjtName = projects[i].getName();
					ArrayList xmlFileList = getPropertiesFile(projects[i]);

					HashMap map = builder
							.collectAllQueryIdsForBuilder(projects[i]);

					daoSet = builder.getUsedQueryIDsSet();
					daoMap = builder.getUsedQueryIDsMap();
					duplicateIds = builder.getDupllicateIds();
					duplAliasIdsMap = builder.getDuplAliasIdsMap();
					usedQueryIDsLocationMap = builder
							.getUsedQueryIDsLocationMap();

					HashMap resultMap = new HashMap();

					Iterator it = map.keySet().iterator();
					while (it.hasNext()) {
						Object ob = it.next();
						Object temp = map.get(ob);
						for (int n = 0; n < xmlFileList.size(); n++) {
							String file = projects[i].getLocation() + "/"
									+ xmlFileList.get(n).toString();
							String fileName = ob.toString();
							if (fileName.equals(file)) {
								resultMap.put(ob, map.get(ob));
							}
						}
					}

					if (map != null && map.size() > 0)
						projectMap.put(pjtName, resultMap);
				}
			} catch (Exception projectReadingException) {
				continue outer;
			}
		}
		return projectMap;
	}

	private ArrayList getPropertiesFile(IProject projects) throws Exception {
		Document document;

		ArrayList<String> resultList = new ArrayList<String>();
		File file = new File(projects.getProject().getLocation()
				+ Constants.FILE_SEPERATOR + Constants.SETTING_HOME
				+ Constants.FILE_SEPERATOR + QueryManagerConstants.PROPERTY_FILE);
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

		return resultList;
	}

	public ArrayList collectTreeViewQueries(IProgressMonitor monitor) {
		HashMap returnMap = new HashMap();

		Set returnTempSet = null;
		ArrayList<QueryTreeVO> returnList = new ArrayList<QueryTreeVO>();
		ArrayList<FileInfoVO> fileList = new ArrayList<FileInfoVO>();

		HashMap projectMap = getAllQueriesForAllProjects(monitor);

		String projectName = "";
		Location location = null;

		Iterator iterator = projectMap.keySet().iterator();
		while (iterator.hasNext()) {
			projectName = (String) iterator.next();
			// file map
			Map queryMap = (Map) projectMap.get(projectName);
			Iterator it = queryMap.keySet().iterator();
			while (it.hasNext()) {
				Collection duplicateIds = null;
				Object individualFileName = it.next();
				Map ids = (Map) queryMap.get(individualFileName);

				Iterator ite = ids.keySet().iterator();
				while (ite.hasNext()) {
					Object queryId = ite.next();
					Object idInfo = ids.get(queryId);
				}
				FileInfoVO fiVO = new FileInfoVO();
				File tempFile = new File(individualFileName.toString());
				fiVO.setFileName(tempFile.getName());
				fiVO.setQueryId(ids);

				fileList.add(fiVO);
			}
			QueryTreeVO qtVO = new QueryTreeVO();
			qtVO.setProject(projectName);
			qtVO.setFileList(fileList);

			returnList.add(qtVO);
		}
		return returnList;
	}

	public ArrayList collectTreeViewQueriesFromProperties(
			IProgressMonitor monitor) {
		HashMap returnMap = new HashMap();

		ArrayList<QueryTreeVO> returnList = new ArrayList<QueryTreeVO>();

		HashMap projectMap = getAllQueriesForAllProjectsFromProperties(monitor);
		String projectName = "";

		Iterator iterator = projectMap.keySet().iterator();
		while (iterator.hasNext()) {
			projectName = iterator.next() + "";
			// file map
			Map queryMap = (Map) projectMap.get(projectName);
			Iterator it = queryMap.keySet().iterator();
			if (it.hasNext()) {
				ArrayList<FileInfoVO> fileList = new ArrayList<FileInfoVO>();
				while (it.hasNext()) {
					Object individualFileName = it.next();
					Map ids = (Map) queryMap.get(individualFileName);

					FileInfoVO fiVO = new FileInfoVO();
					int num = individualFileName.toString()
							.indexOf(projectName);
					String filePath = individualFileName.toString().substring(
							num + projectName.length() + 1);
					fiVO.setFileName(filePath);
					fiVO.setPath(individualFileName + "");
					fiVO.setQueryId(mergeBoolUseDao(ids, projectName));
					String alias = fileAlias(projectName, filePath);
					fiVO.setAlias(alias);
					fileList.add(fiVO);
				}
				QueryTreeVO qtVO = new QueryTreeVO();
				qtVO.setProject(projectName);
				qtVO.setFileList(fileList);

				returnList.add(qtVO);
			} else {
				ArrayList<FileInfoVO> fileList = new ArrayList<FileInfoVO>();
				QueryTreeVO qtVO = new QueryTreeVO();
				qtVO.setProject(projectName);
				qtVO.setFileList(fileList);
				returnList.add(qtVO);
			}
		}
		return returnList;
	}

	private Map mergeBoolUseDao(Map ids, String projectName) {
		Map map = new HashMap();
		HashSet set = (HashSet) daoMap.get(projectName);
		HashMap allDuplicateIds = (HashMap) ((HashMap) duplicateIds)
				.get(projectName);

		QMPropertiesXMLUtil util = new QMPropertiesXMLUtil();
		if (allDuplicateIds != null) {
			Set keySet = allDuplicateIds.keySet();

			Iterator itr = ids.keySet().iterator();
			while (itr.hasNext()) {
				Object object = itr.next();
				String strId = "";
				if (keySet.contains(object.toString())) {
					// 중복된 ID중 suffix 안붙인 것들
					int count = 0;
					while (keySet
							.contains(object
									+ MessagePropertiesLoader.view_explorer_util_queryid_suffix
									+ count)) {
						count++;
					}

					if (util.getPropertiesDupl2(((Location) ids.get(object))
							.getFile().getProject())) {
						strId = object
								+ MessagePropertiesLoader.view_explorer_util_queryid_suffix
								+ count;
					} else {
						strId = object + "";
					}

					// dao 사용여부 체크
					if (util.getPropertiesUsed2(((Location) ids.get(object))
							.getFile())) {
						if (set.contains(object.toString())) {
							map.put("O" + strId, ids.get(object));
						} else {
							map.put("X" + strId, ids.get(object));
						}
					} else {
						if (set.contains(object.toString())) {
							map.put("O" + strId, ids.get(object));
						} else {
							map.put("O" + strId, ids.get(object));
						}
					}

				} else {
					// 중복되지 않은 것들
					strId = object.toString();
					// dao 사용여부 체크
					if (util.getPropertiesUsed2(((Location) ids.get(object))
							.getFile())) {
						if (set.contains(object.toString())) {
							map.put("O" + strId, ids.get(object));
						} else {
							map.put("X" + strId, ids.get(object));
						}
					} else {
						if (set.contains(object.toString())) {
							map.put("O" + strId, ids.get(object));
						} else {
							map.put("O" + strId, ids.get(object));
						}
					}
				}
			}
		} else {
			Iterator itr = ids.keySet().iterator();
			while (itr.hasNext()) {
				Object object = itr.next();
				String strId = object.toString();
				if (util.getPropertiesUsed2(((Location) ids.get(object))
						.getFile())) {
					if (set.contains(strId)) {
						map.put("O" + strId, ids.get(object));
					} else {
						map.put("X" + strId, ids.get(object));
					}
				} else {
					if (set.contains(strId)) {
						map.put("O" + strId, ids.get(object));
					} else {
						map.put("O" + strId, ids.get(object));
					}
				}
			}
		}
		return map;
	}

	private String fileAlias(String projectName, Object filePath) {
		String result = "";

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();

		IProject project = null;
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].getName().equals(projectName)) {
				project = projects[i];
			}
		}

		IPath path = new Path(filePath.toString());
		IFile ifile = project.getFile(path);

		String content = BuilderUtil.readFile(ifile);

		String fileAliasString = "@alias";
		int queriesStart = content.indexOf("<queries>");

		int aliasStart = content.indexOf(fileAliasString);
		int aliasNameStart = -1;
		int aliasEnd = -1;
		int start = content.indexOf("<queries>");
		// TODO
		String aliasFileName = "";
		if (aliasStart > -1 && aliasStart < start) {
			aliasNameStart = aliasStart + fileAliasString.length();
			aliasEnd = content.indexOf("\n", aliasNameStart);
			int tempCommentEnd = content.indexOf("-->", aliasNameStart);
			if (aliasEnd == tempCommentEnd + 4) {
				aliasFileName = content.substring(aliasNameStart,
						tempCommentEnd).trim();
			} else if (aliasEnd < tempCommentEnd + 4) {
				aliasFileName = content.substring(aliasNameStart, aliasEnd)
						.trim();
			} else {
			}
		}
		return aliasFileName;
	}

	private ArrayList convertMapToQueryTreeVOList(HashMap returnMap) {
		ArrayList returnList = new ArrayList();
		Iterator mapIterator = returnMap.keySet().iterator();
		while (mapIterator.hasNext()) {
			// QueryTreeVO treeVO = new QueryTreeVO();
			// String category = mapIterator.next().toString();
			// ArrayList locationList = (ArrayList) returnMap.get(category);
			// treeVO.setCategory(category);
			// treeVO.setLocationList(locationList);
			// returnList.add(treeVO);
		}
		return returnList;
	}

	private static String category = "";

	private void addLocationToReturnMap(Object obj, HashMap returnMap) {

		Location loc = (Location) obj;
		// String category = loc.getCategory();
		// if(category == null || category.equalsIgnoreCase(""))
		// category = getDefaultCategoryValue();
		// if(returnMap.containsKey(category)){
		// ArrayList categoryList = (ArrayList) returnMap.get(category);
		// categoryList.add(loc);
		// }else{
		ArrayList categoryList = new ArrayList();
		categoryList.add(loc);
		returnMap.put(category, categoryList);
		// }
	}

	/**
	 * @param loc
	 *            Location of the Query which should be shown in Editor.
	 */
	public static void openQueryInEditorFromTreeViewer(Location loc) {
		if (loc == null)
			return;
		IFile file = loc.getFile();
		IProject project = file.getProject();
		try {
			file.deleteMarkers(QMMarkerHelper.QUERYNAV_DBLCLK_BOOK_MARKER_ID,
					false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			return;
		}
		IEditorPart editorPart = QMExplorerView.openEditor(file);

		IMarker marker = QMExplorerView.createMarker(file, loc.getCharStart(),
				loc.getCharEnd(),
				QMMarkerHelper.QUERYNAV_DBLCLK_BOOK_MARKER_ID,
				"Opening the Query from Query Navigator.");
		// show the location of query id.
		IWorkbenchPage activePage1 = QueryManagerActivator.getDefault()
				.getActiveWorkbenchPage();
		if (activePage1.getActiveEditor() != null)
			IDE.gotoMarker(activePage1.getActiveEditor(), marker);

		try {
			file.deleteMarkers(QMMarkerHelper.QUERYNAV_DBLCLK_BOOK_MARKER_ID,
					false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			// was deleting the marker which helped
			// me to go to the
			// query id from Query Navigator - Tree View.
			// If some problem occurs no need to do
			// anything here.....
			return;
		}
	}
}
