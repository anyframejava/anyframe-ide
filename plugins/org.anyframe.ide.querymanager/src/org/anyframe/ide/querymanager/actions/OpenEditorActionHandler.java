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
package org.anyframe.ide.querymanager.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.build.Location;
import org.anyframe.ide.querymanager.messages.MessagePropertiesLoader;
import org.anyframe.ide.querymanager.model.EditorInput;
import org.anyframe.ide.querymanager.model.FileInfoVO;
import org.anyframe.ide.querymanager.views.QMExplorerView;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Open Query Add, Edit Editor Action. Setting query information in editor. This
 * class extends AbstractQueryManagerAction class.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
@SuppressWarnings("restriction")
public class OpenEditorActionHandler {

	boolean modify = false;
	ISelection selected = null;

	IWorkbenchPartSite site;
	String fileName = "";
	IProject project = null;

	// boolean isServiceType = true;
	boolean isResultMapping = false;
	boolean isTableMapping = false;
	boolean isResultClassOnlyMapping = false;
	boolean isQueryOnly = false;
	private String modifiedCategory;
	private boolean categoryCheck = false;
	private String classValue;
	private String resultMapperClass;

	private String authorName;
	private String createdDateString;
	private String modifiedDateString;
	private int paramcount = 0;
	private NodeList queries;
	private ArrayList paramList;
	private Node existNode;

	private Document document = null;

	private void setDocument(File file) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();

			// builder.setEntityResolver(new DTDResolver());
			document = builder.parse(file);

		} catch (FileNotFoundException e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Check DTD in XML file.", e);
			MessageDialogUtil.openMessageDialog("Not available DTD in XML file",
					"Check DTD in XML file.", MessageDialog.ERROR);
		} catch (Exception e) {
			// e.printStackTrace();
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Check DTD in XML file.", e);
			MessageDialogUtil.openMessageDialog("Not available DTD in XML file",
					"Check DTD in XML file.", MessageDialog.ERROR);
		}
	}

	public void setModify(boolean b, ISelection selected) {
		this.modify = b;
		this.selected = selected;

		if (modify) {
			settingModifyQuery();
		} else {
			settingAddQuery();
		}
	}

	private void settingAddQuery() {

		String classValue = null;
		String modifiedCategory = null;
		boolean categoryCheck = false;
		String resultMapperClass = null;

		String queryId = "";
		String authorName = "";
		String createdDateString = "";
		int paramcount = 0;
		String queryString = "";

		File file = new File(
				((FileInfoVO) QMExplorerView.getSelected()).getPath());
		IPath path = new Path(file.getAbsolutePath());
		IFile iFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFileForLocation(path);

		try {
			setDocument(file);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		setParamcount(paramcount);
		setModifiedCategory(modifiedCategory);
		setCategoryCheck(categoryCheck);
		setClassValue(classValue);
		setResultMapperClass(resultMapperClass);
		setAuthorName(authorName);
		setCreatedDateString(createdDateString);

		try {
			queries = document.getDocumentElement().getElementsByTagName(
					"query");

			IWorkbenchPage Page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();

			// add project 12.10.26 by Junghwan Hong
			project = iFile.getProject();

			EditorInput queryinput = new EditorInput();

			// input setting
			queryinput.setQueryId(queryId);
			queryinput.setQueryString(queryString);
			queryinput.setModifyFlag(modify);
			queryinput.setQueries(queries);
			// TODO check site
			queryinput.setSite(getSite());
			queryinput.setDocument(document);
			queryinput.setFileName(fileName);
			queryinput.setProject(project);
			// add file
			queryinput.setFile(iFile);
			try {
				Page.openEditor(queryinput,
						"org.anyframe.ide.querymanager.editors.QueryEditor");
			} catch (PartInitException e) {
				// TODO must exception process
				e.printStackTrace();
			}
		} catch (NullPointerException e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Check DTD in XML file.", e);
		}

	}

	public void settingModifyQuery() {

		String classValue = null;
		String modifiedCategory = null;
		boolean categoryCheck = false;
		String resultMapperClass = null;

		String queryId = "";
		String authorName = "";
		String createdDateString = "";
		int paramcount = 0;
		String queryString = "";

		Object object = ((TreeSelection) selected).getFirstElement();

		Object ob = ((FileInfoVO) ((TreeSelection) selected).getPaths()[0]
				.getSegment(1)).getQueryId().get(object);
		Location loc = (Location) ob;

		IFile file = loc.getFile();
		File obj = file.getLocation().toFile();

		try {
			setDocument(obj);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		setParamcount(paramcount);
		setModifiedCategory(modifiedCategory);
		setCategoryCheck(categoryCheck);
		setClassValue(classValue);
		setResultMapperClass(resultMapperClass);
		setAuthorName(authorName);
		setCreatedDateString(createdDateString);

		String tempId = object.toString().substring(1);
		int index = tempId
				.indexOf(MessagePropertiesLoader.view_explorer_util_queryid_suffix);
		if (index == -1) {
			queryId = tempId;
		} else {
			queryId = tempId.substring(0, index);
		}
		queryString = loc.getQuery();

		try {
			queries = document.getDocumentElement().getElementsByTagName(
					"query");
			fileName = loc.getFile().getName();
			project = loc.getFile().getProject();

			IWorkbenchPage Page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			EditorInput queryinput = new EditorInput();

			setExistNode(queryId);

			// input setting
			queryinput.setQueryId(queryId);
			queryinput.setQueryString(queryString);
			queryinput.setModifyFlag(modify);
			queryinput.setQueries(queries);
			// TODO check site
			queryinput.setSite(getSite());
			queryinput.setDocument(document);
			queryinput.setFileName(fileName);
			queryinput.setProject(project);
			queryinput.setExistNode(existNode);
			queryinput.setFile(file);

			try {
				Page.openEditor(queryinput,
						"org.anyframe.ide.querymanager.editors.QueryEditor");
			} catch (PartInitException e) {
				// TODO must exception process
				e.printStackTrace();
			}
		} catch (NullPointerException e) {
			// QueryManagerPlugin.error("Check DTD file in XML file.", e);
		}

	}

	private void setExistNode(String queryId) {
		Element root = document.getDocumentElement();
		NodeList children = document.getElementsByTagName("query");

		for (int i = 0; i < children.getLength(); i++) {
			Element node = (Element) children.item(i);
			if (node != null) {
				if (children.item(i).getAttributes().getNamedItem("id")
						.getNodeValue().equals(queryId)) {
					existNode = children.item(i);
				}
			}
		}
	}

	/**
	 * getter
	 * 
	 * @return site
	 */
	public IWorkbenchPartSite getSite() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getPartService().getActivePart().getSite();
	}

	/**
	 * getter
	 * 
	 * @return fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * setter
	 * 
	 * @param site
	 *            IWorkbenchPartSite object
	 */
	public void setSite(IWorkbenchPartSite site) {
		this.site = site;
	}

	/**
	 * setter
	 * 
	 * @param fileName
	 *            the name of the file
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public String getClassValue() {
		return classValue;
	}

	public void setClassValue(String classValue) {
		this.classValue = classValue;
	}

	public String getResultMapperClass() {
		return resultMapperClass;
	}

	public void setResultMapperClass(String resultMapperClass) {
		this.resultMapperClass = resultMapperClass;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getCreatedDateString() {
		return createdDateString;
	}

	public void setCreatedDateString(String createdDateString) {
		this.createdDateString = createdDateString;
	}

	public String getModifiedDateString() {
		return modifiedDateString;
	}

	public void setModifiedDateString(String modifiedDateString) {
		this.modifiedDateString = modifiedDateString;
	}

	public int getParamcount() {
		return paramcount;
	}

	public void setParamcount(int paramcount) {
		this.paramcount = paramcount;
	}

	public ArrayList getParamListForTheSelectedQuery() {

		return paramList;
	}

	public void setParamList(ArrayList paramList) {
		this.paramList = paramList;
	}

	public boolean isResultMapping() {
		return isResultMapping;
	}

	public void setResultMapping(boolean isResultMapping) {
		this.isResultMapping = isResultMapping;
	}

	public boolean isTableMapping() {
		return isTableMapping;
	}

	public void setTableMapping(boolean isTableMapping) {
		this.isTableMapping = isTableMapping;
	}

	public boolean isResultClassOnlyMapping() {
		return isResultClassOnlyMapping;
	}

	public void setResultClassOnlyMapping(boolean isResultClassOnlyMapping) {
		this.isResultClassOnlyMapping = isResultClassOnlyMapping;
	}

	public boolean isQueryOnly() {
		return isQueryOnly;
	}

	public void setQueryOnly(boolean isQueryOnly) {
		this.isQueryOnly = isQueryOnly;
	}

	public String getModifiedCategory() {
		return modifiedCategory;
	}

	public void setModifiedCategory(String modifiedCategory) {
		this.modifiedCategory = modifiedCategory;
	}

	public boolean isCategoryCheck() {
		return categoryCheck;
	}

	public void setCategoryCheck(boolean categoryCheck) {
		this.categoryCheck = categoryCheck;
	}

}
