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
package org.anyframe.ide.common.util;

import java.lang.Character.UnicodeBlock;

import org.eclipse.jdt.core.Signature;

/**
 * This is StringUtil class.
 * 
 * @author Sujeong Lee
 */
public class StringUtil {
	public static String null2str(String str) {
		return str == null ? "" : str;
	}

	public static String null2str(Object obj) {
		return obj == null ? "" : obj.toString();
	}

	public static String emptyToNull(String str) {
		if (str == null)
			return null;
		else if (str.length() == 0)
			return null;
		return str;
	}

	public static String getFileExtention(String name) {
		if (name == null)
			return null;
		int lastDotPos = name.lastIndexOf('.');
		if (lastDotPos != -1) {
			return name.substring(lastDotPos, name.length());
		} else {
			return "";
		}
	}

	public static String getFileNameWithoutExtention(String name) {
		if (name == null)
			return null;
		int lastDotPos = name.lastIndexOf('.');
		if (lastDotPos != -1) {
			return name.substring(0, lastDotPos);
		} else {
			return name;
		}
	}

	/**
	 * Exctract Java Class Name from FullPackageName
	 * 
	 * @param name
	 * @return
	 */
	public static String unqualifyJavaName(String name) {
		if (name == null)
			return null;
		int lastDotPos = name.lastIndexOf('.');
		if (lastDotPos != -1) {
			return name.substring(lastDotPos + 1);
		} else {
			return name;
		}
	}

	/**
	 * get packageName from FullPackageNameWithClass (ex. input :
	 * com.sds.anyfrane.sample.SampleClass, output : com.sds.anyfrane.sample)
	 * 
	 * @param qualifiedName
	 * @return
	 */
	public static String getPackageNameFromQualifiedName(String qualifiedName) {
		if (qualifiedName == null)
			return null;
		int pos = qualifiedName.lastIndexOf('.');
		if (pos > 0)
			return qualifiedName.substring(0, pos);
		return "";
	}

	public static String getFileNameFromQualifiedName(String qualifiedName) {
		if (qualifiedName == null)
			return null;
		int pos = qualifiedName.lastIndexOf('.');
		if (pos > 0 || (pos < qualifiedName.length() - 1))
			return qualifiedName.substring(pos + 1);
		return qualifiedName;
	}

	public static String unqualifySpringName(String name) {
		int lastDotPos = name.indexOf('/');
		if (lastDotPos != -1) {
			return name.substring(lastDotPos + 1);
		} else {
			return name;
		}
	}

	public static String toLowerFirstLetter(String str) {
		if (isEmptyOrNull(str)) {
			return "";
		}
		String firstLetter = str.substring(0, 1).toLowerCase();
		String restLetters = str.substring(1);
		return firstLetter + restLetters;
	}

	public static String capitalizeFirstLetter(String str) {
		if (isEmptyOrNull(str)) {
			return "";
		}
		String firstLetter = str.substring(0, 1).toUpperCase();
		return firstLetter + str.substring(1);
	}

	public static String milisecondToSecond(long milisecond) {
		return milisecond != 0 ? String.valueOf(milisecond / 1000.0)
				: "0";
	}

	// ##### bonobono : this methods are derived from <Anyframe Core>
	/**
	 * convert to string camel case typed.
	 * @param targetString a String to be converted to Camel Case
	 * @param posChar a character to make next Character upper case (ex: "_")
	 * @param useOrigicalCase 모든 문자의 대소문자를 그대로 유지할지 여부(기본적으로는 false 로 작업한다.)
	 * @return
	 */
	public static String toCamelCase(String targetString, char posChar, boolean useOrigicalCase) {
		StringBuffer result = new StringBuffer();
		boolean nextUpper = false;
		
		String allLower = targetString;
		if(!useOrigicalCase){
			allLower = targetString.toLowerCase();
		}

		for (int i = 0; i < allLower.length(); i++) {
			char currentChar = allLower.charAt(i);
			if (currentChar == posChar) {
				nextUpper = true;
			} else {
				if (nextUpper) {
					currentChar = Character.toUpperCase(currentChar);
					nextUpper = false;
				}
				result.append(currentChar);
			}
		}
		return result.toString();
	}

	/**
	 * convert from String underScored(DB Column Name Style) to Camel Case
	 * String .
	 * 
	 * @param underScoredStr
	 * @return
	 */
	public static String toCamelCase(String underScoredStr) {
		if (isEmptyOrNull(underScoredStr))
			return "";
		return toCamelCase(underScoredStr, '_', false);
	}
	
	/**
	 * sampleData -> SAMPLE_DATA
	 * @param str
	 * @return
	 */
	public static String convertToUnderScore(String str) {
		String result = "";
		for (int i = 0; i < str.length(); ++i) {
			char currentChar = str.charAt(i);
			if ((i > 0) && (Character.isUpperCase(currentChar))) {
				result = result.concat("_");
			}
			result = result.concat(Character.toString(currentChar).toUpperCase());
		}
		return result;
	}

	// works exactly same as Velocity StringUtils.removeUnderScores method, and
	// faster
	public static String removeUnderScores(String underScoredStr) {
		return capitalizeFirstLetter(toCamelCase(underScoredStr));
	}

	// ##### bonobono : 090218
	public static boolean isEmptyOrNull(String str) {
		return (str == null || str.length() == 0) ? true : false;
	}

	/**
	 * whether trimmed String is empty (null safe)
	 */
	public static boolean isTrimmedEmptyOrNull(String str) {
		return "".equals(null2str(str).trim());
	}

	// ##### bonobono : 090401
	/**
	 * package명과 class명을 입력받아 Qualified Name을 조합해 반환한다. 조합이 불가할 시에는 empty
	 * String("")을 반환한다.
	 */
	public static String generateQualifiedName(String packageName,
			String className) {

		// className이 없으면 Qualified 생성 불가
		if (isEmptyOrNull(className))
			return "";
		// packageName 없으면 className이 곧 Qualified Name
		if (isEmptyOrNull(packageName))
			return className.trim();

		return packageName.trim() + "." + className.trim();
	}

	public static String[] resolveFieldGenericType(String genericType) {
		int indexOf = genericType.indexOf('<');
		if (indexOf > -1) {
			String listFieldRefClassName = genericType.substring(indexOf + 1,
					genericType.length() - 1);
			String fldNm = genericType.substring(0, indexOf);
			String[] resolvedTypes = new String[2];
			resolvedTypes[0] = fldNm;
			resolvedTypes[1] = listFieldRefClassName;
			return resolvedTypes;
		}
		return null;
	}

	public static String[] resolveGenericType(String genericType) {
		// java.util.List<sample.SampleVO>
		int indexOf = genericType.indexOf('<');
		if (indexOf > -1) {
			String listFieldRefClassName = genericType.substring(indexOf + 1,
					genericType.length() - 1);
			String listTypeName = genericType.substring(0, indexOf);
			int lastIndexOf = listTypeName.lastIndexOf('.');
			String prmEngNm = listTypeName.substring(lastIndexOf + 1,
					listTypeName.length());
			String pkgEngNm = listTypeName.substring(0, lastIndexOf); // ?
			String[] resolvedTypes = new String[3];
			resolvedTypes[0] = prmEngNm;
			resolvedTypes[2] = listFieldRefClassName;
			resolvedTypes[1] = pkgEngNm;
			return resolvedTypes;
		}
		return null;
	}

	/**
	 * deprecated bonobono : 제약 : generic 안에 generic 형태 처리에 문제가 있음.
	 * 
	 * 
	 * typeName이 qualified type name이면 unqualify 하여 return ex1> int -> int ex2>
	 * java.math.BigDecimal -> BigDecimal ex3>
	 * java.util.List<com.sds.anyframe.vo.TestInnerVO> -> List<TestInnerVO>
	 * 
	 * @author bonobono
	 * @param typeName
	 * @return simpleTypeName
	 */
	/*
	 * public static String getSimpleTypeName(String typeName) { if(typeName ==
	 * null) { return null; } typeName = typeName.trim(); int indexOfGeneric =
	 * typeName.indexOf('<');
	 * 
	 * if(indexOfGeneric != -1) { // generic type ���� String
	 * genericTypeQNString = typeName.substring(indexOfGeneric + 1,
	 * typeName.length() - 1); String typeNameWithoutGenericQN =
	 * typeName.substring(0, indexOfGeneric).trim(); StringBuilder sb = new
	 * StringBuilder(unqualifyJavaName(typeNameWithoutGenericQN)).append("<");
	 * if(genericTypeQNString.contains("<")) {
	 * sb.append(getSimpleTypeName(genericTypeQNString)); } else { String[]
	 * genericTypeQNs = genericTypeQNString.split(","); int i = 0; for(String
	 * genericTypeQN : genericTypeQNs) {
	 * sb.append(getSimpleTypeName(genericTypeQN.trim())); if(i !=
	 * genericTypeQNs.length - 1) { sb.append(","); } ++i; } } sb.append(">");
	 * return sb.toString(); } else { return unqualifyJavaName(typeName); } }
	 */

	/**
	 * qualified name을 받아 simple Name을 반환한다.
	 * 
	 * @param typeName
	 * @return simpleTypeName
	 */
	public static String getSimpleTypeName(String typeName) {
		if (typeName == null) {
			return null;
		}
		typeName = typeName.trim().replaceAll("\\p{Space}+", " ")
				.replaceAll("\\p{Space}*,\\p{Space}*", ",")
				.replaceAll("\\p{Space}*<\\p{Space}*", "<")
				.replaceAll("\\p{Space}*>\\p{Space}*", ">")
				.replaceAll("\\p{Space}*\\.\\p{Space}*", "\\.");
		return Signature.getSimpleName(typeName);
	}

	/**
	 * 파일명과 확장자명을 받아 확장자를 포함한 파일명을 반환 확장자가 없으면 파일명을 그대로 반환 ex1> ( "testService",
	 * "xml" ) -> "testService.xml" ex2> ( "testService", null ) ->
	 * "testService"
	 * 
	 * @param fileNameWithOutExtension
	 * @param extensionName
	 * @return fileNameWithExtension
	 */
	public static String getFilenameWithExtension(
			String fileNameWithOutExtension, String extensionName) {
		fileNameWithOutExtension = null2str(fileNameWithOutExtension);
		extensionName = null2str(extensionName);
		if ("".equals(fileNameWithOutExtension) && "".equals(extensionName)) {
			return "";
		}
		if ("".equals(extensionName)) {
			return fileNameWithOutExtension;
		}
		return fileNameWithOutExtension + "." + extensionName;
	}

	public static String removePrefix(String string, String prefix) {
		if (string == null || prefix == null)
			return null;
		if (string.startsWith(prefix)) {
			return string.substring(prefix.length());
		} else {
			return string;
		}
	}

	public static String removePrefixIgnoreCase(String string, String prefix) {
		if (string == null || prefix == null)
			return null;
		if (string.toLowerCase().startsWith(prefix.toLowerCase())) {
			return string.substring(prefix.length());
		} else {
			return string;
		}
	}

	public static String removeSuffix(String string, String suffix) {
		if (string == null || suffix == null)
			return null;
		if (string.endsWith(suffix)) {
			return string.substring(0, string.length() - suffix.length());
		} else {
			return string;
		}
	}

	public static String removeSuffixIgnoreCase(String string, String suffix) {
		if (string == null || suffix == null)
			return null;
		if (string.toLowerCase().endsWith(suffix.toLowerCase())) {
			return string.substring(0, string.length() - suffix.length());
		} else {
			return string;
		}
	}

	public static String removeNumericSuffix(String eName) {
		if (eName == null) {
			return null;
		}
		return eName.replaceAll("[0-9]+$", "");
	}

	/**
	 * substring by bytes cf. java.lang.String In most cases, not suitable to
	 * use at plug-in development. At plug-in development, use cropByteUTF8
	 * instead!
	 * 
	 * @param Original
	 *            String
	 * @param maximum
	 *            Length in Bytes
	 * @return cropped String
	 */
	public static String cropByte(String str, int maxLength) {
		if (str == null)
			return null;
		String tmp = str;
		int slen = 0, blen = 0;
		char c;
		if (tmp.getBytes().length > maxLength) {
			while (blen + 1 < maxLength) {
				c = tmp.charAt(slen);
				blen++;
				slen++;
				if (c > 127)
					blen++; // 2-byte character..
			}
			tmp = tmp.substring(0, slen);
		}
		return tmp;
	}

	/**
	 * substring UTF8 String by bytes cf. java.lang.String suitable to use at
	 * plug-in development.
	 * 
	 * @param Original
	 *            String
	 * @param maximum
	 *            Length in Bytes
	 * @return cropped String
	 */
	public static String cropByteUTF8(String str, int maxLength) {
		int b = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);

			int skip = 0;
			int more;
			if (c <= 0x007f) {
				more = 1;
			} else if (c <= 0x07FF) {
				more = 2;
			} else if (c <= 0xd7ff) {
				more = 3;
			} else if (c <= 0xDFFF) {
				// surrogate area, consume next char as well
				more = 4;
				skip = 1;
			} else {
				more = 3;
			}

			if (b + more > maxLength) {
				return str.substring(0, i);
			}
			b += more;
			i += skip;
		}
		return str;
	}

	public static String getAccessorName(String fieldName) {
		fieldName = null2str(fieldName);
		char[] nameChar = fieldName.toCharArray();
		if (nameChar.length > 0
				&& Character.isLowerCase(nameChar[0])
				&& (nameChar.length == 1 || !Character.isUpperCase(nameChar[1]))) {
			fieldName = capitalizeFirstLetter(fieldName);
		}
		return fieldName;
	}

	public static String getAccessorName(String fieldName, boolean isOldStyle) {
		if (isOldStyle) {
			return capitalizeFirstLetter(fieldName);
		} else {
			return getAccessorName(fieldName);
		}
	}

	/**
	 * Unicode기준 한글 포함 여부 체크 (단 1글자라도 한글이 포함되면 true) derived from
	 * http://entireboy.egloos.com
	 */
	public static boolean containsHangul(String str) {
		if (isEmptyOrNull(str)) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(ch);
			if (UnicodeBlock.HANGUL_SYLLABLES.equals(unicodeBlock)
					|| UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
							.equals(unicodeBlock)
					|| UnicodeBlock.HANGUL_JAMO.equals(unicodeBlock))
				return true;
		}
		return false;
	}

	/**
	 * 명에서 상위 몇 자리까지를 가져온다. depth가 0이면 ALL e.g.
	 * extractPackageByDepth("abc.def.ghi.jkl", 3) => "abc.def.ghi
	 */
	public static String extractPackageToDepth(String packageName, int depth) {
		if (isEmptyOrNull(packageName)) {
			return "";
		}
		if (depth == 0) {
			return packageName;
		}
		String[] splited = packageName.split("\\.");
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < splited.length && i < depth; i++) {
			if (i > 0) {
				result.append(".");
			}
			result.append(splited[i]);
		}
		return result.toString();
	}

	public static String splitLine(String originalStr, String prefixStr,
			String suffixStr) {
		return splitLine(originalStr, prefixStr, suffixStr, false);
	}

	/**
	 * for usage in Velocity Template
	 */
	public static String splitLine(String originalStr, String prefixStr,
			String suffixStr, boolean useUnixLF) {
		originalStr = null2str(originalStr).trim();
		if ("".equals(originalStr)) {
			return "";
		}
		String linefeedChar = useUnixLF ? "\n" : "\r\n";
		originalStr = originalStr.replace("\r\n", "\n").replace("\r", "\n");
		prefixStr = null2str(prefixStr);
		suffixStr = null2str(suffixStr);
		String[] lines = originalStr.split("\n");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines.length; i++) {
			sb.append(prefixStr + lines[i] + suffixStr
					+ ((i == lines.length - 1) ? "" : linefeedChar));
		}
		return sb.toString();
	}

	public static String unifyCRLF(String originalStr) {
		return null2str(originalStr).trim().replace("\r\n", "\n");
	}

	public static String removeCRLF(String originalStr) {
		return null2str(originalStr).trim().replace("\r\n", "")
				.replace("\n", "");
	}

	/**
	 * helper method for java script work similar as java.lang.String.endsWith,
	 * null safe
	 * 
	 * @param str
	 * @param suffix
	 * @return
	 */
	public static boolean endsWith(String str, String suffix) {
		if (null2str(str).endsWith(suffix)) {
			return true;
		}
		return false;
	}

	/**
	 * helper method for java script work similar as
	 * java.lang.String.startsWith, null safe
	 * 
	 * @param str
	 * @param prefix
	 * @return
	 */
	public static boolean startsWith(String str, String prefix) {
		if (null2str(str).startsWith(prefix)) {
			return true;
		}
		return false;
	}

	/**
	 * helper method for java script work similar as java.lang.String.trim, null
	 * safe
	 * 
	 * @param str
	 * @return
	 */
	public static String trim(String str) {
		if (isEmptyOrNull(str)) {
			return "";
		}
		return str.trim();
	}
	
	/** 
	* 입력 문자열 중 pattern replace  
	* @param String source String
	* @param String pattern String
	* @param String replace String
	* @return String 
	*/
   public static String replace(String source, String pattern, String replace) 
   { 
	   int sIndex = 0;
	   int eIndex = 0;
	   String sourceTemp = null;
	   StringBuffer result = new StringBuffer();    
	   sourceTemp = source.toUpperCase();
	   while ((eIndex = sourceTemp.indexOf(pattern.toUpperCase(), sIndex)) >= 0) 
	   { 
		   result.append(source.substring(sIndex, eIndex)); 
		   result.append(replace); 
		   sIndex = eIndex + pattern.length(); 
	   } 
	   result.append(source.substring(sIndex)); 
	   return result.toString(); 
   }

	public static boolean isContainKorean(String text) {
		char[] temp = text.toCharArray();

		for(int i=0; i<temp.length; i++){
			int type = Character.getType(temp[i]);
			if(type == 5){
				return true;
			}
		}
		return false;
	} 
   
	// for simple test
	// public static void main(String...args) {
	//
	// System.out.println(removeNumericSuffix("12132test101"));
	// System.out.println(removeNumericSuffix("test"));
	// System.out.println(removeNumericSuffix("한글13"));
	// System.out.println(removeNumericSuffix("te22st34"));
	//
	// System.out.println(getSimpleTypeName("int"));
	// System.out.println(getSimpleTypeName("java.math.BigDecimal"));
	// System.out.println(getSimpleTypeName("    java.util.List<com.sds.  anyframe.vo.TestInnerVO,   java.lang.String     >    ")+"*");
	// System.out.println(getSimpleTypeName("java.util.Map<String, String>"));
	// System.out.println(getSimpleTypeName("java.util.Map<String,Map<java.lang.BigDecimal,   java.lang.BigInteger>>"));
	// System.out.println(getSimpleTypeName("java.util.Map   <String,Map    <java.lang.BigDecimal,java.lang.BigInteger   >    >"));
	// System.out.println(toUnderScore("XmlTest_re"));
	// String s = "com.sds.anyframe.Dotremove";
	// System.out.println(removeSuffixIgnoreCase(s, ".dotremove"));
	// String a = "Test_VO_T_H_I_S_TEST_VO_PDFSDRFSDR";
	// long t1 = System.nanoTime();
	// System.out.println(removeUnderScores(a));
	// long t2 = System.nanoTime();
	// System.out.println(StringUtils.removeUnderScores(a));
	// long t3 = System.nanoTime();
	// System.out.println("" + ((t3-t2) / (t2-t1)) );
	// System.out.println(getAccessorName(null));
	// System.out.println(getAccessorName(""));
	// System.out.println(getAccessorName("aField"));
	// System.out.println(getAccessorName("asField"));
	// System.out.println(getAccessorName("asfField"));
	//
	// System.out.println(containsHangul("ㄱㄴㄷㅏ"));
	// System.out.println(containsHangul("abc"));
	// System.out.println(containsHangul("한"));
	// System.out.println(containsHangul("한글"));
	// System.out.println(containsHangul("한abc"));
	// System.out.println(containsHangul("한abc글"));
	// System.out.println(containsHangul("ab한c"));
	// System.out.println(containsHangul("a한b글c테d스e트f"));
	// System.out.println(containsHangul("123abc"));
	// System.out.println(containsHangul("123"));
	// System.out.println(containsHangul("한123"));
	// System.out.println(containsHangul("a1b2c3d4"));
	// System.out.println(containsHangul("a1b2한글c3d4"));
	// System.out.println(containsHangul("にほんご"));
	// System.out.println(containsHangul("日本語"));
	//
	// System.out.println(toUnderScore(toCamelCase("CASE_INFO")));
	// System.out.println(toUnderScore(toCamelCase("A_FIELD_S")));
	// System.out.println(toUnderScore(toCamelCase("A_2_FIELD")));
	// System.out.println(toUnderScore("CASE_INFO"));
	// System.out.println(toUnderScore("A_FIELD_S"));
	// System.out.println(toUnderScore("A_2_FIELD"));
	// System.out.println(toUnderScore("정말_날아라병아리"));
	//
	// System.out.println(extractPackageToDepth("abc.def.ghi.jkl", 0));
	// System.out.println(extractPackageToDepth("abc.def.ghi.jkl", 2));
	// System.out.println(extractPackageToDepth("abc.def.ghi.jkl", 7));
	//
	// String testStr = "동해물과 백두산이\n마르고닳도록\r하느님이 보우하사\n\n우리나라만세\r";
	// System.out.println(splitLine(testStr, "     * "));

}
