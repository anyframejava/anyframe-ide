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
package org.anyframe.ide.querymanager.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.anyframe.ide.querymanager.preferences.PreferencesHelper;
import org.anyframe.query.impl.jdbc.setter.DefaultDynamicSqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * the class DynamicQueryUtil convets Dynamic query into Executable SQL Query
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public final class DynamicQueryUtil {

	public static boolean isDynamicQuerryCorrect = true;

	private String variableName = "vo";

	public static String errorMsg = null;

	// private LobHandler lobHandler = new DefaultLobHandler();
	private static final String DELIMETER = "=";

	/**
	 * Deafault Constructor
	 */
	public DynamicQueryUtil() {
	}

	public void setVariableName() {
		String voPrefix = PreferencesHelper.getPreferencesHelper()
				.getVOPrefix();
		if (voPrefix != null)
			this.variableName = voPrefix;
	}

	/**
	 * checks wether given string is empty or not
	 * 
	 * @param str
	 *            string to be checked
	 * @return boolean
	 */
	public static boolean isEmpty(String str) {
		return (str == null || str.length() == 0);
	}

	/**
	 * split is a recursive method that splits the string with separator
	 * characters (or delimitters) and return the list of splitted strings.
	 * 
	 * @param list
	 *            list of strings.
	 * @param inputString
	 *            string that should be splitted.
	 * @param separatorChars
	 *            separation character.
	 * @return list the list of splitted strings.
	 */
	private static ArrayList split(ArrayList list, String inputString,
			String separatorChars) {
		int index = inputString.indexOf(separatorChars);

		if (isEmpty(inputString))
			return list;

		if (index == -1)
			list.add(inputString);
		else {
			list.add(inputString.substring(0, index));
			String newInputString = inputString.substring(index
					+ separatorChars.length());
			split(list, newInputString, separatorChars);
		}
		return list;
	}

	/**
	 * split It splits a string with given separator into a list
	 * 
	 * @param str
	 *            string that should be split.
	 * @param separatorChars
	 *            character for separation
	 * @return list of strings.
	 */
	public static ArrayList split(String str, String separatorChars) {

		ArrayList list = new ArrayList();

		if (isEmpty(separatorChars)) {
			list.add(str);
			return list;
		}

		return split(list, str, separatorChars);
	}

	/**
	 * Replaces given string by new string
	 * 
	 * @param originalString
	 *            the original string
	 * @param findText
	 *            text to be searched in statement
	 * @param replaceText
	 *            text to be replaced
	 * @return string
	 */
	public static String replace(String originalString, String findText,
			String replaceText) {

		originalString = (originalString == null) ? "" : originalString;

		int pos = 0;

		pos = originalString.indexOf(findText);
		if (pos > 0) {
			String preString = originalString.substring(0, pos);
			String postString = originalString.substring(pos
					+ findText.length());

			originalString = preString + replaceText + postString;
			// pos = originalString.indexOf(findText);
		}

		return originalString;
	}

	/**
	 * getter
	 * 
	 * @return errorMsg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * setter
	 * 
	 * @param errorMsg
	 *            error message.
	 */

	public void setErrorMsg(String errorMsg) {
		DynamicQueryUtil.errorMsg = errorMsg;
	}

	protected String getRunnableSQL(String sql, SqlParameterSource searchParams) {
		StringBuffer tempStatement = new StringBuffer(sql);
		SortedMap replacementPositions = findTextReplacements(tempStatement);

		Iterator properties = replacementPositions.entrySet().iterator();
		int valueLengths = 0;
		while (properties.hasNext()) {
			Map.Entry entry = (Map.Entry) properties.next();
			Integer pos = (Integer) entry.getKey();
			String key = (String) entry.getValue();
			Object replaceValue = (String) searchParams.getValue(key);
			if (replaceValue == null) {
				// throw new QueryServiceException(getMessageSource(),
				// "error.query.runnablesql.replace", new Object[] {entry
				// .getValue() });
			}
			String value = replaceValue.toString();
			tempStatement.insert(pos.intValue() + valueLengths, value);
			valueLengths += value.length();
		}
		return tempStatement.toString();
	}

	protected SortedMap findTextReplacements(StringBuffer sql) {
		TreeMap textReplacements = new TreeMap();
		int startPos = 0;
		while ((startPos = sql.indexOf("{{", startPos)) > -1) {
			int endPos = sql.indexOf("}}", startPos);
			String replacementKey = sql.substring(startPos + 2, endPos);
			sql.replace(startPos, endPos + 2, "");
			textReplacements.put(new Integer(startPos), replacementKey);
		}
		return textReplacements;
	}

	// private CallbackResultSetMapper createResultSetMapper(IQueryInfo
	// queryInfo,
	// LobHandler lobHandler, Map nullchecks)
	// throws ClassNotFoundException {
	// Class targetClazz = null;
	// if (queryInfo.doesNeedColumnMapping()) {
	// targetClazz = Class.forName(queryInfo.getResultClass());
	// } else {
	// targetClazz = HashMap.class;
	// }
	//
	// IMappingInfo mappingInfo = new IMappingInfo() {
	//
	// public String getInsertQuery() {
	// return null;
	// }
	//
	// public String getDeleteQuery() {
	// return null;
	// }
	//
	// public Map getMappingInfoAsMap() {
	// return new Map() {
	//
	// public void clear() {
	// }
	//
	// public boolean containsKey(Object key) {
	// return true;
	// }
	//
	// public boolean containsValue(Object value) {
	// return true;
	// }
	//
	// public Set entrySet() {
	// return null;
	// }
	//
	// public Object get(Object key) {
	// //if (queryInfo.isCamelCase())
	// if(true)
	// return NameConverter
	// .convertToCamelCase((String) key);
	// else
	// return (String) key;
	// }
	//
	// public boolean isEmpty() {
	// return false;
	// }
	//
	// public Set keySet() {
	// return null;
	// }
	//
	// public Object put(Object key, Object value) {
	// return null;
	// }
	//
	// public void putAll(Map t) {
	// }
	//
	// public Object remove(Object key) {
	// return null;
	// }
	//
	// public int size() {
	// return 0;
	// }
	//
	// public Collection values() {
	// return null;
	// }
	// };
	// }
	//
	// public String[] getPrimaryKeyColumns() {
	// return null;
	// }
	//
	// public String getSelectByPrimaryKeyQuery() {
	// return null;
	// }
	//
	// public String getTableName() {
	// return null;
	// }
	//
	// public String getClassName() {
	// return null;
	// }
	//
	// public String getUpdateQuery() {
	// return null;
	// }
	//
	// };
	// return new CallbackResultSetMapper(targetClazz, mappingInfo,
	// lobHandler, nullchecks, queryInfo.isCamelCase());
	// }

	public String convertDynamicQuery(String sql, Map typeMap, Object[] values,
			boolean isDynamic, Map contextMap) throws Exception {
		// Template t3=ParseTemplateString.getTemplate(sql);
		Map properties = generatePropertiesMap(values, null, null);
		if (isDynamic) {
			if (properties == null)
				properties = new Properties();
			DefaultDynamicSqlParameterSource sqlParameterSource = new DefaultDynamicSqlParameterSource(
					properties);
			sql = getRunnableSQL(sql, sqlParameterSource);
			// if (isVelocity(sql)) {
			// Template t3=ParseTemplateString.getTemplate(sql);
			// if(ParseTemplateString.getTemplate(sql) == null){
			// isDynamicQuerryCorrect=false;
			// return sql;
			// }
			// StringWriter writer = new StringWriter();
			// VelocityContext velocityContext = new VelocityContext();
			// Iterator itr = AddQueryWizardPage.velocityContextkey.iterator();
			// while (itr.hasNext()) {
			// String key = (String) itr.next();
			// key = key.substring(1);
			//
			// velocityContext.put(key, contextMap.get(key));
			// }
			//
			// t3.merge(velocityContext, writer);
			//
			// sql = writer.toString();
			// }
		} else {
			// Handle when not Dynamic
		}
		sql = substituteNamedParameters(sql, properties, typeMap);
		return sql;
	}

	private String substituteNamedParameters(String sql, Map properties,
			Map typeMap) {
		Iterator propertiesItr = properties.keySet().iterator();
		while (propertiesItr.hasNext()) {
			String namedParam = propertiesItr.next().toString();
			String key = ":" + namedParam;
			String typeValue = typeMap.get(namedParam).toString();
			String value = "";
			if (typeValue.equalsIgnoreCase("BIGINT")
					|| typeValue.equalsIgnoreCase("DECIMAL")
					|| typeValue.equalsIgnoreCase("DOUBLE")
					|| typeValue.equalsIgnoreCase("FLOAT")
					|| typeValue.equalsIgnoreCase("INTEGER")
					|| typeValue.equalsIgnoreCase("NUMERIC")
					|| typeValue.equalsIgnoreCase("REAL")
					|| typeValue.equalsIgnoreCase("SMALLINT")
					|| typeValue.equalsIgnoreCase("TINYINT")) {
				value = properties.get(namedParam).toString();
			} else
				value = "'" + properties.get(namedParam) + "'";

			sql = DynamicQueryUtil.replace(sql, key, value);
		}
		return sql;
	}

	protected boolean isVelocity(String sql) {
		return ((sql.indexOf("#if") > -1 || sql.indexOf("#foreach") > -1) && sql
				.indexOf("#end") > -1);
	}

	private Map generatePropertiesMap(Object[] values, int[] types,
			MapSqlParameterSource mapSqlParameterSource) {
		Map properties = new HashMap();
		String tempStr = null;
		Object[] tempArray = null;

		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof String) {
				tempStr = (String) values[i];
				int pos = tempStr.indexOf(DELIMETER);
				if (pos < 0) {
					System.out
							.println("1. ERROR in DynamicQueryUtil.generatePropertiesMap");
					// throw new QueryServiceException(getMessageSource(),
					// "error.query.generate.valuemap.string");
				}
				properties.put(tempStr.substring(0, pos),
						tempStr.substring(pos + 1));
				if (mapSqlParameterSource != null)
					mapSqlParameterSource.addValue(tempStr.substring(0, pos),
							tempStr.substring(pos + 1), types[i]);
			} else if (values[i] instanceof Object[]) {
				tempArray = (Object[]) values[i];
				if (tempArray.length != 2) {
					System.out
							.println("2. ERROR in DynamicQueryUtil.generatePropertiesMap");
					// throw new QueryServiceException(getMessageSource(),
					// "error.query.generate.valuemap.array");
				}
				properties.put(tempArray[0], tempArray[1]);
				if (mapSqlParameterSource != null)
					mapSqlParameterSource.addValue((String) tempArray[0],
							tempArray[1], types[i]);
			} else if (values[i] == null) {
				continue;
			} else {
				return null;
			}
		}
		return properties;
	}
}
