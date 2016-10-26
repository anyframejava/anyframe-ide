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
package org.anyframe.ide.common;

import org.anyframe.ide.common.dialog.IDBSettingDialog;
import org.anyframe.ide.common.properties.IPropertyPage;
import org.anyframe.ide.common.util.PluginLogger;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.StringUtil;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * This is CommonActivator class.
 * 
 * @author Joonil Kim
 */
public class CommonActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.anyframe.ide.common";

	// The shared instance
	private static CommonActivator plugin;

	// The shared instance.
	protected static CommonActivator instance;

	// DB Setting Dialog Interface
	private IDBSettingDialog dbSettingDialog;

	// Anyframe Common Property Page Interface
	private IPropertyPage propertyPage;

	/**
	 * The constructor
	 */
	public CommonActivator() {
		plugin = this;
		instance = this;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return instance
	 */
	public static CommonActivator getInstance() {
		return instance;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);

		dbSettingDialog = loadIDBSettingDialog();
		propertyPage = loadIPropertyPage();
	}

	/**
	 * @param context
	 *            .
	 * @throws Exception .
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	private IDBSettingDialog loadIDBSettingDialog() {
		return (IDBSettingDialog) getConnector(PLUGIN_ID, "database");
	}

	private IPropertyPage loadIPropertyPage() {
		return (IPropertyPage) getConnector(PLUGIN_ID, "property");
	}

	public IDBSettingDialog getDBSettingDialog() {
		return dbSettingDialog;
	}

	public IPropertyPage getPropertyPage() {
		return propertyPage;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CommonActivator getDefault() {
		return plugin;
	}

	/**
	 * return matched class instance defined in extension point configuration
	 * (fragment.xml or plugin.xml). if no one matched, return default class
	 * instance defined type="default" in extension point. else return first
	 * connector found in configurationElements variable. else return null.
	 * 
	 * @param pluginId
	 * @param extensionPointId
	 * @return
	 * 
	 *         referecne com.anyframe.ide.common.PluginUtilExt.getConnector
	 */
	private Object getConnector(String pluginId, String extensionPointId) {
		String pluginIdWithExtensionPointId = StringUtil.generateQualifiedName(
				pluginId, extensionPointId);

		// Get Extension Point Elements.(Extension Point Name : ex>
		// "com.sds.anyframe.securityconnector")
		IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();
		IConfigurationElement[] configurationElements = pluginRegistry
				.getConfigurationElementsFor(pluginIdWithExtensionPointId);

		Object defaultConnector = null;

		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement configurationElement = configurationElements[i];
			try {
				// Create and Return Instance of Class defined in "fragment.xml"
				// or "plugin.xml" (Extended Project)
				Object tempConnector = configurationElement
						.createExecutableExtension("class");

				// Return String value defined in "fragment.xml" or "plugin.xml"
				// (Extended Project)
				String type = (String) configurationElement
						.getAttribute("type");

				if ("default".equals(type) || i == 0) {
					defaultConnector = tempConnector;
				}
			} catch (Exception e) {
				PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, e.getMessage(), e);
			}
		}
		// PluginLogger.info("Connector(" + pluginIdWithExtensionPointId +
		// ") : "
		// + selectedName);
		if (defaultConnector == null) {
			return null;
		}
		return defaultConnector;
	}
}
