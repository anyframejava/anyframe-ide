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
package org.anyframe.ide.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import org.anyframe.ide.common.CommonActivator;

/**
 * This is an PropertyUtil class. This class is a utility for properties.
 * 
 * @author SoYon Lim
 */
public class PropertyUtil {

	private String file;
	private OrderedProperties properties;

	/**
	 * Initialize this class and load properties file at the same time
	 * 
	 * @param file
	 *            to load
	 */
	public PropertyUtil(String file) {
		this.file = file;
		properties = new OrderedProperties(new File(this.file));
		this.loadProperties();
	}

	/**
	 * Initialize this class and load properties at the same time
	 * 
	 * @param inputStream
	 *            to load
	 */
	public PropertyUtil(InputStream inputStream) {
		this.properties = new OrderedProperties();
		try {
			this.properties.load(inputStream);
		} catch (IOException e) {
			PluginLoggerUtil
					.error(CommonActivator.PLUGIN_ID, e.getMessage(), e);
		}
	}

	/**
	 * Load content of properties file into memory
	 */
	private void loadProperties() {
		try {
			properties.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			PluginLoggerUtil
					.error(CommonActivator.PLUGIN_ID, e.getMessage(), e);
		} catch (IOException e) {
			PluginLoggerUtil
					.error(CommonActivator.PLUGIN_ID, e.getMessage(), e);
		}
	}

	/**
	 * Read all value in properties file into ArrayList object
	 * 
	 * @return ArrayList object containing all values in properties file
	 */
	private ArrayList<Object> readAllValues() {
		ArrayList<Object> values = new ArrayList<Object>();
		Enumeration<Object> e = properties.elements();
		while (e.hasMoreElements()) {
			values.add(e.nextElement());
		}
		return values;
	}

	/**
	 * Read all keys in properties file into ArrayList object
	 * 
	 * @return ArrayList object containing all keys in properties file
	 */
	public ArrayList<String> readAllKeys() {
		ArrayList<String> keys = new ArrayList<String>();
		Enumeration<Object> e = properties.keys();
		while (e.hasMoreElements()) {
			keys.add((String) e.nextElement());
		}
		return keys;
	}

	/**
	 * Read value that matched the key
	 * 
	 * @param key
	 *            use to search in properties file
	 * @return value that matched key
	 */
	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	/**
	 * Write key/value pair into properties file
	 * 
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	public void write() {
		try {
			this.properties.store(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			// ignore
		} catch (IOException e) {
			// ignore
		}
	}

	public Properties getProperties() {
		return properties;
	}

	private class OrderedProperties extends Properties {
		private File file = null;
		private final LinkedHashSet<Object> keys = new LinkedHashSet<Object>();

		public OrderedProperties() {
			super();
		}

		public OrderedProperties(File file) {
			super();
			this.file = file;
		}

		@Override
		public Enumeration<Object> keys() {
			return Collections.<Object> enumeration(keys);
		}

		@Override
		public Object put(Object key, Object value) {
			keys.add(key);
			return super.put(key, value);
		}

		public synchronized void store(InputStream in) throws IOException {
			if (this.file != null) {
				// 1. copy file to temp directory
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));

				// 2. set writer
				File tempFile = File.createTempFile(file.getName(), ".tmp");

				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(tempFile)));

				try {
					// 3. read properties fileF
					String aLine;
					List<String> oldKeyList = new ArrayList<String>();

					while ((aLine = reader.readLine()) != null) {
						String trimmedLine = aLine.trim();

						if (trimmedLine.length() == 0
								|| trimmedLine.startsWith("#")) {
							writer.write(aLine + "\n");
							continue;
						}

						int idx = trimmedLine.indexOf("=");

						trimmedLine = trimmedLine.replace("\\", "/");

						if (idx != -1) {
							String key = trimmedLine.substring(0, idx).trim();
							if (keys.contains(key)) {
								oldKeyList.add(key);
							}

							writer.write(key
									+ "="
									+ this.getProperty(key).replace("\\",
											"\\\\") + "\n");
						}
					}

					// 4. add new properties into properties file
					Iterator keyItr = keys.iterator();
					while (keyItr.hasNext()) {
						String propertyKey = (String) keyItr.next();
						if (!oldKeyList.contains(propertyKey)) {
							writer.write(propertyKey
									+ "="
									+ this.getProperty(propertyKey).replace(
											"\\", "\\\\") + "\n");
						}
					}
				} finally {
					reader.close();
					writer.close();
				}

				copyFile(tempFile, file);
			}
		}

		private void copyFile(File source, File destination) throws IOException {
			InputStream in = null;
			OutputStream out = null;

			try {
				in = new FileInputStream(source);
				out = new FileOutputStream(destination);

				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			} finally {
				in.close();
				out.close();
			}
		}
	}

}