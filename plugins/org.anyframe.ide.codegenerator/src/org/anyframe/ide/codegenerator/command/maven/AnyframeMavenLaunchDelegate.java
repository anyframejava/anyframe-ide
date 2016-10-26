/*   
 * Copyright 2002-2013 the original author or authors.   
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
package org.anyframe.ide.codegenerator.command.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.maven.ide.eclipse.embedder.IMavenLauncherConfiguration;
import org.maven.ide.eclipse.internal.embedder.MavenExternalRuntime;
import org.maven.ide.eclipse.internal.launch.MavenLauncherConfigurationHandler;

/**
 * This is an AnyframeMavenLaunchDelegate class.
 * 
 * @author Sooyeon Park
 */
public class AnyframeMavenLaunchDelegate extends JavaLaunchDelegate {

	private static final String LAUNCH_M2CONF_FILE = "org.maven.ide.eclipse.internal.launch.M2_CONF";
	private MavenExternalRuntime runtime;
	private MavenLauncherConfigurationHandler m2conf;
	private File confFile;

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		String mavenHome = configuration
				.getAttribute(AnyframeMavenLaunchConfiguration.ATTR_MAVEN_HOME,
						(String) null);
		runtime = new MavenExternalRuntime(mavenHome);
		m2conf = new MavenLauncherConfigurationHandler();
		runtime.createLauncherConfiguration(m2conf, new NullProgressMonitor());

		File state = CodeGeneratorActivator.getDefault().getStateLocation()
				.toFile();
		try {
			File dir = new File(state, "launches");
			dir.mkdirs();
			confFile = File.createTempFile("m2conf", ".tmp", dir);
			launch.setAttribute(LAUNCH_M2CONF_FILE, confFile.getCanonicalPath());
			OutputStream os = new FileOutputStream(confFile);
			try {
				m2conf.save(os);
			} finally {
				os.close();
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					CodeGeneratorActivator.PLUGIN_ID, -1,
					Message.exception_log_cannotcreatem2conf, e));
		}

		super.launch(configuration, mode, launch, monitor);
	}

	public String[] getClasspath(ILaunchConfiguration configuration)
			throws CoreException {
		List<String> cp = m2conf
				.getRealmEntries(IMavenLauncherConfiguration.LAUNCHER_REALM);
		return cp.toArray(new String[cp.size()]);
	}

	public IVMRunner getVMRunner(final ILaunchConfiguration configuration,
			String mode) throws CoreException {
		final IVMRunner runner = super.getVMRunner(configuration, mode);

		return new IVMRunner() {
			public void run(VMRunnerConfiguration runnerConfiguration,
					ILaunch launch, IProgressMonitor monitor)
					throws CoreException {
				runner.run(runnerConfiguration, launch, monitor);

				IProcess[] processes = launch.getProcesses();
				if (processes != null && processes.length > 0) {
					AnyframeBackgroundResourceRefresher refresher = new AnyframeBackgroundResourceRefresher(
							configuration, launch);
					refresher.init();
				} else {
					removeTempFiles(launch);
				}
			}
		};
	}

	static void removeTempFiles(ILaunch launch) {
		String m2confName = launch.getAttribute(LAUNCH_M2CONF_FILE);
		if (m2confName != null) {
			new File(m2confName).delete();
		}
	}
}
