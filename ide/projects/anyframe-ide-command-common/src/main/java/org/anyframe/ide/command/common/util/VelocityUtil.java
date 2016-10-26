package org.anyframe.ide.command.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

public class VelocityUtil {

	public static VelocityEngine initializeFileResourceVelocity(String resourcePath) throws Exception {
		String characterSet = "UTF-8";
		// 1. initialize velocity engine
		VelocityEngine velocity = new VelocityEngine();
		velocity.setProperty("runtime.log.logsystem.log4j.logger.level", "WARNING");
		velocity.setProperty("resource.loader", "file");
		velocity.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
		velocity.setProperty("file.resource.loader.path", resourcePath);
		velocity.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");

		velocity.setProperty(VelocityEngine.INPUT_ENCODING, characterSet);
		velocity.setProperty(VelocityEngine.OUTPUT_ENCODING, characterSet);
		velocity.setProperty(VelocityEngine.ENCODING_DEFAULT, characterSet);
		velocity.init();

		return velocity;
	}

	public static void mergeTemplate(VelocityEngine velocity, Context context, String template, File output, String encoding) throws Exception {
		Writer writer = new OutputStreamWriter(new FileOutputStream(output), encoding);
		velocity.mergeTemplate(template, encoding, context, writer);
		writer.flush();
	}
}
