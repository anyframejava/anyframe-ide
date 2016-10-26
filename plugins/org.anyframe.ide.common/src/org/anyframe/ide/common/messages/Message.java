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
package org.anyframe.ide.common.messages;

import org.eclipse.osgi.util.NLS;

/**
 * This is Message class.
 * 
 * @author Sujeong Lee
 */
public class Message extends NLS {
	private static final String BUNDLE_NAME = Message.class.getName();

	// [File / Path]
	public static String meta_inf;
	public static String metadata_file;

	// [Common]
	public static String ide_button_browse;
	public static String ide_button_build;
	public static String ide_button_refresh;
	public static String ide_button_apply;
	public static String ide_button_install;
	public static String ide_button_uninstall;
	public static String ide_button_add;
	public static String ide_button_edit;
	public static String ide_message_title;
	public static String ide_button_new;
	public static String ide_button_remove;
	public static String ide_button_save;
	public static String ide_button_run;
	public static String ide_button_close;
	public static String ide_button_update;
	public static String ide_button_ok;
	public static String ide_button_cancel;
	public static String ide_button_setdefault;
	public static String ide_button_connect;

	public static String image_schema;

	// jdbc configuration
	public static String wizard_jdbc_page;
	public static String wizard_jdbc_title;
	public static String wizard_jdbc_description;
	public static String wizard_jdbc_label;
	public static String wizard_jdbc_driver_label;

	public static String wizard_jdbc_typename;
	public static String wizard_jdbc_url;
	public static String wizard_jdbc_dbname;
	public static String wizard_jdbc_schemaname;
	public static String wizard_jdbc_username;
	public static String wizard_jdbc_password;
	public static String wizard_jdbc_server;
	public static String wizard_jdbc_port;
	public static String wizard_jdbc_dialect;
	public static String wizard_jdbc_driverclass;
	public static String wizard_jdbc_driverjar;
	public static String wizard_jdbc_defaultschema;
	public static String wizard_jdbc_checkjdbc;

	// configuration;
	public static String properties_config_section;
	public static String properties_config_template_description;
	public static String properties_config_description;
	public static String properties_config_database_description;
	public static String properties_config_apphome_section;
	public static String properties_config_apphome_description;
	public static String properties_config_apphome;
	public static String properties_templatehome_location;

	public static String properties_database_profile;
	public static String properties_database_config_description;
	public static String properties_database_error_delete_default;
	public static String properties_database_others;

	// Editor jdbc setting;
	public static String properties_problem_connection;
	public static String properties_validation_dbname;
	public static String properties_validation_error_dbname;
	public static String properties_validation_dbname_detail;
	public static String properties_validation_dbschema;
	public static String properties_validation_dbserver;
	public static String properties_validation_dbserver_error;
	public static String properties_validation_dbserver_detail;
	public static String properties_validation_dbclassname;
	public static String properties_validation_dburl;
	public static String properties_validation_dbclassname_error;
	public static String properties_validation_dbclassname_detail;
	public static String properties_validation_dbjar;
	public static String properties_validation_dbjar_valid;
	public static String properties_validation_dbjar_detail;
	public static String properties_validation_username_error;
	public static String properties_validation_username_detail;
	public static String properties_validation_password_error;
	public static String properties_validation_password_detail;
	public static String properties_validation_port_error;
	public static String properties_validation_port_detail;

	public static String properties_jdbc_setting;
	public static String properties_jdbc_setjdbc;
	public static String properties_jdbc_defaultschema;
	public static String properties_confirm_jdbcconf;
	public static String properties_error_selected_project;
	public static String properties_error_current_project;
	public static String properties_info_different;

	public static String properties_connection_success;
	public static String properties_connection_fail;

	// excetion
	public static String exception_getconnection;
	public static String exception_closeconnection;
	public static String exception_gettable;
	public static String exception_getschema;
	public static String exception_log_find_file;
	public static String exception_log_io_file;
	public static String exception_log_refresh;
	public static String exception_load_jdbcconfig;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Message.class);
	}

	private Message() {
	}
}
