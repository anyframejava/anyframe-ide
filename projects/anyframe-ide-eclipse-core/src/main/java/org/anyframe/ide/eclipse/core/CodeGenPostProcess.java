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
package org.anyframe.ide.eclipse.core;

import org.anyframe.ide.eclipse.core.command.vo.CommandVO;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.PostProcess;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.eclipse.core.runtime.IStatus;

/**
 * This is a CodeGenPostProcess class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class CodeGenPostProcess implements PostProcess {

    private CommandVO vo = null;

    public CodeGenPostProcess(CommandVO vo) {
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
                ProjectUtil.refreshProject(vo.getProjectName());
            }
        }).start();
    }

}
