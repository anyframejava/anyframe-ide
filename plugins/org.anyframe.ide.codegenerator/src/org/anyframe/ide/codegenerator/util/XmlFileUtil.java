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
package org.anyframe.ide.codegenerator.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Properties;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.config.AnyframeConfig;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.dialog.JdbcType;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.taskdefs.optional.ReplaceRegExp;
import org.appfuse.mojo.installer.AntUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.hibernate.tool.hbm2x.XMLPrettyPrinter;
import org.w3c.tidy.Tidy;

import com.thoughtworks.xstream.XStream;

/**
 * This is an XmlFileUtil class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class XmlFileUtil {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	protected XmlFileUtil() {
		throw new UnsupportedOperationException(); // prevents calls from
		// subclass
	}

	public static void saveObjectToXml(Object xmlContents,
			IFile anyframeConfigFileLocation, IProject project) {
		XStream xstream = getXStream();

		String anyframeConfigXml = xstream.toXML(xmlContents);

		// File anyframeConfigFile = new File(anyframeConfigFileLocation);

		InputStream in = null;
		try {
			// anyframeConfigFile.createNewFile();
			// FileOutputStream fileOutputStream =
			// new FileOutputStream(anyframeConfigFile);
			// fileOutputStream.write(anyframeConfigXml.getBytes());
			// fileOutputStream.close();
			IFolder folder = project.getFolder(Constants.SETTING_HOME);
			if (!folder.exists()) {
				folder.create(true, true, new NullProgressMonitor());
			}

			in = new ByteArrayInputStream(anyframeConfigXml.getBytes("UTF-8"));
			if (anyframeConfigFileLocation.exists()) {
				anyframeConfigFileLocation.delete(true,
						new NullProgressMonitor());
				anyframeConfigFileLocation.create(in, true,
						new NullProgressMonitor());
			} else
				anyframeConfigFileLocation.create(in, true,
						new NullProgressMonitor());
		} catch (Exception e) {
			PluginLoggerUtil.error(ID, Message.exception_log_findfile, e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				PluginLoggerUtil.error(ID, Message.exception_log_findfile, e);
			}
		}
	}

	public static Object getObjectFromXml(String anyframeConfigFileLocation) {
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(anyframeConfigFileLocation);
		} catch (FileNotFoundException e) {
			PluginLoggerUtil.error(ID, Message.exception_log_findfile, e);
		}
		return getXStream().fromXML(fileReader);
	}

	public static Object getObjectFromInputStream(InputStream inputStream) {
		return getXStream().fromXML(inputStream);
	}

	private static XStream getXStream() {
		XStream xstream = new XStream();

		// Annotations.configureAliases(xstream, AnyframeConfig.class);
		// Annotations.configureAliases(xstream, JdbcType.class);

		xstream.alias("config", AnyframeConfig.class);
		xstream.alias("jdbcType", JdbcType.class);

		xstream.setMode(XStream.NO_REFERENCES);

		return xstream;
	}

	public static void replaceStringXMLFile(File existingFile,
			String nameInComment, String tokenToReplace, String replaceString) {
		if (!existingFile.exists())
			return;

		Project antProject = AntUtils.createProject();

		Replace replace1 = (Replace) antProject.createTask("replace");
		replace1.setFile(existingFile);
		replace1.setToken("<!--" + nameInComment + "-START-->");
		replace1.setValue("REGULAR-START");
		replace1.execute();

		Replace replace2 = (Replace) antProject.createTask("replace");
		replace2.setFile(existingFile);
		replace2.setToken("<!--" + nameInComment + "-END-->");
		replace2.setValue("REGULAR-END");
		replace2.execute();

		ReplaceRegExp regExpTask = (ReplaceRegExp) antProject
				.createTask("replaceregexp");
		regExpTask.setFile(existingFile);
		regExpTask.setMatch("REGULAR-START(?s:.)*REGULAR-END");
		regExpTask.setReplace("");
		regExpTask.setFlags("g");
		regExpTask.execute();

		Replace replaceData = (Replace) antProject.createTask("replace");
		replaceData.setFile(existingFile);
		replaceData.setToken(tokenToReplace);
		String stringWithProperLineEndings = adjustLineEndingsForOS(replaceString);
		replaceData.setValue(stringWithProperLineEndings);
		replaceData.execute();

		try {
			XMLPrettyPrinter.prettyPrintFile(getDefaultTidy(), existingFile,
					existingFile, true);
		} catch (IOException e) {
			PluginLoggerUtil.error(ID, Message.exception_log_parsejtidy, e);
		}
	}

	public static String adjustLineEndingsForOS(String property) {
		String os = System.getProperty("os.name");

		if (os.startsWith("Linux") || os.startsWith("Mac")) {
			// remove the \r returns
			property = property.replaceAll("\r", "");
		} else if (os.startsWith("Windows")) {
			// use windows line endings
			property = property.replaceAll(">\n", ">\r\n");
		}

		return property;
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

		return tidy;
	}

}
