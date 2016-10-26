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
package org.anyframe.ide.querymanager.editors;

import java.util.ArrayList;

import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.model.QMXMLTableTreeViewer;
import org.anyframe.ide.querymanager.model.QueryInputAttribute;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The ModifyQueryActionDelegate class is for reading the values in the xml file
 * while opening the AddQuery Wizard for modifying anything in the xml file.
 * 
 * @author Neha.Prasad
 */
public class ModifyQueryActionDelegate extends AbstractQueryActionDelegate {

	static private ModifyQueryActionDelegate modifyQueryActionDelegate;
	private String modifiedDateString;
	private String authorName = "";
	private String modifiedCategory = null;
	private boolean categoryCheck = false;
	private String classValue;
	private String createdDateString = "";

	// boolean isServiceType = false;
	boolean isResultMapping = true;
	boolean isTableMapping = false;
	boolean isResultClassOnlyMapping = false;
	boolean isQueryOnly = false;

	private ArrayList paramList;
	private int paramcount = 0;

	public ModifyQueryActionDelegate() {
		super();
		modifyQueryActionDelegate = this;
	}

	public void run(IAction action) {
		if (targetEditor == null) {
			textSelection = null;
			return;
		}
		if (textSelection == null) {

			if (targetEditor.getSite().getShell() != null) {
				MessageDialog.openInformation(
						targetEditor.getSite().getShell(), "Modify Query",
						"Select the query to modify");
			}
			return;
		}

		getTheQueryAndOpenModifyWizard();

		textSelection = null;
	}

	public void selectionChanged(IAction action, ISelection selection) {

		this.action = action;
		this.action.setEnabled(false);

		if (targetEditor == null || selection == null) {

			return;
		} else {
			textSelection = null;
			if (selection instanceof ITextSelection) {
				textSelection = (TextSelection) selection;

				if (textSelection != null) {

					action.setEnabled(selectTextSelection());

				} else {
					action.setEnabled(false);
				}

			} else {
				action.setEnabled(false);
			}
		}

	}

	private void getTheQueryAndOpenModifyWizard() {

		impl = getElementImplFromStructuredTextEditor();

		if (impl != null) {
			openModifyWizard();
		}
	}

	private boolean selectTextSelection() {

		impl = getElementImplFromStructuredTextEditor();
		if (impl == null) {
			return false;
		}

		if (impl.getTagName() != null && impl.getTagName().equals("query")) {

			IEditorPart part = getQueryMgrEditorPart();

			if (part instanceof QMEditor) {

				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	public void openModifyWizard() {

		// Always make sure that class value is read from the Element Impl
		classValue = null;
		paramList = new ArrayList();

		StructuredTextEditor editor = (StructuredTextEditor) (targetEditor);
		IEditorInput input = editor.getEditorInput();
		IFileEditorInput ifei = (IFileEditorInput) input;
		IFile file = ifei.getFile();

		IWorkbenchPage activePage = QueryManagerActivator.getDefault()
				.getActiveWorkbenchPage();
		IEditorPart part = activePage.getActiveEditor();

		if (part instanceof QMEditor) {

			QMXMLTableTreeViewer tableTreeViewer = ((QMEditor) part)
					.getTableTreeViewer();

			if (tableTreeViewer != null
					&& tableTreeViewer.getSelection() instanceof TreeSelection) {
				TreeSelection treeSelected = (TreeSelection) tableTreeViewer
						.getSelection();
				Object element = treeSelected.getFirstElement();
				if (element != null && element instanceof ElementImpl) {
					impl = (ElementImpl) treeSelected.getFirstElement();

				}
			}

			String ID = impl.getAttribute("id");
			if (impl.getAttributes().getNamedItem("category") != null) {
				modifiedCategory = impl.getAttributes()
						.getNamedItem("category").getNodeValue();

				categoryCheck = true;

			} else {
				modifiedCategory = null;
				categoryCheck = false;
			}

			Node node = impl.getAttributes().getNamedItem("class");

			if (impl.getNodeName().equalsIgnoreCase("result")) {
				classValue = impl.getAttributes().getNamedItem("class")
						.getNodeValue();

			}
			if (node != null)
				classValue = node.getNodeValue();

			String queryString = "";

			String queryservicetype = "";

			NodeList queryIdNodeChildrens = impl.getChildNodes();

			queryString = getQueryString(queryString, queryIdNodeChildrens);

			NodeList queries;

			queries = impl.getContainerDocument().getDocumentElement()
					.getElementsByTagName("query");

			String authorString = "@author";
			String dateCreatedString = "@date.created";
			String dateModifiedString = "@date.modified";

			for (int i = 0; i < queryIdNodeChildrens.getLength(); i++) {
				Node queryIdChildNode = queryIdNodeChildrens.item(i);

				if (queryIdChildNode.getNodeName().equalsIgnoreCase("#comment")) {

					String content = queryIdChildNode.toString();

					int authorStartIndex = content.indexOf(authorString, 0);
					int authorEndIndex = -1;
					int dateCreatedEndIndex = -1;
					int dateModifiedEndIndex = -1;
					if (authorStartIndex > -1) {
						authorStartIndex = authorStartIndex
								+ authorString.length();
						authorEndIndex = content.indexOf("@", authorStartIndex);
					}
					int dateCreatedStartIndex = content.indexOf(
							dateCreatedString, 0);
					if (dateCreatedStartIndex > -1) {
						dateCreatedStartIndex = dateCreatedStartIndex
								+ dateCreatedString.length();
						dateCreatedEndIndex = content.indexOf("@",
								dateCreatedStartIndex);
					}
					int dateModifiedStartIndex = content.indexOf(
							dateModifiedString, 0);
					if (dateModifiedStartIndex > -1) {
						dateModifiedStartIndex = dateModifiedStartIndex
								+ dateModifiedString.length();
						dateModifiedEndIndex = content.indexOf("-->",
								dateModifiedStartIndex);
					}
					if (authorStartIndex > 6
							&& authorEndIndex > authorStartIndex)
						authorName = content.substring(authorStartIndex,
								authorEndIndex - 1);

					if (dateCreatedStartIndex > 12
							&& dateCreatedEndIndex > dateCreatedStartIndex)
						createdDateString = content.substring(
								dateCreatedStartIndex, dateCreatedEndIndex);
					if (dateModifiedStartIndex > 12
							&& dateModifiedEndIndex > dateModifiedStartIndex)
						modifiedDateString = content.substring(
								dateModifiedStartIndex, dateModifiedEndIndex);

					authorEndIndex = -1;
					dateCreatedEndIndex = -1;
					dateModifiedEndIndex = -1;
					authorStartIndex = -1;
					dateCreatedStartIndex = -1;
					dateModifiedStartIndex = -1;

				}

				if (queryIdChildNode.getNodeName()
						.equalsIgnoreCase("statement")) {
					Node statementNode = queryIdChildNode.getFirstChild();
					queryString = statementNode.getNodeValue();

					if (queryIdChildNode.hasChildNodes()) {
						NodeList ch = queryIdChildNode.getChildNodes();
						for (int j = 0; j < ch.getLength(); j++) {
							if ("#cdata-section".equalsIgnoreCase(ch.item(j)
									.getNodeName())) {
								queryString = ch.item(j).getNodeValue();
								break;
							}
						}
					}
					break;
				}

			}
			NodeList nodeListTab = null;
			Node node1 = impl.getParentNode();
			Node node2 = node1.getParentNode();
			NodeList nodeList = node2.getChildNodes();
			for (int count = 0; count < nodeList.getLength(); count++) {
				if ("table-mapping".equalsIgnoreCase(nodeList.item(count)
						.getNodeName())) {
					Node tableMapNode = nodeList.item(count);
					nodeListTab = tableMapNode.getChildNodes();
					break;
				}
			}
			boolean noResult = true;
			for (int count = 0; count < queryIdNodeChildrens.getLength(); count++) {
				Node queryNode = queryIdNodeChildrens.item(count);

				if (queryNode.getNodeName().equalsIgnoreCase("result")) {
					noResult = false;
					classValue = queryNode.getAttributes()
							.getNamedItem("class").getNodeValue();
					setResultMapping(false);
					setResultClassOnlyMapping(true);
					setTableMapping(false);
					setQueryOnly(false);
					if ("Put your VO name here".equals(classValue)) {
						classValue = null;
					}

					if (nodeListTab != null)
						for (int c = 0; c < nodeListTab.getLength(); c++) {
							Node tabNode = nodeListTab.item(c);
							if ("table".equalsIgnoreCase(tabNode.getNodeName())
									&& classValue != null
									&& classValue.equals(tabNode
											.getAttributes()
											.getNamedItem("class")
											.getNodeValue())) {
								setResultClassOnlyMapping(false);
								setTableMapping(true);
								break;
							}
						}

					NodeList queryNodechildrens = queryNode.getChildNodes();
					for (int count1 = 0; count1 < queryNodechildrens
							.getLength(); count1++) {

						Node queryNodeSubNode = queryNodechildrens.item(count1);
						if (queryNodeSubNode != null)
							if (queryNodeSubNode.getNodeName()
									.equalsIgnoreCase("result-mapping")) {
								setResultMapping(true);
								setResultClassOnlyMapping(false);
								setTableMapping(false);
								setQueryOnly(false);

							}
					}

				}
			}
			if (noResult) {
				setResultMapping(false);
				setResultClassOnlyMapping(true);
				setTableMapping(false);
				setQueryOnly(true);
			}

			int paramCount = 0;
			for (int count = 0; count < queryIdNodeChildrens.getLength(); count++) {
				Node queryNode = queryIdNodeChildrens.item(count);

				// int paramcount;
				if (queryNode.getNodeName().equalsIgnoreCase("param")) {
					paramCount++;

					QueryInputAttribute attribute = new QueryInputAttribute();
					Node bindingNode = queryNode.getAttributes().getNamedItem(
							"binding");
					if (bindingNode != null) {
						String binding = bindingNode.getNodeValue();
						attribute.setBinding(binding);
					}
					Node namedNode = queryNode.getAttributes().getNamedItem(
							"name");
					if (namedNode != null) {
						String name = namedNode.getNodeValue();
						attribute.setName(name);
					}

					attribute.setNo(paramCount);
					Node typeNode = queryNode.getAttributes().getNamedItem(
							"type");
					if (typeNode != null) {
						String type = typeNode.getNodeValue();
						attribute.setType(type);
					}

					paramList.add(attribute);

				}
			}
			// openWizard(editor, file, queryString, queries);
		}

	}

	// private void openWizard(StructuredTextEditor editor, IFile file,
	// String queryString, NodeList queries) {
	// 		AddQueryWizard wizard = new AddQueryWizard(false);
	//
	// 		if (impl.getAttribute("id") != null) {
	// 			wizard.setData(impl.getAttribute("id"), queryString);
	// 			wizard.setModifyFlag(true);
	// 			wizard.setQueries(queries);
	// 			wizard.setSite(editor.getSite());
	// 			wizard.setDocument(impl.getContainerDocument());
	// 			wizard.setFileName(file.getName());
	// 			wizard.setProject(file.getProject());
	// 			wizard.init(PlatformUI.getWorkbench(), impl, editor);
	// 			IWorkbenchPage activePage = QueryManagerPlugin.getDefault().getActiveWorkbenchPage();
	// 			IEditorPart part = activePage.getActiveEditor();
	//
	// 			if (part instanceof QMEditor) {
	//
	// 				QMXMLTableTreeViewer tableTreeViewer = ((QMEditor) part).getTableTreeViewer();
	// 				wizard.setQueryMgrXMLTableTreeViewer(tableTreeViewer);
	// 				tableTreeViewer.setClassValue(classValue);
	// 				tableTreeViewer.setAuthorName(authorName);
	// 				tableTreeViewer.setModifiedDateString(modifiedDateString);
	// 				tableTreeViewer.setCreatedDateString(createdDateString);
	// 				tableTreeViewer.setParamcount(paramcount);
	// 				tableTreeViewer.setParamList(paramList);
	// 				tableTreeViewer.setResultMapping(isResultMapping);
	// 				tableTreeViewer.setModifiedCategory(modifiedCategory);
	// 				tableTreeViewer.setCategoryCheck(categoryCheck);
	// 				tableTreeViewer.setClassValue(classValue);
	// 				tableTreeViewer.setResultClassOnlyMapping(isResultClassOnlyMapping);
	// 				tableTreeViewer.setTableMapping(isTableMapping);
	// 				tableTreeViewer.setQueryOnly(isQueryOnly);
	// 				wizard.setData(impl.getAttribute("id"), queryString);
	//
	// 			}
	//
	// 			WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench()
	// 				.getActiveWorkbenchWindow().getShell(), wizard);
	//
	// 			dialog.open();
	// 		}
	// }

	private String getQueryString(String queryString,
			NodeList queryIdNodeChildrens) {
		for (int i = 0; i < queryIdNodeChildrens.getLength(); i++) {
			Node queryIdChildNode = queryIdNodeChildrens.item(i);
			if (queryIdChildNode.getNodeName().equalsIgnoreCase("statement")) {
				Node statementNode = queryIdChildNode.getFirstChild();
				queryString = statementNode.getNodeValue();

				if (queryIdChildNode.hasChildNodes()) {
					NodeList ch = queryIdChildNode.getChildNodes();
					for (int j = 0; j < ch.getLength(); j++) {
						if ("#cdata-section".equalsIgnoreCase(ch.item(j)
								.getNodeName())) {
							queryString = ch.item(j).getNodeValue();
							break;
						}
					}
				}
				break;
			}
		}
		return queryString;
	}

	public static ModifyQueryActionDelegate getModifyQueryActionDelegate() {
		return modifyQueryActionDelegate;
	}

	public String getCreatedDateString() {
		return createdDateString;
	}

	public void setCreatedDateString(String createdDateString) {
		this.createdDateString = createdDateString;
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

}
