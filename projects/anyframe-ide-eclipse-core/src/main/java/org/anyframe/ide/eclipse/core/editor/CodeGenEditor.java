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
package org.anyframe.ide.eclipse.core.editor;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Locale;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.eclipse.core.config.AnyframeConfig;
import org.anyframe.ide.eclipse.core.config.JdbcType;
import org.anyframe.ide.eclipse.core.config.ProjectInfo;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.anyframe.ide.eclipse.core.util.XmlFileUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

/**
 * This is an ConfigurationEditor class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class CodeGenEditor extends MultiPageEditorPart implements
        IResourceChangeListener {

    private CodeGenEditor instance;
    private TextEditor textEditor;

    private AnyframeConfig anyframeConfig;
    private java.util.List<JdbcType> jdbcTypes;
    private IFile fileAnyframeConfig;
    private PropertiesIO projectMF;
    private IEditorPart editor;
    private Shell shell;
    private IProject currentProject;

    private HashMap<Integer, Page> mapPages = new HashMap<Integer, Page>();

    private int indexTextEditor = -1;

    private static String[] pageNameKeys = {"editor.codegen.domain.title",
        "editor.codegen.crud.title", "editor.codegen.config.title",
        "editor.codegen.jdbc.title", "editor.codegen.installation.title",
        "editor.codegen.ctip.title" };

    public CodeGenEditor() {

        super();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.addResourceChangeListener(this);
        instance = this;
    }

    public HashMap<Integer, Page> getMapPages() {
        return mapPages;
    }

    public void setMapPages(HashMap<Integer, Page> mapPages) {
        this.mapPages = mapPages;
    }

    public CodeGenEditor getInstance() {
        return instance;
    }

    public IEditorPart getEditor() {
        return editor;
    }

    public void setEditor(IEditorPart editor) {
        this.editor = editor;
    }

    public IProject getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(IProject currentProject) {
        this.currentProject = currentProject;
    }

    public Shell getShell() {
        return shell;
    }

    public void setShell(Shell shell) {
        this.shell = shell;
    }

    public IFile getFileAnyframeConfig() {
        return fileAnyframeConfig;
    }

    public void setFileAnyframeConfig(IFile fileAnyframeConfig) {
        this.fileAnyframeConfig = fileAnyframeConfig;
    }

    public AnyframeConfig getAnyframeConfig() {
        return anyframeConfig;
    }

    public java.util.List<JdbcType> getJdbcTypes() {
        return jdbcTypes;
    }

    protected void createPages() {
        for (int i = 0; i < pageNameKeys.length; i++) {
            createPage(pageNameKeys[i]);
        }
        createEditorPage();
    }

    void createPage(String pageNameKey) {
        Page page = null;
        if (pageNameKey.equals(pageNameKeys[0])) {
            page = new DomainGenPage(this);
        } else if (pageNameKey.equals(pageNameKeys[1])) {
            page = new CRUDGenPage(this);
        } else if (pageNameKey.equals(pageNameKeys[2])) {
            page = new ConfigPage(this);
        } else if (pageNameKey.equals(pageNameKeys[3])) {
            page = new JdbcPage(this);
        } else if (pageNameKey.equals(pageNameKeys[4])) {
            page = new InstallationPage(this);
        } else if (pageNameKey.equals(pageNameKeys[5])) {
            page = new CtipPage(this);
        }
        int index = addPage(page.getPage(getContainer()));
        setPageText(index, MessageUtil.getMessage(pageNameKey));
        mapPages.put(index, page);
    }

    void createEditorPage() {
        try {
            textEditor = new TextEditor();
            indexTextEditor = addPage(textEditor, getEditorInput());
            setPageText(indexTextEditor,
                MessageUtil.getMessage("editor.codegen.source.title"));
        } catch (PartInitException e) {
            ErrorDialog.openError(getSite().getShell(),
                MessageUtil.getMessage("editor.dialog.error.createtext"), null,
                e.getStatus());
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.dialog.error.createtext"),
                IStatus.ERROR, e);
        }
    }

    private void loadAnyframeConfig(IFile file) throws PartInitException {
        if (!file.getName().equals("anyframe.config")) {

            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.dialog.error.selectedproject"),
                MessageDialog.ERROR);

            throw new PartInitException(
                MessageUtil.getMessage("editor.exception.init"));
        } else {

            this.anyframeConfig =
                (AnyframeConfig) XmlFileUtil.getObjectFromXml(file
                    .getLocation().toOSString());
            currentProject =
                ProjectUtil.findProject(this.anyframeConfig.getPjtName());

            if (currentProject == null) {
                throw new PartInitException(
                    MessageUtil.getMessage("editor.dialog.error.currentpjt"));
            } else {
                try {
                    projectMF =
                        ProjectUtil.getProjectProperties(this.currentProject);
                    String pjtName = this.anyframeConfig.getPjtName();

                    if (projectMF.readValue(CommonConstants.PROJECT_NAME)
                        .compareTo(pjtName) != 0) {
                        throw new PartInitException(
                            MessageUtil
                                .getMessage("editor.dialog.error.currentpjt"));
                    }
                } catch (Exception e) {
                    ExceptionUtil.showException(MessageUtil
                        .getMessage("editor.dialog.error.get.properties"),
                        IStatus.ERROR, e);

                }
            }
        }
    }

    private void createAnyframeConfig(IFile fileAnyframeConfig) {
        try {
            this.anyframeConfig = new AnyframeConfig();
            anyframeConfig.setPjtName(currentProject.getName());

            XmlFileUtil.saveObjectToXml(this.anyframeConfig, fileAnyframeConfig
                .getLocation().toOSString());

        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.conf"), IStatus.ERROR,
                e);
        }
    }

    @SuppressWarnings("unchecked")
    public void init(IEditorSite site, IEditorInput editorInput)
            throws PartInitException {
        shell = site.getShell();
        IResource selectionResource = null;
        ISelection selection = site.getPage().getSelection();
        if (selection != null)
            selectionResource = ProjectUtil.getSelectedResource(selection);

        if (selectionResource != null) {
            currentProject = selectionResource.getProject();
            fileAnyframeConfig = currentProject.getFile("anyframe.config");
        }

        if (currentProject == null || fileAnyframeConfig.exists()) {
            fileAnyframeConfig = ((IFileEditorInput) editorInput).getFile();
            loadAnyframeConfig(fileAnyframeConfig);
        } else
            createAnyframeConfig(fileAnyframeConfig);

        ProjectInfo.setLocation(currentProject.getLocation().toOSString());
        ProjectInfo.setName(currentProject.getName());

        Locale.setDefault(new Locale("en"));

        try {
            projectMF = ProjectUtil.getProjectProperties(this.currentProject);
            File jdbcConfigFile =
                new File(projectMF.readValue(CommonConstants.ANYFRAME_HOME)
                    + ProjectUtil.SLASH + "ide" + ProjectUtil.SLASH + "db"
                    + ProjectUtil.SLASH + "jdbc.config");
            if (jdbcConfigFile.exists()) {
                jdbcTypes =
                    (java.util.List<JdbcType>) XmlFileUtil
                        .getObjectFromInputStream(new FileInputStream(
                            jdbcConfigFile));
            } else
                jdbcTypes =
                    (java.util.List<JdbcType>) XmlFileUtil
                        .getObjectFromInputStream(this.getClass()
                            .getClassLoader()
                            .getResourceAsStream("jdbc.config"));

        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.jdbctypes"),
                IStatus.ERROR, e);
            throw new PartInitException(
                "fail to get jdbc types from jdbc.config file");
        }

        super.init(site, editorInput);

        refreshWorkspace();
    }

    public void resourceChanged(final IResourceChangeEvent event) {
        int type = event.getType();

        if (type == IResourceChangeEvent.PRE_CLOSE) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    IWorkbenchPage[] pages =
                        getSite().getWorkbenchWindow().getPages();
                    for (int i = 0; i < pages.length; i++) {
                        IEditorInput editorInput = textEditor.getEditorInput();
                        IFile file = ((FileEditorInput) editorInput).getFile();
                        IProject project = file.getProject();
                        if (project.equals(event.getResource())) {
                            IEditorInput eInput = textEditor.getEditorInput();
                            IEditorPart editorPart =
                                pages[i].findEditor(eInput);
                            pages[i].closeEditor(editorPart, true);
                        }
                    }
                }
            });
        }
    }

    public void saveOnlyAnyframeConfigFile() {
        XmlFileUtil.saveObjectToXml(anyframeConfig, fileAnyframeConfig
            .getLocation().toOSString());
    }

    public void refreshWorkspace() {
        try {
            this.currentProject.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.refresh"),
                IStatus.ERROR, e);
        }
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    public void doSave(IProgressMonitor monitor) {
        textEditor.doSave(monitor);
    }

    public void doSaveAs() {
        textEditor.doSaveAs();

        String title = textEditor.getTitle();
        IEditorInput editorInput = textEditor.getEditorInput();

        setPageText(indexTextEditor, title);
        setInput(editorInput);
    }

    public void dispose() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.removeResourceChangeListener(this);

        super.dispose();
    }

    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);
        if (newPageIndex == 1) {
            CRUDGenPage page = (CRUDGenPage) mapPages.get(newPageIndex);
            page.onRefreshClicked();
        }
    }
}
