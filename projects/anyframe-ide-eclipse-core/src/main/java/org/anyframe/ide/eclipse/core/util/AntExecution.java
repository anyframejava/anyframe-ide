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
package org.anyframe.ide.eclipse.core.util;

import java.text.MessageFormat;
import java.util.List;

import org.anyframe.ide.command.cli.util.CommandUtil;
import org.anyframe.ide.eclipse.core.CodeGenPostProcess;
import org.anyframe.ide.eclipse.core.DBConfigChangePostProcess;
import org.anyframe.ide.eclipse.core.ModelGenPostProcess;
import org.anyframe.ide.eclipse.core.PluginInstallPostProcess;
import org.anyframe.ide.eclipse.core.PluginUninstallPostProcess;
import org.anyframe.ide.eclipse.core.ProjectCreationPostProcess;
import org.anyframe.ide.eclipse.core.command.vo.CommandVO;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.DebugUIMessages;

/**
 * This is an AntExecution class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class AntExecution {

    public static boolean runAnt(List<String[]> antConfigList, CommandVO vo) {
        String command = vo.getCommand();

        AntJob job = new AntJob(command, antConfigList);
        PostProcess postProcess = null;
        if (command.equals(CommandUtil.CMD_CREATE_MODEL)) {
            postProcess = new ModelGenPostProcess(vo);
        } else if (command.equals(CommandUtil.CMD_CREATE_CRUD)) {
            postProcess = new CodeGenPostProcess(vo);
        } else if (command.equals(CommandUtil.CMD_CHANGE_DB)) {
            postProcess = new DBConfigChangePostProcess(vo);
        } else if (command.equals(CommandUtil.CMD_INSTALL)) {
            postProcess = new PluginInstallPostProcess(vo);
        } else if (command.equals(CommandUtil.CMD_UNINSTALL)) {
            postProcess = new PluginUninstallPostProcess(vo);
        } else if (command.equals(CommandUtil.CMD_CREATE_PROJECT))
            postProcess = new ProjectCreationPostProcess(vo);

        job.setPriority(Job.INTERACTIVE);
        job.setPostProcess(postProcess);
        job.setName(MessageFormat.format(DebugUIMessages.DebugUIPlugin_25,
            new Object[] {command + " ant process" }));

        job.schedule();

        return true;
    }
}
