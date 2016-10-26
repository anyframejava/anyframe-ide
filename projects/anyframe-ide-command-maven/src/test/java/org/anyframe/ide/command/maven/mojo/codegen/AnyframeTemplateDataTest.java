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
package org.anyframe.ide.command.maven.mojo.codegen;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.anyframe.ide.command.maven.mojo.codegen.AnyframeTemplateData;

import junit.framework.TestCase;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;

/**
 * This is an AnyframeTemplateDataTest class.
 * 
 * @author Sooyeon Park
 */
public class AnyframeTemplateDataTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@SuppressWarnings("unchecked")
	public void testLoadTemplateData() throws Exception {
		try {
			XStream xstream = new XStream();
			Annotations.configureAliases(xstream, AnyframeTemplateData.class);
			xstream.setMode(XStream.NO_REFERENCES);

			String templateHome = new File(".").getAbsolutePath()
					+ "/src/test/resources/templates/";
			List<AnyframeTemplateData> templates = (java.util.List<AnyframeTemplateData>) xstream
					.fromXML(new FileInputStream(templateHome
							+ "query/template.config"));

			for (AnyframeTemplateData template : templates) {
				System.out.println(template.getVm());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
