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
package org.anyframe.ide.command.maven.mojo.codegen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.anyframe.ide.command.cli.util.Messages;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.ConfigXmlUtil;
import org.anyframe.ide.command.common.util.DBUtil;
import org.anyframe.ide.command.common.util.JdbcOption;
import org.anyframe.ide.command.common.util.ProjectConfig;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.components.interactivity.Prompter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;

/**
 * This is a SourceCodeChecker class to check existing model classes and crud
 * codes.
 * 
 * @author SooYeon Park
 * 
 */
public class SourceCodeChecker {

	private static String SOURCE_MERGE_KEY = "!--{class-name}-START--";
	private static String SOURCE_MERGE_KEY_PROPERTIES = "# -- {class-name}-START";

	/**
	 * check existing model files
	 * 
	 * @param isCLIMode
	 *            whether current mode is cli mode or not
	 * @param prompter
	 *            prompter to interact with user input
	 * @param projectConfig
	 *            project configuration
	 * @param projectHome
	 *            project root path
	 * @param packageName
	 *            target package name to generate model files
	 * @param table
	 *            db table names
	 * @return messages to display
	 * @throws Exception
	 */
	public String checkExistingModel(boolean isCLIMode, Prompter prompter, ProjectConfig projectConfig, String projectHome, String packageName,
			String table) throws Exception {

		JdbcOption jdbcOption = ConfigXmlUtil.getDefaultDatabase(projectConfig);

		String[] domainList = new String[0];
		if (table.equals("*")) {
			domainList = DBUtil.getTableListAsDomainName(jdbcOption);
		} else {
			domainList = parseTableNameWithComma(table);
		}

		StringBuffer sb = new StringBuffer();
		boolean isExist = false;
		for (int i = 0; i < domainList.length; i++) {
			if (new File(projectHome + "/src/main/java/" + packageName.replaceAll("\\.", "/") + "/" + domainList[i] + ".java").exists()) {
				sb.append("/src/main/java/" + packageName.replaceAll("\\.", "/") + "/" + domainList[i] + ".java" + "\n");
				isExist = true;
			}
		}

		StringBuffer message = new StringBuffer();
		message.append("----------------------------------------------- \n");
		message.append(sb.toString());
		message.append("-----------------------------------------------");
		message.append(Messages.CRUD_KEEP_OVERWRITE);

		if (isExist) {
			if (isCLIMode) {
				String answer = prompter.prompt(message.toString());

				if (answer.equalsIgnoreCase("n")) {
					System.out.println(Messages.USER_CANCEL);
					System.exit(0);
				}
			} else {
				return sb.toString();
			}
		}

		return null;
	}

	/**
	 * check existing crud files
	 * 
	 * @param isCLIMode
	 *            whether current mode is cli mode or not
	 * @param prompter
	 *            prompter to interact with user input
	 * @param templateType
	 *            selected template type
	 * @param templateHome
	 *            template root path
	 * @param projectHome
	 *            project root path
	 * @param packageName
	 *            target package name
	 * @param entityClassName
	 *            entity class name
	 * @param scope
	 *            generation scope(all,service,web)
	 * @return messages to display
	 * @throws Exception
	 */
	public String checkExistingCrud(boolean isCLIMode, Prompter prompter, String templateType, String templateHome, String projectHome,
			String basePackage, String packageName, String entityClassName, String scope) throws Exception {
		StringBuffer sb = new StringBuffer();
		boolean isExist = false;

		List<AnyframeTemplateData> templateList = getTemplateList(templateType, templateHome, basePackage, packageName, entityClassName, scope);

		for (int i = 0; i < templateList.size(); i++) {
			AnyframeTemplateData templateInfo = templateList.get(i);

			String msg = "";
			String src = templateInfo.getSrc();

			if (Boolean.valueOf(templateInfo.getMerge()) || templateInfo.getVm().startsWith("sample-data")) {
				String mergeKey = SOURCE_MERGE_KEY;
				if (src.endsWith(".properties")) {
					mergeKey = SOURCE_MERGE_KEY_PROPERTIES;
				}
				if (isSrcFileContainsKey(projectHome, src, mergeKey)) {
					msg = src;
				}
			} else {
				if (new File(projectHome, src).exists()) {
					msg = src;
				}
			}

			if (!StringUtils.isEmpty(msg)) {
				sb.append(msg + "\n");
				isExist = true;
			}
		}

		StringBuffer message = new StringBuffer();
		message.append("----------------------------------------------- \n");
		message.append(sb.toString());
		message.append("-----------------------------------------------");
		message.append(Messages.CRUD_KEEP_OVERWRITE);

		if (isExist) {
			if (isCLIMode) {
				String answer = prompter.prompt(message.toString());

				if (answer.equalsIgnoreCase("n")) {
					System.out.println(Messages.USER_CANCEL);
					System.exit(0);
				}
			} else {
				return sb.toString();
			}
		}

		return null;
	}

	private String[] parseTableNameWithComma(String tableName) {
		ArrayList<String> ret = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(tableName, ",");
		while (st.hasMoreElements()) {
			String token = (String) st.nextElement();
			ret.add(getDomainName(token.trim()));
		}
		return ret.toArray(new String[0]);
	}

	private String getDomainName(String tableName) {
		StringTokenizer st = new StringTokenizer(tableName.toUpperCase(), "_");
		String domainName = "";
		while (st.hasMoreElements()) {
			String token = (String) st.nextElement();
			domainName += token.substring(0, 1).toUpperCase() + token.substring(1).toLowerCase();
		}
		return domainName;
	}

	@SuppressWarnings("unchecked")
	public List<AnyframeTemplateData> getTemplateList(String templateType, String templateHome, String basePackage, String packageName, String domainName,
			String scope) throws Exception {
		File file = new File(templateHome + CommonConstants.fileSeparator + templateType + CommonConstants.fileSeparator + "template.config");
		XStream xstream = new XStream();
		Annotations.configureAliases(xstream, AnyframeTemplateData.class);
		xstream.setMode(XStream.NO_REFERENCES);

		List<AnyframeTemplateData> templates = (java.util.List<AnyframeTemplateData>) xstream.fromXML(new FileInputStream(file));
		String domainNameLower = domainName.substring(0, 1).toLowerCase() + domainName.substring(1);
		
		for (int i = 0; i < templates.size(); i++) {
			AnyframeTemplateData templateData = templates.get(i);
			if (validScope(scope, templateData.getType())) {
				String src = templateData.getSrc();
				src = src.replaceAll("\\{class-name\\}", domainName);
				src = src.replaceAll("\\{class-name-lower\\}", domainNameLower);
				String basePkg = basePackage;
				if(!Boolean.valueOf(templateData.getMerge())){
					basePkg = packageName;
				}
				src = src.replaceAll("\\{basepkg-name\\}", basePkg.replaceAll("\\.", "/").toLowerCase());
				
				templateData.setSrc(src);
			}
		}
		return templates;
	}

	private boolean validScope(String scope, String templateType) {
		String[] serviceTypes = {"dao", "service"};
		if(scope.equals("all")){
			return true;
		}else if(scope.equals("service") && Arrays.asList(serviceTypes).contains(templateType)){
			return true;
		}
		return false;
	}

	private boolean isSrcFileContainsKey(String projectHome, String mergeSrc, String mergeKey) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(projectHome, mergeSrc))));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.indexOf(mergeKey) != -1) {
					return true;
				}
			}

		} catch (Exception e) {
			if (br != null)
				try {
					br.close();
				} catch (Exception ex) {
					// ignore
				}
		}
		return false;
	}
}
