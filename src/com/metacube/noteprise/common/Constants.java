package com.metacube.noteprise.common;

public class Constants 
{
	public static final Boolean DEBUGGING_ENABLED = true;
	public static final Boolean STACKTRACE_ENABLED = true;
	public static String LOG_TAG = "Noteprise";
	public static final String APP_NAME = "Noteprise";  
	public static final String APP_VERSION = "1.0";
	
	//public static final String EVERNOTE_HOST = "sandbox.evernote.com";	
	public static final String EVERNOTE_HOST = "www.evernote.com";
	public static final String CONSUMER_KEY = "noteprise-3933";
	public static final String CONSUMER_SECRET = "ce361e9ac663ad4a";	
	
	public static final String NOTEPRISE_PREFS = "NoteprisePrefs";	
	public static final String EVERNOTE_LOGGED_IN_PREF = "evernote_loggedin";
	public static final String EVERNOTE_AUTH_TOKEN = "evernote_auth_token";
	public static final String EVERNOTE_NOTESTORE_URL = "evernote_notestore_url";
	public static final String EVERNOTE_USER_ID = "evernote_user_id";
	public static final String EVERNOTE_WEBAPI_PREFIX = "evernote_webapi_prefix";
	
	public static final String USER_SAVED_SALESFORCE_OBJECT_NAME = "user_saved_salesforce_object_mapping";
	public static final String USER_SAVED_SALESFORCE_FIELD_NAME = "user_saved_salesforce_field_mapping";
	public static final String USER_SAVED_SALESFORCE_OBJECT_LABEL = "user_saved_salesforce_object_label";
	public static final String USER_SAVED_SALESFORCE_FIELD_LABEL = "user_saved_salesforce_field_label";
	public static final String USER_SAVED_SALESFORCE_FIELD_LENGTH = "user_saved_salesforce_field_length";	
	
	public static final String APP_DATA_PATH = "/Android/data/com.metacube.noteprise/data/";		
	public final static String APP_PATH_SD_CARD = "/Note/";
	public static final String IMAGE_PATH = APP_DATA_PATH + APP_PATH_SD_CARD;
	public static final String NOTE_PREFIX = 
		    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		    "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" +
		    "<en-note><div>";
	public static final String NOTE_SUFFIX = "</div></en-note>";	
	public static final Integer MAX_NOTES = 100;
	
	public static final Integer USER_PAGE_BATCH_SIZE = 1000;
	public static final Integer GROUP_PAGE_BATCH_SIZE = 250;
	public static final Integer USER_MENTION_LIMIT = 25;
	public static final Integer CHATTER_POST_LIMIT = 1000;
	
	public static String SORT_BY_LABEL = "SORT_BY_LABEL";
	public static String SORT_BY_NAME = "SORT_BY_NAME";
	public static String SORT_BY_ID = "SORT_BY_ID";
	public static String SORT_BY_SORT_ORDER = "SORT_BY_SORT_ORDER";	
	
	public static final String ITEM_TYPE_LIST_SECTION = "LIST_SECTION";
	public static final String ITEM_TYPE_LIST_ITEM = "LIST_ITEM";
	
	public static final String CREATE_NOTE_ACTION = "CREATE_NOTE_ACTION";
	public static final String DELETE_NOTE_ACTION = "DELETE_NOTE_ACTION";	
}