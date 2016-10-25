package org.anyframe.ide.command.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import junit.framework.TestCase;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.EscapeTool;

public class VelocityTestCase extends TestCase {
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
