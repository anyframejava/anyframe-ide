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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.anyframe.ide.eclipse.core.command.vo.CreateCRUDVO;
import org.anyframe.ide.eclipse.core.util.AntExecution;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.eclipse.core.runtime.IStatus;

import org.anyframe.ide.command.common.util.PropertiesIO;

/**
 * TestCase Name : AnyframeIDEExecutionTestCase <br>
 * <br>
 * [Description] : Anyframe IDE execution test<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : execute 'create-crud' command</li>
 * </ul>
 */
public class AnyframeIDEExecutionTestCase extends TestCase{
    
    /**
     * [Flow #-1] Positive Case : execute 'create-crud' command.
     * 
     * @throws Exception
     */
    public void testCreateCRUD() throws Exception {
        // 1. get application location
        String appLocation = "";
        PropertiesIO pio = null;
        String anyframeHomeLocation = "";
        Boolean createWebProject = true;
        String domainClassName = "Board";
        String serviceProjectName = "sample";
        String packageName = "com.sds.emp";        
        
        try {
            appLocation = "./src/test/resources/";
            pio = new PropertiesIO(appLocation+"/.metadata/project.mf");
            anyframeHomeLocation = pio.readValue("anyframe.home");
        } catch (Exception e) {
            ExceptionUtil.showException(MessageUtil
                .getMessage("editor.exception.loadconfig"), IStatus.ERROR, e);
        }

        // 2. run ant
        String scope = "service";
        if (createWebProject)
            scope = "all";
        String[] args =
            {"create-crud", domainClassName, "-project", serviceProjectName,
                "-package", packageName, "-scope", scope, "-project.home",
                appLocation, "-genHome", anyframeHomeLocation };

        List<String[]> antConfigList = new ArrayList<String[]>();
        antConfigList.add(args);       
        
        CreateCRUDVO vo = new CreateCRUDVO();
        vo.setCommand("crud");
        AntExecution.runAnt(antConfigList, vo);
    }
}
