/*
 * Copyright 2002-2008 the original author or authors.
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
package org.anyframe.ide.command.cli.util;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * This is an PropertiesIO class. This class is a utility for properties.
 * 
 * @author SoYon Lim
 */
public class PropertiesIO {
	private String propertiesFile;
	private Properties properties;

	/**
	 * Initialize this class and load properties file at the same time
	 * 
	 * @param file
	 *            to load
	 */
	public PropertiesIO(String propertiesFile) throws Exception {
		this.propertiesFile = propertiesFile;
		properties = new Properties();
		this.loadProperties();
	}

	/**
	 * Load content of properties file into memory
	 */
	public void loadProperties() throws Exception {
		properties.load(new FileInputStream(propertiesFile));
	}

	/**
	 * Read value that matched the key
	 * 
	 * @param key
	 *            use to search in properties file
	 * @return value that matched key
	 */
	public String readValue(String key) {
		return properties.getProperty(key);
	}
}
