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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.anyframe.ide.command.cli.util.Messages;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.DBUtil;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * This is a SourceCodeChecker class to check existing model classes and crud
 * codes.
 * 
 * @author SooYeon Park
 * 
 */
public class SourceCodeChecker {

	/**
	 * check existing model files
	 * 
	 * @param isCLIMode
	 *            whether current mode is cli mode or not
	 * @param prompter
	 *            prompter to interact with user input
	 * @param pio
	 *            project information properties
	 * @param projectHome
	 *            project root path
	 * @param packageName
	 *            target package name to generate model files
	 * @param table
	 *            db table names
	 * @return messages to display
	 * @throws Exception
	 */
	public String checkExistingModel(boolean isCLIMode, Prompter prompter,
			PropertiesIO pio, String projectHome, String packageName,
			String table) throws Exception {

		String[] domainList = new String[0];
		if (table.equals("*")) {
			domainList = DBUtil.getTableListAsDomainName(pio);
		} else {
			domainList = parseTableNameWithComma(table);
		}

		StringBuffer sb = new StringBuffer();
		boolean isExist = false;
		for (int i = 0; i < domainList.length; i++) {
			if (new File(projectHome + "/src/main/java/"
					+ packageName.replaceAll("\\.", "/") + "/" + domainList[i]
					+ ".java").exists()) {
				sb.append("/src/main/java/"
						+ packageName.replaceAll("\\.", "/") + "/"
						+ domainList[i] + ".java" + "\n");
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
	 * @param basePackage
	 *            base package name
	 * @param packageName
	 *            target package name
	 * @param entityClassName
	 *            entity class name
	 * @param scope
	 *            generation scope(all,service,web)
	 * @param daoframework
	 *            dao framework name
	 * @return messages to display
	 * @throws Exception
	 */
	public String checkExistingCrud(boolean isCLIMode, Prompter prompter,
			String templateType, String templateHome, String projectHome,
			String basePackage, String packageName, String entityClassName,
			String scope, String daoframework) throws Exception {
		StringBuffer sb = new StringBuffer();
		boolean isExist = false;
		ArrayList<String> templateFileList = getTemplateFileList(templateType,
				templateHome, packageName, entityClassName, scope, daoframework);

		for (int i = 0; i < templateFileList.size(); i++) {
			if (new File(projectHome, templateFileList.get(i)).exists()) {
				sb.append(templateFileList.get(i) + "\n");
				isExist = true;
			}
		}

		ArrayList<HashMap> mergeFileList = getMergeTemplateFileList(
				templateType, templateHome, basePackage + ".domain",
				entityClassName, scope);

		for (int i = 0; i < mergeFileList.size(); i++) {
			HashMap hm = mergeFileList.get(i);
			String mergeSrc = (String) hm.get("mergeSrc");
			String mergeKey = (String) hm.get("mergeKey");

			if (isSrcFileContainsKey(projectHome, mergeSrc, mergeKey)) {
				sb.append(mergeSrc + "\n");
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
			domainName += token.substring(0, 1).toUpperCase()
					+ token.substring(1).toLowerCase();
		}
		return domainName;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<String> getTemplateFileList(String templateType,
			String templateHome, String basePackage, String domainName,
			String scope, String daoFramework) throws JDOMException,
			IOException {
		ArrayList<String> templateFileList = new ArrayList<String>();

		File file = new File(templateHome + CommonConstants.fileSeparator
				+ templateType + CommonConstants.fileSeparator + "source"
				+ CommonConstants.fileSeparator + "template.config");
		Document doc = new SAXBuilder().build(file);
		List<Element> children = doc.getRootElement().getChildren();

		String domainNameLower = domainName.substring(0, 1).toLowerCase()
				+ domainName.substring(1);

		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			if (scope.equals("all") || child.getChildText("type").equals(scope)) {
				String src = child.getChildText("src");
				src = src.replaceAll("\\{class-name\\}", domainName);
				src = src.replaceAll("\\{class-name-lower\\}", domainNameLower);
				src = src.replaceAll("\\{basepkg-name\\}", basePackage
						.replaceAll("\\.", "/").toLowerCase());

				if (child.getChild("dao") != null) {
					if (!child.getChildText("dao").equals(daoFramework)) {
						continue;
					}
				}

				templateFileList.add(src);
			}
		}
		return templateFileList;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<HashMap> getMergeTemplateFileList(String templateType,
			String templateHome, String modelPackage, String domainName,
			String scope) throws JDOMException, IOException {
		ArrayList<HashMap> templateFileList = new ArrayList<HashMap>();

		File file = new File(templateHome + CommonConstants.fileSeparator
				+ templateType + CommonConstants.fileSeparator + "source"
				+ CommonConstants.fileSeparator + "template.config");
		Document doc = new SAXBuilder().build(file);
		List<Element> children = doc.getRootElement().getChildren();

		String domainNameLower = domainName.substring(0, 1).toLowerCase()
				+ domainName.substring(1);

		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			if (scope.equals("all") || child.getChildText("type").equals(scope)) {
				String mergeSrc = child.getChildText("mergeSrc");
				if (mergeSrc == null || mergeSrc.trim().equals("")) {
					continue;
				}

				String mergeKey = child.getChildText("mergeKey");
				mergeKey = mergeKey.replaceAll("\\{class-name\\}", domainName);
				mergeKey = mergeKey.replaceAll("\\{class-name-lower\\}",
						domainNameLower);
				mergeKey = mergeKey.replaceAll("\\{model-package\\}",
						modelPackage);

				HashMap hm = new HashMap<String, String>();
				hm.put("mergeSrc", mergeSrc);
				hm.put("mergeKey", mergeKey);
				templateFileList.add(hm);
			}
		}

		HashMap hm = new HashMap<String, String>();
		hm.put("mergeSrc", "src/main/resources/spring/context-hibernate.xml");
		hm.put("mergeKey", modelPackage + "." + domainName);
		templateFileList.add(hm);

		hm = new HashMap<String, String>();
		hm.put("mergeSrc",
				"src/main/webapp/miplatform/Anyframe_MiP_Sample_sdi.xml");
		hm.put("mergeKey", "!--Miplatform " + domainNameLower
				+ "Service-START--");
		templateFileList.add(hm);

		return templateFileList;
	}

	private boolean isSrcFileContainsKey(String projectHome, String mergeSrc,
			String mergeKey) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					new File(projectHome, mergeSrc))));
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
