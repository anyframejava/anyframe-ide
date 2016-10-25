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
package org.anyframe.ide.command.maven.mojo.codegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.hibernate.tool.hbm2x.ExporterException;
import org.hibernate.tool.hbm2x.TemplateHelper;
import org.hibernate.tool.hbm2x.TemplateProducer;

/**
 * This is an AnyframeTemplateProducer class to generate codes based on
 * templates.
 * 
 * @author Sooyeon Park
 */
public class AnyframeTemplateProducer extends TemplateProducer {
	private static final Log log = LogFactory
			.getLog(AnyframeTemplateProducer.class);
	private final TemplateHelper th;
	private ArtifactCollector ac;

	public AnyframeTemplateProducer(TemplateHelper th, ArtifactCollector ac) {
		super(th, ac);
		this.th = th;
		this.ac = ac;
	}

	public void produce(Map additionalContext, String templateName,
			File destination, String identifier, String fileType,
			String rootContext) {

		String tempResult = produceToString(additionalContext, templateName,
				rootContext);

		if (tempResult.trim().length() == 0) {
			log.warn("Generated output is empty. Skipped creation for file "
					+ destination);
			return;
		}
		Writer fileWriter = null;
		try {
			th.ensureExistence(destination);
			ac.addFile(destination, fileType);
			log.debug("Writing " + identifier + " to "
					+ destination.getAbsolutePath());
			fileWriter = new FileWriterWithEncoding(destination, "UTF-8");
			fileWriter.write(tempResult);
		} catch (Exception e) {
			throw new ExporterException("Error while writing result to file", e);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e) {
					log.warn("'"
							+ destination
							+ "' file stream destroying is skipped. The reason is '"
							+ e.getMessage() + "'.");
				}
			}
		}
	}

	private String produceToString(Map additionalContext, String templateName,
			String rootContext) {

		Map contextForFirstPass = additionalContext;
		putInContext(th, contextForFirstPass);
		StringWriter tempWriter = new StringWriter();
		BufferedWriter bw = new BufferedWriter(tempWriter);

		// First run - writes to in-memory string
		th.processTemplate(templateName, bw, rootContext);
		removeFromContext(th, contextForFirstPass);
		try {
			bw.flush();
		} catch (IOException e) {
			throw new RuntimeException("Error while flushing to string", e);
		}
		return tempWriter.toString();
	}

	private void removeFromContext(TemplateHelper templateHelper, Map context) {
		Iterator iterator = context.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry element = (Map.Entry) iterator.next();
			templateHelper.removeFromContext((String) element.getKey(),
					element.getValue());
		}
	}

	private void putInContext(TemplateHelper templateHelper, Map context) {
		Iterator iterator = context.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry element = (Map.Entry) iterator.next();
			templateHelper.putInContext((String) element.getKey(),
					element.getValue());
		}
	}
}
