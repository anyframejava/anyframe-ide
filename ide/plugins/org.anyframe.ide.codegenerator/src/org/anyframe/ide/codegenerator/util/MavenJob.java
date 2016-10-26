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
package org.anyframe.ide.codegenerator.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This is an MavenJob class.
 * 
 * @author Sooyeon Park
 */
public class MavenJob extends Job {

	private PostProcess postProcess;

	public MavenJob(final String name) {
		super(name);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		executePostProcess();
		return Status.OK_STATUS;

	}

	public void setPostProcess(PostProcess postProcess) {
		this.postProcess = postProcess;
	}

	public void executePostProcess() {
		if (postProcess != null)
			postProcess.execute(null);
	}
}
