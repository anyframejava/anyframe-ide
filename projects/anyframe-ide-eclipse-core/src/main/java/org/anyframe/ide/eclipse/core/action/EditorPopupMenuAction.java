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
package org.anyframe.ide.eclipse.core.action;

import java.io.File;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.eclipse.core.editor.CodeGenEditor;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * This is an EditorPopupMenuAction class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class EditorPopupMenuAction implements IObjectActionDelegate {

    private IProject activeProject;

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    public void run(IAction action) {
        IFile fileSpider = activeProject.getFile("anyframe.config");

        // open .spider
        IWorkbenchPage activePage =
            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage();
        try {
            IEditorPart editor =
                activePage.openEditor(new FileEditorInput(fileSpider),
                    "org.anyframe.ide.eclipse.core.configurationEditor");
            CodeGenEditor codegenEditor = (CodeGenEditor) editor;
            codegenEditor.setEditor(editor);
            codegenEditor.setCurrentProject(activeProject);
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.open"), IStatus.ERROR,
                e);
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        IResource resource = ProjectUtil.getSelectedResource(selection);
        if (resource == null)
            return;
        activeProject = resource.getProject();

        boolean popupMenuEnabled = false;

        if (activeProject.isOpen()) {
            String projectName = "";
            try {
                PropertiesIO appProps =
                    ProjectUtil.getProjectProperties(activeProject);
                projectName = appProps.readValue(CommonConstants.PROJECT_NAME);
                if (appProps.readValue(CommonConstants.PROJECT_BUILD_TYPE)
                    .equals(CommonConstants.PROJECT_BUILD_TYPE_ANT)) {
                    if (!new File(appProps.readValue(CommonConstants.ANYFRAME_HOME)).exists()) {
                        ExceptionUtil
                            .showException(
                                MessageUtil
                                    .getMessage("editor.dialog.error.contextmenu.anyframehome"),
                                IStatus.ERROR, new Exception());
                    } else if (!new File(appProps.readValue(CommonConstants.PROJECT_HOME))
                        .exists()) {
                        ExceptionUtil
                            .showException(
                                MessageUtil
                                    .getMessage("editor.dialog.error.contextmenu.pjthome"),
                                IStatus.ERROR, new Exception());
                    }
                }
            } catch (Exception e) {
                ExceptionUtil.showException(
                    MessageUtil.getMessage("editor.exception.popupmenu"),
                    IStatus.ERROR, e);
            }

            if (projectName != null && projectName.length() > 0) {
                if (projectName.equals(activeProject.getName()))
                    popupMenuEnabled = true;
                else
                    ExceptionUtil.showException(MessageUtil
                        .getMessage("editor.exception.popupmenu.notdomain"),
                        IStatus.INFO, new Exception());
            } else
                ExceptionUtil.showException(MessageUtil
                    .getMessage("editor.exception.popupmenu.nodomain"),
                    IStatus.INFO, null);
        }

        action.setEnabled(popupMenuEnabled);
    }
}
