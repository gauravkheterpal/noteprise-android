package com.metacube.noteprise.util;

import com.metacube.noteprise.common.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class NoteprisePreferences 
{
	
	private SharedPreferences appSharedPrefs;
	private Editor prefsEditor;
	
	public NoteprisePreferences(Context context) 
	{
		this.appSharedPrefs = context.getSharedPreferences(Constants.NOTEPRISE_PREFS, Activity.MODE_PRIVATE);
		this.prefsEditor = appSharedPrefs.edit();
	}

	public boolean isSignedInToEvernote() 
	{
		return appSharedPrefs.getBoolean(Constants.EVERNOTE_LOGGED_IN_PREF, false);
	}

	public void saveSignedInToEvernote(Boolean Status) 
	{
		prefsEditor.putBoolean(Constants.EVERNOTE_LOGGED_IN_PREF, Status);		
		prefsEditor.commit();
	}
	
	public String getEvetnoteAuthToken() 
	{
		return appSharedPrefs.getString(Constants.EVERNOTE_AUTH_TOKEN, null);
	}

	public void saveEvernoteAuthToken(String authToken) 
	{
		prefsEditor.putString(Constants.EVERNOTE_AUTH_TOKEN, authToken);		
		prefsEditor.commit();
	}
	public Integer getEvetnoteUserId() 
	{
		return appSharedPrefs.getInt(Constants.EVERNOTE_USER_ID, 0);
	}

	public void saveEvetnoteUserId(Integer userId) 
	{
		prefsEditor.putInt(Constants.EVERNOTE_USER_ID, userId);		
		prefsEditor.commit();
	}
	public String getEvetnoteNoteStoreUrl() 
	{
		return appSharedPrefs.getString(Constants.EVERNOTE_NOTESTORE_URL, null);
	}

	public void saveEvetnoteNoteStoreUrl(String noteStoreUrl) 
	{
		prefsEditor.putString(Constants.EVERNOTE_NOTESTORE_URL, noteStoreUrl);		
		prefsEditor.commit();
	}
	public String getEvernoteWebApiPrefix() 
	{
		return appSharedPrefs.getString(Constants.EVERNOTE_WEBAPI_PREFIX, null);
	}

	public void saveEvernoteWebApiPrefix(String webApiPrefix) 
	{
		prefsEditor.putString(Constants.EVERNOTE_WEBAPI_PREFIX, webApiPrefix);		
		prefsEditor.commit();
	}
	
	public String getUserSavedSalesforceObjectName() 
	{
		return appSharedPrefs.getString(Constants.USER_SAVED_SALESFORCE_OBJECT_NAME, null);
	}
	
	public String getUserSavedSalesforceFieldName() 
	{
		return appSharedPrefs.getString(Constants.USER_SAVED_SALESFORCE_FIELD_NAME, null);
	}
	
	public String getUserSavedSalesforceObjectLabel() 
	{
		return appSharedPrefs.getString(Constants.USER_SAVED_SALESFORCE_OBJECT_LABEL, null);
	}
	
	public String getUserSavedSalesforceFieldLabel() 
	{
		return appSharedPrefs.getString(Constants.USER_SAVED_SALESFORCE_FIELD_LABEL, null);
	}

	public void saveUserSalesforceObjectFieldMapping(String userSavedSalesforceObjectName, String userSavedSalesforceObjectLabel, 
			String userSavedSalesforceFieldName, String userSavedSalesforceFieldLabel, int fieldLength) 
	{
		prefsEditor.putString(Constants.USER_SAVED_SALESFORCE_OBJECT_NAME, userSavedSalesforceObjectName);		
		prefsEditor.putString(Constants.USER_SAVED_SALESFORCE_OBJECT_LABEL, userSavedSalesforceObjectLabel);
		prefsEditor.putString(Constants.USER_SAVED_SALESFORCE_FIELD_NAME, userSavedSalesforceFieldName);
		prefsEditor.putString(Constants.USER_SAVED_SALESFORCE_FIELD_LABEL, userSavedSalesforceFieldLabel);
		prefsEditor.putInt(Constants.USER_SAVED_SALESFORCE_FIELD_LENGTH, fieldLength);
		prefsEditor.commit();
	}	
}