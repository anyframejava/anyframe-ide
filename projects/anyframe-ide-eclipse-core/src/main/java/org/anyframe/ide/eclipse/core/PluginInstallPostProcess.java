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
package org.anyframe.ide.eclipse.core;

import org.anyframe.ide.eclipse.core.command.vo.CommandVO;
import org.anyframe.ide.eclipse.core.command.vo.InstallPluginVO;
import org.anyframe.ide.eclipse.core.editor.CodeGenEditor;
import org.anyframe.ide.eclipse.core.editor.ConfigPage;
import org.anyframe.ide.eclipse.core.editor.InstallationPage;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.PostProcess;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;


/**
 * This is a PluginInstallPostProcess class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class PluginInstallPostProcess implements PostProcess {

    private CommandVO vo = null;

    public PluginInstallPostProcess(CommandVO vo) {
        this.vo = vo;
    }

    public void execute(String[] config) {

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    ExceptionUtil.showException(
                        MessageUtil.getMessage("editor.exception.timesleep"),
                        IStatus.ERROR, e);
                }
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        InstallPluginVO pluginVO = (InstallPluginVO) vo;
                        CodeGenEditor ideEditor = pluginVO.getIdeEditor();
                        ((ConfigPage) ideEditor.getMapPages().get(2))
                            .refreshAction();
                        InstallationPage installPage =
                            (InstallationPage) ideEditor.getMapPages().get(4);
                        Event event = new Event();
                        event.widget = installPage.getButtonRefresh();
                        installPage.getListener().handleEvent(event);
                    }
                });
                ProjectUtil.refreshProject(vo.getProjectName());
            }
        }).start();

    }

}
