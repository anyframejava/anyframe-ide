/*   
 * Copyright 2002-2012 the original author or authors.   
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

/**
 * This is a CommandVO class.
 * 
 * @author Sooyeon Park
 */
public class CommandVO {

    private String anyframeHome = "";
    private String command = "";
    private String projectHome = "";
    private String projectName = "";
    private String packageName = "";
    private String basedir = "";

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getProjectHome() {
        return projectHome;
    }

    public void setProjectHome(String projectHome) {
        this.projectHome = projectHome;
    }

    public String getAnyframeHome() {
        return anyframeHome;
    }

    public void setAnyframeHome(String anyframeHome) {
        this.anyframeHome = anyframeHome;
    }

    public String getBasedir() {
        return basedir;
    }

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
