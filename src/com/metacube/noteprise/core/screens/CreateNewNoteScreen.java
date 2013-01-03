package com.metacube.noteprise.core.screens;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore.Client;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.metacube.noteprise.R;
import com.metacube.noteprise.common.BaseFragment;
import com.metacube.noteprise.common.CommonCustomDialog;
import com.metacube.noteprise.common.CommonListItems;
import com.metacube.noteprise.common.CommonSpinnerAdapter;
import com.metacube.noteprise.common.Constants;
import com.metacube.noteprise.common.base.NotepriseFragment;
import com.metacube.noteprise.util.Utilities;
import com.metacube.noteprise.util.richtexteditor.Html;

public class CreateNewNoteScreen extends BaseFragment implements OnClickListener, OnItemClickListener
{	
	List<Notebook> notebookList;
	Button notebookListSpinnerButton;
	String authToken;
	Client client;
	ArrayList<CommonListItems> spinnerItems;
	CommonSpinnerAdapter notebookSpinnerAdapter;
	EditText noteTitleEditText, noteContenteditText;
	int GET_DATA = 0, SAVE_DATA = 1, CURRENT_TASK = 0;
	Note createdNote, savedNote;
	CommonCustomDialog notebookSpinnerDialog;
	TextView notebookSpinnerDialogPromptText;
	ListView notebookSpinnerListView;
	String selectedNotebookGuid = null;
	
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		initFragment("Create Note");
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        notebookSpinnerDialog = new CommonCustomDialog(R.layout.custom_spinner_dropdown_layout, this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	clearContainer(container);
    	View contentView = inflater.inflate(R.layout.create_new_note_layout, container);    	
    	notebookListSpinnerButton = (Button) contentView.findViewById(R.id.create_note_notebook_list_spinner); 
    	notebookListSpinnerButton.setOnClickListener(this);
    	noteTitleEditText = (EditText) contentView.findViewById(R.id.note_title_edit_text);
    	noteContenteditText = (EditText) contentView.findViewById(R.id.content);
    	baseActivity.saveButton.setOnClickListener(this);
    	return super.onCreateView(inflater, container, savedInstanceState);
    }

	@Override
	public void onClick(View view) 
	{
		if (view == baseActivity.saveButton)
		{
			String noteTitle = noteTitleEditText.getText().toString().trim();
			String noteContent = noteContenteditText.getText().toString().trim();
			if (Utilities.verifyStringData(noteTitle) && Utilities.verifyStringData(noteContent) && Utilities.verifyStringData(selectedNotebookGuid))
			{
				CURRENT_TASK = SAVE_DATA;
				showFullScreenProgresIndicator(getString(R.string.progress_dialog_title),getString(R.string.progress_dialog_note_create_message));
				executeAsyncTask();
			}
			else
			{
				showToastNotification(getString(R.string.note_creation_all_fields_required_message));
			}
		}
		else if (view == notebookListSpinnerButton)
		{
			if (notebookSpinnerDialog != null)
			{
				notebookSpinnerDialog.show(getFragmentManager(), "NotebookSpinnerDialog");
			}			
		}
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		showProgresIndicator();
		executeAsyncTask();		
	}
	
	@Override
	public void onStop() 
	{
		super.onStop();
		baseActivity.saveButton.setVisibility(View.GONE);
	}
	
	@Override
	public void instantiateCustomDialog(View view) 
	{
		super.instantiateCustomDialog(view);
		if (((Integer) view.getTag()) == R.layout.custom_spinner_dropdown_layout)
		{
			notebookSpinnerDialogPromptText = (TextView) view.findViewById(R.id.custom_spinner_prompt_text);
			notebookSpinnerDialogPromptText.setText(R.string.choose_notebook_prompt);
			notebookSpinnerListView = (ListView) view.findViewById(R.id.custom_spinner_list_view);
			notebookSpinnerListView.setAdapter(notebookSpinnerAdapter);
			notebookSpinnerListView.setOnItemClickListener(this);
		}
	}
	
	@Override
	public void doTaskInBackground() 
	{
		super.doTaskInBackground();
		if (CURRENT_TASK == GET_DATA)
		{
			try 
			{
				
				authToken = evernoteSession.getAuthToken();
	        	client = evernoteSession.createNoteStore();
	        	notebookList = client.listNotebooks(authToken);
	        	spinnerItems = new ArrayList<CommonListItems>();
	        	for (int i = 0; i < notebookList.size(); i++)
	        	{
	        		Notebook notebook = notebookList.get(i);
	        		CommonListItems item = new CommonListItems();
	        		item.setLabel(notebook.getName());
	        		item.setId(notebook.getGuid());
	        		spinnerItems.add(item);
	        	}        	
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
		}
		else if (CURRENT_TASK == SAVE_DATA)
		{
			try 
			{
				authToken = evernoteSession.getAuthToken();
	        	client = evernoteSession.createNoteStore();
	        	createdNote = new Note();	        	
	        	createdNote.setNotebookGuid(selectedNotebookGuid);
	        	createdNote.setTitle(noteTitleEditText.getText().toString().trim());
				createdNote.setContent(Constants.NOTE_PREFIX + Html.toHtml(noteContenteditText.getText()) + Constants.NOTE_SUFFIX);
	        	createdNote.setContent(createdNote.getContent().replace("<br>", "<br />"));
	        	createdNote.setContent(createdNote.getContent().replace("&#160;", ""));	        	
	        	savedNote = client.createNote(authToken, createdNote);
			} 
			catch (TTransportException e) 
			{
				e.printStackTrace();
			} 
			catch (TException e) 
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
			catch (EDAMNotFoundException e) 
			{
				e.printStackTrace();
			}

		}
	}
	
	@Override
	public void onTaskFinished() 
	{
		super.onTaskFinished();
		if (CURRENT_TASK == GET_DATA)
		{
			if (notebookList != null)
	        {
				hideProgresIndicator();
				baseActivity.saveButton.setVisibility(View.VISIBLE);
				notebookSpinnerAdapter = new CommonSpinnerAdapter(inflater, spinnerItems);
	        }
		}
		else if (CURRENT_TASK == SAVE_DATA)
		{
			CURRENT_TASK = GET_DATA;
			hideFullScreenProgresIndicator();
			if (savedNote != null && savedNote.getGuid() != null)
			{
				showToastNotification(getString(R.string.note_created_success_message));
				baseActivity.previousScreenAction = Constants.CREATE_NOTE_ACTION;
				
			baseActivity.savedCurrentTask = 0;
			baseActivity.savedListAdapter = null;
			baseActivity.savedQueryString = "";
			baseActivity.isDataSaved = true;
			baseActivity.savedSelectedRadioButtonId= R.id.search_all_radio_button;
			finishScreen();
			
				//changeScreen(new NotepriseFragment("MainMenuScreen", MainMenuScreen.class));
				
				
			}
			else
			{
				showToastNotification(getString(R.string.note_creation_failed_message));
			}			
		}		
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) 
	{
		if (adapter.getAdapter() == notebookSpinnerAdapter)
		{
			selectedNotebookGuid = notebookSpinnerAdapter.getListItem(position).getId();
			notebookSpinnerDialog.dismiss();
			notebookListSpinnerButton.setText(notebookSpinnerAdapter.getListItem(position).getLabel());
		}
	}
}