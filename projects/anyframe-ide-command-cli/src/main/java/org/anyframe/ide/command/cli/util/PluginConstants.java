/*
 * Copyright 2002-2008 the original author or authors.
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
package org.anyframe.ide.command.cli.util;

/**
 * This is an PluginConstants class. This class manage constants.
 * 
 * @author SoYon Lim
 */
public interface PluginConstants {

	// 1. plugin name
	String CORE_PLUGIN = "core";

	String PROJECT_NAME = "project.name";
	String PACKAGE_NAME = "package.name";

	String PROJECT_TYPE = "project.type";
	String PROJECT_TYPE_SERVICE = "service";
	String PROJECT_TYPE_WEB = "web";

	String DB_TYPE = "db.type";
	String DB_DRIVER_PATH = "db.lib";
	String DB_DRIVER_CLASS = "db.driver";
	String DB_URL = "db.url";
	String DB_USERNAME = "db.userId";
	String DB_PASSWORD = "db.password";
	String DB_SCHEMA = "db.schema";
}
