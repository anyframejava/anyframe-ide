/*   
 * Copyright 2002-2009 the original author or authors.   
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

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.appfuse.tool.DataHelper;
import org.hibernate.mapping.Column;

/**
 * This is an AnyframeDataHelper class.
 * 
 * @author hzhang(mb4henry@yahoo.com.au)
 * @author Matt Raible
 * @author David Carter
 * @author modified by SooYeon Park
 */
public class AnyframeDataHelper extends DataHelper {
	private static Log log = LogFactory.getLog(AnyframeDataHelper.class);
	private static String datePattern = "yyyy-MM-dd";

	private static final List<String> PRIMITIVES = new ArrayList<String>();
	static {
		PRIMITIVES.add("Character");
		PRIMITIVES.add("Byte");
		PRIMITIVES.add("Short");
		PRIMITIVES.add("Integer");
		PRIMITIVES.add("Long");
		PRIMITIVES.add("Boolean");
		PRIMITIVES.add("Float");
		PRIMITIVES.add("Double");
		PRIMITIVES.add("String");
	}

	/**
	 * Generate a random value in a format that makes DbUnit happy.
	 * 
	 * @param column
	 *            the column (i.e. "java.lang.String")
	 * @return a generated string for the particular type
	 */
	public String getTestValueForDbUnit(Column column) {
		StringBuffer result = new StringBuffer();
		String type = column.getValue().getType().getReturnedClass().getName();
		String datetype = column.getValue().getType().getName();

		/* messages for debug 
		System.out.println("[AnyframeDataHelper] type: " + type);
		System.out.println("[AnyframeDataHelper] name: " + column.getName());
		System.out.println("[AnyframeDataHelper] precision: "+ column.getPrecision());
		System.out.println("[AnyframeDataHelper] scale: " + column.getScale());
		System.out.println("[AnyframeDataHelper] length: " + column.getLength());
		System.out.println("-------------------------------------------------");
	 	*/
		
		int precision = (int) Math.pow(10, column.getPrecision());
		int scale = column.getScale() == 0 ? 2 : column.getScale();
		int difference = column.getPrecision() - scale;
		if (difference < 0) difference = 0;

		if(precision != 0)
			precision = (int) Math.pow(10, difference);
		
		if ("java.lang.Integer".equals(type) || "int".equals(type)) {
			result.append((int) ((Math.random() * (precision == 0 ? Integer.MAX_VALUE
					: precision))) - 1);
		} else if ("java.lang.Float".equals(type) || "float".equals(type)) {
			result.append((float) ((Math.random() * (precision == 0 ? Integer.MAX_VALUE
					: precision))) - 1);
		} else if ("java.lang.Long".equals(type) || "long".equals(type)) {
			result.append((long) ((Math.random() * (precision == 0 ? Integer.MAX_VALUE
					: precision))) - 1);
		} else if ("java.lang.Double".equals(type) || "double".equals(type)) {
			result.append((long) ((Math.random() * (precision == 0 ? Float.MAX_VALUE
					: precision))) - 1);
		} else if ("java.lang.Short".equals(type) || "short".equals(type)) {
			result.append((short) ((Math.random() * (precision == 0 ? Short.MAX_VALUE
					: precision))) - 1);
		} else if ("java.lang.Byte".equals(type) || "byte".equals(type) || "[B".equals(type)) {
			result.append((byte) ((Math.random() * Byte.MAX_VALUE)));
		} else if ("java.math.BigDecimal".equals(type)) {
			result.append((int) ((Math.random() * (precision == 0 ? Integer.MAX_VALUE
					: precision))) - 1);
		} else if ("java.lang.Boolean".equals(type) || "boolean".equals(type)) {
			result.append("0");
		} else if ("java.util.Date".equals(type)
				|| "java.sql.Date".equals(type)) {
			if (datetype.equals("time")) {
				result.append(new Time(new Date().getTime()).toString());
			} else if (datetype.equals("timestamp")) {
				result.append(new Timestamp(new Date().getTime()).toString());
			} else {
				result.append(getDate(new Date()));
			}
		} else if ("java.sql.Time".equals(type)) {
			result.append(new Time(new Date().getTime()).toString());
		} else if ("java.sql.Timestamp".equals(type)) {
			result.append(new Timestamp(new Date().getTime()).toString());
		} else { // default to String for everything
			// else
			String stringWithQuotes = generateStringValue(column);
			result.append(stringWithQuotes.substring(1,
					stringWithQuotes.length() - 1));
		}

		return result.toString();
	}

	/**
	 * Method to generate a random value for use in setting values in a Java
	 * test
	 * 
	 * @param column
	 *            the type of object (i.e. "java.util.Date")
	 * @return The string-ified version of the type
	 */
	public String getValueForJavaTest(Column column) {
		StringBuffer result = new StringBuffer();
		String type = column.getValue().getType().getReturnedClass().getName();
		String datetype = column.getValue().getType().getName();

		int precision = (int) Math.pow(10, column.getPrecision());
		int scale = column.getScale() == 0 ? 2 : column.getScale();
		int difference = column.getPrecision() - scale;
		if (difference < 0) difference = 0;
		
		if(precision != 0)
			precision = (int) Math.pow(10, difference);
		
		if ("java.lang.Integer".equals(type)) {
			result.append("new Integer(").append((int) ((Math.random() * (precision == 0 ? Integer.MAX_VALUE: precision))) - 1).append(")");
		} else if ("int".equals(type)) {
			result.append("(int) ").append((int) ((Math.random() * (precision == 0 ? Integer.MAX_VALUE : precision))) -1);
		} else if ("java.lang.Float".equals(type)) {
			result.append("new Float(").append((float) ((Math.random() * (precision == 0 ? Integer.MAX_VALUE : precision))) -1).append(")");
		} else if ("java.math.BigDecimal".equals(type)) {
			result.append("new java.math.BigDecimal(").append((int) ((Math.random() * (precision == 0 ? Integer.MAX_VALUE : precision))) -1).append(")");
		} else if ("float".equals(type)) {
			result.append("(float) ").append((float) ((Math.random() * (precision == 0 ? Integer.MAX_VALUE : precision))) -1);
		} else if ("java.lang.Long".equals(type)) {
			result.append("new Long(\"").append((long) ((Math.random() * (precision == 0 ? Integer.MAX_VALUE : precision))) - 1).append("\")");			
		} else if ("long".equals(type)) {
			result.append((long) ((Math.random() * (precision == 0 ? Integer.MAX_VALUE : precision))) -1);
		} else if ("java.lang.Double".equals(type)) {
			result.append("new Double(").append((long) (Math.random() * (precision == 0 ? Float.MAX_VALUE : precision)) -1).append(")");
		} else if ("double".equals(type)) {
			result.append((long) ((Math.random() * (precision == 0 ? Float.MAX_VALUE : precision))) - 1);
		} else if ("java.lang.Short".equals(type)) {
			result.append("new Short(\"").append((short) ((Math.random() * (precision == 0 ? Short.MAX_VALUE : precision))) -1).append("\")");		
		} else if ("short".equals(type)) {
			result.append("(short)").append((short) ((Math.random() * (precision == 0 ? Short.MAX_VALUE: precision))) - 1);
		} else if ("java.lang.Byte".equals(type)) {
			result.append("new Byte(\"").append((byte) ((Math.random() * Byte.MAX_VALUE))).append("\")");
		} else if ("byte".equals(type)) {
			result.append("(byte) ").append(
					(byte) ((Math.random() * Byte.MAX_VALUE)));
		} else if ("[B".equals(type)) {
			result.append("new byte[]{").append((byte) ((Math.random() * Byte.MAX_VALUE))).append("}");
		} else if ("java.lang.Boolean".equals(type)) {
			result.append("Boolean.FALSE");
		} else if ("boolean".equals(type)) {
			result.append("false");
		} else if ("java.util.Date".equals(type)) {
			if (datetype.equals("time")) {
				result.append("java.sql.Time.valueOf(\"")
						.append(new Time(new Date().getTime()).toString())
						.append("\")");
			} else if (datetype.equals("timestamp")) {
				result.append("java.sql.Timestamp.valueOf(\"")
						.append(new Timestamp(new Date().getTime()).toString())
						.append("\")");
			} else {
				result.append("new java.util.Date()");
			}
		} else if ("java.sql.Date".equals(type)) {
			result.append("new java.sql.Date()");
		} else if ("java.sql.Timestamp".equals(type)) {
			result.append("java.sql.Timestamp.valueOf(\"")
					.append(new Timestamp(new Date().getTime()).toString())
					.append("\")");
		} else { // default to String for everything
			// else
			result.append(generateStringValue(column));
		}

		return result.toString();
	}

	/**
	 * Method to generate a random string value for use in setting values in a
	 * Java test
	 * 
	 * @param column
	 *            the type of object (i.e. "java.util.Date")
	 * @return The string-ified version of the type
	 */
	public String getStringValueForJavaTest(Column column) {
		StringBuffer result = new StringBuffer();
		result.append(generateStringValue(column));
		return result.toString();
	}

	/**
	 * Method to generate a random value for use in setting values in a Java
	 * test
	 * 
	 * @param column
	 *            the type of object (i.e. "java.util.Date")
	 * @return The string-ified version of the type
	 */
	public String getExistingValueForJavaTest(Column column,
			String existingValue) {
		StringBuffer result = new StringBuffer();
		String type = column.getValue().getType().getReturnedClass().getName();
		String datetype = column.getValue().getType().getName();

		if ("java.lang.Integer".equals(type)) {
			result.append("new Integer(" + existingValue + ")");
		} else if ("int".equals(type)) {
			result.append("new Integer(" + existingValue + ").intValue()");
		} else if ("java.lang.Float".equals(type)) {
			result.append("new Float(" + existingValue + ")");
		} else if ("java.math.BigDecimal".equals(type)) {
			result.append("new java.math.BigDecimal(" + existingValue + ")");
		} else if ("float".equals(type)) {
			result.append("new Float(" + existingValue + ").floatValue()");
		} else if ("java.lang.Long".equals(type)) {
			// not sure why, but Long.MAX_VALUE results
			// in too large a number
			result.append(existingValue + "L");
		} else if ("long".equals(type)) {
			// not sure why, but Long.MAX_VALUE results
			// in too large a number
			result.append("new Long(" + existingValue + ").longValue()");
		} else if ("java.lang.Double".equals(type)) {
			result.append("new Double(" + existingValue + ")");
		} else if ("double".equals(type)) {
			result.append("new Double(" + existingValue + ").DoubleValue()");
		} else if ("java.lang.Short".equals(type)) {
			result.append("new Short(\"" + existingValue + "\")");
		} else if ("short".equals(type)) {
			result.append("new Short(" + existingValue + ").shortValue()");
		} else if ("java.lang.Byte".equals(type)) {
			result.append("new Byte(\"" + existingValue + "\")");
		} else if ("byte".equals(type)) {
			result.append("new Byte(" + existingValue + ").byteValue()");
		} else if ("[B".equals(type)) {
			result.append("new byte[]{" + existingValue + "}");
		} else if ("java.lang.Boolean".equals(type)) {
			result.append("Boolean.FALSE");
		} else if ("boolean".equals(type)) {
			result.append("false");
		} else if ("java.util.Date".equals(type)) {
			if (datetype.equals("time")) {
				result.append("java.sql.Time.valueOf(\"")
						.append(new Time(new Date().getTime()).toString())
						.append("\")");
			} else if (datetype.equals("timestamp")) {
				result.append("java.sql.Timestamp.valueOf(\"")
						.append(new Timestamp(new Date().getTime()).toString())
						.append("\")");
			} else {
				result.append("new java.util.Date()");
			}
		} else if ("java.sql.Date".equals(type)) {
			result.append("new java.sql.Date()");
		} else if ("java.sql.Timestamp".equals(type)) {
			result.append("java.sql.Timestamp.valueOf(\"")
					.append(new Timestamp(new Date().getTime()).toString())
					.append("\")");
		} else { // default to String for everything
			// else
			result.append("\"" + existingValue + "\"");
		}

		return result.toString();
	}

	/**
	 * Method to generate a random value for use in setting values in a Java
	 * test
	 * 
	 * @param column
	 *            the type of object (i.e. "java.util.Date")
	 * @return The string-ified version of the type
	 */
	public String getColumnType(Column column) {
		String result = "";
		String type = column.getValue().getType().getReturnedClass().getName();

		if ("java.lang.Integer".equals(type) || "int".equals(type)
				|| "java.lang.Float".equals(type) || "float".equals(type)
				|| "java.lang.Long".equals(type)
				|| "java.lang.Long".equals(type) || "long".equals(type)
				|| "java.lang.Double".equals(type) || "double".equals(type)
				|| "java.lang.Short".equals(type) || "short".equals(type)
				|| "java.lang.Byte".equals(type) || "[B".equals(type)
				|| "java.math.BigDecimal".equals(type) || "byte".equals(type)) {
			result = "NUM";
		} else if ("java.lang.Boolean".equals(type) || "boolean".equals(type)
				|| "java.util.Date".equals(type)
				|| "java.sql.Date".equals(type)
				|| "java.sql.Timestamp".equals(type)
				|| "java.sql.Time".equals(type)) {
			result = "OTHER";
		} else { // default to String for everything
			// else
			result = "STR";
		}

		return result;
	}

	private String generateStringValue(Column column) {
		int maxLen = column.getLength();
		if (maxLen > 20) {
			// log.info("Column length greater than 20 characters for '" +
			// column.getName() + "', setting maxlength to 20.");
			maxLen = 20;
		}

		StringBuffer result = new StringBuffer("\"");

		for (int i = 0; (i < maxLen); i++) {
			int j = 0;
			if (i % 2 == 0) {
				j = (int) ((Math.random() * 26) + 65);
			} else {
				j = (int) ((Math.random() * 26) + 97);
			}
			result.append(Character.toString((char) j));
		}

		result.append("\"");

		return result.toString();
	}

	private static String getDate(Date aDate) {
		return getDate(aDate, datePattern);
	}

	private static String getDate(Date aDate, String pattern) {
		SimpleDateFormat df;
		String returnValue = "";

		if (aDate != null) {
			df = new SimpleDateFormat(pattern);
			returnValue = df.format(aDate);
		}

		return returnValue;
	}

	public boolean isPrimitive(String typeName) {
		return PRIMITIVES.contains(typeName);
	}

}
