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
package org.anyframe.ide.codegenerator;

import org.anyframe.ide.codegenerator.views.CtipView;
import org.anyframe.ide.codegenerator.views.InstallationView;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author Sujeong Lee
 */
public class CodeGeneratorActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.anyframe.ide.codegenerator";

	// The shared instance
	private static CodeGeneratorActivator plugin;

	private InstallationView installationView;

	private CtipView ctipView;

	/**
	 * The constructor
	 */
	public CodeGeneratorActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CodeGeneratorActivator getDefault() {
		return plugin;
	}

	public InstallationView getInstallationView() {
		return getInstallationView(true);
	}

	public CtipView getCtipView() {
		return getCtipView(true);
	}

	public InstallationView getInstallationView(boolean create) {
		IWorkbenchPage page = getActivePage();

		if (page != null) {
			installationView = (InstallationView) page.findView(PLUGIN_ID
					+ ".views.InstallationView");
			if (installationView == null && create) {
				try {
					installationView = (InstallationView) page
							.showView(PLUGIN_ID + ".views.InstallationView");
				} catch (PartInitException partInitException) {
					PluginLoggerUtil.error(CodeGeneratorActivator.PLUGIN_ID, partInitException.getMessage(), partInitException);
				}
			}
		}

		return installationView;
	}

	public CtipView getCtipView(boolean create) {
		IWorkbenchPage page = getActivePage();

		if (page != null) {
			ctipView = (CtipView) page.findView(PLUGIN_ID + ".views.CtipView");
			if (ctipView == null && create) {
				try {
					ctipView = (CtipView) page.showView(PLUGIN_ID
							+ ".views.CtipView");
				} catch (PartInitException partInitException) {
					PluginLoggerUtil.error(CodeGeneratorActivator.PLUGIN_ID, partInitException.getMessage(), partInitException);
				}
			}
		}

		return ctipView;
	}

	private IWorkbenchPage getActivePage() {
		if (getWorkbench() != null
				&& getWorkbench().getActiveWorkbenchWindow() != null)
			return getWorkbench().getActiveWorkbenchWindow().getActivePage();
		return null;
	}
}
