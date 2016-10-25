/*   
 * Copyright 2002-2010 the original author or authors.   
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
package org.anyframe.ide.eclipse.core.command.vo;

import org.anyframe.ide.eclipse.core.editor.CodeGenEditor;

/**
 * This is an InstallPluginVO class.
 * 
 * @author Sooyeon Park
 */
public class InstallPluginVO extends PluginVO {

    private boolean excludeSrc = false;
    private CodeGenEditor ideEditor = null;
    
    public boolean isExcludeSrc() {
        return excludeSrc;
    }

    public CodeGenEditor getIdeEditor() {
        return ideEditor;
    }

    public void setIdeEditor(CodeGenEditor ideEditor) {
        this.ideEditor = ideEditor;
    }

    public void setExcludeSrc(boolean excludeSrc) {
        this.excludeSrc = excludeSrc;
    }

}
