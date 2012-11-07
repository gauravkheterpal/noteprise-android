package com.metacube.noteprise.core.screens;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore.Client;
import com.evernote.edam.type.Notebook;
import com.metacube.noteprise.R;
import com.metacube.noteprise.common.BaseActivity;
import com.metacube.noteprise.common.BaseFragment;
import com.metacube.noteprise.common.CommonCustomDialog;
import com.metacube.noteprise.common.CommonListAdapter;
import com.metacube.noteprise.common.CommonListItems;
import com.metacube.noteprise.common.Constants;
import com.metacube.noteprise.common.base.NotepriseFragment;
import com.metacube.noteprise.core.NotepriseActivity;
import com.metacube.noteprise.evernote.EvernoteUtils;
import com.metacube.noteprise.util.Utilities;

public class MainMenuScreen extends BaseFragment implements OnClickListener,
		OnItemClickListener, OnCheckedChangeListener {
	List<Notebook> notebookList;
	ListView listView;
	String authToken;
	Client client;
	NoteList noteList;
	ArrayList<CommonListItems> listItems, previousList, previousListForSearch;
	CommonListAdapter noteListAdapter, oldListAdapter, oldListAdapterForSearch;
	CommonCustomDialog logoutConfirmationDialog;
	RadioGroup searchCriteriaRadioGroup;
	ImageButton searchButton, cancelButton;
	ImageView listSectionUpButton;
	RadioButton allRadioButton, notebookRadioButton, tagRadioButton;
	EditText searchQueryEditText;
	String queryString;
	Integer selectedRadioButtonId;
	Boolean isDataRestored = Boolean.FALSE, isInnerList = Boolean.FALSE;
	Button logoutConfirmationDialogYesButton, logoutConfirmationDialogNoButton;
	Integer GET_ALL_NOTEBOOKS = 0, GET_NOTE_LIST_FOR_NOTEBOOK = 1,
			SEARCH_KEYWORD = 2, GET_NOTEBOOK = 3, CLEAR_LIST = 4, GET_TAGS = 5,
			GET_NOTE_LIST_FOR_TAG = 6, TASK = 0, PREVIOUS_TASK = 0,
			PREVIOUS_TASK_FOR_SEARCH = 0;
	String selectedNotebookID = "", selectedTagID = "";

	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		clearContainer(container);
		View contentView = inflater.inflate(R.layout.home_screen_layout,
				container);
		listView = (ListView) contentView.findViewById(R.id.notes_list_view);
		searchCriteriaRadioGroup = (RadioGroup) contentView
				.findViewById(R.id.search_criteria_radio_group);
		searchCriteriaRadioGroup.setOnCheckedChangeListener(this);
		searchQueryEditText = (EditText) contentView
				.findViewById(R.id.search_query_edit_text);
		// searchQueryEditText.addTextChangedListener(this);
		// searchQueryEditText.setOnFocusChangeListener(this);
		searchQueryEditText.setOnClickListener(this);

		allRadioButton = (RadioButton) contentView
				.findViewById(R.id.search_all_radio_button);
		tagRadioButton = (RadioButton) contentView
				.findViewById(R.id.search_tag_radio_button);
		notebookRadioButton = (RadioButton) contentView
				.findViewById(R.id.search_notebook_radio_button);

		// searchQueryEditText.setImeActionLabel(getString(R.string.search_field_hint_text),
		// R.id.search_button);

		searchButton = (ImageButton) contentView
				.findViewById(R.id.search_button);
		cancelButton = (ImageButton) contentView
				.findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(this);
		searchButton.setOnClickListener(this);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onClick(View view) {
		if (view == searchButton) {
			String queryString = searchQueryEditText.getText().toString()
					.trim();
			if (!Utilities.verifyStringData(queryString)) {
				showToastNotification(getString(R.string.note_please_enter_text_for_search_message));
				return;
			}
			Integer searchMessage = R.string.progress_dialog_note_search_message;
			listView.setAdapter(null);

			TASK = SEARCH_KEYWORD;
			searchMessage = R.string.progress_dialog_keyword_search_message;
			executeAsyncTask();

			showFullScreenProgresIndicator(
					getString(R.string.progress_dialog_title),
					getString(searchMessage));
		}

		else if (view == baseActivity.logoutButton) {

			logoutConfirmationDialog = new CommonCustomDialog(
					R.layout.logout_confirmation_dialog_layout, this, null);
			logoutConfirmationDialog.show(getFragmentManager(),
					"LogoutConfirmationDialog");

		}

		else if (view == logoutConfirmationDialogYesButton) {
			((NotepriseActivity) baseActivity).signOutFromEvernote();
			logoutConfirmationDialog.dismiss();

		} else if (view == logoutConfirmationDialogNoButton) {
			logoutConfirmationDialog.dismiss();
		}

		else if (view == searchQueryEditText) {
			hideRadioGroup(Boolean.TRUE);
			
			savePreviousList(true);

			TASK = CLEAR_LIST;
			executeAsyncTask();

		} else if (view == cancelButton) {
			if (searchCriteriaRadioGroup.getVisibility() != View.VISIBLE) {

				hideRadioGroup(Boolean.FALSE);
				searchCriteriaRadioGroup.setEnabled(Boolean.TRUE);
				searchQueryEditText.setText("");

				if (PREVIOUS_TASK_FOR_SEARCH == GET_NOTE_LIST_FOR_NOTEBOOK
						|| PREVIOUS_TASK_FOR_SEARCH == GET_NOTE_LIST_FOR_TAG)
					isInnerList = true;
				else
					isInnerList = false;

				loadPreviousList(true);

			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		setHeaderTitle(null);
		listView.requestFocus();
		listView.requestFocusFromTouch();

		baseActivity.logoutButton.setVisibility(View.VISIBLE);
		baseActivity.logoutButton.setOnClickListener(MainMenuScreen.this);
		baseActivity.createNewNoteButton.setVisibility(View.VISIBLE);
		baseActivity.salesforceObjectsButton.setVisibility(View.VISIBLE);
		baseActivity.saveToSFButton.setVisibility(View.GONE);
		baseActivity.publishToChatterButton.setVisibility(View.GONE);

		if (isEvernoteAuthenticationComplete()) {
			if (baseActivity.isDataSaved
					&& checkPreviousScreenActionForNotRefresh()) {
				loadPreviousState();
			} else {
				getAllNotes();

			}
		} else {

		}
	}

	@Override
	public void instantiateCustomDialog(View view) {
		super.instantiateCustomDialog(view);

		logoutConfirmationDialogYesButton = (Button) view
				.findViewById(R.id.delete_note_yes_button);
		logoutConfirmationDialogYesButton.setOnClickListener(this);
		logoutConfirmationDialogNoButton = (Button) view
				.findViewById(R.id.delete_note_no_button);
		logoutConfirmationDialogNoButton.setOnClickListener(this);

	}

	@Override
	public void doTaskInBackground() {
		super.doTaskInBackground();
		try {
			authToken = evernoteSession.getAuthToken();
			client = evernoteSession.createNoteStore();
			queryString = searchQueryEditText.getText().toString().trim();

			if (TASK == GET_ALL_NOTEBOOKS) {

				listItems = EvernoteUtils.getAllNotes(authToken, client);

			}

			else if (TASK == SEARCH_KEYWORD) {

				listItems = EvernoteUtils.searchKeywords(authToken, client,
						queryString);
			} else if (TASK == GET_NOTEBOOK) {

				listItems = (ArrayList<CommonListItems>) EvernoteUtils
						.getNotebooksList(authToken, client);

			} else if (TASK == GET_TAGS) {

				listItems = (ArrayList<CommonListItems>) EvernoteUtils
						.getTagsList(authToken, client);

			} else if (TASK == CLEAR_LIST) {

				listItems = new ArrayList<CommonListItems>();
				// executeAsyncTask();
			} else if (TASK == GET_NOTE_LIST_FOR_NOTEBOOK) {

				listItems = (ArrayList<CommonListItems>) EvernoteUtils
						.getNoteListFromNotebook(authToken, client,
								selectedNotebookID);
			} else if (TASK == GET_NOTE_LIST_FOR_TAG) {

				listItems = EvernoteUtils.getNoteListForTag(evernoteSession,
						selectedTagID);

			}

		} catch (TTransportException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onTaskFinished() {
		super.onTaskFinished();
		hideFullScreenProgresIndicator();
		if (listItems != null) {
			noteListAdapter = new CommonListAdapter(MainMenuScreen.this,
					inflater, listItems);

			if (TASK == GET_NOTE_LIST_FOR_NOTEBOOK
					|| TASK == GET_NOTE_LIST_FOR_TAG) {
				noteListAdapter.isInnerList(Boolean.TRUE);
				isInnerList = true;
				listView.setAdapter(noteListAdapter);

			} else {
				listView.setAdapter(noteListAdapter);

			}

			listView.setOnItemClickListener(this);
			// TASK = GET_ALL_NOTEBOOKS;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {

		if (listItems.get(position).getListItemType()
				.equalsIgnoreCase(Constants.LIST_ITEM_TYPE_NOTE)) {
			if (noteListAdapter.isListItem(position)) {

				saveCurrentState();

				String noteGuid = noteListAdapter.getListItemId(position);
				Bundle args = new Bundle();
				args.putString("noteGuid", noteGuid);
				changeScreen(new NotepriseFragment("NoteDetails",
						NoteDetailsScreen.class, args));
			}
		} else if (listItems.get(position).getListItemType()
				.equalsIgnoreCase(Constants.LIST_ITEM_TYPE_NOTEBOOK)) {

			if (noteListAdapter.isListItem(position)) {
				selectedNotebookID = listItems.get(position).getId();
				savePreviousList(false);

				TASK = GET_NOTE_LIST_FOR_NOTEBOOK;
				showFullScreenProgresIndicator(
						getString(R.string.progress_dialog_title),
						getString(R.string.progress_dialog_getting_notes_for_Notebook_message));

				executeAsyncTask();

			}

		} else if (listItems.get(position).getListItemType()
				.equalsIgnoreCase(Constants.LIST_ITEM_TYPE_TAG)) {
			if (noteListAdapter.isListItem(position)) {

				selectedTagID = listItems.get(position).getId();

				savePreviousList(false);
				TASK = GET_NOTE_LIST_FOR_TAG;
				showFullScreenProgresIndicator(
						getString(R.string.progress_dialog_title),
						getString(R.string.progress_dialog_getting_notes_for_tag_message));

				executeAsyncTask();

			}

		}

	}

	public void getAllNotes() {
		if (evernoteSession != null) {
			// setSearchBarEnabled(Boolean.FALSE);
			showFullScreenProgresIndicator(
					getString(R.string.progress_dialog_title),
					getString(R.string.progress_dialog_getting_all_notes_message));
			executeAsyncTask();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		saveCurrentState();
		baseActivity.logoutButton.setVisibility(View.GONE);
		baseActivity.createNewNoteButton.setVisibility(View.GONE);
		baseActivity.salesforceObjectsButton.setVisibility(View.GONE);
	}

	public void saveCurrentState() {
		baseActivity.savedListAdapter = noteListAdapter;
		//baseActivity.listItems = listItems; 
		baseActivity.savedCurrentTask = TASK;
		baseActivity.savedQueryString = queryString;
		baseActivity.savedSelectedRadioButtonId = searchCriteriaRadioGroup
				.getCheckedRadioButtonId();
		baseActivity.isDataSaved = Boolean.TRUE;
	}

	public void loadPreviousState() {
		if (baseActivity.savedListAdapter != null) {
			noteListAdapter = baseActivity.savedListAdapter;
			//listItems = baseActivity.listItems;

			if (isInnerList)
				noteListAdapter.isInnerList(Boolean.TRUE);
			else
				noteListAdapter.isInnerList(Boolean.FALSE);

			isInnerList = false;

			listView.setAdapter(noteListAdapter);

			listView.setOnItemClickListener(this);

		}
		if (baseActivity.savedQueryString != null) {
			queryString = baseActivity.savedQueryString;
			searchQueryEditText.setText(queryString);
		}
		if (baseActivity.savedCurrentTask != null) {
			TASK = baseActivity.savedCurrentTask;
		}
		if (baseActivity.savedSelectedRadioButtonId != null) {
			if (baseActivity.savedSelectedRadioButtonId != R.id.search_all_radio_button) {
				isDataRestored = Boolean.TRUE;
			} else {
				isDataRestored = Boolean.FALSE;
			}
			selectedRadioButtonId = baseActivity.savedSelectedRadioButtonId;
			searchCriteriaRadioGroup.check(selectedRadioButtonId);

		}
		if (noteListAdapter == null) {
			getAllNotes();
		}
	}

	public void setSearchBarEnabled(Boolean status) {
		searchQueryEditText.setEnabled(status);
		searchButton.setEnabled(status);
	}

	public void hideRadioGroup(Boolean status) {
		if (status)
			searchCriteriaRadioGroup.setVisibility(View.GONE);
		else
			searchCriteriaRadioGroup.setVisibility(View.VISIBLE);

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {

		isInnerList = false;

		if (group == searchCriteriaRadioGroup
				&& checkedId == R.id.search_all_radio_button) {
			// setSearchBarEnabled(Boolean.TRUE);
			TASK = GET_ALL_NOTEBOOKS;
			if (isDataRestored) {
				isDataRestored = Boolean.FALSE;
			}
			// else {
			getAllNotes();
			// }
		} else if (group == searchCriteriaRadioGroup
				&& checkedId == R.id.search_notebook_radio_button) {

			// setSearchBarEnabled(Boolean.TRUE);
			TASK = GET_NOTEBOOK;
			showFullScreenProgresIndicator(
					getString(R.string.progress_dialog_title),
					getString(R.string.progress_dialog_getting_all_notebook_message));
			executeAsyncTask();

		} else if (group == searchCriteriaRadioGroup
				&& checkedId == R.id.search_tag_radio_button) {
			// setSearchBarEnabled(Boolean.TRUE);
			TASK = GET_TAGS;
			showFullScreenProgresIndicator(
					getString(R.string.progress_dialog_title),
					getString(R.string.progress_dialog_getting_all_tags_message));
			executeAsyncTask();

		}
	}

	public void loadPreviousList(Boolean isSearch) {

		if (isSearch) {
			TASK = PREVIOUS_TASK_FOR_SEARCH;
			listItems = previousListForSearch;
			noteListAdapter = oldListAdapterForSearch;
		} else {
			TASK = PREVIOUS_TASK;
			listItems = previousList;
			noteListAdapter = oldListAdapter;

		}

		if (isInnerList)
			noteListAdapter.isInnerList(Boolean.TRUE);
		else
			noteListAdapter.isInnerList(Boolean.FALSE);

		listView.setAdapter(noteListAdapter);
		listView.setOnItemClickListener(this);

		isInnerList = false;

	}

	public void savePreviousList(Boolean isSearch) {

		if (isSearch) {
			PREVIOUS_TASK_FOR_SEARCH = TASK;
			previousListForSearch = listItems;
			oldListAdapterForSearch = noteListAdapter;
		} else {
			PREVIOUS_TASK = TASK;
			previousList = listItems;
			oldListAdapter = noteListAdapter;
		}
	}

}