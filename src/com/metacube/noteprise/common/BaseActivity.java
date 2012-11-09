package com.metacube.noteprise.common;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.client.conn.ApplicationInfo;
import com.evernote.client.oauth.android.AuthenticationResult;
import com.evernote.client.oauth.android.EvernoteSession;
import com.metacube.noteprise.R;
import com.metacube.noteprise.common.base.NotepriseFragment;
import com.metacube.noteprise.common.base.NotepriseFragmentManager;
import com.metacube.noteprise.salesforce.SalesforceLoginUtility;
import com.metacube.noteprise.util.AsyncTaskDataLoader;
import com.metacube.noteprise.util.NoteprisePreferences;
import com.metacube.noteprise.util.Utilities;
import com.metacube.noteprise.util.imageloader.ImageLoader;
import com.salesforce.androidsdk.rest.RestClient;

@SuppressLint("Registered")
public class BaseActivity extends FragmentActivity
{
	public ApplicationInfo evernoteAppInfo;
	public EvernoteSession evernoteSession;
	public SalesforceLoginUtility salesforceLoginUtility;
	public RestClient salesforceRestClient;
	
	public NotepriseFragmentManager notepriseFragmentManager;
	public AsyncTaskDataLoader backgroundDataLoader;
	public ImageLoader backgroundImageLoader;
	
	public NoteprisePreferences noteprisePreferences;
	public CommonMessageDialog commonMessageDialog;
	public ProgressBar headerProgressBar;
	public CommonProgressDialog commonProgressDialog;
	public ImageView salesforceObjectsButton, createNewNoteButton, deleteNoteButton, logoutButton, nextButton, previousButton,
						saveToSFButton, publishToChatterButton, editButton, saveButton, notepirseLogoImageView;
	public TextView baseHeaderTitleTextView,recordCount;
	public RelativeLayout baseHeaderLayout, recordCountLayout;
	
	public Boolean loggedInSalesforce = Boolean.FALSE;
	public Boolean loggedInEvernote = Boolean.FALSE;
	
	public String SELECTED_OBJECT_NAME, SELECTED_OBJECT_LABEL;
	public String SELECTED_FIELD_NAME, SELECTED_FIELD_LABEL;
	public Integer SELECTED_FIELD_LENGTH;
	
	//Main menu save data..
	public CommonListAdapter savedListAdapter = null;
	public ArrayList<CommonListItems> listItems = null;
	public Integer savedCurrentTask = null;
	public String savedQueryString = null;
	public Integer savedSelectedRadioButtonId = null;
	public Boolean isDataSaved = Boolean.FALSE;
	
	public String previousScreenAction;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		noteprisePreferences = new NoteprisePreferences(this);
		commonMessageDialog = new CommonMessageDialog(this);
		backgroundDataLoader = new AsyncTaskDataLoader(this);		
		getObjectFieldMappingsFromPreferences();
	}	
	
	public void startEvernoteAuthentication() 
	{
		setupEvernoteSession();
		if (!evernoteSession.isLoggedIn()) 
		{
			evernoteSession.authenticate(this);			
		}
	}
	
	@Override
	protected void onPause() 
	{
		super.onPause();
		if (salesforceLoginUtility != null && !loggedInSalesforce)
		{
			salesforceLoginUtility.onAppPause();
		}
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		if (salesforceLoginUtility != null && !loggedInSalesforce)
		{
			salesforceLoginUtility.onAppResume();
		}		
	}
	
	@Override
	public void onUserInteraction() 
	{
		super.onUserInteraction();
		
		if (salesforceLoginUtility != null && !loggedInSalesforce)
		{
			salesforceLoginUtility.onAppUserInteraction();
		}
	}
	
	/*@Override
	public void onConfigurationChanged(Configuration newConfig) {
	
		super.onConfigurationChanged(newConfig);
		
	}*/
	public void initializeViews()
	{
		return;
	}

	public void setupEvernoteSession() 
	{		
		evernoteAppInfo = new ApplicationInfo(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET, Constants.EVERNOTE_HOST, 
				Constants.APP_NAME, Constants.APP_VERSION);
	    if (noteprisePreferences.isSignedInToEvernote()) 
	    {
	    	AuthenticationResult authResult = new AuthenticationResult(noteprisePreferences.getEvetnoteAuthToken(), noteprisePreferences.getEvetnoteNoteStoreUrl(), 
	    			noteprisePreferences.getEvernoteWebApiPrefix(), noteprisePreferences.getEvetnoteUserId());
	    	evernoteSession = new EvernoteSession(evernoteAppInfo, authResult, Utilities.getTempStorageDirectory());	    	
	    } 
	    else 
	    {
	    	evernoteSession = new EvernoteSession(evernoteAppInfo, Utilities.getTempStorageDirectory());
	    }
	}
	
	public Boolean isEvernoteAuthenticationComplete()
	{
		if  (evernoteSession != null)
		{
			if (evernoteSession.completeAuthentication() || evernoteSession.isLoggedIn())
			{
				return true;
			}
		}
		return false;
	}
	
	public void changeScreen(NotepriseFragment npFragment)
	{
		notepriseFragmentManager.changeScreen(npFragment);
	}
	
	public void clearScreen()
	{
		notepriseFragmentManager.clearScreen();
	}
	
	public void showFullScreenProgresIndicator()
	{
		if (commonProgressDialog == null || !commonProgressDialog.isVisible())
		{
			commonProgressDialog = new CommonProgressDialog(getResources().getString(R.string.progress_dialog_title), getResources().getString(R.string.progress_dialog_message));
			commonProgressDialog.show(getSupportFragmentManager(), "CommonProgressDialog");			
		}
	}
	
	public void showFullScreenProgresIndicator(String title, String message)
	{
		if (commonProgressDialog == null || !commonProgressDialog.isVisible())
		{
			commonProgressDialog = new CommonProgressDialog(title, message);
			commonProgressDialog.show(getSupportFragmentManager(), "CommonProgressDialog");			
		}
	}
	
	public void hideFullScreenProgresIndicator()
	{
		if (commonProgressDialog != null)
		{
			commonProgressDialog.dismiss();
		}
	}
	
	public void showProgresIndicator()
	{
		if (headerProgressBar != null)
		{
			headerProgressBar.setVisibility(View.VISIBLE);
		}
	}
	
	public void hideProgresIndicator()
	{
		if (headerProgressBar != null)
		{
			headerProgressBar.setVisibility(View.GONE);
		}
	}
	
	public void showToastNotification(String message)
	{
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.custom_toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));
		TextView text = (TextView) layout.findViewById(R.id.toast_layout_text);
		text.setText(message);

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
		//Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	public void handleSalesforceLoginComplete()
	{
		
	}
	
	public void finishScreen()
	{
		notepriseFragmentManager.onBackPressed();
	}
	
	public void loadImageOnView(String path, ImageView imageView, int compress)
	{
		if (backgroundImageLoader != null)
		{
			backgroundImageLoader.DisplayImage(path, this, imageView, compress);
		}
	}
	
	public void getObjectFieldMappingsFromPreferences()
	{
		SELECTED_OBJECT_NAME = noteprisePreferences.getUserSavedSalesforceObjectName();
		SELECTED_OBJECT_LABEL = noteprisePreferences.getUserSavedSalesforceObjectLabel();
		SELECTED_FIELD_NAME = noteprisePreferences.getUserSavedSalesforceFieldName();
		SELECTED_FIELD_LABEL = noteprisePreferences.getUserSavedSalesforceFieldLabel();
		SELECTED_FIELD_LENGTH = noteprisePreferences.getUserSavedSalesforceFieldLength();
	}
	
	public void executeAsyncTask()
	{
		backgroundDataLoader = new AsyncTaskDataLoader(this);
		backgroundDataLoader.execute();		
	}
	
	public void doTaskInBackground()
	{
		//Override this method in subclassses to do background tasks.
	}
	
	public void onTaskCancelled()
	{
		
	}
	
	public void onTaskStarted()
	{
		
	}
	
	public void onTaskFinished()
	{
		
	}
	
	public void onTaskUpdate()
	{
		
	}
}