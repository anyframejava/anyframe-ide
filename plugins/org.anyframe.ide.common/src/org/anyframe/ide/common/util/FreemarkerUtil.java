package org.anyframe.ide.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.anyframe.ide.common.CommonActivator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class FreemarkerUtil {

	public static void generateFreemarker(String templatePath, String templateFile,
			String outputFile, IProject project, Map inputParameter) throws Exception {

		Configuration cfg = new Configuration();
		Template temp = null;
		FileOutputStream fos = null;
		Writer out = null;

		cfg.setDirectoryForTemplateLoading(new File(templatePath));
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setDefaultEncoding(getProjectCharsetName(project));
		temp = cfg.getTemplate(templateFile);
		fos = new FileOutputStream(outputFile);
		out = new OutputStreamWriter(fos, getProjectCharsetName(project));
		temp.process(inputParameter, out);
		out.flush();
		
		fos.close();
		out.close();
	}

	private static String getProjectCharsetName(IProject project) {
		if (project == null) {
			return null;
		}
		String charsetName = null;
		try {
			charsetName = project.getDefaultCharset();
		} catch (CoreException e) {
			PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, e.getMessage());
		}
		return charsetName;
	}
}
