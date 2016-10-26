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
package org.anyframe.ide.common.databases;

/**
 * This is DatabasesConstants class.
 * 
 * @author Sujeong Lee
 */
public class DatabasesConstants {

	// constants for db connection
	public static final String DRIVER = "datasource.default.DRIVER";
	public static final String DRIVER_CLASS_NAME = "datasource.default.DRIVER_CLASS_NAME";
	public static final String URL = "datasource.default.URL";
	public static final String USER_NAME = "datasource.default.USER_NAME";
	public static final String PASSWORD = "datasource.default.PASSWORD";
	public static final String SCHEMAPATTERN = "datasource.default.SCHEMAPATTERN";
	public static final String DATASOURCE = "datasource.default.DATA_SOURCE";

	// constants for other options
	public static final String USE_DB_SPECIFIC = "USE_DB_SPECIFIC";
	public static final String RUN_EXPLAIN_PLAN = "DB_EXPLOERER_RUN_EXPLAIN_PLAN";

	public static final String PROGRAM_NAME = "DB Explorer";

	public static final String PLAN_OBJECT_NAME_COLUMN = "OBJECT_NAME";
}
