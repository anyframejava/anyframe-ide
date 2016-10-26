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

import java.io.File;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * This is a ProjectRecord class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class ProjectRecord {
    private File projectSystemFile;

    private String projectName;

    private IProjectDescription description;

    public IProjectDescription getDescription() {
        return description;
    }

    public void setDescription(IProjectDescription description) {
        this.description = description;
    }

    ProjectRecord(File file, String projectName) {
        projectSystemFile = file;
        this.projectName = projectName;
    }

    ProjectRecord(File file) {
        projectSystemFile = file;
        setProjectName();
    }

    private void setProjectName() {
        IProjectDescription newDescription = null;
        try {

            IPath path = new Path(projectSystemFile.getPath());
            // if the file is in the default location,
            // use the directory
            // name as the project name
            if (isDefaultLocation(path)) {
                projectName = path.segment(path.segmentCount() - 2);
                newDescription =
                    IDEWorkbenchPlugin.getPluginWorkspace()
                        .newProjectDescription(projectName);
            } else {
                newDescription =
                    IDEWorkbenchPlugin.getPluginWorkspace()
                        .loadProjectDescription(path);
            }

        } catch (CoreException e) {
            ExceptionUtil.showException(MessageUtil
                .getMessage("exception.log.name"), IStatus.ERROR, e);
        }

        if (newDescription == null) {
            this.description = null;
            projectName = "";
        } else {
            this.description = newDescription;
            projectName = this.description.getName();
        }
    }

    private boolean isDefaultLocation(IPath path) {
        if (path.segmentCount() < 2)
            return false;
        return path.removeLastSegments(2).toFile().equals(
            Platform.getLocation().toFile());
    }

    public String getProjectName() {
        return projectName;
    }

    public File getProjectSystemFile() {
        return projectSystemFile;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
