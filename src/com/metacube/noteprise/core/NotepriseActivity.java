package com.metacube.noteprise.core;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.evernote.client.oauth.android.AuthenticationResult;
import com.metacube.noteprise.R;
import com.metacube.noteprise.common.BaseActivity;
import com.metacube.noteprise.common.base.NotepriseFragment;
import com.metacube.noteprise.common.base.NotepriseFragmentManager;
import com.metacube.noteprise.core.screens.CreateNewNoteScreen;
import com.metacube.noteprise.core.screens.MainMenuScreen;
import com.metacube.noteprise.core.screens.SalesforceObjectChooser;
import com.metacube.noteprise.salesforce.SalesforceLoginUtility;
import com.metacube.noteprise.util.NotepriseLogger;

public class NotepriseActivity extends BaseActivity implements OnClickListener
{	
	Button evernoteLoginButton;
	Boolean authenticationStarted = false;
	Boolean isGoMenuCalled=false;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    	        
        FragmentManager.enableDebugLogging(Boolean.FALSE);    
        if (android.os.Build.VERSION.SDK_INT > 9) 
        {
        	android.os.StrictMode.ThreadPolicy policy = new android.os.StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        	android.os.StrictMode.setThreadPolicy(policy);
        }      
        setUpWelcomeScreen();   
    }
    
    public void setUpWelcomeScreen()
    {
    	setContentView(R.layout.welcome_screen_layout);
        evernoteLoginButton = (Button) findViewById(R.id.evernote_login_button);
        evernoteLoginButton.setOnClickListener(this);
    }
    
    @Override
    protected void onStart() 
    {
    	super.onStart();    	
    	if (noteprisePreferences.isSignedInToEvernote())
		{
			setupEvernoteSession();
			if (evernoteSession != null && evernoteSession.isLoggedIn())
			{
				handleEvernoteLoginComplete();
			}
		}
    }
    
    public void checkButtons()
    {
    	if (evernoteSession != null)
    	{
    		loggedInEvernote = evernoteSession.isLoggedIn();
    	}    	
    	if (loggedInEvernote && loggedInSalesforce && isGoMenuCalled ==false)
    	{
    		
    		isGoMenuCalled=true;
    		gotoMainMenu();
    	}
    }
    
    @Override
    protected void onResume() 
    {
    	super.onResume();    	
    	doSalesforceLoginCheck();
    	checkButtons();    	
    	if (isEvernoteAuthenticationComplete() && authenticationStarted)
		{
    		//hideFullScreenProgresIndicator();    		
			authenticationStarted = false;
			noteprisePreferences.saveEvernoteAuthToken(evernoteSession.getAuthToken());						
			AuthenticationResult authResult = evernoteSession.getAuthenticationResult();
			noteprisePreferences.saveEvetnoteNoteStoreUrl(authResult.getNoteStoreUrl());
			noteprisePreferences.saveEvernoteWebApiPrefix(authResult.getWebApiUrlPrefix());
			noteprisePreferences.saveEvetnoteUserId(authResult.getUserId());
			noteprisePreferences.saveSignedInToEvernote(evernoteSession.isLoggedIn());
			handleEvernoteLoginComplete();
		}
    }

	@Override
	public void onClick(View view) 
	{
		if (view == evernoteLoginButton)
		{
			authenticationStarted = true;
			//showFullScreenProgresIndicator();
			executeAsyncTask();
		}
		else if (view == salesforceObjectsButton)
		{
			changeScreen(new NotepriseFragment("SalesforceObjectChooser", SalesforceObjectChooser.class));
		}
		else if (view == createNewNoteButton)
		{
			changeScreen(new NotepriseFragment("CreateNewNote", CreateNewNoteScreen.class));
		}
		else if (view == logoutButton)
		{
			signOutFromEvernote();
		}
	}
	
	@Override
	public void doTaskInBackground() 
	{
		super.doTaskInBackground();
		startEvernoteAuthentication();
	}
	
	public void gotoMainMenu()
	{
		setContentView(R.layout.base_layout);
		baseHeaderLayout = (RelativeLayout) findViewById(R.id.base_header_layout);
		baseHeaderTitleTextView = (TextView) findViewById(R.id.base_header_title_text_view);
		headerProgressBar = (ProgressBar) findViewById(R.id.header_progress_bar);
		notepirseLogoImageView = (ImageView) findViewById(R.id.base_header_title_image_view);
		salesforceObjectsButton = (ImageView) findViewById(R.id.object_mapping_settings_button);
    	salesforceObjectsButton.setOnClickListener(this);
    	createNewNoteButton = (ImageView) findViewById(R.id.create_new_note_button);
    	createNewNoteButton.setOnClickListener(this);
    	deleteNoteButton = (ImageView) findViewById(R.id.delete_note_button);
    	deleteNoteButton.setOnClickListener(this);
    	saveToSFButton = (ImageView) findViewById(R.id.save_to_sf_button);
    	saveToSFButton.setOnClickListener(this);
    	publishToChatterButton = (ImageView) findViewById(R.id.chatter_button);
    	publishToChatterButton.setOnClickListener(this);
    	editButton = (ImageView) findViewById(R.id.edit_button);
    	editButton.setOnClickListener(this);
    	saveButton = (ImageView) findViewById(R.id.save_button);
    	saveButton.setOnClickListener(this);
    	logoutButton = (ImageView) findViewById(R.id.logout_button);
    	logoutButton.setOnClickListener(this);
		notepriseFragmentManager = new NotepriseFragmentManager(this);
        notepriseFragmentManager.changeScreen(new NotepriseFragment("MainMenu", MainMenuScreen.class));
	}
	
	public void handleEvernoteLoginComplete()
	{
		evernoteLoginButton.setText("Logged in as " + evernoteSession.getAuthenticationResult().getUserId());
		evernoteLoginButton.setEnabled(Boolean.FALSE);
		checkButtons();
	}
	
	public void doSalesforceLoginCheck()
	{
		salesforceLoginUtility = new SalesforceLoginUtility(this);
		salesforceLoginUtility.onAppResume();
	}
	
	public void signOutFromEvernote()
	{
		if (evernoteSession != null)
		{
			evernoteSession.logOut();
			noteprisePreferences.saveSignedInToEvernote(evernoteSession.isLoggedIn());
			setUpWelcomeScreen();
		}
	}
	
	@Override
	public void handleSalesforceLoginComplete() 
	{
		super.handleSalesforceLoginComplete();
		if (salesforceLoginUtility != null && salesforceLoginUtility.salesforceRestClient != null)
		{
			loggedInSalesforce = Boolean.TRUE;
			salesforceRestClient = salesforceLoginUtility.salesforceRestClient;
			checkButtons();
		}
	}
	
	@Override
	public void onBackPressed() 
	{
		if (notepriseFragmentManager != null && notepriseFragmentManager.onBackPressed())
		{
			return;
		}
		super.onBackPressed();		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.noteprise_menu, menu);
		if (evernoteSession != null)
		{
			if (evernoteSession.isLoggedIn())
			{
				menu.findItem(R.id.refresh_noteprise_menu_option).setVisible(Boolean.TRUE);
				menu.findItem(R.id.signout_from_evernote_menu_option).setVisible(Boolean.TRUE);
			}
			else
			{
				menu.findItem(R.id.refresh_noteprise_menu_option).setVisible(Boolean.FALSE);
				menu.findItem(R.id.signout_from_evernote_menu_option).setVisible(Boolean.FALSE);
			}
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch(item.getItemId())
		{
			case R.id.refresh_noteprise_menu_option:
			{
				break;
			}
			case R.id.signout_from_evernote_menu_option:
			{
				signOutFromEvernote();
				break;
			}
			case R.id.exit_noteprise_menu_option:
			{
				this.finish();
				break;
			}
			default:
			{
				NotepriseLogger.logMessage("No menu action found for this menu item= " + item.getTitle());
			}
		}
		return super.onOptionsItemSelected(item);
	}
}