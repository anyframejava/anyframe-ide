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
package org.anyframe.ide.command.ant.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.taskdefs.optional.ReplaceRegExp;
import org.apache.tools.ant.types.FileSet;
import org.appfuse.mojo.installer.AntUtils;
import org.hibernate.tool.hbm2x.XMLPrettyPrinter;

import org.anyframe.ide.command.common.util.PrettyPrinter;

/**
 * This is a FileUtil class.
 * 
 * @author Sooyeon Park
 */
public class FileUtil {

	private static final int DELETE_RETRY_SLEEP_MILLIS = 10;

	protected FileUtil() {
		throw new UnsupportedOperationException(); // prevents calls from
		// subclass
	}

	public static void replaceStringXMLFilePretty(File existingFile,
			String tokenToReplace, String replaceString) throws Exception {

		if (!existingFile.exists())
			throw new FileNotFoundException();

		replaceStringXMLFile(existingFile, null, null, replaceString, "");
		replaceStringXMLFile(existingFile, null, null, tokenToReplace,
				tokenToReplace + "\n" + replaceString);
		try {
			XMLPrettyPrinter.prettyPrintFile(PrettyPrinter.getDefaultTidy(),
					existingFile, existingFile, true);
		} catch (IOException e) {
			// ignore
		}
	}

	public static void replaceStringEmptyXMLFilePretty(File existingFile,
			String tokenToReplace) {

		if (!existingFile.exists())
			return;

		replaceStringXMLFile(existingFile, null, null, tokenToReplace, "");
		try {
			XMLPrettyPrinter.prettyPrintFile(PrettyPrinter.getDefaultTidy(),
					existingFile, existingFile, true);
		} catch (IOException e) {
			// ignore
		}
	}

	public static void replaceStringXMLFile(File existingFile, String beanName,
			String pojoName, String tokenToReplace, String replaceString) {
		if (!existingFile.exists())
			return;

		String nameInComment = beanName == null ? pojoName : beanName;

		replaceString(existingFile, nameInComment, tokenToReplace,
				replaceString);
	}

	public static void replaceStringXMLFilePretty(File existingFile,
			String nameInComment, String tokenToReplace, String replaceString)
			throws Exception {
		if (!existingFile.exists())
			throw new FileNotFoundException();

		replaceString(existingFile, nameInComment, tokenToReplace,
				replaceString);

		try {
			XMLPrettyPrinter.prettyPrintFile(PrettyPrinter.getDefaultTidy(),
					existingFile, existingFile, true);
		} catch (IOException e) {
			// ignore
		}
	}

	private static void replaceString(File existingFile, String nameInComment,
			String tokenToReplace, String replaceString) {
		Project antProject = AntUtils.createProject();
		String encoding = "UTF-8";

		Replace replace1 = (Replace) antProject.createTask("replace");
		replace1.setFile(existingFile);
		replace1.setEncoding(encoding);
		replace1.setToken("<!--" + nameInComment + "-START-->");
		replace1.setValue("REGULAR-START");
		replace1.execute();

		Replace replace2 = (Replace) antProject.createTask("replace");
		replace2.setFile(existingFile);
		replace2.setEncoding(encoding);
		replace2.setToken("<!--" + nameInComment + "-END-->");
		replace2.setValue("REGULAR-END");
		replace2.execute();

		ReplaceRegExp regExpTask = (ReplaceRegExp) antProject
				.createTask("replaceregexp");
		regExpTask.setFile(existingFile);
		regExpTask.setEncoding(encoding);
		regExpTask.setMatch("REGULAR-START(?s:.)*REGULAR-END");
		regExpTask.setReplace("");
		regExpTask.setFlags("g");
		regExpTask.execute();

		Replace replaceData = (Replace) antProject.createTask("replace");
		replaceData.setFile(existingFile);
		replaceData.setEncoding(encoding);
		replaceData.setToken(tokenToReplace);
		String stringWithProperLineEndings = adjustLineEndingsForOS(replaceString);

		replaceData.setValue(stringWithProperLineEndings);
		replaceData.execute();
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

	/**
	 * Accommodate Windows bug encountered in both Sun and IBM JDKs. Others
	 * possible. If the delete does not work, call System.gc(), wait a little
	 * and try again.
	 */
	public static boolean delete(File f) {
		if (!f.delete()) {
			if (Os.isFamily("windows")) {
				System.gc();
			}
			try {
				Thread.sleep(DELETE_RETRY_SLEEP_MILLIS);
			} catch (InterruptedException ex) {
				// Ignore Exception
			}
			if (!f.delete()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Delete a directory
	 * 
	 * @param d
	 *            the directory to delete
	 */
	public static boolean deleteDir(File d) {
		String[] list = d.list();
		if (list == null) {
			list = new String[0];
		}
		for (int i = 0; i < list.length; i++) {
			String s = list[i];
			File f = new File(d, s);
			if (f.isDirectory()) {
				deleteDir(f);
			} else {
				if (!delete(f)) {
					System.out.println("Unable to delete file "
							+ f.getAbsolutePath());
				}
			}
		}
		if (!delete(d)) {
			System.out.println("Unable to delete file " + d.getAbsolutePath());
			return false;
		}
		return true;
	}

	/**
	 * This method will copy files from the source directory to the destination
	 * directory based on the pattern.
	 * 
	 * @param inSourceDirectory
	 *            The source directory name to copy from.
	 */
	public static void copyJars(String inSourceDirectory,
			String inDestinationDirectory, boolean multiple) {
		Project antProject = AntUtils.createProject();
		Copy copyTask = (Copy) antProject.createTask("copy");

		if (multiple) {
			FileSet fileSet = AntUtils.createFileset(inSourceDirectory,
					"**/*.jar", new ArrayList<Object>());
			copyTask.addFileset(fileSet);
		} else
			copyTask.setFile(new File(inSourceDirectory));

		copyTask.setTodir(new File(inDestinationDirectory));
		copyTask.execute();
	}
}
