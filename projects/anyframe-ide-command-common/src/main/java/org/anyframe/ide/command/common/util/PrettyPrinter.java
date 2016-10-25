/*   
 * Copyright 2008-2011 the original author or authors.   
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Properties;

import org.hibernate.tool.hbm2x.XMLPrettyPrinter;
import org.w3c.tidy.Tidy;

/**
 * This is an PrettyPrinter class.
 * 
 * @author Sooyeon Park
 */
public final class PrettyPrinter {

	protected PrettyPrinter() {
		// prevents calls from subclass
		throw new UnsupportedOperationException();
	}

	public static Tidy getDefaultTidy() throws IOException {
		Tidy tidy = new Tidy();

		// no output please!
		tidy.setErrout(new PrintWriter(new Writer() {
			public void close() throws IOException {
			}

			public void flush() throws IOException {
			}

			public void write(char[] cbuf, int off, int len) throws IOException {

			}
		}));

		Properties properties = new Properties();

		properties.load(XMLPrettyPrinter.class
				.getResourceAsStream("jtidy.properties"));

		tidy.setConfigurationFromProps(properties);

		String defaultCharset = "utf-8";
		tidy.setInputEncoding(defaultCharset);
		tidy.setOutputEncoding(defaultCharset);
		return tidy;
	}
}
