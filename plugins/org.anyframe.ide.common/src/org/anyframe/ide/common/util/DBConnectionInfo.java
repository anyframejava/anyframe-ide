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

import java.sql.Connection;

import org.eclipse.datatools.modelbase.sql.schema.Database;

/**
 * This is DBConnectionInfo class.
 * 
 * @author Junghwan Hong
 */
public class DBConnectionInfo {
	private Connection connection;

	private Database database;

	public DBConnectionInfo() {
	}

	public DBConnectionInfo(Connection connection, Database databse) {
		this.connection = connection;
		this.database = databse;
	}

	/**
	 * @return
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * @param connection
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	/**
	 * @return
	 */
	public Database getDatabase() {
		return database;
	}

	/**
	 * @param database
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}

}
