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

/**
 * This is GenerateSQLQuery class.
 * 
 * @author Surindhar.Kondoor
 * @author Thulasi.Devi
 */
public class GenerateSQLQuery {

	private String query;

	private int newCursorPos;

	private String errorMessage;

	private void makeCommaColumn(int cursorPos, String strToAppend, boolean type) {
		if (type) {
			newCursorPos = cursorPos + strToAppend.length();
			String tempStr = query;
			if ((query.toLowerCase().substring(0, cursorPos)
					.lastIndexOf("select") > -1 && query
					.substring(
							query.toLowerCase().substring(0, cursorPos)
									.lastIndexOf("select") + 6, cursorPos)
					.trim().length() > 0)
					|| (query.toLowerCase().substring(0, cursorPos)
							.lastIndexOf("insert into") > -1 && query
							.substring(
									query.toLowerCase().substring(0, cursorPos)
											.lastIndexOf("(") + 1, cursorPos)
							.trim().length() > 0)) {
				tempStr = query.substring(0, cursorPos) + ", " + strToAppend
						+ " ";
				newCursorPos += 3;
			} else
				tempStr = query.substring(0, cursorPos) + strToAppend;
			if (query.length() > cursorPos)
				query = tempStr + query.substring(cursorPos);
			else
				query = tempStr;
		}
	}

	private void makeConditionColumn(int cursorPos, String strToAppend,
			boolean type) {
		if (type) {
			newCursorPos = cursorPos + strToAppend.length() + 5;
			String tempStr = query;
			tempStr = query.substring(0, cursorPos) + strToAppend + " = ? ";
			if (query.length() > cursorPos) {
				query = tempStr + query.substring(cursorPos);
			} else {
				query = tempStr;
			}
		}

	}

	private void makeSpaceColumn(int cursorPos, String strToAppend, boolean type) {
		if (type) {
			newCursorPos = cursorPos + strToAppend.length() + 3;
			String tempStr = query;

			if ((query.toLowerCase().substring(0, cursorPos)
					.lastIndexOf("from") > -1 && query
					.substring(
							query.toLowerCase().substring(0, cursorPos)
									.lastIndexOf("from") + 4, cursorPos).trim()
					.length() > 0)) {
				tempStr = query.substring(0, cursorPos) + ", " + strToAppend
						+ " ";
			} else {
				tempStr = query.substring(0, cursorPos) + " " + strToAppend
						+ " ";
			}

			if (query.length() > cursorPos)
				query = tempStr + query.substring(cursorPos);
			else
				query = tempStr;
		}
	}

	public String appendAtCursorPosInSQLClause(String query, int cursorPos,
			String strToAppend, String type) {
		this.query = query;
		if (cursorPos < 0)
			return "";
		if (isValidClause(query, "having", cursorPos)
				&& isSpacePreceded("having", cursorPos))
			makeConditionColumn(cursorPos, strToAppend,
					type.equals("COLUMN") ? true : false);
		else if (isValidClause(query, "order by", cursorPos)
				&& isSpacePreceded("order by", cursorPos))
			makeCommaColumn(cursorPos, strToAppend,
					type.equals("COLUMN") ? true : false);
		else if (isValidClause(query, "group by", cursorPos)
				&& isSpacePreceded("group by", cursorPos))
			makeCommaColumn(cursorPos, strToAppend,
					type.equals("COLUMN") ? true : false);
		else if (isValidClause(query, "between", cursorPos)
				&& isSpacePreceded("between", cursorPos))
			makeSpaceColumn(cursorPos, strToAppend,
					type.equals("COLUMN") ? true : false);
		else if (isValidClause(query, "where", cursorPos)
				&& isSpacePreceded("where", cursorPos))
			makeConditionColumn(cursorPos, strToAppend,
					type.equals("COLUMN") ? true : false);
		else if (isValidClause(query, "on", cursorPos)
				&& isSpacePreceded("on", cursorPos))
			makeSpaceColumn(cursorPos, strToAppend,
					type.equals("COLUMN") ? true : false);
		else if (isValidClause(query, "from", cursorPos)
				&& isSpacePreceded("from", cursorPos))
			makeSpaceColumn(cursorPos, strToAppend, type.equals("TABLE") ? true
					: false);
		else if (isValidClause(query, "select", cursorPos))
			makeCommaColumn(cursorPos, strToAppend,
					type.equals("COLUMN") ? true : false);
		else if (isValidClause(query, "set", cursorPos)
				&& isSpacePreceded("set", cursorPos))
			makeConditionColumn(cursorPos, strToAppend,
					type.equals("COLUMN") ? true : false);
		else if (isValidClause(query, "insert into", cursorPos))
			makeInsertIntoClause(cursorPos, strToAppend, type);
		else if (isValidClause(query, "update", cursorPos))
			makeSpaceColumn(cursorPos, strToAppend, type.equals("TABLE") ? true
					: false);
		else if (isValidClause(query, "delete from", cursorPos))
			makeSpaceColumn(cursorPos, strToAppend, type.equals("TABLE") ? true
					: false);
		else
			makeSpaceColumn(cursorPos, strToAppend, true);
		return this.query;
	}

	private void makeInsertIntoClause(int cursorPos, String strToAppend,
			String type) {
		if (query
				.substring(
						query.toLowerCase().substring(0, cursorPos)
								.lastIndexOf("insert into") + 11, cursorPos)
				.trim().length() > 0
				&& query.substring(
						query.toLowerCase().substring(0, cursorPos)
								.lastIndexOf("insert into") + 11, cursorPos)
						.trim().indexOf('(') > 0)
			makeCommaColumn(cursorPos, strToAppend,
					type.equals("COLUMN") ? true : false);
		else if (query
				.substring(
						query.toLowerCase().substring(0, cursorPos)
								.lastIndexOf("insert into") + 11, cursorPos)
				.trim().length() > 0
				&& query.substring(
						query.toLowerCase().substring(0, cursorPos)
								.lastIndexOf("insert into") + 11, cursorPos)
						.trim().indexOf('(') == -1)
			errorMessage = "org.anyframe.querymanager.eclipse.core.addQeuryWizardPage.group.dbBrowser.errMsg.noOpenParenthesis";
		else if (query
				.substring(
						query.toLowerCase().substring(0, cursorPos)
								.lastIndexOf("insert into") + 11, cursorPos)
				.trim().length() == 0)
			makeSpaceColumn(cursorPos, strToAppend, type.equals("TABLE") ? true
					: false);
		else
			makeSpaceColumn(cursorPos, strToAppend, true);
	}

	private boolean isValidClause(String query, String clause, int cursorPos) {
		int index = query.toLowerCase().substring(0, cursorPos)
				.lastIndexOf(clause);
		if (index != -1 && index + clause.length() == cursorPos)
			return true;
		else if (index != -1
				&& (query.charAt(index + clause.length()) == ' '
						|| query.charAt(index + clause.length()) == '\n'
						|| query.charAt(index + clause.length()) == '\t' || query
						.charAt(index + clause.length()) == '\r'))
			return true;
		return false;
	}

	private boolean isSpacePreceded(String clause, int cursorPos) {
		if (query.charAt(query.toLowerCase().substring(0, cursorPos)
				.lastIndexOf(clause) - 1) == ' ') {
			return true;
		} else if (query.charAt(query.toLowerCase().substring(0, cursorPos)
				.lastIndexOf(clause) - 1) == '\n') {
			return true;
		} else if (query.charAt(query.toLowerCase().substring(0, cursorPos)
				.lastIndexOf(clause) - 1) == '\t') {
			return true;
		}
		return false;
	}

	public int getNewCursorPos() {
		return newCursorPos;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	// Call this when you populate this error message.
	public void setErrorMessageToNull() {
		this.errorMessage = null;
	}

}
