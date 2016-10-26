/*
 * Copyright 2008-2013 the original author or authors.
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

import java.io.File;

import org.anyframe.ide.command.common.util.AntUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.tools.ant.Project;

/**
 * This is a AnyframeCodeMergeHelper class.
 * 
 * @author Sujeong Lee
 */
public class AnyframeCodeMergeHelper {

	private Project antProject;
	private String domainName;

	private static String ANYFRAME_JSP = "anyframe.jsp";
	private static String MESSAGE_PROPERTIES = "message-generation.properties";
	private static String HIBERNATE_CFG_XML = "hibernate.cfg.xml";
	private static String HIBERNATE_CONTEXT_XML = "context-hibernate.xml";
	private static String IBATIS2_SQLMAP_XML = "SqlMapConfig.xml";
	private static String MYBATIS_CONFIG_XML = "mybatis-config.xml";
	private static String MIP_QUERY_CONTROLLER_BEAN_XML = "mip-query-generation-servlet.xml";
	private static String MIP_QUERY_SDI_XML = "mip_query_sdi.xml";
	private static String XP_QUERY_CONTROLLER_BEAN_XML = "xp-query-generation-servlet.xml";
	private static String XP_QUERY_TYPEDEF_XML = "default_typedef.xml";

	private static String ANYFRAME_JSP_REPLACE = "<!--Add new crud generation menu here-->";
	private static String MESSAGE_PROPERTIES_REPLACE = "# -- Add new messages here";
	private static String HIBERNATE_CFG_XML_REPLACE = "<!--Add new mapping class name here-->";
	private static String HIBERNATE_FILE_CONTEXT_XML_REPLACE = "<!--Add new file name here-->";
	private static String IBATIS2_SQLMAP_XML_REPLACE = "<!--Add new file name here-->";
	private static String MYBATIS_CONFIG_XML_REPLACE = "<!--Add new alias here-->";
	private static String MIP_QUERY_SDI_XML_REPLACE = "<!--new miplatform service group xml here-->";
	private static String MIP_QUERY_CONTROLLER_BEAN_XML_REPLACE = "<!--Add additional controller beans here-->";
	private static String XP_QUERY_CONTROLLER_BEAN_XML_REPLACE = "<!--Add additional controller beans here-->";
	private static String XP_QUERY_TYPEDEF_XML_REPLACE = "<!--new xplatform service group xml here-->";

	private static String XML_START = "<!--";
	private static String XML_END = "-->";
	private static String PROPERTY_START = "# -- ";
	private static String PROPERTY_END = "-";
	private static String START = "-START";
	private static String END = "-END";

	public void merge(AnyframeTemplateData info, String domainName, String originalTarget, String tempTarget, String templateType) throws Exception {
		this.setDomainName(domainName);
		setAntProject();

		File targetFile = new File(originalTarget);
		targetFile.getParentFile().mkdirs();

		mergeCommon(info, originalTarget, tempTarget);

		if (templateType.equals(CommonConstants.DAO_SPRINGJDBC)) {
			mergeSpringJdbc(info, originalTarget, tempTarget);
		} else if (templateType.equals(CommonConstants.QUERY_PLUGIN)) {
			mergeQuery(info, originalTarget, tempTarget);
		} else if (templateType.equals(CommonConstants.HIBERNATE_PLUGIN)) {
			mergeHibernate(info, originalTarget, tempTarget);
		} else if (templateType.equals(CommonConstants.IBATIS2_PLUGIN)) {
			mergeIbatis2(info, originalTarget, tempTarget);
		} else if (templateType.equals(CommonConstants.MYBATIS_PLUGIN)) {
			mergeMybatis(info, originalTarget, tempTarget);
		} else if (templateType.equals(CommonConstants.MIP_QUERY_PLUGIN)) {
			mergeMipQuery(info, originalTarget, tempTarget);
		} else if (templateType.equals(CommonConstants.XP_QUERY_PLUGIN)) {
			mergeXpQuery(info, originalTarget, tempTarget);
		} else {
			// N/A
		}

	}

	private void mergeCommon(AnyframeTemplateData info, String originalTarget, String tempTarget) throws Exception {
		// 1. anyframe.jsp
		if (originalTarget.endsWith(ANYFRAME_JSP)) {
			executeLoadFileAndReplaceXml(originalTarget, tempTarget, ANYFRAME_JSP_REPLACE, "anyframe.jsp");
		}

		// 2. message-generation.properties
		if (originalTarget.endsWith(MESSAGE_PROPERTIES)) {
			executeLoadFileAndReplaceProperty(originalTarget, tempTarget, MESSAGE_PROPERTIES_REPLACE, "message.properties");
		}
	}

	private void mergeSpringJdbc(AnyframeTemplateData info, String originalTarget, String tempTarget) throws Exception {
	}

	private void mergeQuery(AnyframeTemplateData info, String originalTarget, String tempTarget) throws Exception {
	}

	private void mergeHibernate(AnyframeTemplateData info, String originalTarget, String tempTarget) throws Exception {
		// 1. context-hibernate.xml
		if (originalTarget.endsWith(HIBERNATE_CONTEXT_XML)) {
			executeLoadFileAndReplaceXml(originalTarget, tempTarget, HIBERNATE_FILE_CONTEXT_XML_REPLACE, "hibernate.file");
		}

		// 2. hibernate.cfg.xml
		if (originalTarget.endsWith(HIBERNATE_CFG_XML)) {
			executeLoadFileAndReplaceXml(originalTarget, tempTarget, HIBERNATE_CFG_XML_REPLACE, "hibernate.cfg");
		}
	}

	private void mergeIbatis2(AnyframeTemplateData info, String originalTarget, String tempTarget) throws Exception {
		// 1. SqlMapConfig.xml
		if (originalTarget.endsWith(IBATIS2_SQLMAP_XML)) {
			executeLoadFileAndReplaceXml(originalTarget, tempTarget, IBATIS2_SQLMAP_XML_REPLACE, "ibatis2.sqlmap");
		}
	}

	private void mergeMybatis(AnyframeTemplateData info, String originalTarget, String tempTarget) throws Exception {
		// 1. mybatis-config.xml
		if (originalTarget.endsWith(MYBATIS_CONFIG_XML)) {
			executeLoadFileAndReplaceXml(originalTarget, tempTarget, MYBATIS_CONFIG_XML_REPLACE, "mybatis.config");
		}
	}

	private void mergeMipQuery(AnyframeTemplateData info, String originalTarget, String tempTarget) throws Exception {
		// 1. mip-query-generation-servlet.xm
		if (originalTarget.endsWith(MIP_QUERY_CONTROLLER_BEAN_XML)) {
			executeLoadFileAndReplaceXml(originalTarget, tempTarget, MIP_QUERY_CONTROLLER_BEAN_XML_REPLACE, "mip.servlet");
		}
		// 2. mip_query_sdi.xml
		if (originalTarget.endsWith(MIP_QUERY_SDI_XML)) {
			executeLoadFileAndReplaceXml(originalTarget, tempTarget, MIP_QUERY_SDI_XML_REPLACE, "mip.sdi");
		}
	}

	private void mergeXpQuery(AnyframeTemplateData info, String originalTarget, String tempTarget) throws Exception {
		// 1. xp-query-generation-servlet.xml
		if (originalTarget.endsWith(XP_QUERY_CONTROLLER_BEAN_XML)) {
			executeLoadFileAndReplaceXml(originalTarget, tempTarget, XP_QUERY_CONTROLLER_BEAN_XML_REPLACE, "xp.servlet");
		}
		// 2. default_typedef.xml
		if (originalTarget.endsWith(XP_QUERY_TYPEDEF_XML)) {
			executeLoadFileAndReplaceXml(originalTarget, tempTarget, XP_QUERY_TYPEDEF_XML_REPLACE, "xp.typedef");
		}
	}

	private void executeLoadFileAndReplaceXml(String originalFile, String tempFile, String token, String propName) throws Exception {
		AntUtil.executeLoadFileTask(antProject, tempFile, propName);
		AntUtil.executeReplaceRegExpTask(antProject, new File(originalFile), XML_START + getDomainName() + START + XML_END, XML_START
				+ getDomainName() + END + XML_END, "", token, antProject.getProperty(propName));
	}

	private void executeLoadFileAndReplaceProperty(String originalFile, String tempFile, String token, String propName) throws Exception {
		AntUtil.executeLoadFileTask(antProject, tempFile, propName);
		AntUtil.executeReplaceRegExpTask(antProject, new File(originalFile), PROPERTY_START + getDomainName() + START + PROPERTY_END, PROPERTY_START
				+ getDomainName() + END + PROPERTY_END, "", token, antProject.getProperty(propName));
	}

	public void setAntProject() {
		this.antProject = new Project();
		antProject.init();
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
}
