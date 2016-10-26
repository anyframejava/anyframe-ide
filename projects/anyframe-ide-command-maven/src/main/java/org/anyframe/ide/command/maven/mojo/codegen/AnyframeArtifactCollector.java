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
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.hibernate.tool.hbm2x.ExporterException;
import org.hibernate.tool.hbm2x.XMLPrettyPrinter;

import org.anyframe.ide.command.common.util.PrettyPrinter;
import de.hunsicker.jalopy.Jalopy;

/**
 * This is an AnyframeArtifactCollector class to format java file and xml file.
 * 
 * @author Sooyeon Park
 */
public class AnyframeArtifactCollector extends ArtifactCollector {

	@Override
	public void formatFiles() {
		formatJava("java");
		formatXml("xml");
		formatXml("hbm.xml");
		formatXml("cfg.xml");
	}

	private void formatJava(String type) {
		List<?> list = (List<?>) files.get(type);
		if (list != null && !list.isEmpty()) {
			for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
				File javaFile = (File) iter.next();
				try {
					Jalopy jalopy = new Jalopy();

					// set encoding
					jalopy.setEncoding("utf-8");
					// specify input and output target
					jalopy.setInput(javaFile);
					jalopy.setOutput(javaFile);

					// format and overwrite the given
					// input file
					jalopy.format();

				} catch (IOException e) {
					throw new ExporterException("Could not format Java file: "
							+ javaFile, e);
				}
			}
		}
	}

	private void formatXml(String type) {
		List<?> list = (List<?>) files.get(type);
		if (list != null && !list.isEmpty()) {
			for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
				File xmlFile = (File) iter.next();
				try {
					XMLPrettyPrinter.prettyPrintFile(
							PrettyPrinter.getDefaultTidy(), xmlFile, xmlFile,
							true);
				} catch (IOException e) {
					throw new ExporterException("Could not format XML file: "
							+ xmlFile, e);
				}
			}
		}
	}

}
