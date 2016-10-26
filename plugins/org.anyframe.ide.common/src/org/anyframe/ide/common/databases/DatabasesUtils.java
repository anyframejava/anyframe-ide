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
package org.anyframe.ide.common.databases;

import org.anyframe.ide.common.messages.Message;
import org.anyframe.ide.common.util.EncryptUtil;
import org.anyframe.ide.common.util.MessageUtil;
import org.anyframe.ide.common.util.PropertyHandler;
import org.anyframe.ide.common.util.StringUtil;
import org.eclipse.core.resources.IProject;

/**
 * This is DatabasesUtils class.
 * 
 * @author Sujeong Lee
 */
public class DatabasesUtils {

	/**
	 * import jdbc setting from old properties
	 */
	@Deprecated
	public static JdbcOption getJdbcOptionFromOldProperties(IProject project) {
		JdbcOption jdbc = new JdbcOption();

		PropertyHandler prop = new PropertyHandler(project);

		jdbc.setDriverJar(prop.getProperty(DatabasesConstants.DRIVER));
		jdbc.setDriverClassName(prop
				.getProperty(DatabasesConstants.DRIVER_CLASS_NAME));
		jdbc.setUrl(prop.getProperty(DatabasesConstants.URL));
		jdbc.setUserName(prop.getProperty(DatabasesConstants.USER_NAME));
		jdbc.setPassword(EncryptUtil.decrypt(prop
				.getProperty(DatabasesConstants.PASSWORD)));
		jdbc.setSchema(prop.getProperty(DatabasesConstants.SCHEMAPATTERN));

		String dataSourceName = prop.getProperty(DatabasesConstants.DATASOURCE);
		jdbc.setDbName(StringUtil.isEmptyOrNull(dataSourceName) ? "dataSource"
				: dataSourceName);

		jdbc.setUseDbSpecific(prop
				.getNullSafeBoolean(DatabasesConstants.USE_DB_SPECIFIC));
		jdbc.setRunExplainPaln(prop
				.getNullSafeBoolean(DatabasesConstants.RUN_EXPLAIN_PLAN));

		return jdbc;
	}

	@Deprecated
	public static void removeOldDbSettingFromProperties(IProject project) {
		PropertyHandler prop = new PropertyHandler(project);
		prop.getProperty().remove(DatabasesConstants.DRIVER);
		prop.getProperty().remove(DatabasesConstants.DRIVER_CLASS_NAME);
		prop.getProperty().remove(DatabasesConstants.URL);
		prop.getProperty().remove(DatabasesConstants.USER_NAME);
		prop.getProperty().remove(DatabasesConstants.PASSWORD);
		prop.getProperty().remove(DatabasesConstants.DATASOURCE);
		prop.getProperty().remove(DatabasesConstants.USE_DB_SPECIFIC);
		prop.getProperty().remove(DatabasesConstants.RUN_EXPLAIN_PLAN);
		prop.saveProperties();
	}

	@Deprecated
	public static boolean validateJdbcOption(JdbcOption jdbc,
			boolean fromProject) {
		if (jdbc.getDriverJar() == null || jdbc.getDriverJar().equals("")
				|| jdbc.getDriverClassName() == null
				|| jdbc.getDriverClassName().equals("")
				|| jdbc.getUrl() == null || jdbc.getUrl().equals("")
				|| jdbc.getUserName() == null || jdbc.getUserName().equals("")
				|| jdbc.getPassword() == null || jdbc.getPassword().equals("")) {

			String message = fromProject ? "project" : "preference";

			MessageUtil.showMessage(Message.exception_find_jdbcoption + message
					+ " properties", DatabasesConstants.PROGRAM_NAME);
			return false;
		}

		return true;
	}

	public static boolean isNumericType(String columnTypeName) {
		if (columnTypeName == null)
			return false;
		if (columnTypeName.toUpperCase().indexOf("NUM") > -1
				|| columnTypeName.toUpperCase().indexOf("INT") > -1) {
			return true;
		}
		return false;
	}
}
