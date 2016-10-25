/*   
 * Copyright 2002-2009 the original author or authors.   
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
package org.anyframe.ide.command.common.util;

import java.util.ArrayList;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;

import junit.framework.TestCase;

/**
 * TestCase Name : PropertiesIOTestCase <br>
 * <br>
 * [Description] : Test for Component 'PropertiesIO'<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : write property</li>
 * <li>#-2 Positive Case : read all property values</li>
 * <li>#-3 Positive Case : read a property value</li>
 * </ul>
 */
public class PropertiesIOTestCase extends TestCase {

	private PropertiesIO pio;
	private ArrayList<Object> pioList;

	/**
	 * [Flow #-1] Positive Case : write property.
	 * 
	 * @throws Exception
	 */
	public void testWriteProperty() throws Exception {
		pio = new PropertiesIO(
				"./src/test/resources/project/sample/META-INF/project.mf");
		pio.setProperty("project.name", "emarketplace");
		pio.write();

		assertEquals("emarketplace", pio
				.readValue(CommonConstants.PROJECT_NAME));
	}

	/**
	 * [Flow #-2] Positive Case : read all property values.
	 * 
	 * @throws Exception
	 */
	public void testReadAllValues() throws Exception {
		pioList = new ArrayList<Object>();
		pio = new PropertiesIO(
				"./src/test/resources/project/sample/META-INF/project.mf");
		pioList = pio.readAllValues();

		assertNotNull(pioList);
	}

	/**
	 * [Flow #-3] Positive Case : read a property value.
	 * 
	 * @throws Exception
	 */
	public void testReadValue() throws Exception {
		pio = new PropertiesIO(
				"./src/test/resources/project/sample/META-INF/project.mf");
		System.out.println(pio.readValue(CommonConstants.PROJECT_HOME));
	}

}
