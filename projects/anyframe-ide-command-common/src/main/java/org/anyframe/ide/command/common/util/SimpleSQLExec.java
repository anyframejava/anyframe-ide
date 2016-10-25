/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.anyframe.ide.command.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.JDBCTask;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;

/**
 * Apache ant's SQLExec is simplified for executing only dbscript of plugin.
 * (There was a bug in SQLExec : When we execute db script based on maven, maven
 * command can't finished successfully because of PrintStream closing problem.
 * 
 * @since Ant 1.2
 * @author modified by Soyon Lim
 * @ant.task name="sql" category="database"
 */
public class SimpleSQLExec extends JDBCTask {

	public static class DelimiterType extends EnumeratedAttribute {
		public static final String NORMAL = "normal", ROW = "row";

		public String[] getValues() {
			return new String[] { NORMAL, ROW };
		}
	}

	/**
	 * Database connection
	 */
	private Connection conn = null;

	/**
	 * SQL statement
	 */
	private Statement statement = null;

	/**
	 * SQL Statement delimiter
	 */
	private String delimiter = ";";

	/**
	 * The delimiter type indicating whether the delimiter will only be
	 * recognized on a line by itself
	 */
	private String delimiterType = DelimiterType.NORMAL;

	/**
	 * Encoding to use when reading SQL statements from a file
	 */
	private String encoding = null;

	public List<File> srcFileList = new ArrayList<File>();

	private ArrayList<Transaction> transactions = new ArrayList<Transaction>();

	/**
	 * Set the name of the SQL file to be run. Required unless statements are
	 * enclosed in the build file
	 * 
	 * @param srcFile
	 *            the file containing the SQL command.
	 */
	public void setSrc(File srcFile) {
		this.srcFileList.add(srcFile);
	}

	/**
	 * Set the file encoding to use on the SQL files read in
	 * 
	 * @param encoding
	 *            the encoding to use on the files
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Set the delimiter that separates SQL statements. Defaults to
	 * &quot;;&quot;; optional
	 * 
	 * <p>
	 * For example, set this to "go" and delimitertype to "ROW" for Sybase ASE
	 * or MS SQL Server.
	 * </p>
	 * 
	 * @param delimiter
	 *            the separator.
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Set the delimiter type: "normal" or "row" (default "normal").
	 * 
	 * <p>
	 * The delimiter type takes two values - normal and row. Normal means that
	 * any occurrence of the delimiter terminate the SQL command whereas with
	 * row, only a line containing just the delimiter is recognized as the end
	 * of the command.
	 * </p>
	 * 
	 * @param delimiterType
	 *            the type of delimiter - "normal" or "row".
	 */
	public void setDelimiterType(DelimiterType delimiterType) {
		this.delimiterType = delimiterType.getValue();
	}

	/**
	 * Add a SQL transaction to execute
	 * 
	 * @return a Transaction to be configured.
	 */
	public Transaction createTransaction() {
		Transaction transaction = new Transaction();
		transactions.add(transaction);
		return transaction;
	}

	/**
	 * Load the sql file and then execute it
	 * 
	 * @throws BuildException
	 *             on error.
	 */
	public void execute() throws BuildException {

		for (File srcFile : srcFileList) {
			Transaction transaction = createTransaction();

			if (srcFile != null && !srcFile.isFile()) {
				throw new BuildException("Source file " + srcFile
						+ " is not a file!", getLocation());
			}
			transaction.setSrc(srcFile);
		}

		conn = getConnection();
		if (!isValidRdbms(conn)) {
			return;
		}

		try {
			statement = conn.createStatement();
			statement.setEscapeProcessing(true);

			// Process all transactions
			for (Transaction transaction : transactions) {
				transaction.runTransaction();
			}

			if (!isAutocommit()) {
				conn.commit();
			}
		} catch (IOException e) {
			close();
			throw new BuildException(e, getLocation());
		} catch (SQLException e) {
			close();
			throw new BuildException(e, getLocation());
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException ex) {
				// ignore
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex) {
				// ignore
			}
		}
	}

	/**
	 * read in lines and execute them
	 * 
	 * @param reader
	 *            the reader contains sql lines.
	 * @param out
	 *            the place to output results.
	 * @throws SQLException
	 *             on sql problems
	 * @throws IOException
	 *             on io problems
	 */
	protected void runStatements(Reader reader) throws SQLException,
			IOException {
		StringBuffer sql = new StringBuffer();
		String line;

		BufferedReader in = new BufferedReader(reader);

		while ((line = in.readLine()) != null) {
			line = line.trim();

			line = getProject().replaceProperties(line);

			if (line.startsWith("//")) {
				continue;
			}
			if (line.startsWith("--")) {
				continue;
			}
			StringTokenizer st = new StringTokenizer(line);
			if (st.hasMoreTokens()) {
				String token = st.nextToken();
				if ("REM".equalsIgnoreCase(token)) {
					continue;
				}
			}

			sql.append(" ").append(line);

			if (line.indexOf("--") >= 0) {
				sql.append("\n");
			}
			if ((delimiterType.equals(DelimiterType.NORMAL) && StringUtils
					.endsWith(sql, delimiter))
					|| (delimiterType.equals(DelimiterType.ROW) && line
							.equals(delimiter))) {
				execSQL(sql.substring(0, sql.length() - delimiter.length()));
				sql.replace(0, sql.length(), "");
			}
		}

		if (sql.length() > 0) {
			execSQL(sql.toString());
		}
	}

	/**
	 * Exec the sql statement.
	 * 
	 * @param sql
	 *            the SQL statement to execute
	 * @param out
	 *            the place to put output
	 * @throws SQLException
	 *             on SQL problems
	 */
	protected void execSQL(String sql) throws SQLException {
		// Check and ignore empty statements
		if ("".equals(sql.trim())) {
			return;
		}

		ResultSet resultSet = null;
		try {
			statement.execute(sql);
			statement.getUpdateCount();
			resultSet = statement.getResultSet();

			SQLWarning warning = conn.getWarnings();
			while (warning != null) {
				warning = warning.getNextWarning();
			}
			conn.clearWarnings();
		} catch (SQLException e) {
			throw e;
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// ignore
				}
			}
		}
	}

	private void close() {
		if (!isAutocommit() && conn != null) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				// ignore
			}
		}
	}

	/**
	 * Contains the definition of a new transaction element. Transactions allow
	 * several files or blocks of statements to be executed using the same JDBC
	 * connection and commit operation in between.
	 */
	public class Transaction {
		private Resource srcResource = null;

		public void setSrc(File src) {
			if (src != null) {
				srcResource = new FileResource(src);
			}
		}

		private void runTransaction() throws IOException, SQLException {
			if (srcResource != null) {
				InputStream is = null;
				Reader reader = null;
				try {
					is = srcResource.getInputStream();
					reader = (encoding == null) ? new InputStreamReader(is)
							: new InputStreamReader(is, encoding);
					runStatements(reader);
				} finally {
					FileUtils.close(is);
					FileUtils.close(reader);
				}
			}
		}
	}
}
