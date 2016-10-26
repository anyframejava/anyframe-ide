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
package org.anyframe.ide.querymanager.build;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * This class is a Query Manager Nature class.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class AnyframeNature implements IProjectNature {

	public static final String NATURERID = "org.anyframe.ide.querymanager.build.AnyframeNature";

	private IProject pjt;

	/**
	 * Configures the Query Manager Nature to the project.
	 * 
	 * @throws CoreException
	 *             when the method fails.
	 */
	public void configure() throws CoreException {
		IProjectDescription dsc = pjt.getDescription();
		ICommand[] cmds = dsc.getBuildSpec();

		for (int loop = 0; loop < cmds.length; ++loop) {
			if (cmds[loop].getBuilderName().equals(
					AnyframeProjectBuilder.BUILDERID)) {
				return;
			}
		}

		ICommand[] newCmds = new ICommand[cmds.length + 1];
		System.arraycopy(cmds, 0, newCmds, 0, cmds.length);
		ICommand command = dsc.newCommand();
		command.setBuilderName(AnyframeProjectBuilder.BUILDERID);
		newCmds[newCmds.length - 1] = command;
		dsc.setBuildSpec(newCmds);
		pjt.setDescription(dsc, null);
	}

	/**
	 * Utility method to de configure the anyframe nature.
	 * 
	 * @throws CoreException
	 *             if something goes wrong.
	 */
	public void deconfigure() throws CoreException {
		IProjectDescription dsc = getProject().getDescription();
		ICommand[] cmds = dsc.getBuildSpec();
		for (int loop = 0; loop < cmds.length; ++loop) {
			if (cmds[loop].getBuilderName().equals(
					AnyframeProjectBuilder.BUILDERID)) {
				ICommand[] newCmds = new ICommand[cmds.length - 1];
				System.arraycopy(cmds, 0, newCmds, 0, loop);
				System.arraycopy(cmds, loop + 1, newCmds, loop, cmds.length - loop - 1);
				dsc.setBuildSpec(newCmds);
				return;
			}
		}
	}

	/**
	 * Utility method to get the project object.
	 * 
	 * @return IProject object
	 */
	public IProject getProject() {
		return pjt;
	}

	/**
	 * @param project
	 *            the IProject object
	 */
	public void setProject(IProject pjt) {
		this.pjt = pjt;
	}

}
