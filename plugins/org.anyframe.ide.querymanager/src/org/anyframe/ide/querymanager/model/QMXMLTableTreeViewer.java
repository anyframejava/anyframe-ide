/*
 * Copyright 2008-2013 the original author or authors.
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
package org.anyframe.ide.querymanager.model;

import java.util.ArrayList;

import org.anyframe.ide.querymanager.actions.QMXMLNodeActionManager;
import org.anyframe.ide.querymanager.editors.QMXMLTreeExtension;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.ui.internal.actions.NodeAction;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLTableTreeViewer;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * The class QMXMLTableTreeViewer extends XML TableTreeViewer .This class will
 * be used to create design view for QueryMgr editor
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class QMXMLTableTreeViewer extends XMLTableTreeViewer {

	protected QMXMLTreeExtension treeExtension;

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
	// private String queryString;
	private int paramcount = 0;

	// AddQueryWizard wizard;

	/**
	 * Constructor with parameter parent
	 * 
	 * @param parent
	 *            The composite object
	 */

	public QMXMLTableTreeViewer(Composite parent) {

		super(parent);
		// set up providers
		this.treeExtension = new QMXMLTreeExtension(getTree());
		QMXMLTableTreeContentProvider provider = new QMXMLTableTreeContentProvider();
		setContentProvider(provider);
		setLabelProvider(provider);

	}

	/**
	 * This creates a context menu for the viewer and adds a listener as well
	 * registering the menu for extension.
	 */
	protected void createContextMenu() {
		// TODO : XML Editor 에서 Query Editor 관련 Action 삭제~~
		MenuManager contextMenu = new MenuManager("#PopUp"); //$NON-NLS-1$
		contextMenu.add(new Separator("additions")); //$NON-NLS-1$
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(new NodeActionMenuListener());
		Menu menu = contextMenu.createContextMenu(getControl());
		getControl().setMenu(menu);
		// makeActions();
		// hookSingleClickAction();

	}

	/**
	 * Implementing single click action on node in the tree viewr
	 */
	private void hookSingleClickAction() {
		this.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				singleClickAction.run();
			}
		});
	}

	/**
	 * The class NodeActionMenuListener adds listener to Node Action Menu
	 */

	class NodeActionMenuListener implements IMenuListener {
		/**
		 * @param menuManager
		 *            IMenuManager object
		 */
		public void menuAboutToShow(IMenuManager menuManager) {
			// used to disable NodeSelection listening
			// while running
			// NodeAction
			QMXMLNodeActionManager nodeActionManager = new QMXMLNodeActionManager(
					((IDOMDocument) getInput()).getModel(),
					QMXMLTableTreeViewer.this) {
				public void beginNodeAction(NodeAction action) {
					super.beginNodeAction(action);
				}

				public void endNodeAction(NodeAction action) {
					super.endNodeAction(action);
				}
			};

			// Filling Context menu
			nodeActionManager.fillContextMenu(menuManager, getSelection());
			menuManager.add(new Separator());

			// Add Query and Modify Query option is
			// added to the context menu
			// TODO : Query Explorer 로 관련 context menu 이동
			// menuManager.add(modifyQueryAction);
			// menuManager.add(addQueryAction);
			// menuManager.add(searchAction);

		}
	}

	/**
	 * returns document object
	 * 
	 * @return Document
	 */
	public Document getDocument() {
		return (Document) getInput();
	}

	private Action modifyQueryAction;

	private Action addQueryAction;

	private Action singleClickAction;

	private Action searchAction;

	private NodeList queries;

	private ArrayList paramList;

	/**
	 * Opens the wizard page where developer can add and modify query
	 * 
	 * @param query
	 *            query string
	 * @param modifyFlag
	 *            boolean representing modify condition
	 */
	// public void openWizard(String query, boolean modifyFlag) {
	// String classValue = null;
	// String modifiedCategory = null;
	// boolean categoryCheck = false;
	// String resultMapperClass = null;
	//
	// String queryId = "";
	// String authorName = "";
	// String createdDateString = "";
	// int paramcount = 0;
	// String queryString = "";
	// paramList = new ArrayList();
	//
	// if (modifyFlag) {
	// ISelection viewerSelection = getSelection();
	// Object obj = ((IStructuredSelection) viewerSelection)
	// .getFirstElement();
	//
	// if (obj == null) {
	// MessageDialog.openInformation(this.getControl().getShell(),
	// "Please Select a Query Id.",
	// "To Modify a Query Please select one First.");
	// return;
	// }
	// Node queryIdNode = (Node) obj;
	//
	// queryId = queryIdNode.getAttributes().getNamedItem("id")
	// .getNodeValue();
	//
	// if (queryIdNode.getAttributes().getNamedItem("category") != null) {
	// modifiedCategory = queryIdNode.getAttributes().getNamedItem(
	// "category").getNodeValue();
	//
	// categoryCheck = true;
	//
	// }
	//
	// String authorString = "@author";
	// String dateCreatedString = "@date.created";
	// String dateModifiedString = "@date.modified";
	// String result = "<result";
	// String resultMapp = "<result-mapping";
	// NodeList queryIdNodeChildrens = queryIdNode.getChildNodes();
	//
	// // for getting the information about
	// // author,created date and
	// // modified date
	// for (int i = 0; i < queryIdNodeChildrens.getLength(); i++) {
	// Node queryIdChildNode = queryIdNodeChildrens.item(i);
	//
	// if (queryIdChildNode.getNodeName().equalsIgnoreCase("#comment")) {
	//
	// String content = queryIdChildNode.toString();
	//
	// int authorStartIndex = content.indexOf(authorString, 0);
	// int authorEndIndex = -1;
	// int dateCreatedEndIndex = -1;
	// int dateModifiedEndIndex = -1;
	// if (authorStartIndex > -1) {
	// authorStartIndex = authorStartIndex
	// + authorString.length();
	// authorEndIndex = content
	// .indexOf("\n", authorStartIndex);
	// }
	// int dateCreatedStartIndex = content.indexOf(
	// dateCreatedString, 0);
	// if (dateCreatedStartIndex > -1) {
	// dateCreatedStartIndex = dateCreatedStartIndex
	// + dateCreatedString.length();
	// dateCreatedEndIndex = content.indexOf("\n",
	// dateCreatedStartIndex);
	// }
	// int dateModifiedStartIndex = content.indexOf(
	// dateModifiedString, 0);
	// if (dateModifiedStartIndex > -1) {
	// dateModifiedStartIndex = dateModifiedStartIndex
	// + dateModifiedString.length();
	// dateModifiedEndIndex = content.indexOf("\n",
	// dateModifiedStartIndex);
	// }
	// if (authorStartIndex > 6
	// && authorEndIndex > authorStartIndex)
	// authorName = content.substring(authorStartIndex,
	// authorEndIndex /*- 1*/);
	//
	// if (dateCreatedStartIndex > 12
	// && dateCreatedEndIndex > dateCreatedStartIndex)
	// createdDateString = content.substring(
	// dateCreatedStartIndex, dateCreatedEndIndex);
	// if (dateModifiedStartIndex > 12
	// && dateModifiedEndIndex > dateModifiedStartIndex)
	// modifiedDateString = content.substring(
	// dateModifiedStartIndex, dateModifiedEndIndex);
	//
	// authorEndIndex = -1;
	// dateCreatedEndIndex = -1;
	// dateModifiedEndIndex = -1;
	// authorStartIndex = -1;
	// dateCreatedStartIndex = -1;
	// dateModifiedStartIndex = -1;
	//
	// }
	//
	// if (queryIdChildNode.getNodeName()
	// .equalsIgnoreCase("statement")) {
	// Node statementNode = queryIdChildNode.getFirstChild();
	// queryString = statementNode.getNodeValue();
	//
	// if (queryIdChildNode.hasChildNodes()) {
	// NodeList ch = queryIdChildNode.getChildNodes();
	// for (int j = 0; j < ch.getLength(); j++) {
	// if ("#cdata-section".equalsIgnoreCase(ch.item(j)
	// .getNodeName())) {
	// queryString = ch.item(j).getNodeValue();
	// break;
	// }
	// }
	// }
	// break;
	// }
	//
	// }
	// NodeList nodeListTab = null;
	// Node node1 = queryIdNode.getParentNode();
	// Node node2 = node1.getParentNode();
	// NodeList nodeList = node2.getChildNodes();
	// for (int count = 0; count < nodeList.getLength(); count++) {
	// if ("table-mapping".equalsIgnoreCase(nodeList.item(count)
	// .getNodeName())) {
	// Node tableMapNode = nodeList.item(count);
	// nodeListTab = tableMapNode.getChildNodes();
	// break;
	// }
	// }
	// boolean noResult = true;
	// for (int count = 0; count < queryIdNodeChildrens.getLength(); count++) {
	// Node queryNode = queryIdNodeChildrens.item(count);
	//
	// if (queryNode.getNodeName().equalsIgnoreCase("result")) {
	// noResult = false;
	// classValue = queryNode.getAttributes()
	// .getNamedItem("class").getNodeValue();
	// if (queryNode.getAttributes().getNamedItem("mapper") != null)
	// resultMapperClass = queryNode.getAttributes()
	// .getNamedItem("mapper").getNodeValue();
	// setResultMapping(false);
	// setResultClassOnlyMapping(true);
	// setTableMapping(false);
	// setQueryOnly(false);
	// if ("Put your VO name here".equals(classValue)) {
	// classValue = null;
	// }
	// if (nodeListTab != null)
	// for (int c = 0; c < nodeListTab.getLength(); c++) {
	// Node tabNode = nodeListTab.item(c);
	// if ("table".equalsIgnoreCase(tabNode.getNodeName())
	// && classValue != null
	// && classValue.equals(tabNode
	// .getAttributes().getNamedItem(
	// "class").getNodeValue())) {
	// setResultClassOnlyMapping(false);
	// setTableMapping(true);
	// break;
	// }
	// }
	//
	// NodeList queryNodechildrens = queryNode.getChildNodes();
	// for (int count1 = 0; count1 < queryNodechildrens
	// .getLength(); count1++) {
	//
	// Node queryNodeSubNode = queryNodechildrens.item(count1);
	// if (queryNodeSubNode != null)
	// if (queryNodeSubNode.getNodeName()
	// .equalsIgnoreCase("result-mapping")) {
	// setResultMapping(true);
	// setResultClassOnlyMapping(false);
	// setTableMapping(false);
	// setQueryOnly(false);
	// }
	// }
	// }
	// }
	// if (noResult) {
	// setResultMapping(false);
	// setResultClassOnlyMapping(true);
	// setTableMapping(false);
	// setQueryOnly(true);
	// }
	//
	// int paramCount = 0;
	// for (int count = 0; count < queryIdNodeChildrens.getLength(); count++) {
	// Node queryNode = queryIdNodeChildrens.item(count);
	//
	// // int paramcount;
	// if (queryNode.getNodeName().equalsIgnoreCase("param")) {
	// paramCount++;
	//
	// QueryInputAttribute attribute = new QueryInputAttribute();
	// Node bindingNode = queryNode.getAttributes().getNamedItem(
	// "binding");
	// if (bindingNode != null) {
	// String binding = bindingNode.getNodeValue();
	// attribute.setBinding(binding);
	// }
	// Node namedNode = queryNode.getAttributes().getNamedItem(
	// "name");
	// if (namedNode != null) {
	// String name = namedNode.getNodeValue();
	// attribute.setName(name);
	// }
	//
	// attribute.setNo(paramCount);
	// Node typeNode = queryNode.getAttributes().getNamedItem(
	// "type");
	// if (typeNode != null) {
	// String type = typeNode.getNodeValue();
	// attribute.setType(type);
	// }
	//
	// paramList.add(attribute);
	//
	// }
	// }
	// }
	//
	// setParamcount(paramcount);
	// setModifiedCategory(modifiedCategory);
	// setCategoryCheck(categoryCheck);
	// setClassValue(classValue);
	// setResultMapperClass(resultMapperClass);
	// setAuthorName(authorName);
	// setCreatedDateString(createdDateString);
	// Object input = this.getInput();
	//
	// queries = getDocument().getDocumentElement().getElementsByTagName(
	// "query");
	// // wizard = new AddQueryWizard(modifyFlag);
	// //
	// // wizard.setData(queryId, queryString);
	// // wizard.setModifyFlag(modifyFlag);
	// // wizard.setQueries(queries);
	// // wizard.setSite(site);
	// // wizard.setDocument(getDocument());
	// // wizard.setFileName(fileName);
	// // wizard.setProject(project);
	// //
	// // wizard.init(PlatformUI.getWorkbench(),
	// // (IStructuredSelection) getSelection());
	// //
	// // WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench()
	// // .getActiveWorkbenchWindow().getShell(), wizard);
	// // dialog.open();
	// // 에디터 오픈으로 변경하는 부분
	//
	// // Object ob1 = queryId;
	// // Object ob2 = queryString;
	// // Object ob3 = modifyFlag;
	// // Object ob4 = queries;
	// // Object ob5 = site;
	// // Object ob6 = getDocument();
	// // Object ob7 = fileName;
	// // Object ob8 = project;
	// //
	// // System.out.println(queryId);
	// // System.out.println(queryString);
	// // System.out.println(modifyFlag);
	// // System.out.println(queries);
	// // System.out.println(site);
	// // System.out.println(getDocument());
	// // System.out.println(fileName);
	// // System.out.println(project);
	//
	// IWorkbenchPage Page = PlatformUI.getWorkbench()
	// .getActiveWorkbenchWindow().getActivePage();
	// EditorInput queryinput = new EditorInput();
	//
	// // input setting
	// queryinput.setQueryId(queryId);
	// queryinput.setQueryString(queryString);
	// queryinput.setModifyFlag(modifyFlag);
	// queryinput.setQueries(queries);
	// queryinput.setSite(site);
	// queryinput.setDocument(getDocument());
	// queryinput.setFileName(fileName);
	// queryinput.setProject(project);
	//
	// try {
	// Page.openEditor(queryinput,
	// "anyframe.querymanager.eclipse.core.querytest.QueryEditor");
	// } catch (PartInitException e) {
	// // TODO must exception process
	// e.printStackTrace();
	// }
	// }

	/**
	 * Creating Add Query and Modify Query Action
	 */
	private void makeActions() {
		// creating search Query Action

		// searchAction = new Action() {
		//
		// public void run() {
		// // // Opening Query Manager View
		// try {
		// PlatformUI
		// .getWorkbench()
		// .getActiveWorkbenchWindow()
		// .getActivePage()
		// .showView(
		// "anyframe.querymanager.eclipse.core.views.QueryNavigatorView");
		// } catch (PartInitException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// }
		//
		// };
		// searchAction.setText("Search Queries");
		// searchAction.setToolTipText("Search");
		//
		// // Creating Modify Query Action
		// modifyQueryAction = new Action() {
		// public void run() {
		// // Opening Modify query wizard page
		// openWizard("", true);
		//
		// }
		// };
		// modifyQueryAction.setEnabled(false);
		//
		// modifyQueryAction.setText("Modify Query");
		// modifyQueryAction.setToolTipText("Modify the query");
		//
		// // Creating Add Query Action
		// addQueryAction = new Action() {
		// public void run() {
		//
		// // Opening Add query wizard page
		// openWizard("", false);
		//
		// }
		// };
		// addQueryAction.setText("Add Query");
		// addQueryAction.setToolTipText("Add a query");
		//
		// singleClickAction = new Action() {
		// public void run() {
		//
		// ISelection viewerSelection = getSelection();
		// Object object = ((IStructuredSelection) viewerSelection)
		// .getFirstElement();
		//
		// if (object instanceof Node) {
		// Node node = (Node) object;
		// switch (node.getNodeType()) {
		// case Node.ATTRIBUTE_NODE: {
		// addQueryAction.setEnabled(true);
		// modifyQueryAction.setEnabled(false);
		// break;
		// }
		// case Node.DOCUMENT_TYPE_NODE: {
		// modifyQueryAction.setEnabled(false);
		// addQueryAction.setEnabled(false);
		// break;
		// }
		// case Node.ELEMENT_NODE: {
		// if (node != null
		// && node.getParentNode() != null
		// && node.getParentNode().getNodeName() != null
		// && node.getParentNode().getNodeName()
		// .equalsIgnoreCase("queries")) {
		// modifyQueryAction.setEnabled(true);
		// addQueryAction.setEnabled(true);
		// break;
		// } else {
		// modifyQueryAction.setEnabled(false);
		// addQueryAction.setEnabled(true);
		// break;
		// }
		// }
		// case Node.PROCESSING_INSTRUCTION_NODE: {
		// modifyQueryAction.setEnabled(false);
		// addQueryAction.setEnabled(false);
		// break;
		// }
		//
		// }
		// }
		// }
		// };

	}

	/**
	 * getter
	 * 
	 * @return site
	 */
	public IWorkbenchPartSite getSite() {
		return site;
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

	// public boolean isServiceType() {
	// return isServiceType;
	// }
	//
	// public void setServiceType(boolean serviceType) {
	// this.isServiceType = serviceType;
	// }

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
