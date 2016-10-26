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
package org.anyframe.ide.querymanager.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPartSite;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a configured Server profile. This class extends Alias class and
 * implements some "Server" specific cases.
 *
 * @author Junghwan Hong
 * @since 2.1.0
 */
public class EditorInput implements IEditorInput {

	private String queryId;

	private String queryString;

	private boolean modifyFlag;

	private NodeList queries;

	private IWorkbenchPartSite site;

	private Document document;

	private String fileName;

	private IProject project;

	private StructuredSelection querySelect;

	private Node existNode;

	private IFile file;

	private String title;

	public IFile getFile() {
		return file;
	}

	public void setFile(IFile file) {
		this.file = file;
	}

	public Node getExistNode() {
		return existNode;
	}

	public void setExistNode(Node existNode) {
		this.existNode = existNode;
	}

	public StructuredSelection getQuerySelect() {
		return querySelect;
	}

	public void setQuerySelect(StructuredSelection querySelect) {
		this.querySelect = querySelect;
	}

	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return this.queryId;
	}

	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolTipText() {
		// TODO Auto-generated method stub
		return "";
	}

	public Object getAdapter(Class class1) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * member variables getter and setter method
	 */
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public boolean isModifyFlag() {
		return modifyFlag;
	}

	public void setModifyFlag(boolean modifyFlag) {
		this.modifyFlag = modifyFlag;
	}

	public NodeList getQueries() {
		return queries;
	}

	public void setQueries(NodeList queries) {
		this.queries = queries;
	}

	public IWorkbenchPartSite getSite() {
		return site;
	}

	public void setSite(IWorkbenchPartSite site) {
		this.site = site;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
