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
package org.anyframe.ide.querymanager.messages;

import org.eclipse.osgi.util.NLS;

/**
 * This is Message class.
 * 
 * @author Sujeong Lee
 */
public class Message extends NLS {
	private static final String BUNDLE_NAME = Message.class.getName();
	
	// 1. Query Test Editor
	public static String editor_querymanager_addqurey_title;
	public static String editor_querymanager_editquery_title;

	public static String editor_querymanager_message_addquerytitle_prefix;
	public static String editor_querymanager_message_addquerytitle_suffix;

	public static String editor_querymanager_message_queryidexist;
	public static String editor_querymanager_message_rowvalidation;
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
//	public static String editor_querymanager_message_query_succed;
	public static String editor_querymanager_message_query_doesnt_suported;
//	public static String editor_querymanager_message_enter_parameter_name;
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
//	public static String editor_querymanager_message_improperVelocityTemplate_error;
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
	
	/**
	 *  added [2013.03.22] jaekwan.yoo
	 */
	public static String view_explorer_action_confirmdelete;//"Confirm Delete"
	public static String view_explorer_action_deletequeryid;//"Are you sure you want to delete the Query ID '"
	public static String view_explorer_action_deleteconfirmation;//"Delete Confirmation"
	public static String view_explorer_action_deletenodeconfirm;//"Are you sure you want to Delete "
	public static String view_explorer_getthequery;//"Getting the Queries...."
	public static String view_explorer_searchthequery;//"Searching the queries for Tree View"
	public static String view_explorer_messages;//"Messages"
	public static String view_explorer_status;//"Status"
	public static String view_explorer_location;//"Location"
	public static String view_explorer_sql;//"SQL"
	public static String view_explorer_text;//"Text"
	public static String view_explorer_searchquery;//"Searching Queries"
	public static String view_explorer_openthequeryfromquerynavigator;//"Opening the Query from Query Navigator."
	public static String view_explorer_queryresult;//"Query Results"

	public static String build_addtionalproposalinfo;//"Additional Proposal Info"
	public static String build_analyzingtheproject;//"Analyzing the project cached information....."
	public static String build_duplicatequeryid_one;//"Duplictae Query Id :"
	public static String build_getthecacheinfo;//"Getting the cached information if exist....."
	public static String build_queryidisnotused_message;//"\" is not used in any of the DAO files in the project or used in GenericDAO class. "
	public static String build_queryidisnotexistinxmlfile_message;//"\" does not exist in any of the Query Mapping XML files defined in this Project. "
	public static String build_queryidexist;//"Query Id exist in "
	public static String build_queryidexistintheproject;//" exists in the project"
	public static String build_renamequeryid;//"Rename query id '"
	public static String build_renamequeryidsentence;//"Rename query id."
	public static String build_duplicatequeryid_two;//" exists in the project"
	public static String build_createmarkersfor;//"Creating the Markers for  "

	public static String parsefile_collectionthequeryidsxmlfile;//"Collecting the QueryIds : XML File "
	public static String parsefile_collectionthequeryidsjavafile;//"Collecting the QueryIds : Java File "
	public static String parsefile_searchfile;//"Searching the file : "
	
	/* Label of the text fields */
	public static String preferences_createprefix;//"Create Prefix:";
	public static String preferences_updateprefix;// = "Update Prefix:";
	public static String preferences_removeprefix;// = "Remove Prefix:";
	public static String preferences_findprefix;// = "Find Prefix:";
	public static String preferences_listsuffix;// = "Find List suffix:";
	public static String preferences_pksuffix;// = "Find by PK suffix:";
	public static String preferences_valueobjectprefix;// = "Value Object prefix:";
	public static String preferences_abstractdaoprefixandsuffix;//"AbstractDAO prefix and suffix"

	public static String dialog_searchmappingxml;//"Searching mapping-xml files"
	public static String dialog_searchmappingxmlfordialog;//"Searching the mapping-xml files for Dialog"
	public static String dialog_getthefile;//"Getting the Files...."
	public static String dialog_queryidselectiondialog_title;//"Query Id Selection Dialog"
	public static String dialog_queryidhas_message;//"List of all files in the respective projects that below Query Id has."
	public static String dialog_queryid;//"Query Id: "
	public static String dialog_projectname;//"Project name"
	public static String dialog_queryfile;//"Query file"
	public static String dialog_filename;//"File path"
	public static String dialog_scanmappingxml;//"Scanning mapping-xml files"
	public static String dialog_scanmappingxmlfordialog;//"Scanning the mapping-xml files for Dialog"
	public static String dialog_warning;//" Warning"
	public static String dialog_selectfile_message;//"Select a file in the table before pressing 'Ok' button. Press 'Cancel' button to cancel the window."

	public static String action_checkdtdinxmlfile;//"Check DTD in XML file."
	public static String action_repetitionofqueryid;//"Repetition of Query Id"
	
	public static String util_checkbuilderalreadyadded;//"Checking whether builder is added already..."
	public static String util_addingbuildertoproject;//"Adding Builder to project "
	
	public static String property_enablequerycommentappend;//"Enable query comment append."
	public static String property_enablequeryidduplicationchecking;//"Enable Query ID duplication checking."
	public static String property_enablequeryidcheck;//"Enable Query ID is used or unused checking."
	
	public static String wizard_createquerymappingxmlfile;//"This wizard will create a new Anyframe Query Mapping XML file."
	public static String wizard_createquerymappingxmlfile_title;//"Create a New Query Mapping XML File"
	public static String wizard_createsqlfile;//"An error occured during creating SQL File."
	public static String wizard_createdbioeditor;//"An error occured during opening DBIO Editor."

	public static String editor_querymanager_javaclassnotexist;//"Java Class does not exist"
	public static String editor_querymanager_queryisnotusedone;//"Selected Query ID \""
	public static String editor_querymanager_queryisnotusedtwo;//"\" does not used in any java file."
	public static String editor_voclassnotexist;//"VO Class does not exist"
	public static String editor_selectvoclass_one;//"Selected VO Class \""
	public static String editor_selectvoclass_two;//"\" does not exist."
	public static String editor_querymanager_resultmapping;//"Result Mapping"
	public static String editor_querymanager_resultclass_text;//"Result Class :"
	public static String editor_querymanager_resultclass_title;//"Result Class"
	public static String editor_querymanager_modifyquery;//"Modify Query"
	public static String editor_querymanager_selectthequerytomodify;//"Select the query to modify"
	public static String editor_querymanager_mappingstyle;//"Mapping Style:"
	public static String editor_explorer_openthequeryfromqueryxml;//"Opening the Query from Query XML file."
	public static String editor_querymanager_queryisnotavailable;//"Check the query. Query is not available."
	public static String editor_button_browse;//"Browse..."
	public static String editor_querymanager_connectionprofile;//"Connection Profile:"
	public static String editor_querymanager_queryisnotsupported;//"Check the query. Query is not supported."
	
	public static String exception_getsqlconnection;//"Get SQL Connection Exception"
	public static String exception_problemreadfile;//" Problem reading file :" 
	public static String exception_problemclosingstream;//" Problem closing stream of :"
	public static String exception_savefail;//"Save failed: "
	public static String exception_loadjarfile;//"There was a problem while Loading And Reading the Jar File. "
	public static String exception_queryidnotexist;//"Query Id does not exist"
	public static String exception_selectedqueryidnotexist_one;//"Query Id with the selected text \""
	public static String exception_selectedqueryidnotexist_two;//"\" does not exist."
	public static String exception_resultclassisnotset;//"The result class is not set or does not exist. Please add a valid class. "
	public static String exception_createmarker;//"Problem creating a marker"
	public static String exception_sqlquery;//"Exception occurred while running SQL Query."
	public static String exception_generatetemplatesource;//"Exception occurred while generating template source."
	public static String exception_gettemplatefile;//"Exception occurred while getting template file."
	public static String exception_removeexistmarker;//"Error while removing existing markers for project "
	public static String exception_queryidisnotdefined;//"Expected Query Id is not defined in this Project. "
	public static String excpetion_getpropertyfile;//"Exception occurred while getting propertyfiles."
	public static String exception_parsefile;//"Exception occurred while parsing file."
	public static String exception_couldnotaddanyframebuilder;//"could not add Anyframe Builder to project : "
	public static String exception_couldnotfindthetemplate;//"couldn't find the template"
	public static String exception_problemparsingthetemplate;//"syntax error: problem parsing the template"
	public static String exception_somethinginvokedinthetemplate;//"something invoked in the template threw an exception"
	public static String exception_someerroroccured;//"Some error occured"
	public static String exception_failtotransformtemplate;//"Fail to transform template"
	public static String exception_accessingmessage;//"CompletionProcessor_error_accessing_message"
	public static String exception_notonbuildpathtitle;//"CompletionProcessor_error_notOnBuildPath_title"
	public static String exception_notonbuildpathmessage;//"CompletionProcessor_error_notOnBuildPath_message"
	public static String exception_accessingtitle;//"CompletionProcessor_error_accessing_title"
	public static String exception_notavailabledtd;//"Not available DTD in XML file"
	public static String exception_disablenatureaction;//"Can't disable nature"
	public static String exception_couldnotenableanyframenature;//"could not enable Anyframe Nature"
	
	public static String editor_querymanager_resultclass_message;//"Select an item to open (? = any character, * = any string):"
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Message.class);
	}

	private Message() {
	}
}