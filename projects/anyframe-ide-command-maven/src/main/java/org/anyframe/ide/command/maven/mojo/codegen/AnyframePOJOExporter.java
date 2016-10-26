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

import java.io.File;
import java.util.Map;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.ConfigXmlUtil;
import org.anyframe.ide.command.common.util.JdbcOption;
import org.anyframe.ide.command.common.util.ProjectConfig;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.hibernate.tool.hbm2x.TemplateProducer;
import org.hibernate.tool.hbm2x.pojo.POJOClass;

/**
 * This is an AnyframePOJOExporter class to generate pojo class based on model
 * template files.
 * 
 * @author Sooyeon Park
 */
public class AnyframePOJOExporter extends POJOExporter {

	// private PropertiesIO pio = null;
	//
	// public void setPropertiesIO(PropertiesIO pio) {
	// this.pio = pio;
	// }

	private ProjectConfig projectConfig = null;

	public void setProjectConfig(ProjectConfig projectConfig) {
		this.projectConfig = projectConfig;
	}

	@Override
	public void start() {
		setTemplateHelper(new AnyframeTemplateHelper());
		setupTemplates();
		setupContext();
		doStart();
		cleanUpContext();
		setTemplateHelper(null);
		getArtifactCollector().formatFiles();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void exportPOJO(Map additionalContext, POJOClass element) {
		TemplateProducer producer = new AnyframeTemplateProducer(getTemplateHelper(), getArtifactCollector());
		additionalContext.put("pojo", element);
		additionalContext.put("clazz", element.getDecoratedObject());

		AnyframeDBData dbdata = new AnyframeDBData(getCfg2JavaTool(), getCfg2HbmTool(), this);
		additionalContext.put("dbdata", dbdata);

		// if sybase db, hibernate dao
		if (projectConfig != null) {
			JdbcOption jdbcOption = new JdbcOption();
			try {
				jdbcOption = ConfigXmlUtil.getDefaultDatabase(projectConfig);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// TODO DAO Type 선택
			// String daoframework = pio
			// .readValue(CommonConstants.APP_DAOFRAMEWORK_TYPE);
			String daoframework = "";
			String dbType = jdbcOption.getDbType();
			if (dbType.equals("sybase") && daoframework.equals("hibernate"))
				additionalContext.put("dbschema", true);
			else
				additionalContext.put("dbschema", false);
		} else
			additionalContext.put("dbschema", false);
		String filename = resolveFilename(element);
		if (filename.endsWith(".java") && filename.indexOf('$') >= 0) {
			log.warn("Filename for " + getClassNameForFile(element) + " contains a $. Innerclass generation is not supported.");
		}
		producer.produce(additionalContext, getTemplateName(), new File(getOutputDirectory(), filename), getTemplateName(), element.toString());
	}
}
