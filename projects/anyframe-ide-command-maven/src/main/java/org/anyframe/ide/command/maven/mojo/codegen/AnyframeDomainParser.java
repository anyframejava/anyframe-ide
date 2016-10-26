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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.anyframe.ide.command.common.util.DBUtil;
import org.anyframe.ide.command.common.util.JdbcOption;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;

/**
 * This is AnyframeDomainParser class for generate CRUD class with domain info.
 * 
 * @author Sujeong Lee
 */
public class AnyframeDomainParser {

	private static String datePattern = "yyyy-MM-dd";

	private static String COLUMN_NAME = "COLUMN_NAME";
	private static String TYPE_NAME = "TYPE_NAME";
	private static String COLUMN_SIZE = "COLUMN_SIZE";

	private static Map<String, String> typeMapper = new HashMap<String, String>();

	// parse
	public static Domain parse(String targetFile, String targetDomain, JdbcOption jdbcOption, String templateHome) throws Exception {
		typeMapper = GenerationUtil.getTypeMappingConfig(templateHome);

		Domain domain = new Domain();

		JavaDocBuilder builder = new JavaDocBuilder();

		builder.addSource(new File(targetFile));
		JavaClass clz = builder.getClassByName(targetDomain);

		// java code parsing with comment annotation

		domain.setName(clz.getName());
		domain.setTable(clz.getTagByName(Domain.TABLE).getValue());
		domain.setPackageName(clz.getPackageName());

		List<Column> columns = new ArrayList<Column>();
		JavaField[] fields = clz.getFields();
		for (JavaField field : fields) {

			if (field.getTagByName(Column.COLUMN_NAME) != null) {
				String fkKeyValue = "";
				Column column = new Column();
				column.setFieldName(field.getName());
				column.setFieldType(field.getType().getValue());

				// init
				column.setIsKey(false);
				column.setFkey(false);
				column.setNotNull(false);

				for (DocletTag tag : field.getTags()) {
					String tagName = tag.getName();
					String tagValue = "";

					if (tagName.startsWith(Column.LENGTH)) {
						// order fix
						tagValue = tagName.substring(tagName.indexOf("(") + 1, tagName.indexOf(")"));
						tagName = tagName.substring(0, tagName.indexOf("("));
					} else {
						tagValue = tag.getValue();
					}

					if (tagName.equals(Column.COLUMN_NAME)) {
						column.setColumnName(tagValue);
					} else if (tagName.equals(Column.COLUMN_TYPE)) {
						column.setColumnType(tagValue);
					} else if (tagName.equals(Column.LENGTH)) {
						column.setLength(tagValue);
					} else if (tagName.equals(Column.KEY)) {
						column.setIsKey(true);
					} else if (tagName.equals(Column.FKEY)) {
						column.setFkey(true);
						column.setFkTable(tagValue);

						// fk sample data - return sample key for insert data
						// with fk
						fkKeyValue = setFkSampleDataList(column, jdbcOption);
					} else if (tagName.equals(Column.NOT_NULL)) {
						column.setNotNull(true);
					}
				}

				if ("".equals(fkKeyValue)) {
					setSampleDataArray(column);
				} else {
					// if this column is fk, then sample data are all fk keys
					String[] fkSampleData = { fkKeyValue, fkKeyValue, fkKeyValue, fkKeyValue };
					column.setSampleDataArray(fkSampleData);
				}
				setTestData(column);
				columns.add(column);
			}
		}
		domain.setColumns(columns);
		return domain;
	}

	private static void setSampleDataArray(Column column) {
		String columnFieldType = column.getFieldType();
		int length = Integer.valueOf(column.getLength());

		// make sample data 4
		String[] data = new String[4];

		for (int i = 0; i < 4; i++) {
			data[i] = makeRandomData(columnFieldType, length);
		}
		column.setSampleDataArray(data);
	}

	private static String setFkSampleDataList(Column column, JdbcOption jdbcOption) throws Exception {
		return generateFkSample(column, jdbcOption);
	}

	private static String generateFkSample(Column column, JdbcOption jdbcOption) throws Exception {
		String fkTable = column.getFkTable();
		String pkValue = "";

		Connection conn = DBUtil.getConnection(jdbcOption.getDriverJar(), jdbcOption.getDriverClassName(), jdbcOption.getUrl(),
				jdbcOption.getUserName(), jdbcOption.getPassword());

		List<Map<String, String>> samples = new ArrayList<Map<String, String>>();

		Map<String, String> pkMap = new HashMap<String, String>();

		DatabaseMetaData dmeta = conn.getMetaData();

		ResultSet primaryKeys = dmeta.getPrimaryKeys(null, jdbcOption.getSchema(), fkTable);
		while (primaryKeys.next()) {
			pkMap.put(primaryKeys.getString(COLUMN_NAME), primaryKeys.getString(COLUMN_NAME));
		}
		primaryKeys.close();

		ResultSet columns = dmeta.getColumns(null, jdbcOption.getSchema(), fkTable, "%");
		
		List<Column> domainColumns = new ArrayList<Column>();
		
		while (columns.next()) {
			Column col = new Column();
			col.setColumnName(columns.getString(COLUMN_NAME));
			col.setColumnType(columns.getString(TYPE_NAME));
			col.setLength(String.valueOf(columns.getInt(COLUMN_SIZE)));
			domainColumns.add(col);
		}

		for (int i = 0; i < 4; i++) {
			Map<String, String> columnMap = new HashMap<String, String>();
			for (Column col : domainColumns) {
				String sampleData = makeRandomData(typeMapper.get(col.getColumnType()), Integer.parseInt(col.getLength()));
				columnMap.put(col.getColumnName(), sampleData);

				if (pkMap.containsKey(col.getColumnName())) {
					pkValue = sampleData;
				}
			}
			samples.add(columnMap);
		}

		column.setFkSampleDataList(samples);

		return pkValue;
	}

	private static String makeRandomData(String columnFieldType, int length) {
		String result = "";

		String[] numberType = { Integer.class.getName(), Float.class.getName(), Long.class.getName(), Double.class.getName(), Short.class.getName(),
				"int", "float", "long", "double", "short" };

		if (Arrays.asList(numberType).contains(columnFieldType)) {
			// number type
			double num = Math.pow(10, length + 1);
			if (num > Integer.MAX_VALUE) {
				num = Integer.MAX_VALUE;
			}
			result = String.valueOf(Math.round(Math.random() * num));
		} else if (java.util.Date.class.getName().equals(columnFieldType) || java.sql.Date.class.getName().equals(columnFieldType)) {
			Date date = new Date();
			SimpleDateFormat df = new SimpleDateFormat(datePattern);
			result = df.format(date);
		} else if (java.sql.Timestamp.class.getName().equals(columnFieldType)) {
			result = new Timestamp(new Date().getTime()).toString();
		} else {
			// else - string type
			result = randomString(length);
		}

		return result;
	}

	// data with type casting
	private static void setTestData(Column column) {
		String columnFieldType = column.getFieldType();
		String sampleData = column.getSampleDataArray()[0];

		String testData = "";

		if (Integer.class.getName().equals(columnFieldType)) {
			testData = "(int)" + sampleData;
		} else if ("int".equals(columnFieldType)) {
			testData = "(int)" + sampleData;
		} else if (Float.class.getName().equals(columnFieldType)) {
			testData = "new Float(" + sampleData + ")";
		} else if ("float".equals(columnFieldType)) {
			testData = "(float)" + sampleData;
		} else if (Long.class.getName().equals(columnFieldType)) {
			testData = sampleData + "L";
		} else if ("long".equals(columnFieldType)) {
			testData = "(long)" + sampleData;
		} else if (Double.class.getName().equals(columnFieldType)) {
			testData = "new Double(" + sampleData + ")";
		} else if ("double".equals(columnFieldType)) {
			testData = "(double)" + sampleData;
		} else if (Short.class.getName().equals(columnFieldType)) {
			testData = "new Short(" + sampleData + ")";
		} else if ("short".equals(columnFieldType)) {
			testData = "(short)" + sampleData;
		} else if (Boolean.class.getName().equals(columnFieldType)) {
			testData = "Boolean." + Boolean.valueOf(sampleData).toString().toUpperCase();
		} else if ("boolean".equals(columnFieldType)) {
			testData = Boolean.valueOf(sampleData).toString();
		} else if (Byte.class.getName().equals(columnFieldType)) {
			testData = "new Byte(" + sampleData + ")";
		} else if ("byte".equals(columnFieldType)) {
			testData = "(byte)" + sampleData;
		} else if (java.util.Date.class.getName().equals(columnFieldType)) {
			testData = "new java.util.Date()";
		} else if (java.sql.Date.class.getName().equals(columnFieldType)) {
			testData = "new java.sql.Date(new java.util.Date().getTime())";
		} else if (java.sql.Timestamp.class.getName().equals(columnFieldType)) {
			testData = "new java.sql.Timestamp.valueOf(\"" + new Timestamp(new Date().getTime()).toString() + "\")";
		} else {
			testData = "\"" + sampleData + "\"";
		}

		column.setTestData(testData);
	}

	private static String randomString(int length) {
		if (length > 10) {
			length = 10;
		}
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			buf.append(chars.charAt(new Random().nextInt(chars.length())));
		}
		return buf.toString();
	}

}
