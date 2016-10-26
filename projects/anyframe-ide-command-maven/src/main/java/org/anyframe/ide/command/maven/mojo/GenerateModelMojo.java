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
package org.anyframe.ide.command.maven.mojo;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.anyframe.ide.command.common.CommandException;
import org.anyframe.ide.command.common.PluginInfoManager;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.ConfigXmlUtil;
import org.anyframe.ide.command.common.util.DBUtil;
import org.anyframe.ide.command.common.util.FileUtil;
import org.anyframe.ide.command.common.util.JdbcOption;
import org.anyframe.ide.command.common.util.ProjectConfig;
import org.anyframe.ide.command.common.util.VelocityUtil;
import org.anyframe.ide.command.maven.mojo.codegen.Column;
import org.anyframe.ide.command.maven.mojo.codegen.Domain;
import org.anyframe.ide.command.maven.mojo.codegen.GenerationUtil;
import org.anyframe.ide.command.maven.mojo.codegen.SourceCodeChecker;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.codehaus.plexus.components.interactivity.Prompter;

/**
 * This is an AnyframeModelGenerator class.
 * 
 * @goal create-model
 * @author Matt Raible
 * @author modified by SooYeon Park
 * @author modified by Sujeong Lee
 */
public class GenerateModelMojo extends AbstractPluginMojo {

	/** @component role="org.codehaus.plexus.components.interactivity.Prompter" */
	private Prompter prompter;

	/**
	 * @component 
	 *            role="org.anyframe.ide.command.common.DefaultPluginInfoManager"
	 */
	protected PluginInfoManager pluginInfoManager;

	/**
	 * target folder to generate model-class
	 * 
	 * @parameter expression="${basedir}"
	 */
	protected File baseDir;

	/**
	 * target table
	 * 
	 * @parameter expression="${table}" default-value="*"
	 */
	private String table = "";

	/**
	 * The project's home.
	 * 
	 * @parameter expression="${projecthome}" default-value="${basedir}"
	 */
	protected String projectHome = "";

	/**
	 * The package name.
	 * 
	 * @parameter expression="${package}"
	 */
	protected String packageName = "";

	/**
	 * <i>Maven Internal</i>: Project to interact with.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 * @noinspection UnusedDeclaration
	 */
	protected MavenProject project;

	/**
	 * execution mode
	 * 
	 * @parameter expression="${isCLIMode}" default-value="true"
	 */
	private boolean isCLIMode;

	private ProjectConfig projectConfig;
	private JdbcOption jdbcOption;

	// project meta information
	protected String templateHome;

	private static String COLUMN_NAME = "COLUMN_NAME";
	private static String TYPE_NAME = "TYPE_NAME";
	private static String COLUMN_SIZE = "COLUMN_SIZE";
	private static String NULLABLE = "NULLABLE";
	private static String FK_TABLE_NAME = "PKTABLE_NAME";
	private static String FK_COLUMN_NAME = "FKCOLUMN_NAME";

	private SourceCodeChecker sourceCodeChecker = new SourceCodeChecker();

	private Map<String, String> typeMapper = new HashMap<String, String>();

	private VelocityEngine velocity;
	private Connection conn;

	private Set<String> importList = new HashSet<String>();

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			setConfiguration();
			checkInstalledPlugins(new String[] { CommonConstants.CORE_PLUGIN });

			// mapping column type with java type
			typeMapper = GenerationUtil.getTypeMappingConfig(templateHome);

			if (isCLIMode) {
				sourceCodeChecker.checkExistingModel(this.isCLIMode, this.prompter, projectConfig, this.projectHome, this.packageName, this.table);
			}

			String[] tables = null;
			if(table.equals("*")){
				tables = DBUtil.getTableList(jdbcOption);
			}else if(table.indexOf(",") > 0){
				tables = table.split(",");
			}
			
			String strTable = table;
			
			conn = DBUtil.getConnection(jdbcOption.getDriverJar(), jdbcOption.getDriverClassName(), jdbcOption.getUrl(),
					jdbcOption.getUserName(), jdbcOption.getPassword());
			
			if(tables != null){
				for(int i=0; i<tables.length; i++){
					strTable = tables[i];
					Domain targetDomain = generateModel(strTable);
					executeGenerate(targetDomain);
				}
			}else{
				Domain targetDomain = generateModel(strTable);
				executeGenerate(targetDomain);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			getLog().error("Fail to execute GenerateModelMojo. The reason is '" + ex.getMessage() + "'.");
			throw new MojoFailureException(null);
		}
	}

	private void setConfiguration() throws Exception {
		// 1. get project configuration
		String configFile = ConfigXmlUtil.getCommonConfigFile(baseDir.getAbsolutePath());
		projectConfig = ConfigXmlUtil.getProjectConfig(configFile);
		jdbcOption = ConfigXmlUtil.getDefaultDatabase(projectConfig);

		// 2. set member variables from project configuration
		if(StringUtils.isEmpty(templateHome)){
			this.templateHome = projectConfig.getTemplatePath(CommonConstants.PROJECT_NAME_CODE_GENERATOR);
		}

		// 3. package default setting
		if (StringUtils.isEmpty(packageName)) {
			this.packageName = projectConfig.getPackageName() + ".domain";
		}
	}

	public Domain generateModel(String strTable) throws Exception {
		// init importList
		importList = new HashSet<String>();
		
		List<Column> targetColumns = new ArrayList<Column>();
		Map<String, String> pkMap = new HashMap<String, String>();
		Map<String, String> fkMap = new HashMap<String, String>();

		DatabaseMetaData dmeta = conn.getMetaData();

		ResultSet primaryKeys = dmeta.getPrimaryKeys(null, jdbcOption.getSchema(), strTable);
		while (primaryKeys.next()) {
			pkMap.put(primaryKeys.getString(COLUMN_NAME), primaryKeys.getString(COLUMN_NAME));
		}
		
		ResultSet foreignKeys = dmeta.getImportedKeys(null, jdbcOption.getSchema(), strTable);
		while (foreignKeys.next()) {
			fkMap.put(foreignKeys.getString(FK_COLUMN_NAME), foreignKeys.getString(FK_TABLE_NAME));
			// remove from pkMap
			pkMap.remove(foreignKeys.getString(FK_COLUMN_NAME));
		}

		ResultSet columns = dmeta.getColumns(null, jdbcOption.getSchema(), strTable, "%");

		while (columns.next()) {
			Column column = new Column();
			column.setColumnName(columns.getString(COLUMN_NAME));
			column.setColumnType(columns.getString(TYPE_NAME));
			column.setLength(String.valueOf(columns.getInt(COLUMN_SIZE)));
			column.setFieldName(underscoreToVariable(columns.getString(COLUMN_NAME)));
			
			String fieldType = typeMapper.get(columns.getString(TYPE_NAME));
			if(fieldType == null || "".equals(fieldType)){
				fieldType = Object.class.getName(); // defaultType = Object
			}
			column.setFieldType(fieldType);
			
			if (pkMap.get(columns.getString(COLUMN_NAME)) != null) {
				column.setIsKey(true);
			} else {
				column.setIsKey(false);
			}
			
			if (fkMap.get(columns.getString(COLUMN_NAME)) != null) {
				column.setFkey(true);
				column.setFkTable(fkMap.get(columns.getString(COLUMN_NAME)));
			} else {
				column.setFkey(false);
			}
			
			int nullable = columns.getInt(NULLABLE);
			if (nullable == DatabaseMetaData.columnNullable) {
				column.setNotNull(false);
			} else {
				column.setNotNull(true);
			}

			if (column.getFieldType().indexOf(".") > 0) {
				importList.add(column.getFieldType());
			}
			targetColumns.add(column);
		}

		primaryKeys.close();
		foreignKeys.close();
		columns.close();
		
		Domain targetDomain = new Domain();
		targetDomain.setTable(strTable);
		targetDomain.setName(underscoreToCamel(strTable));
		targetDomain.setPackageName(packageName);
		targetDomain.setColumns(targetColumns);

		return targetDomain;
	}

	public void executeGenerate(Domain targetDomain) throws Exception {
		velocity = VelocityUtil.initializeFileResourceVelocity(templateHome);

		String packageLocation = targetDomain.getPackageName().replace(".", CommonConstants.fileSeparator);

		Collections.sort(new ArrayList<String>(importList));

		Context context = new VelocityContext();
		context.put("domain", targetDomain);
		context.put("package", targetDomain.getPackageName());
		context.put("StringUtils", new StringUtils());
		context.put("importList", importList);
		context.put("author", System.getProperty("user.name"));

		String targetPath = baseDir.getAbsolutePath() + CommonConstants.SRC_MAIN_JAVA + packageLocation;
		FileUtil.makeDir(targetPath, "");
		
		String target = targetPath + CommonConstants.fileSeparator + targetDomain.getName() + ".java";
		String template = "common" + CommonConstants.fileSeparator + "vo.vm";

		try {
			VelocityUtil.mergeTemplate(velocity, context, template, new File(target), "UTF-8");
		} catch (Exception e) {
			getLog().warn("Merging of " + target + " with template" + " is skipped. The reason is a '" + e.getMessage() + "'.");
		}
	}

	/**
	 * Check that the any plugin was installed.
	 * 
	 * @throws Exception
	 */
	protected void checkInstalledPlugins(String[] pluginNames) throws Exception {
		Map<String, PluginInfo> installedPlugins = pluginInfoManager.getInstalledPlugins(baseDir.getAbsolutePath());
		if (installedPlugins.size() == 0) {
			throw new CommandException("Can not find any installed plugin information. Please install any plugin at the very first.");
		}

		boolean isInstalled = false;

		StringBuffer uninstallPluginsNames = new StringBuffer();
		for (String pluginName : pluginNames) {
			if (installedPlugins.containsKey(pluginName)) {
				isInstalled = true;
			} else {
				uninstallPluginsNames.append("'" + pluginName + "',");
			}
		}

		if (!isInstalled) {
			throw new CommandException("Can not find installed plugin ["
					+ uninstallPluginsNames.toString().substring(0, uninstallPluginsNames.toString().length() - 1)
					+ "] information. Please install those plugins at the very first.");
		}
	}

	private String underscoreToVariable(String s) {
		String camelCaseString = underscoreToCamel(s);
		return camelCaseString.substring(0, 1).toLowerCase() + camelCaseString.substring(1);
	}

	private String underscoreToCamel(String s) {
		String camelCaseString = "";
		String[] parts = s.split("_");
		for (String part : parts) {
			camelCaseString = camelCaseString + part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase();
		}
		return camelCaseString;
	}

	// getter, setter for ANT Task
	public void setTable(String table) {
		this.table = table;
	}
	
	public void setTemplateHome(String templateHome) {
		this.templateHome = templateHome;
	}

	public void setProjectHome(String projectHome) {
		this.projectHome = projectHome;
	}

	public void setMavenProject(MavenProject project) {
		this.project = project;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setCLIMode(boolean isCLIMode) {
		this.isCLIMode = isCLIMode;
	}

	public void setPrompter(Prompter prompter) {
		this.prompter = prompter;
	}

	public void setPluginInfoManager(PluginInfoManager pluginInfoManager) {
		this.pluginInfoManager = pluginInfoManager;
	}
}
