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
package org.anyframe.ide.ctip.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.anyframe.ide.ctip.integration.CtipGenServlet;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;


/**
 * TestCase Name : CtipGenServletTestCase <br>
 * <br>
 * [Description] : Test for Component 'CtipGenServlet'<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : get configuration information of Hudson server.</li>
 * <li>#-2 Positive Case : save some configuration of Hudson server.</li>
 * <li>#-3 Positive Case : get configuration of job on a Hudson server.</li>
 * <li>#-4 Positive Case : save configuration of job on a Hudson server.</li>
 * </ul>
 */
public class CtipGenServletTestCase extends TestCase {
	
	private CtipGenServlet servlet = new CtipGenServlet();
	
	/**
	 * initialize
	 */
	@Override
	protected void setUp() throws Exception {
		MockServletConfig config = new MockServletConfig();
		config.addInitParameter("hudsonHome", "./src/test/resources/hudson");
		config.addInitParameter("hudsonJobDir", "./src/test/resources/hudson/jobs");
		
		servlet.init(config);
		
		super.setUp();
	}
	
	/**
	 * [Flow #-1] Positive Case : get configuration information of Hudson server.
	 * 
	 * @throws Exception
	 */
	public void testGetHudsonConfig() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addParameter("service", "getHudsonConfig");
	
		servlet.doGet(request, response);
		
		String resultString = new String(response.getContentAsByteArray());
		String expectedString = "<hudsongen><antHome>c:/gen-1.5.0.RC1/ant</antHome><mavenHome>C:\\maven-2.2.1</mavenHome><hudsonURL>http://localhost:9090</hudsonURL></hudsongen>"; 
		assertEquals(expectedString, resultString);
	}
	
	/**
	 * [Flow #-2] Positive Case : save some configuration of Hudson server.
	 * 
	 * @throws Exception
	 */
	public void testSaveHudsonConfig() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addParameter("service", "saveHudsonConfig");
		request.addParameter("antHome", "c:/gen-1.5.0.RC1/ant");
		request.addParameter("mavenHome", "C:\\maven-2.2.1");
		request.addParameter("hudsonURL", "http://localhost:9090");
		
		servlet.doGet(request, response);
		
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}
	
	/**
	 * [Flow #-3] Positive Case : get configuration of job on a Hudson server.
	 * 
	 * @throws Exception
	 */
	public void testGetJobConfig() throws ServletException, IOException, DocumentException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addParameter("service", "getJobConfig");
		request.addParameter("jobName", "myproject");
		
		servlet.doGet(request, response);
		
		byte[] contentArray = getExpectedOriginalConfigXmlContent();
        
        String expectedString = new String(contentArray); 
		String resultString = new String(response.getContentAsByteArray());

		assertEquals(expectedString, resultString);
	}
	
	/**
	 * [Flow #-4] Positive Case : save configuration of job on a Hudson server.
	 * 
	 * @throws Exception
	 */
	public void testSaveJobConfig() throws ServletException, IOException, DocumentException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.addParameter("service", "saveJobConfig");
		request.addParameter("jobName", "myproject");
		request.setContent(getExpectedOriginalConfigXmlContent());
		
		servlet.doGet(request, response);
		
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}

	/**
	 * method for test.
	 */
	private byte[] getExpectedOriginalConfigXmlContent() throws DocumentException, UnsupportedEncodingException, IOException {
		SAXReader reader = new SAXReader();
		Document expectedDoc = reader.read("./src/test/resources/hudson/jobs/myproject/config.xml");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLWriter writer = new XMLWriter(baos);
        writer.write(expectedDoc);
		return baos.toByteArray();
	}
}