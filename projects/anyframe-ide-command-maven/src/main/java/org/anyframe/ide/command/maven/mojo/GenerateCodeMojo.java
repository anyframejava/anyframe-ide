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
import java.util.List;
import java.util.Map;

import org.anyframe.ide.command.common.CommandException;
import org.anyframe.ide.command.common.PluginInfoManager;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.AntUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.ConfigXmlUtil;
import org.anyframe.ide.command.common.util.FileUtil;
import org.anyframe.ide.command.common.util.JdbcOption;
import org.anyframe.ide.command.common.util.ProjectConfig;
import org.anyframe.ide.command.common.util.VelocityUtil;
import org.anyframe.ide.command.maven.mojo.codegen.AnyframeCodeMergeHelper;
import org.anyframe.ide.command.maven.mojo.codegen.AnyframeDomainParser;
import org.anyframe.ide.command.maven.mojo.codegen.AnyframeTemplateData;
import org.anyframe.ide.command.maven.mojo.codegen.Domain;
import org.anyframe.ide.command.maven.mojo.codegen.SourceCodeChecker;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.codehaus.plexus.components.interactivity.Prompter;

/**
 * This is an AnyframeCodeGenerator class.
 * 
 * @goal create-crud
 * @author Matt Raible
 * @author modified by SooYeon Park
 * @author modified by Sujeong Lee
 */
public class GenerateCodeMojo extends AbstractPluginMojo {
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
	 * The entity's home.
	 * 
	 * @parameter expression="${entity}"
	 * @required
	 */
	private String entity = "";

	/**
	 * The project's home.
	 * 
	 * @parameter expression="${projecthome}" default-value="${basedir}"
	 */
	protected String projectHome = "";

	/**
	 * The generated code's package.
	 * 
	 * @parameter expression="${package}"
	 */
	private String packageName;

	/**
	 * The generated code's templateType.
	 * 
	 * @parameter expression="${templateType}"
	 */
	private String templateType = "";

	/**
	 * The scope.
	 * 
	 * @parameter expression="${scope}" default-value="all"
	 */
	private String scope;

	/**
	 * input sample data to database
	 * 
	 * @parameter expression="${insertSampleData}" default-value="true"
	 */
	private boolean insertSampleData;

	/**
	 * execution mode
	 * 
	 * @parameter expression="${isCLIMode}" default-value="true"
	 */
	private boolean isCLIMode;

	/**
	 * project's name
	 * 
	 * @parameter expression="${projectName}" default-value="${project.groupId}"
	 */
	private String projectName;

	private ProjectConfig projectConfig;
	private JdbcOption jdbcOption;

	// project meta information
	private String templateHome;
	private String basePackage;

	// etc.
	private String modelpackage = "";
	private String entityClassName = "";
	private String packaging = "war";

	private SourceCodeChecker sourceCodeChecker = new SourceCodeChecker();

	private VelocityEngine velocity;

	/**
	 * main method for executing GenerateCodeMojo. This mojo is executed when
	 * you input 'mvn anyframe:create-crud [-options]'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			setConfiguration();
			checkInstalledPlugins(new String[] { CommonConstants.CORE_PLUGIN });

			if (isCLIMode) {
				sourceCodeChecker.checkExistingCrud(this.isCLIMode, this.prompter, this.templateType, this.templateHome, this.projectHome,
						this.basePackage, this.packageName, this.entityClassName, this.scope);
			}

			// get target domain with sample data contains
			Domain targetDomain = generateDomain();

			// file generate
			executeGenerate(targetDomain);

			// insert sample data
			if (insertSampleData) {
				insertInitialSampleData();
			}

			postExecute();

			System.out.println("CRUD source codes for the given domain name are generated successfully.");
		} catch (Exception ex) {
			ex.printStackTrace();
			getLog().error("Fail to execute GenerateCodeMojo. The reason is '" + ex.getMessage() + "'.");
			throw new MojoFailureException(null);
		}
	}

	private void setConfiguration() throws Exception {
		// 1. read project configuration
		String configFile = ConfigXmlUtil.getCommonConfigFile(baseDir.getAbsolutePath());
		projectConfig = ConfigXmlUtil.getProjectConfig(configFile);
		jdbcOption = ConfigXmlUtil.getDefaultDatabase(projectConfig);

		// 2. set member variables from configuration
		if (StringUtils.isEmpty(templateHome)) {
			this.templateHome = projectConfig.getTemplatePath(CommonConstants.PROJECT_NAME_CODE_GENERATOR);
		}
		this.basePackage = projectConfig.getPackageName();

		getLog().info("Scope for generation code is automatically set to 'all'.");
		this.packaging = "war";

		int lastDotIndex = this.entity.lastIndexOf(".");
		this.modelpackage = (lastDotIndex != -1) ? entity.substring(0, lastDotIndex) : this.basePackage + ".domain";
		this.entityClassName = lastDotIndex != -1 ? entity.substring(lastDotIndex + 1) : entity;
		if (StringUtils.isEmpty(this.packageName)) {
			this.packageName = this.basePackage + "." + this.entity.toLowerCase();
		}

		// 3. check temporary directory
		checkTemporaryDirectory();
	}

	public Domain generateDomain() throws Exception {
		String simpleEntity = entity;
		if (simpleEntity.indexOf(".") > 0) {
			simpleEntity = entity.substring(entity.lastIndexOf(".") + 1);
		}
		
		String targetDomainFile = this.baseDir.getAbsolutePath() + CommonConstants.SRC_MAIN_JAVA
				+ this.modelpackage.replace(".", CommonConstants.fileSeparator) + CommonConstants.fileSeparator + simpleEntity + ".java";

		String targetEntity = entity;
		if (targetEntity.indexOf(".") < 0) {
			targetEntity = modelpackage + "." + targetEntity;
		}

		return AnyframeDomainParser.parse(targetDomainFile, targetEntity, jdbcOption, templateHome);
	}

	private void executeGenerate(Domain targetDomain) throws Exception {

		List<AnyframeTemplateData> templateInfoList = sourceCodeChecker.getTemplateList(templateType, templateHome, this.basePackage,
				this.packageName, this.entityClassName, scope);

		velocity = VelocityUtil.initializeFileResourceVelocity(templateHome);

		Context context = new VelocityContext();
		context.put("package", packageName);
		context.put("domain", targetDomain);
		context.put("author", System.getProperty("user.name"));

		for (AnyframeTemplateData info : templateInfoList) {
			String template = "";
			String target = this.baseDir.getAbsolutePath() + CommonConstants.fileSeparator + info.getSrc();

			String vmFile = info.getVm();
			if (Boolean.valueOf(info.getCommon())) {
				template = "common" + CommonConstants.fileSeparator + vmFile;
			} else {
				template = templateType + CommonConstants.fileSeparator + vmFile;
			}

			boolean isMerge = Boolean.valueOf(info.getMerge());
			String tempTarget = this.baseDir.getAbsolutePath() + CommonConstants.fileSeparator + ".temp" + CommonConstants.fileSeparator
					+ info.getSrc();
			String originalTarget = target;

			if (isMerge) {
				// merge type template backup original target file name make
				// temp file
				target = tempTarget;
				File originalTargetFile = new File(originalTarget);
				originalTargetFile.getParentFile().mkdirs();
			}

			File targetFile = new File(target);
			targetFile.getParentFile().mkdirs();

			try {
				VelocityUtil.mergeTemplate(velocity, context, template, new File(target), "UTF-8");
			} catch (Exception e) {
				getLog().warn("Merging of " + target + " with template" + " is skipped. The reason is a '" + e.getMessage() + "'.");
			}

			if (isMerge) {
				// temp file copy, replace
				try {
					AnyframeCodeMergeHelper helper = new AnyframeCodeMergeHelper();
					helper.merge(info, targetDomain.getName(), originalTarget, target, templateType);
				} catch (Exception e) {
					// TODO merge fail
					e.printStackTrace();
				}
			}
		}
	}

	public void insertInitialSampleData() throws Exception {
		File sampleDataFile = new File(this.projectHome, "/src/test/resources/sample-data.xml");

		if (sampleDataFile.exists()) {
			try {
				AntUtil.executeDbUnitTask(this.jdbcOption.getDbType().toLowerCase(), this.jdbcOption.getUrl(), this.jdbcOption.getDriverClassName(),
						this.jdbcOption.getUserName(), this.jdbcOption.getPassword(), this.jdbcOption.getSchema(), sampleDataFile, this.projectHome);
			} catch (Exception e) {
				getLog().warn("Inserting sample data using DBUnit is skipped. The reason is '" + e.getMessage() + "'.");
			}
		}
	}

	public void postExecute() throws Exception {
		String genLocation = this.projectHome + CommonConstants.fileSeparator + ".temp";
		FileUtil.deleteDir(new File(genLocation));
	}

	public Class classForName(String name) throws ClassNotFoundException {
		try {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			if (contextClassLoader != null) {
				return contextClassLoader.loadClass(name);
			}
		} catch (Throwable ignore) {
		}
		return Class.forName(name);
	}

	private void checkTemporaryDirectory() {
		String temp = this.projectHome + CommonConstants.fileSeparator + ".temp";
		File tempFolder = new File(temp);
		try {
			if (!tempFolder.exists()) {
				if (tempFolder.mkdir())
					getLog().debug("Temporary directory to generate source codes is created in " + tempFolder.getAbsolutePath());
				else
					getLog().debug("Temporary directory to generate source codes is not created in " + tempFolder.getAbsolutePath());
			} else {
				FileUtil.deleteDir(tempFolder);
			}
		} catch (Exception e) {
			getLog().warn("Check temporary directory : " + e.getMessage());
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

	// getter, setter for ANT Task
	public void setEntity(String entity) {
		this.entity = entity;
	}

	public void setProjectHome(String projectHome) {
		this.projectHome = projectHome;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public void setInsertSampleData(boolean insertSampleData) {
		this.insertSampleData = insertSampleData;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setPrompter(Prompter prompter) {
		this.prompter = prompter;
	}

	public void setCLIMode(boolean isCLIMode) {
		this.isCLIMode = isCLIMode;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	public void setTemplateHome(String templateHome) {
		this.templateHome = templateHome;
	}

	public void setPluginInfoManager(PluginInfoManager pluginInfoManager) {
		this.pluginInfoManager = pluginInfoManager;
	}
}
