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
package org.anyframe.ide.querymanager.editors;

import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.model.EditorInput;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;

/**
 * Implement Query editor. This class extends EditorPart class.
 *
 * @author Junghwan Hong
 * @since 2.1.0
 */
public class QueryEditor extends MultiPageEditorPart implements
		IResourceChangeListener {

	private IEditorPart editor;

	private Shell shell;

	private QueryPage page;

	private boolean isDirty;

	private String title;

	public void setDirty(boolean isDirty) {
		if (this.isDirty == isDirty)
			return;
		this.isDirty = isDirty;
		firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
		isDirty();
	}

	public Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	public IEditorPart getEditor() {
		return editor;
	}

	public void setEditor(IEditorPart editor) {
		this.editor = editor;
	}

	@Override
	protected void createPages() {
		// TODO Auto-generated method stub
		page = QueryPage.getInstance();

		int index = addPage(page.getPage(getContainer()));
		setPageText(index, "");

		setActivePage(0);

		page.setInput((EditorInput) getEditorInput());
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// XML file save
		if (getDefault(title).page.validate()) {
			getDefault(title).page.editQuery();
			setDirty(false);
			QueryManagerActivator.getDefault().getQMExplorerView(true)
					.refresh_with_link();
		}
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		// TODO Auto-generated method stub
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			System.out.println("close");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.MultiPageEditorPart#init(org.eclipse.ui.IEditorSite,
	 * org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException {
		QueryPage page = QueryPage.getInstance();

		setSite(site);
		setInputWithNotify(editorInput);
		setPartName(((EditorInput) editorInput).getName());
		setTitleToolTip(editorInput.getToolTipText());

		// hideTabs();
		updateTitle();
		((EditorInput) getEditorInput()).setTitle(title);

		super.init(site, editorInput);
		page.setInput(getEditorInput());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.MultiPageEditorPart#isDirty()
	 */
	public boolean isDirty() {
		return isDirty;
	}

	void updateTitle() {
		title = getTitleName();
		setPartName(title);
		setTitleToolTip(getEditorInput().getToolTipText());
	}

	private String getTitleName() {
		Object t = getEditorInput();
		String title = getEditorInput().getName();
		if (title.equals(""))
			// add query
			return QueryManagerActivator.getDefault().getAddQuertyTitleName();
		else
			// modify query
			return title;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @param editor
	 *            title name
	 * @return the shared instance
	 */
	public static QueryEditor getDefault(String title) {
		IWorkbenchPage Page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorPart[] editors = Page.getEditors();

		for (IEditorPart edit : editors)
			if (edit.getTitle().equals(title))
				return (QueryEditor) edit;
		return (QueryEditor) editors[0];

	}

	public QueryPage getQueryPage() {
		return page;
	}

}
