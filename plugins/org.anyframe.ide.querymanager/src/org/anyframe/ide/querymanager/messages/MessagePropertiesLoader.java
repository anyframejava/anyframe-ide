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
package org.anyframe.ide.querymanager.messages;

import org.eclipse.osgi.util.NLS;

/**
 * This is MessagePropertiesLoader class.
 * 
 * @author Junghwan Hong
 */
public abstract class MessagePropertiesLoader extends NLS {
	private static final String BUNDLE_NAME = MessagePropertiesLoader.class.getName();

	// 1. Query Test Editor
	public static String editor_querymanager_addqurey_title;
	public static String editor_querymanager_editquery_title;

	public static String editor_querymanager_message_addquerytitle_prefix;
	public static String editor_querymanager_message_addquerytitle_suffix;

	public static String editor_querymanager_message_queryidexist;
	public static String editor_querymanager_message_queryidempty;
	public static String editor_querymanager_message_statementEmpty;
	public static String editor_querymanager_queryinfo_title;
	public static String editor_querymanager_queryinfo_inputparam;
	public static String editor_querymanager_dbbrowser_title;
	public static String editor_querymanager_queryinfo_queryid;
	public static String editor_querymanager_queryinfo_querystatement;
	public static String editor_querymanager_queryinfo_isdynamic;
	public static String editor_querymanager_queryinfo_querystatement_rowlimit;
	public static String editor_querymanager_queryinfo_querystatement_defaultlimit;
	public static String editor_querymanager_queryinfo_querystatement_testquerybutton;
	public static String editor_querymanager_message_connection_not;
	public static String editor_querymanager_message_query_succed;
	public static String editor_querymanager_message_query_doesnt_suported;
	public static String editor_querymanager_message_enter_parameter_name;
	public static String editor_querymanager_queryinfo_parameter_varchar;
	public static String editor_querymanager_queryinfo_parameter_no;
	public static String editor_querymanager_queryinfo_parameter_name;
	public static String editor_querymanager_queryinfo_parameter_datatype;
	public static String editor_querymanager_queryinfo_parameter_binding;
	public static String editor_querymanager_queryinfo_parameter_testdata;
	public static String editor_querymanager_queryinfo_parameter_getparameter;
	public static String editor_querymanager_queryinfo_parameter_add;
	public static String editor_querymanager_queryinfo_parameter_delete;

	public static String editor_querymanager_message_error;
	public static String editor_querymanager_message_improperVelocityTemplate_error;
	public static String editor_querymanager_addqurey_title_description;
	public static String editor_querymanager_editqurey_title_description;

	public static String editor_querymanager_message_connection;
	public static String editor_querymanager_queryinfo_section_desc;
	public static String editor_querymanager_querymapping_section_desc;
	public static String editor_querymanager_parammapping_section_desc;
	public static String editor_querymanager_databrowser_section_desc;
	
	// 2. Query Explorer
	public static String view_explorer_action_addqueryeditor_desc;

	public static String view_explorer_action_addqueryeditor_title;

	public static String view_explorer_action_remove_query_confirm;

	public static String view_explorer_action_remove_query_confirm_suffix;

	public static String view_explorer_action_remove_query_confirm_title;

	public static String view_explorer_action_editqueryeditor_desc;

	public static String view_explorer_action_editqueryeditor_title;

	public static String view_explorer_action_linkeditor_desc;

	public static String view_explorer_action_linkeditor_title;

	public static String view_explorer_action_openxmleditor_desc;

	public static String view_explorer_action_openxmleditor_title;

	public static String view_explorer_action_refresh_desc;

	public static String view_explorer_action_refresh_title;

	public static String view_explorer_action_remove_query_desc;

	public static String view_explorer_action_remove_query_title;

	public static String view_explorer_util_queryid_suffix;

	// 3. Property Page
	public static String property_add_button;

	public static String property_dialog_add_message;

	public static String property_dialog_add_title;

	public static String property_dialog_add_typefolder;

	public static String property_dialog_add_typepackage;

	public static String property_dialog_add_xmlfile_extention;

	public static String property_dialog_scan_deselectall_title;

	public static String property_dialog_scan_deselectall_tooltip;

	public static String property_dialog_scan_message;

	public static String property_dialog_scan_title;

	public static String property_dialog_scan_selectall_title;

	public static String property_dialog_scan_selectall_tooltip;

	public static String property_dialog_text;

	public static String property_dialog_remove_button;

	public static String property_dialog_scan_button;

	// 4. Exception
	public static String exception_log_addquerywizard_sql;
	public static String exception_log_editXMLFIlE;
	public static String exception_log_run_query;
	
	// 5. Images
	public static String image_explorer_addquery;
	public static String image_explorer_editorquery;
	public static String image_explorer_linkeditor;
	public static String image_explorer_open_queryeditor;
	public static String image_explorer_action_refresh;
	public static String image_explorer_action_removiequery;
	public static String image_explorer_unusediddup;
	public static String image_explorer_unusedid;
	public static String image_explorer_usediddup;
	public static String image_explorer_usedid;
	public static String image_explorer_xmlfile;
	public static String image_properties_xmlfile;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, MessagePropertiesLoader.class);
	}

	private MessagePropertiesLoader() {
	}
}