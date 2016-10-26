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
package org.anyframe.ide.common.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.anyframe.ide.common.CommonActivator;
import org.anyframe.ide.common.messages.Message;
import org.eclipse.core.resources.IProject;

/**
 * This is PropertyHandler class.
 * 
 * @author Sujeong Lee
 */
public class PropertyHandler {
	public static final String FILENAME = ".settings/com.anyframe.ide.common.prefs";

	Properties prop = new Properties();
	String path;

	public PropertyHandler(String path) {
		this.path = path;
		loadProperties(prop);
	}

	public PropertyHandler(IProject project) {
		this.path = project.getLocation().toString();
		loadProperties(prop);
	}

	public void put(String key, String value) {
		prop.setProperty(key, value);
	}

	public void putBoolean(String key, boolean value) {
		prop.setProperty(key, (value ? "TRUE" : "FALSE"));
	}

	public void saveProperties() {
		path = path + "/" + FILENAME;

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(path);
			prop.store(out, Message.properties_title);
		} catch (IOException e) {
			PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, Message.exception_save_properties + e.getMessage() + ".");
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, Message.exception_save_properties + e.getMessage() + ".");
			}
		}
	}

	private void loadProperties(Properties properties) {
		InputStream in = null;
		try {
			in = new FileInputStream(path + "/" + FILENAME);

			properties.load(in);
		} catch (FileNotFoundException e) {
			PluginLogger.info(Message.exception_find_properties + path + "/"
					+ FILENAME);
		} catch (IOException e) {
			PluginLogger
					.info(Message.exception_load_properties
							+ path + "/" + FILENAME);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, Message.exception_load_propertieswith + e.getMessage() + ".");
			}
		}
	}

	public String getProperty(String key) {
		if (prop.isEmpty()) {
			loadProperties(prop);
		}

		return prop.getProperty(key);
	}

	public String getNullSafeTrimmedProperty(String key) {
		if (prop.isEmpty()) {
			loadProperties(prop);
		}

		return StringUtil.null2str(prop.getProperty(key)).trim();
	}

	public boolean getNullSafeBoolean(String key) {
		String valueString = getNullSafeTrimmedProperty(key);
		if ("true".equalsIgnoreCase(valueString)
				|| "Y".equalsIgnoreCase(valueString)) {
			return true;
		}
		return false;
	}

	public static String getProperty(String path, String key) {
		if (path == null) {
			return null;
		}
		PropertyHandler handler = new PropertyHandler(path);
		return handler.getProperty(key);
	}

	public static String getProperty(IProject project, String key) {
		PropertyHandler handler = new PropertyHandler(project);
		return handler.getProperty(key);
	}

	public Properties getProperty() {
		return prop;
	}
}
