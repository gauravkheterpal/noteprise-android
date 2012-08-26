package com.metacube.noteprise.core.screens;

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore.Client;
import com.evernote.edam.type.Note;
import com.metacube.noteprise.R;
import com.metacube.noteprise.common.BaseFragment;
import com.metacube.noteprise.common.Constants;
import com.metacube.noteprise.common.base.NotepriseFragment;
import com.metacube.noteprise.evernote.EvernoteUtils;
import com.metacube.noteprise.salesforce.SalesforceUtils;
import com.metacube.noteprise.util.NotepriseLogger;
import com.metacube.noteprise.util.Utilities;
import com.salesforce.androidsdk.rest.RestResponse;

public class NoteDetailsScreen extends BaseFragment implements OnClickListener, android.content.DialogInterface.OnClickListener, OnDismissListener
{
	String authToken;
	Client client;
	WebView noteContentWebView;
	LinearLayout editButton;
	LinearLayout topButtonBar;
	RelativeLayout saveButton, publishToChatterButton;
	String noteTitle, noteContent, noteGuid;
	Note note;
	RestResponse publishResponse;
	Integer GET_NOTE_DATA = 0, DELETE_NOTE = 1, PUBLISH_TO_MY_CHATTER_FEED = 2, TASK = 0, deletionId = null;
	
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        noteGuid = Utilities.getStringFromBundle(args, "noteGuid");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	clearContainer(container);
    	View contentView = inflater.inflate(R.layout.note_detail_screen_layout, container);
    	noteContentWebView = (WebView) contentView.findViewById(R.id.note_content_web_view);
    	topButtonBar = (LinearLayout) addViewToBaseHeaderLayout(inflater, R.layout.note_edit_screen_menu_bar_layout, R.id.button_menu_bar_layout);
    	saveButton = (RelativeLayout) topButtonBar.findViewById(R.id.common_save_image_button);
    	publishToChatterButton = (RelativeLayout) topButtonBar.findViewById(R.id.publish_to_chatter_image_button);
    	editButton = (LinearLayout) addViewToBaseHeaderLayout(inflater, R.layout.common_left_edit_button_layout, R.id.common_left_edit_button);
    	baseActivity.deleteNoteButton.setOnClickListener(this);
    	saveButton.setOnClickListener(this);
    	publishToChatterButton.setOnClickListener(this);
    	registerForContextMenu(publishToChatterButton);
    	editButton.setOnClickListener(this); 	
    	return super.onCreateView(inflater, container, savedInstanceState);
    }

	@Override
	public void onClick(View view) 
	{
		if (view == saveButton)
		{
			if (baseActivity.SELECTED_OBJECT != null && baseActivity.SELECTED_FIELD != null)
			{
				Bundle args = new Bundle();
				String saveString = EvernoteUtils.stripNoteHTMLContent(noteContent);
				NotepriseLogger.logMessage("Saving string==" + saveString);
				args.putString("noteContent", saveString);
				changeScreen(new NotepriseFragment("RecordsList", SalesforceRecordsList.class, args));
			}
			else
			{
				showToastNotification(getString(R.string.salesforce_select_object_field_message));
				changeScreen(new NotepriseFragment("SalesforceObjectChooser", SalesforceObjectChooser.class));
			}			
		}
		else if (view == baseActivity.deleteNoteButton)
		{
			try 
			{
				authToken = evernoteSession.getAuthToken();
	        	client = evernoteSession.createNoteStore();
			}
			catch (TException e) 
			{
				e.printStackTrace();
			}
			commonMessageDialog.showDeleteNoteDialog(authToken, client, this);
		}
		else if (view == editButton)
		{
		    Bundle args = new Bundle();
		    args.putString("noteGuid", noteGuid);
			changeScreen(new NotepriseFragment("NoteEditScreen", NoteEditScreen.class,args));
		}
		else if (view == publishToChatterButton)
		{
			
			publishToChatterButton.showContextMenu();
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) 
	{
		if (view == publishToChatterButton)
		{
			MenuInflater inflater = baseActivity.getMenuInflater();
		    inflater.inflate(R.menu.chatter_context_menu, menu);
            menu.setHeaderView(this.inflater.inflate(R.layout.chatter_menu_header_view_layout, null));
        }		
		super.onCreateContextMenu(menu, view, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		if (item.getItemId() == R.id.chatter_menu_post_my_feed)
		{
			TASK = PUBLISH_TO_MY_CHATTER_FEED;
			showFullScreenProgresIndicator();
			executeAsyncTask();
		}
		else if (item.getItemId() == R.id.chatter_menu_post_user_feed)
		{
			Bundle args = new Bundle();
			String publishString = EvernoteUtils.stripNoteHTMLContent(noteContent);
		    args.putString("publishString", publishString);
		    args.putString("publishTask", "USER_FEED");
			changeScreen(new NotepriseFragment("PublishToChatterRecordsList", PublishToChatterRecordsListScreen.class, args));
		}
		else if (item.getItemId() == R.id.chatter_menu_post_group_feed)
		{
			Bundle args = new Bundle();
			String publishString = EvernoteUtils.stripNoteHTMLContent(noteContent);
		    args.putString("publishString", publishString);
		    args.putString("publishTask", "GROUP_FEED");
			changeScreen(new NotepriseFragment("PublishToChatterRecordsList", PublishToChatterRecordsListScreen.class, args));
		}		
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		baseActivity.createNewNoteButton.setVisibility(View.GONE);
		baseActivity.deleteNoteButton.setVisibility(View.VISIBLE);
		showProgresIndicator();
		executeAsyncTask();			
	}
	
	@Override
	public void onStop() 
	{
		super.onStop();
		removeViewFromBaseHeaderLayout(topButtonBar);
		removeViewFromBaseHeaderLayout(editButton);
		baseActivity.createNewNoteButton.setVisibility(View.VISIBLE);
		baseActivity.deleteNoteButton.setVisibility(View.GONE);
	}
	
	@Override
	public void doTaskInBackground() 
	{
		super.doTaskInBackground();
		if (TASK == GET_NOTE_DATA)
		{
			if (evernoteSession != null)
		    {
				try 
				{
					authToken = evernoteSession.getAuthToken();
		        	client = evernoteSession.createNoteStore();
		        	note = client.getNote(authToken, noteGuid, true, false, false, false);
				} 
				catch (TTransportException e) 
				{
					e.printStackTrace();
				} 
				catch (EDAMUserException e) 
				{
					e.printStackTrace();
				} 
				catch (EDAMSystemException e) 
				{
					e.printStackTrace();
				} 
				catch (TException e) 
				{
					e.printStackTrace();
				} 
				catch (EDAMNotFoundException e) 
				{
					e.printStackTrace();
				}
		    }
		}
		else if (TASK == DELETE_NOTE)
		{
			deletionId = EvernoteUtils.deleteNote(authToken, client, noteGuid);
		}
		else if (TASK == PUBLISH_TO_MY_CHATTER_FEED)
		{
			String publishString = EvernoteUtils.stripNoteHTMLContent(noteContent);
			publishResponse = SalesforceUtils.publishNoteToMyChatterFeed(salesforceRestClient, publishString, SF_API_VERSION);
		}
	}
	
	@Override
	public void onTaskFinished() 
	{
		super.onTaskFinished();
		if (TASK == GET_NOTE_DATA)
		{
			hideProgresIndicator();
	    	noteTitle = note.getTitle();
	    	noteContent = note.getContent();//EvernoteUtils.stripNoteContent(note.getContent());
	    	setHeaderTitle(noteTitle);
	    	noteContentWebView.loadData(noteContent, "text/html", "utf-8");
	    	topButtonBar.setVisibility(View.VISIBLE);
	    	editButton.setVisibility(View.VISIBLE);
		}
		else if(TASK == DELETE_NOTE)
		{
			TASK = GET_NOTE_DATA;
			hideFullScreenProgresIndicator();
			if (deletionId != null)
			{
				showToastNotification(getString(R.string.note_delete_success_message));
				baseActivity.previousScreenAction = Constants.DELETE_NOTE_ACTION;
				finishScreen();
			}
			else
			{
				showToastNotification(getString(R.string.note_delete_failed_message));
			}
		}
		else if (TASK == PUBLISH_TO_MY_CHATTER_FEED)
		{
			TASK = GET_NOTE_DATA;
			hideFullScreenProgresIndicator();
			if (publishResponse != null)
			{
				try 
				{
					NotepriseLogger.logMessage(publishResponse.asString());
				} 
				catch (ParseException e) 
				{
					e.printStackTrace();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				showToastNotification(getString(R.string.salesforce_chatter_post_self_success_message));
			}
			else
			{
				showToastNotification(getString(R.string.some_error_ocurred_message));
			}
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) 
	{
		if (which == -1) // For Positive Button
		{
			TASK = DELETE_NOTE;
			showFullScreenProgresIndicator();
			executeAsyncTask();
		}
		else
		{
			dialog.dismiss();
		}		
	}

	@Override
	public void onDismiss() 
	{
				
	}
}
