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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.anyframe.ide.command.common.plugin.DependentPlugin;
import org.anyframe.ide.command.common.plugin.Exclude;
import org.anyframe.ide.command.common.plugin.Fileset;
import org.anyframe.ide.command.common.plugin.Include;
import org.anyframe.ide.command.common.plugin.PluginBuild;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.plugin.PluginInterceptor;
import org.anyframe.ide.command.common.plugin.PluginInterceptorDependency;
import org.anyframe.ide.command.common.plugin.PluginResource;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.common.util.ListScanner;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.tools.ant.Project;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.hibernate.tool.hbm2x.XMLPrettyPrinter;

import com.thoughtworks.xstream.XStream;

/**
 * This is an FileUtils class. This class is a utility for file.
 * 
 * @author <a href="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
 * @author <a href="mailto:sanders@codehaus.org">Scott Sanders</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph.Reck</a>
 * @author <a href="mailto:peter@codehaus.org">Peter Donald</a>
 * @author <a href="mailto:jefft@codehaus.org">Jeff Turner</a>
 * @author modified by Soyon Lim
 * @version $Id: FileUtils.java 8145 2009-03-25 19:54:54Z bentmann $
 */
public class FileUtil {
	// search file list from module library
	public static List<String> resolveFileNames(File archetypeFile)
			throws Exception {
		List<String> fileNames = new ArrayList<String>();

		FileInputStream fis = null;
		ZipInputStream zis = null;

		try {
			fis = new FileInputStream(archetypeFile);
			zis = new ZipInputStream(new BufferedInputStream(fis));

			ZipEntry entry = null;

			while (((entry = zis.getNextEntry()) != null)) {
				if (!entry.getName().trim().endsWith("/"))
					fileNames.add(entry.getName());
			}
		} finally {
			fis.close();
			zis.close();
		}

		return fileNames;
	}

	/**
	 * Finds files within a given directory (and optionally its subdirectories)
	 * which match an array of extensions.
	 * 
	 * @param directory
	 *            the directory to search in
	 * @param extensions
	 *            an array of extensions, ex. {"java","xml"}. If this parameter
	 *            is <code>null</code>, all files are returned.
	 * @param recursive
	 *            if true all subdirectories are searched as well
	 * @return an collection of java.io.File with the matching files
	 */
	public static List<String> findFiles(String baseDir, String[] extensions,
			boolean recursive) {
		if (!new File(baseDir).exists())
			return new ArrayList<String>();
		return (List<String>) findFiles(new File(baseDir), extensions,
				recursive);
	}

	/**
	 * Finds files within a given directory (and optionally its subdirectories)
	 * which match an array of extensions.
	 * 
	 * @param directory
	 *            the directory to search in
	 * @param extensions
	 *            an array of extensions, ex. {"java","xml"}. If this parameter
	 *            is <code>null</code>, all files are returned.
	 * @param recursive
	 *            if true all subdirectories are searched as well
	 * @return an collection of java.io.File with the matching files
	 */
	public static Collection findFiles(File directory, String[] extensions,
			boolean recursive) {
		IOFileFilter filter;
		if (extensions == null) {
			filter = TrueFileFilter.INSTANCE;
		} else {
			String[] suffixes = toSuffixes(extensions);
			filter = new SuffixFileFilter(suffixes);
		}
		return findFiles(
				directory,
				filter,
				(recursive ? TrueFileFilter.INSTANCE : FalseFileFilter.INSTANCE));
	}

	/**
	 * Finds files within a given directory (and optionally its subdirectories).
	 * All files found are filtered by an IOFileFilter.
	 * <p>
	 * If your search should recurse into subdirectories you can pass in an
	 * IOFileFilter for directories. You don't need to bind a
	 * DirectoryFileFilter (via logical AND) to this filter. This method does
	 * that for you.
	 * <p>
	 * An example: If you want to search through all directories called "temp"
	 * you pass in <code>FileFilterUtils.NameFileFilter("temp")</code>
	 * <p>
	 * Another common usage of this method is find files in a directory tree but
	 * ignoring the directories generated CVS. You can simply pass in
	 * <code>FileFilterUtils.makeCVSAware(null)</code>.
	 * 
	 * @param directory
	 *            the directory to search in
	 * @param fileFilter
	 *            filter to apply when finding files.
	 * @param dirFilter
	 *            optional filter to apply when finding subdirectories. If this
	 *            parameter is <code>null</code>, subdirectories will not be
	 *            included in the search. Use TrueFileFilter.INSTANCE to match
	 *            all directories.
	 * @return an collection of java.io.File with the matching files
	 * @see org.apache.commons.io.filefilter.FileFilterUtils
	 * @see org.apache.commons.io.filefilter.NameFileFilter
	 */
	public static Collection findFiles(File directory, IOFileFilter fileFilter,
			IOFileFilter dirFilter) {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException(
					"Parameter 'directory' is not a directory");
		}
		if (fileFilter == null) {
			throw new NullPointerException("Parameter 'fileFilter' is null");
		}

		// Setup effective file filter
		IOFileFilter effFileFilter = FileFilterUtils.andFileFilter(fileFilter,
				FileFilterUtils.notFileFilter(DirectoryFileFilter.INSTANCE));

		// Setup effective directory filter
		IOFileFilter effDirFilter;
		if (dirFilter == null) {
			effDirFilter = FalseFileFilter.INSTANCE;
		} else {
			effDirFilter = FileFilterUtils.andFileFilter(dirFilter,
					DirectoryFileFilter.INSTANCE);
		}

		// Find files
		Collection files = new java.util.LinkedList();
		innerListFiles(files, directory, FileFilterUtils.orFileFilter(
				effFileFilter, effDirFilter));
		return files;
	}

	/**
	 * Finds files within a given directory (and optionally its subdirectories).
	 * All files found are filtered by an IOFileFilter.
	 * 
	 * @param files
	 *            the collection of files found.
	 * @param directory
	 *            the directory to search in.
	 * @param filter
	 *            the filter to apply to files and directories.
	 */
	private static void innerListFiles(Collection files, File directory,
			IOFileFilter filter) {
		File[] found = directory.listFiles((FileFilter) filter);

		if (found != null) {
			for (int i = 0; i < found.length; i++) {
				if (found[i].isDirectory()) {
					innerListFiles(files, found[i], filter);
				} else {
					files.add(found[i].getAbsolutePath());
				}
			}
		}
	}

	/**
	 * Converts an array of file extensions to suffixes for use with
	 * IOFileFilters.
	 * 
	 * @param extensions
	 *            an array of extensions. Format: {"java", "xml"}
	 * @return an array of suffixes. Format: {".java", ".xml"}
	 */
	private static String[] toSuffixes(String[] extensions) {
		String[] suffixes = new String[extensions.length];
		for (int i = 0; i < extensions.length; i++) {
			suffixes[i] = "." + extensions[i];
		}
		return suffixes;
	}

	public static List<String> findFiles(String baseDir,
			String includesPattern, String excludesPattern) {
		File dir = new File(baseDir);

		if (!dir.exists())
			return new ArrayList<String>();

		if (dir.isDirectory()) {
			File[] files = dir.listFiles();

			ArrayList<String> fileList = new ArrayList<String>();
			for (int i = 0; i < files.length; i++)
				fileList.add(files[i].getAbsolutePath());

			return findFiles(fileList, baseDir, includesPattern,
					excludesPattern);
		}
		return new ArrayList<String>();
	}

	public static List<String> findFiles(List<String> files, String baseDir,
			String includesPattern, String excludesPattern) {
		ArrayList<String> includes = null;

		if (includesPattern != null) {
			includes = new ArrayList<String>();
			includes.add(includesPattern);
		}

		ArrayList<String> excludes = null;
		if (excludesPattern != null) {
			excludes = new ArrayList<String>();
			excludes.add(excludesPattern);
		}

		return findFiles(files, baseDir, includes, excludes);
	}

	public static List<String> findFiles(List<String> files, String baseDir,
			List<String> includesPattern, List<String> excludesPattern) {
		ListScanner scanner = new ListScanner();
		scanner.setBasedir(baseDir);

		if (includesPattern != null)
			scanner.setIncludes(includesPattern);
		if (excludesPattern != null)
			scanner.setExcludes(excludesPattern);
		List<String> result = (List<String>) scanner.scan(files);

		return result;
	}

	public static void copyFile(File source, File destination)
			throws IOException {
		InputStream in = null;
		OutputStream out = null;

		try {
			in = new FileInputStream(source);
			out = new FileOutputStream(destination);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			in.close();
			out.close();
		}
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
				if (!deleteFile(f)) {
					System.out.println("Unable to delete file "
							+ f.getAbsolutePath());
				}
			}
		}
		if (!deleteFile(d)) {
			System.out.println("Unable to delete file " + d.getAbsolutePath());
			return false;
		}
		return true;
	}

	public static boolean deleteFile(File file) {
		if (!file.delete()) {
			if (Os.isFamily("windows")) {
				System.gc();
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				// Ignore Exception
			}
			if (!file.delete()) {
				return false;
			}
		}
		return true;
	}

	public static void deleteFile(File dir, String pluginName,
			List<String> excludes, boolean recursive) {
		if (!dir.exists()) {
			return;
		}

		File[] files = dir.listFiles();

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				if (recursive)
					deleteFile(file, pluginName, excludes, recursive);
				else
					continue;
			}

			if (excludes.contains(file.getName()))
				continue;
			if (file.getName().toLowerCase().indexOf(pluginName.toLowerCase()) != -1)
				deleteFile(file);
		}
	}

	public static void deleteFile(File dir, List<String> filenames) {
		for (int i = 0; i < filenames.size(); i++)
			deleteFile(new File(dir, (String) filenames.get(i)));
	}

	/**
	 * create a output folder for installing sample project. Output folder is
	 * the 'target/pjtname'
	 * 
	 * @return output directory
	 * @throws Exception
	 */
	public static File makeDir(String parentDir, String folderName)
			throws Exception {
		File outputDir = new File(parentDir, folderName);
		outputDir.mkdirs();
		return outputDir;
	}

	public static void deleteDir(final File dir, List<String> excludes)
			throws Exception {
		if (!dir.exists()) {
			return;
		}

		if (!dir.isDirectory()) {
			final String message = dir + " is not a directory";
			throw new IllegalArgumentException(message);
		}

		cleanDir(dir, excludes);

		if (dir.listFiles() == null || dir.listFiles().length == 0)
			if (!dir.delete()) {
				final String message = "Directory " + dir
						+ " unable to be deleted.";
				throw new IOException(message);
			}
	}

	public static void cleanDir(final File dir, List<String> excludes)
			throws Exception {
		final File[] files = dir.listFiles();

		if (files == null) {
			return;
		}

		for (int i = 0; i < files.length; i++) {
			final File file = files[i];

			if (excludes.contains(file.getName()))
				continue;
			if (file.isDirectory())
				cleanDir(file, excludes);
			deleteFile(file);
		}
	}

	/**
	 * Delete the directory whose name is match with plugin name. This will
	 * recursively search the directory.
	 * 
	 * @param dir
	 * @param pluginName
	 * @param excludes
	 * @throws Exception
	 */
	public static void deleteDir(final File dir, String pluginName,
			List<String> excludes) throws Exception {
		final File[] files = dir.listFiles();

		if (files == null) {
			return;
		}

		for (final File file : files) {

			if (file.isDirectory()) {
				deleteDir(file, pluginName, excludes);

			} else {
				if (excludes.contains(file.getName()))
					continue;

				deleteFile(file);
			}
		}

		if ((dir.listFiles() == null || dir.listFiles().length == 0)
				&& dir.getName().toLowerCase().equals(pluginName.toLowerCase()))
			deleteFile(dir);
	}

	public static void copyDir(File source, File destination)
			throws IOException {
		copyFile(source, new File(destination, source.getName()));
	}

	/**
	 * change package style to directory style
	 * 
	 * @param packageName
	 * @return directory name
	 */
	public static String changePackageForDir(String packageName) {
		return StringUtils.replace(packageName, ".",
				CommonConstants.fileSeparator);
	}

	public static void commentFileContent(File file, String startToken,
			String startValue, String endToken, String endValue)
			throws Exception {
		if (!file.exists())
			return;

		Project antProject = new Project();
		antProject.init();

		AntUtil.executeReplaceTask(antProject, file, startToken, startValue,
				endToken, endValue);
	}

	public static void commentFileContent(File file, String startToken,
			String endToken) throws Exception {
		commentFileContent(file, startToken, startToken + "<!--", endToken,
				"-->" + endToken);
	}

	public static void uncommentFileContent(File file, String startToken,
			String endToken) throws Exception {
		commentFileContent(file, startToken + "<!--", startToken, "-->"
				+ endToken, endToken);
	}

	public static void addFileContent(File file, String token, String value,
			boolean isPretty) throws Exception {
		if (!file.exists())
			return;

		Project antProject = new Project();
		antProject.init();

		AntUtil.executeReplaceTask(antProject, file, token, value);

		if (isPretty) {
			XMLPrettyPrinter.prettyPrintFile(PrettyPrinter.getDefaultTidy(),
					file, file, true);
		}
	}

	public static void removeFileContent(File file, String token, String value,
			boolean isPretty) throws Exception {
		if (!file.exists())
			return;

		Project antProject = new Project();
		antProject.init();

		AntUtil.executeReplaceRegExpTask(antProject, file, "<!--" + token
				+ "-START-->", "<!--" + token + "-END-->", value);

		if (isPretty) {
			XMLPrettyPrinter.prettyPrintFile(PrettyPrinter.getDefaultTidy(),
					file, file, true);
		}
	}

	public static void replaceFileContent(File file, String token, String value)
			throws Exception {
		if (!file.exists())
			return;

		Project antProject = new Project();
		antProject.init();

		AntUtil.executeReplaceTask(antProject, file, token, value);

		XMLPrettyPrinter.prettyPrintFile(PrettyPrinter.getDefaultTidy(), file,
				file, true);
	}

	// in case of .classpath
	public static void replaceFileContent(File file, String startToken,
			String endToken, String replaceToken, String newToken,
			String newValue) throws Exception {
		Project antProject = new Project();
		antProject.init();

		AntUtil.executeReplaceRegExpTask(antProject, file, startToken,
				endToken, replaceToken, newToken, newValue);
	}

	public static void replaceFileContent(File file, String token,
			String value, boolean isPretty) throws Exception {
		Project antProject = new Project();
		antProject.init();

		AntUtil.executeReplaceTask(antProject, file, token, value);

		if (isPretty)
			XMLPrettyPrinter.prettyPrintFile(PrettyPrinter.getDefaultTidy(),
					file, file, true);
	}

	public static void replaceFileContent(File file, String startToken,
			String endToken, String replaceToken, String value, boolean isPretty)
			throws Exception {
		replaceFileContent(file, startToken, endToken, replaceToken,
				replaceToken, value);

		if (isPretty)
			XMLPrettyPrinter.prettyPrintFile(PrettyPrinter.getDefaultTidy(),
					file, file, true);
	}

	public static void replaceFileContent(File file, String startToken,
			String endToken, String replaceToken, String value)
			throws Exception {
		replaceFileContent(file, startToken, endToken, replaceToken,
				replaceToken, value);

		XMLPrettyPrinter.prettyPrintFile(PrettyPrinter.getDefaultTidy(), file,
				file, true);
	}

	public static Object getObjectFromXML(InputStream inputStream)
			throws Exception {
		try {
			Object pluginInfo = getXStream().fromXML(inputStream);
			return pluginInfo;
		} finally {
			inputStream.close();
		}
	}

	public static Object getObjectFromXML(File file) throws Exception {
		FileReader reader = new FileReader(file);
		try {
			if (file.getName().equals(CommonConstants.PLUGIN_BUILD_FILE))
				return getXStream(PluginInfo.class).fromXML(reader);
			if (file.getName().equals(Constants.ARCHETYPE_POM))
				return getXStream(Model.class).fromXML(reader);
			return getXStream().fromXML(reader);
		} finally {
			reader.close();
		}
	}

	public static void getObjectToXML(Object object, File file)
			throws Exception {
		Writer writer = new FileWriterWithEncoding(file, "UTF-8");
		try {
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");

			getXStream(object.getClass()).toXML(object, writer);
		} finally {
			writer.close();
		}
	}

	private static XStream getXStream() {
		return getXStream(null);
	}

	private static XStream getXStream(Class type) {
		XStream xstream = new XStream();

		if (type == null)
			type = ListOrderedMap.class;

		if (type == ListOrderedMap.class) {
			xstream.alias("plugins", ListOrderedMap.class);
			xstream.aliasAttribute(PluginInfo.class, "latestVersion",
					"latestVersion");
			xstream.alias("versions", ArrayList.class);
			xstream.registerConverter(new PluginMapConverter(xstream
					.getMapper()));
		}

		if (type == ListOrderedMap.class || type == PluginInfo.class) {
			xstream.alias("plugin", PluginInfo.class);
			xstream.aliasAttribute(PluginInfo.class, "name", "name");
			xstream.aliasAttribute(PluginInfo.class, "description",
					"description");
			xstream.alias("interceptor", PluginInterceptor.class);
			xstream.aliasField("class", PluginInterceptor.class, "className");
			xstream.alias("dependencies", ArrayList.class);
			xstream.alias("dependency", PluginInterceptorDependency.class);
			xstream.alias("version", String.class);
			xstream.alias("resources", ArrayList.class);
			xstream.alias("resource", PluginResource.class);
			xstream.aliasAttribute(PluginResource.class, "dir", "dir");
			xstream
					.aliasAttribute(PluginResource.class, "packaged",
							"packaged");
			xstream
					.aliasAttribute(PluginResource.class, "filtered",
							"filtered");
			xstream.aliasAttribute(Include.class, "name", "name");
			xstream.aliasAttribute(Exclude.class, "name", "name");

			xstream.aliasField("dependent-plugins", PluginInfo.class,
					"dependentPlugins");
			xstream.alias("dependent-plugin", DependentPlugin.class);
			xstream.aliasAttribute(DependentPlugin.class, "name", "name");
			xstream.aliasAttribute(DependentPlugin.class, "version", "version");

			xstream.alias("filesets", ArrayList.class);
			xstream.alias("fileset", Fileset.class);
			xstream.aliasAttribute(Fileset.class, "dir", "dir");
			xstream.aliasAttribute(Fileset.class, "filtered", "filtered");
			xstream.aliasAttribute(Fileset.class, "packaged", "packaged");

			xstream.addImplicitCollection(PluginResource.class, "includes",
					"include", Include.class);
			xstream.addImplicitCollection(PluginResource.class, "excludes",
					"exclude", Exclude.class);
			xstream.addImplicitCollection(Fileset.class, "includes", "include",
					Include.class);
			xstream.addImplicitCollection(Fileset.class, "excludes", "exclude",
					Exclude.class);
			xstream.alias("include", Include.class);
			xstream.alias("exclude", Exclude.class);
			xstream.alias("build", PluginBuild.class);
		} else if (type == Model.class) {
			xstream.alias("project", Model.class);
			xstream.omitField(Model.class, "modelEncoding");
			xstream.omitField(ModelBase.class, "modelEncoding");
			xstream.omitField(Parent.class, "relativePath");
			xstream.omitField(Parent.class, "modelEncoding");
			xstream.omitField(Dependency.class, "type");
			xstream.omitField(Dependency.class, "optional");
			xstream.omitField(Dependency.class, "modelEncoding");
			xstream.omitField(Model.class, "properties");
			xstream.omitField(Model.class, "build");
			xstream.omitField(Model.class, "reporting");

			xstream.alias("dependencies", ArrayList.class);
			xstream.alias("dependency", Dependency.class);
			xstream.alias("plugin", Plugin.class);
		}

		xstream.setMode(XStream.NO_REFERENCES);

		return xstream;
	}

	public static Map<String, String> findReplaceRegion(
			InputStream inputStream, String pluginName) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		try {
			Pattern startPattern = Pattern
					.compile("(?i)(?u)(?s)\\A(?:(?:\\<\\!\\-\\-" + pluginName
							+ "\\-)).*(?s).*(?:(?:\\-START\\-\\-\\>))\\z");
			Pattern endPattern = Pattern
					.compile("(?i)(?u)(?s)\\A(?:(?:\\<\\!\\-\\-" + pluginName
							+ "\\-)).*(?s).*(?:(?:\\-END\\-\\-\\>))\\z");

			String aLine = null;

			HashMap<String, String> map = new HashMap<String, String>();
			while ((aLine = reader.readLine()) != null) {
				String trimmedLine = aLine.trim();
				Matcher matcher = startPattern.matcher(trimmedLine);
				if (matcher.find()) {
					int length = trimmedLine.length();

					String key = trimmedLine.substring(
							4 + pluginName.length() + 1, length - 9);

					StringBuffer value = new StringBuffer();
					while ((aLine = reader.readLine()) != null) {
						trimmedLine = aLine.trim();
						matcher = endPattern.matcher(trimmedLine);
						if (matcher.find()) {
							map.put(key, value.toString());
							break;
						}
						value.append(aLine + "\n");
					}
				}
			}

			return map;
		} finally {
			inputStream.close();
			reader.close();
		}
	}

	// from GEN
	/**
	 * This method will copy files from the source directory to the destination
	 * directory based on the pattern.
	 * 
	 * @param inSourceDirectory
	 *            The source directory name to copy from.
	 */
	public static void copyJars(String inSourceDirectory,
			String inDestinationDirectory, boolean multiple) {
		Project antProject = new Project();
		antProject.init();

		AntUtil.executeCopyTask(antProject, inSourceDirectory,
				inDestinationDirectory, "**/*.jar", multiple);
	}

	public static void replaceStringXMLFilePretty(File existingFile,
			String nameInComment, String newToken, String newValue)
			throws Exception {
		if (!existingFile.exists())
			throw new FileNotFoundException();

		replaceString(existingFile, nameInComment, newToken, newValue);

		try {
			XMLPrettyPrinter.prettyPrintFile(PrettyPrinter.getDefaultTidy(),
					existingFile, existingFile, true);
		} catch (IOException e) {
			// ignore
		}
	}

	public static void replaceStringXMLFilePretty(File existingFile,
			String newToken, String newValue) throws Exception {

		if (!existingFile.exists())
			throw new FileNotFoundException();

		replaceStringXMLFile(existingFile, null, null, newValue, "");
		replaceStringXMLFile(existingFile, null, null, newToken, newToken
				+ "\n" + newValue);
		try {
			XMLPrettyPrinter.prettyPrintFile(PrettyPrinter.getDefaultTidy(),
					existingFile, existingFile, true);
		} catch (IOException e) {
			// ignore
		}
	}

	public static void replaceStringXMLFile(File existingFile, String beanName,
			String pojoName, String newToken, String newValue) throws Exception {
		if (!existingFile.exists())
			return;

		String nameInComment = beanName == null ? pojoName : beanName;

		replaceString(existingFile, nameInComment, newToken, newValue);
	}

	private static void replaceString(File existingFile, String nameInComment,
			String newToken, String newValue) throws Exception {
		Project antProject = new Project();
		antProject.init();

		AntUtil.executeReplaceRegExpTask(antProject, existingFile, "<!--"
				+ nameInComment + "-START-->", "<!--" + nameInComment
				+ "-END-->", "", newToken, newValue);
	}

	// public static String SLASH = System.getProperty("file.separator");

	public static File[] dirListByAscAlphabet(File folder) {

		if (!folder.isDirectory()) {
			return null;
		}

		File[] files = folder.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return !name.startsWith(".");
			}
		});

		Arrays.sort(files, new Comparator() {
			public int compare(final Object o1, final Object o2) {
				return ((File) o1).getName().toLowerCase().compareTo(
						((File) o2).getName().toLowerCase());
			}
		});

		return files;
	}

	public static boolean isExistsTemplates(File templateHome) {
		File path = new File(templateHome.getAbsolutePath()
				+ CommonConstants.fileSeparator + "source");
		File templateConfigPath = new File(path + CommonConstants.fileSeparator
				+ "template.config");

		if (path.exists()) {
			if (templateConfigPath.exists())
				return true;
		}
		return false;
	}

	public static void writeStringToFile(File outputFile, String data,
			String encoding) throws Exception {
		FileUtils.writeStringToFile(outputFile, data, encoding);
		if (outputFile.getName().endsWith(".xml")
				|| outputFile.getName().endsWith(".html"))
			XMLPrettyPrinter.prettyPrintFile(PrettyPrinter.getDefaultTidy(),
					outputFile, outputFile, true);

	}

	public static void moveFile(File srcFile, File backupDir) throws Exception {
		String targetPath = srcFile.getCanonicalPath().substring(
				new File(".").getCanonicalPath().length());
		FileUtils.moveFile(srcFile, new File(backupDir, targetPath));
	}

	@SuppressWarnings("unchecked")
	public static boolean moveFile(File srcFolder, File backupDir,
			List<String> includePatterns, Collection<String> excludeFiles,
			List<String> excludePatterns, boolean recursive)
			throws Exception {

		if (!srcFolder.exists()) {
			return false;
		}

		IOFileFilter optionFilter = (recursive) ? TrueFileFilter.TRUE : null;

		IOFileFilter excludesFilter = FileFilterUtils
				.notFileFilter(new NameFileFilter(excludeFiles
						.toArray(new String[excludeFiles.size()]),
						IOCase.SENSITIVE));

		IOFileFilter pluginNameFilter = new WildcardFileFilter(includePatterns, IOCase.INSENSITIVE);

		pluginNameFilter = FileFilterUtils.andFileFilter(pluginNameFilter,
				FileFilterUtils.notFileFilter(new WildcardFileFilter(excludePatterns, IOCase.INSENSITIVE)));

		Collection<File> files = FileUtils.listFiles(srcFolder, FileFilterUtils
				.andFileFilter(excludesFilter, pluginNameFilter), optionFilter);

		// -- move files
		for (File src : files) {
			String targetPath = src.getCanonicalPath().substring(
					new File(".").getCanonicalPath().length());
			FileUtils.moveFile(src, new File(backupDir, targetPath));
		}

		return true;
	}

	public static boolean moveDirectory(File srcFolder, File backupDir,
			Collection<String> excludes) throws Exception {

		if (!srcFolder.exists()) {
			return false;
		}

		IOFileFilter excludesFilter = FileFilterUtils
				.notFileFilter(new NameFileFilter(excludes
						.toArray(new String[excludes.size()]), IOCase.SENSITIVE));

		// TODO :
		Collection<File> files = FileUtils.listFiles(srcFolder, excludesFilter,
				TrueFileFilter.INSTANCE);

		// -- move files
		for (File src : files) {
			String targetPath = src.getCanonicalPath().substring(
					new File(".").getCanonicalPath().length());
			FileUtils.moveFile(src, new File(backupDir, targetPath));
		}

		// --- delete empty folder
		files.clear();
		innerListDirectories(files, srcFolder, DirectoryFileFilter.DIRECTORY);

		for (File file : files) {
			if (file.listFiles() == null || file.listFiles().length == 0) {
				FileUtils.deleteDirectory(file);
			}
		}

		// -- delete current directory if empty
		if (srcFolder.listFiles() == null || srcFolder.listFiles().length == 0) {
			FileUtils.deleteDirectory(srcFolder);
		}

		return true;
	}

	/**
	 * 
	 * @param srcFolder
	 * @param backupDir
	 * @param pluginName
	 * @param excludes
	 * @return
	 * @throws Exception
	 */
	public static boolean moveDirectory(File srcFolder, File backupDir,
			String pluginName, Collection<String> excludes) throws Exception {

		if (!srcFolder.exists()) {
			return false;
		}

		IOFileFilter excludesFilter = new NameFileFilter(excludes
				.toArray(new String[excludes.size()]), IOCase.SENSITIVE);

		IOFileFilter pluginNameFilter = new WildcardFileFilter("*" + pluginName
				+ "*", IOCase.INSENSITIVE);

		Collection<File> files = new java.util.LinkedList<File>();
		innerListDirectories(files, srcFolder, pluginNameFilter);

		// -- move files
		for (File src : files) {

			// check excludes
			File[] excludeFiles = src.listFiles((FileFilter) excludesFilter);

			if (excludeFiles == null || excludeFiles.length == 0) {
				String targetPath = src.getCanonicalPath().substring(
						new File(".").getCanonicalPath().length());
				FileUtils.moveFile(src, new File(backupDir, targetPath));
			}
		}

		// --- delete empty folder
		files.clear();
		innerListDirectories(files, srcFolder, DirectoryFileFilter.DIRECTORY);

		for (File file : files) {
			if (file.listFiles() == null || file.listFiles().length == 0) {
				FileUtils.deleteDirectory(file);
			}
		}

		// -- delete current directory if empty
		if (srcFolder.listFiles() == null || srcFolder.listFiles().length == 0) {
			FileUtils.deleteDirectory(srcFolder);
		}

		return true;
	}

	private static void innerListDirectories(Collection<File> files,
			File directory, IOFileFilter filter) {
		if (directory.isDirectory()) {

			File[] directories = directory
					.listFiles((FileFilter) (DirectoryFileFilter.INSTANCE));
			// File[] found = directory.listFiles((FileFilter) filter);
			if (directories != null) {
				for (int i = 0; i < directories.length; i++) {
					innerListDirectories(files, directories[i], filter);
				}
			}

			File[] founds = directory.listFiles((FileFilter) FileFilterUtils
					.andFileFilter(filter, FileFilterUtils
							.notFileFilter(DirectoryFileFilter.INSTANCE)));

			if (founds != null) {
				for (int i = 0; i < founds.length; i++) {
					files.add(founds[i]);
				}
			}

			if (filter.accept(directory)) {
				files.add(directory);
			}

		}
	}

	// public static Collection<File> listFiles(
	// File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
	//		
	// if (!directory.isDirectory()) {
	// throw new IllegalArgumentException(
	// "Parameter 'directory' is not a directory");
	// }
	// if (fileFilter == null) {
	// throw new NullPointerException("Parameter 'fileFilter' is null");
	// }
	//
	// //Setup effective file filter
	// IOFileFilter effFileFilter = FileFilterUtils.and(fileFilter,
	// FileFilterUtils.notFileFilter(DirectoryFileFilter.INSTANCE));
	//
	// //Setup effective directory filter
	// IOFileFilter effDirFilter;
	// if (dirFilter == null) {
	// effDirFilter = FalseFileFilter.INSTANCE;
	// } else {
	// effDirFilter = FileFilterUtils.and(dirFilter,
	// DirectoryFileFilter.INSTANCE);
	// }
	//
	// //Find files
	// Collection<File> files = new java.util.LinkedList<File>();
	// innerList(files, directory,
	// FileFilterUtils.or(effFileFilter, effDirFilter));
	// return files;
	// }

}
