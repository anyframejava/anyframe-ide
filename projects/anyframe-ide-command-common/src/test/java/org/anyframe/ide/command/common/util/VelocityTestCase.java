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
package org.anyframe.ide.command.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import junit.framework.TestCase;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.EscapeTool;

/**
 * TestCase Name : VelocityTestCase <br>
 * <br>
 * [Description] : Test for dynamic string<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : test for dynamic string</li>
 * </ul>
 */
public class VelocityTestCase extends TestCase {
	
	/**
	 * [Flow #-1] Positive Case : test for dynamic string
	 * 
	 * @throws Exception
	 */
	public void testDynamicString() throws Exception {
		VelocityEngine velocity = new VelocityEngine();
		velocity.setProperty("runtime.log.logsystem.log4j.logger.level",
				"WARNING");
		velocity.setProperty("velocimacro.library", "");
		velocity.setProperty("resource.loader", "classpath");
		velocity
				.setProperty("classpath.resource.loader.class",
						"org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader");
		velocity.setProperty("runtime.log.logsystem.class",
				"org.apache.velocity.runtime.log.NullLogSystem");
		velocity.init();

		File output = new File("./src/test/resources/velocity/output.txt");
		output.createNewFile();

		Writer writer = new OutputStreamWriter(new FileOutputStream(output),
				"utf-8");

		VelocityContext context = new VelocityContext();
		context.put("esc", new EscapeTool());

		velocity.mergeTemplate("velocity/template.txt", "utf-8", context,
				writer);
		writer.flush();
	}
}
