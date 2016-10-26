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
import java.util.ArrayList;
import java.util.HashMap;

import org.anyframe.ide.command.common.CommandException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.taskdefs.LoadFile;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.taskdefs.optional.ReplaceRegExp;
import org.apache.tools.ant.types.FileSet;
import org.dbunit.ant.DbConfig;
import org.dbunit.ant.DbConfig.Feature;
import org.dbunit.ant.DbUnitTask;
import org.dbunit.ant.Operation;

/**
 * This is an AntUtil class. This class is a utility for handling ant task.
 * 
 * @author SoYon Lim
 */
public class AntUtil {
	static String ENCODING = "UTF-8";

	private static HashMap<String, String> datatypeFactories = new HashMap<String, String>();

	static {
		datatypeFactories.put("db2", "org.dbunit.ext.db2.Db2DataTypeFactory");
		datatypeFactories.put("h2", "org.dbunit.ext.h2.H2DataTypeFactory");
		datatypeFactories.put("hsqldb", "org.dbunit.ext.hsqldb.HsqldbDataTypeFactory");
		datatypeFactories.put("mssql", "org.dbunit.ext.mssql.MsSqlDataTypeFactory");
		datatypeFactories.put("mysql", "org.dbunit.ext.mysql.MySqlDataTypeFactory");
		datatypeFactories.put("oracle", "org.dbunit.ext.oracle.OracleDataTypeFactory");
		datatypeFactories.put("postgresql", "org.dbunit.ext.postgresql.PostgresqlDataTypeFactory");
	}

	public static void executeCopyTask(Project antProject, String inSourceDirectory, String inDestinationDirectory, String inPattern, boolean multiple) {
		Copy copyTask = (Copy) antProject.createTask("copy");

		if (multiple) {
			FileSet fileSet = new FileSet();
			fileSet.setDir(new File(inSourceDirectory));
			fileSet.setIncludes(inPattern);
			for (Object inExcludePattern : new ArrayList<Object>()) {
				fileSet.setExcludes((String) inExcludePattern);
			}
			copyTask.addFileset(fileSet);
		} else
			copyTask.setFile(new File(inSourceDirectory));
		copyTask.setTodir(new File(inDestinationDirectory));
		copyTask.execute();
	}

	public static void executeCopyTask(Project antProject, String sourceFile, String targetFile) {
		Copy copy = (Copy) antProject.createTask("copy");
		copy.setFile(new File(sourceFile));
		copy.setTofile(new File(targetFile));
		copy.execute();
	}

	public static void executeEchoTask(Project antProject, File file) {
		Echo echoTask = (Echo) antProject.createTask("echo");
		echoTask.setFile(file);
		echoTask.setAppend(true);
		echoTask.setMessage(antProject.getProperty("i18n.file"));
		echoTask.execute();
	}

	public static void executeLoadFileTask(Project antProject, String inFile, String propName) {
		if (!new File(inFile).exists()) {
			return;
		}
		LoadFile loadFileTask = (LoadFile) antProject.createTask("loadfile");
		loadFileTask.init();
		loadFileTask.setProperty(propName);
		loadFileTask.setSrcFile(new File(inFile));
		loadFileTask.setEncoding(ENCODING);

		loadFileTask.execute();
	}

	public static void executeReplaceTask(Project antProject, File file, String token, String value) throws Exception {
		Replace replaceData = (Replace) antProject.createTask("replace");

		replaceData.setFile(file);
		replaceData.setEncoding(ENCODING);
		replaceData.setToken(token);
		replaceData.setValue(value);
		replaceData.execute();
	}

	public static void executeReplaceTask(Project antProject, File file, String startToken, String startValue, String endToken, String endValue)
			throws Exception {

		Replace start = (Replace) antProject.createTask("replace");
		start.setFile(file);
		start.setEncoding(ENCODING);
		start.setToken(startToken);
		start.setValue(startValue);
		start.execute();

		Replace end = (Replace) antProject.createTask("replace");
		end.setFile(file);
		end.setEncoding(ENCODING);
		end.setToken(endToken);
		end.setValue(endValue);
		end.execute();
	}

	public static void executeReplaceRegExpTask(Project antProject, File file, String startToken, String endToken, String tokenToReplace)
			throws Exception {
		ReplaceRegExp regExpTask = (ReplaceRegExp) antProject.createTask("replaceregexp");
		regExpTask.setFile(file);
		regExpTask.setEncoding(ENCODING);
		regExpTask.setMatch(startToken + "(?s:.)*" + endToken);
		regExpTask.setReplace(tokenToReplace);
		regExpTask.setFlags("g");
		regExpTask.execute();
	}

	public static void executeReplaceRegExpTask(Project antProject, File file, String startToken, String endToken, String tokenToReplace,
			String newToken, String newValue) throws Exception {
		executeReplaceRegExpTask(antProject, file, startToken, endToken, tokenToReplace);

		Replace replaceData = (Replace) antProject.createTask("replace");
		replaceData.setFile(file);
		replaceData.setEncoding(ENCODING);
		replaceData.setToken(newToken);
		replaceData.setValue(newValue);
		replaceData.execute();
	}

	public static void executeDbUnitTask(String dbType, String url, String driverClass, String userName, String password, String dbSchema, File src,
			String projectHome) throws Exception {
		Project antProject = new Project();
		antProject.init();

		antProject.addTaskDefinition("dbunit", DbUnitTask.class);

		DbUnitTask dbunitTask = (DbUnitTask) antProject.createTask("dbunit");
		dbunitTask.setDriver(driverClass);
		dbunitTask.setUrl(url);
		dbunitTask.setUserid(userName);
		dbunitTask.setPassword(password);
		if (dbType.equalsIgnoreCase("sybase")) {
			// 1. read project configuration
			File metadataFile = new File(ConfigXmlUtil.getCommonConfigFile(projectHome));

			if (!metadataFile.exists()) {
				throw new CommandException("Can not find a '" + metadataFile.getAbsolutePath() + "' file. Please check a location of your project.");
			}

			PropertiesIO pio = new PropertiesIO(metadataFile.getAbsolutePath());
			String useSchema = pio.readValue("db.schema.use");
			if (useSchema != null && useSchema.equals("true"))
				dbunitTask.setSchema(userName);
		} else if (dbType.equalsIgnoreCase("db2")) {
			dbunitTask.setSchema(userName);
		} else {
			dbunitTask.setSchema(dbSchema);
		}
		DbConfig dbConfig = new DbConfig();

		Property property = new Property();
		property.setName("datatypeFactory");
		if (datatypeFactories.containsKey(dbType)) {
			property.setValue(datatypeFactories.get(dbType));
		} else {
			// default
			property.setValue("org.dbunit.dataset.datatype.DefaultDataTypeFactory");
		}
		dbConfig.addProperty(property);

		Feature feature = new Feature();
		feature.setName("caseSensitiveTableNames");
		feature.setValue(true);
		dbConfig.addFeature(feature);

		feature = new Feature();
		feature.setName("qualifiedTableNames");
		feature.setValue(false);
		dbConfig.addFeature(feature);

		dbunitTask.addDbConfig(dbConfig);

		Operation operation = new Operation();
		operation.setType("INSERT");
		operation.setSrc(src);
		operation.setFormat("flat");
		dbunitTask.addOperation(operation);

		dbunitTask.execute();
	}
}
