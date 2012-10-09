package com.metacube.noteprise.core.screens;

import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;

import com.evernote.edam.notestore.NoteStore.Client;
import com.evernote.edam.type.Note;
import com.metacube.noteprise.R;
import com.metacube.noteprise.common.BaseFragment;
import com.metacube.noteprise.common.Constants;
import com.metacube.noteprise.evernote.EvernoteUtils;
import com.metacube.noteprise.util.NotepriseLogger;
import com.metacube.noteprise.util.Utilities;
import com.metacube.noteprise.util.richtexteditor.Html;
import com.metacube.noteprise.util.richtexteditor.RichTextEditor;

public class NoteEditScreen extends BaseFragment implements OnClickListener, android.content.DialogInterface.OnClickListener, OnTouchListener
{
	String authToken;
	Client client;
	WebView noteContentWebView;
	String noteTitle, noteContent, noteGuid;
	Note note;
	EditText noteTitleEditText, noteContenteditText;
	int GET_DATA = 0, SAVE_DATA = 1, CURRENT_TASK = 0;
	Integer GET_NOTE_DATA = 0, UPDATE_NOTE = 1, TASK = 0, deletionId = null;
	String saveString,saveTitleString;
	RichTextEditor richTexteditor;
	
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
		View contentView = inflater.inflate(R.layout.note_edit_screen_layout, container);
		noteContenteditText = (EditText) contentView.findViewById(R.id.content);
		noteTitleEditText  = (EditText) contentView.findViewById(R.id.title);
		baseActivity.saveButton.setOnClickListener(this);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
    
    @Override
    public void onStart() 
    {
    	super.onStart();    	
    }

	@Override
	public void onClick(View view) 
	{
		if(view == baseActivity.saveButton)
		{ 
			TASK = UPDATE_NOTE;
			showFullScreenProgresIndicator(getString(R.string.progress_dialog_title),getString(R.string.progress_dialog_note_update_message));
			executeAsyncTask();	
		}
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		TASK = GET_NOTE_DATA;
        showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_getting_note_details_mesage));
		executeAsyncTask();		
	}
	
	@Override
	public void onStop() 
	{
		super.onStop();
		baseActivity.saveButton.setVisibility(View.GONE);	
	}
	
	@Override
	public void doTaskInBackground() 
	{
		super.doTaskInBackground();
		if (TASK == GET_NOTE_DATA)
		{
			if (evernoteSession != null)
		    {
				
				note =	EvernoteUtils.getNotedata(evernoteSession,noteGuid,false);
				
		    }
		}	
		else if (TASK == UPDATE_NOTE)
		{
			authToken = evernoteSession.getAuthToken();
        	try {
				client = evernoteSession.createNoteStore();
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			note.setContent(Constants.NOTE_PREFIX +Html.toHtml(noteContenteditText.getText())+Constants.NOTE_SUFFIX);			
			note.setContent(note.getContent().replace("<br>", "<br />"));
			note.setContent(note.getContent().replace("&#160;", ""));
			
		
			note.setTitle(noteTitleEditText.getText().toString());
			
			NotepriseLogger.logMessage(note.getContent());
			NotepriseLogger.logMessage(note.getTitle());
			EvernoteUtils.updateNote(authToken, client, note);
		}		
	}
	
	@Override
	public void onTaskFinished() 
	{
		super.onTaskFinished();
		if (TASK == GET_NOTE_DATA)
		{
			hideFullScreenProgresIndicator();	    	
	    	noteContent = note.getContent();
	    	noteTitle = note.getTitle();
	    	NotepriseLogger.logMessage(noteContent);
	    	saveTitleString = noteTitle.replace(Constants.NOTE_PREFIX, "");
	    	saveTitleString = noteTitle.replace(Constants.NOTE_SUFFIX, "");
	    	
	    	saveString= noteContent.replace(Constants.NOTE_PREFIX, "");
	    	saveString= noteContent.replace(Constants.NOTE_SUFFIX, "");
	    	noteContenteditText.setText(Html.fromHtml(saveString));	
	    	noteTitleEditText.setText(Html.fromHtml(saveTitleString));
	    	baseActivity.saveButton.setVisibility(View.VISIBLE);
		}
		else if (TASK == UPDATE_NOTE)
		{			
			
			hideFullScreenProgresIndicator();
			//EvernoteUtils.updateNote(authToken, client, note);
			finishScreen();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) 
	{
				
	}
	
	@Override
    public boolean onTouch(View view, MotionEvent event) 
    {
        switch (event.getAction()) 
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
                if (!view.hasFocus()) 
                {
                    view.requestFocus();
                }
                break;
        }
        return false;
    }
}