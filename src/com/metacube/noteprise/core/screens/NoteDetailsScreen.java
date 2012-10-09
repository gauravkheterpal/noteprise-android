package com.metacube.noteprise.core.screens;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.ParseException;
import org.apache.thrift.TException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import com.evernote.edam.notestore.NoteStore.Client;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;
import com.metacube.noteprise.R;
import com.metacube.noteprise.common.BaseFragment;
import com.metacube.noteprise.common.CommonCustomDialog;
import com.metacube.noteprise.common.CommonListAdapter;
import com.metacube.noteprise.common.CommonListItems;
import com.metacube.noteprise.common.Constants;
import com.metacube.noteprise.common.base.NotepriseFragment;
import com.metacube.noteprise.evernote.EvernoteUtils;
import com.metacube.noteprise.salesforce.CommonSOQL;
import com.metacube.noteprise.salesforce.SalesforceUtils;
import com.metacube.noteprise.util.NotepriseLogger;
import com.metacube.noteprise.util.Utilities;
import com.metacube.noteprise.util.imageloader.ImageLoader;
import com.metacube.noteprise.util.richtexteditor.Html;
import com.salesforce.androidsdk.rest.RestResponse;

public class NoteDetailsScreen extends BaseFragment implements OnClickListener, android.content.DialogInterface.OnClickListener, OnDismissListener,OnItemClickListener
{
	String authToken;
	Client client;
	WebView noteContentWebView;
	String noteTitle, noteContent, noteGuid, mediaString, publishString,encodeImage;
	Note note;
	String fileName,attachmentName;
	Bitmap bitmap;
	int checkedItemPosition= -1,response;
	ArrayList<CommonListItems> listItems=null,listitemsChatter=null,listitemsChatterImage=null,listitemsUserChatter=null,listitemsUserChatterImage=null,listitemsGroupChatter=null,listitemsGroupChatterImage=null;
	CommonListAdapter listAdapter,listAdapterChatter,listAdapterChatterImage,listAdapterUserChatter,listAdapterUserChatterImage,listAdapterGroupChatter,listAdapterGroupChatterImage;
	ListView listView,listViewChatter,listViewChatterImage,listViewUserChatter,listViewUserChatterImage,listViewGroupChatter,listViewGroupChatterImage;
	byte[] byteimage;
	ArrayList<String> selectedIds = null;
	boolean mediaContent= false;
	RestResponse publishResponse;
	Integer GET_NOTE_DATA = 0, DELETE_NOTE = 1, PUBLISH_TO_MY_CHATTER_FEED = 2, TASK = 0, deletionId = null, TRUNCATE_NOTE = 3,TEXT_ONLY=4,ATTACHMENT_ONLY=5,TEXT_ATTACHMENT=6;
	public String SD_CARD = Environment.getExternalStorageDirectory().getAbsolutePath(),saveString=null;
	protected String[] _options =null;
	protected boolean[] selection=null;
	Button deleteDialogYesButton, readOnlyDialogOkButton,deleteDialogNoButton, okayButton,truncateDialogYesButton,truncateDialogNoButton, chatterTruncateDialogYesButton, chatterTruncateDialogNoButton,okayChatterButton,okayUserChatterButton,okayGroupChatterButton;
	CommonCustomDialog deleteDialog,readOnlyDialog, imageAttachDialog, truncateContentDialog, chatterTruncateDialog,chatterImageDialog,chatterAttachmentListDialog,chatterUserImageDialog;
	TextView chatterTruncateDialogHeaderText, chatterTruncateDialogMessage,sfTruncateDialogMessage; 
	String CHATTER_TRUNCATE_DIALOG_TAG = "CHATTER_TRUNCATE_DIALOG_TAG", DELETE_DIALOG_TAG = "DELETE_DIALOG_TAG", READ_ONLY_DIALOG_TAG = "READ_ONLY_DIALOG_TAG",
			SF_TRUNCATE_DIALOG_TAG = "SF_TRUNCATE_DIALOG_TAG", SF_ATTACHMENTS_DIALOG_TAG = "SF_ATTACHMENTS_DIALOG_TAG",CHATTER_ATTACHMENT_DIALOG_TAG="CHATTER_ATTACHMENT_DIALOG_TAG",
			CHATTER_ATTACHMENT_LIST_DIALOG_TAG="CHATTER_ATTACHMENT_LIST_DIALOG_TAG",CHATTER_USER_ATTACHMENT_LIST_DIALOG_TAG="CHATTER_USER_ATTACHMENT_LIST_DIALOG_TAG",CHATTER_USER_ATTACHMENT_DIALOG_TAG="CHATTER_USER_ATTACHMENT_DIALOG_TAG",CHATTER_GROUP_ATTACHMENT_DIALOG_TAG="CHATTER_GROUP_ATTACHMENT_DIALOG_TAG",
				CHATTER_GROUP_ATTACHMENT_LIST_DIALOG_TAG="CHATTER_GROUP_ATTACHMENT_LIST_DIALOG_TAG";
	int exceedLength=0;
	int exceedFieldLength=0;
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
    	baseActivity.saveToSFButton.setOnClickListener(this);
    	baseActivity.editButton.setOnClickListener(this);
    	baseActivity.publishToChatterButton.setOnClickListener(this);
    	baseActivity.deleteNoteButton.setOnClickListener(this);
    	registerForContextMenu(baseActivity.saveToSFButton);
    	registerForContextMenu(baseActivity.publishToChatterButton);	
    	return super.onCreateView(inflater, container, savedInstanceState);
    }

	@Override
	public void onClick(View view) 
	{		
		if (view == baseActivity.saveToSFButton)
		{			
			if (baseActivity.SELECTED_OBJECT_NAME != null && baseActivity.SELECTED_FIELD_NAME != null)
			{				
				if(((CommonSOQL.getSupportedObject(baseActivity.SELECTED_OBJECT_NAME))== false && note.getResources() == null) ||
						((CommonSOQL.getSupportedObject(baseActivity.SELECTED_OBJECT_NAME))== true && note.getResources() == null)
						|| ((CommonSOQL.getSupportedObject(baseActivity.SELECTED_OBJECT_NAME))== false && note.getResources() != null))
				{
					if(baseActivity.SELECTED_FIELD_LENGTH >=Html.fromHtml(publishString).length())
					{
						Bundle args = new Bundle();
						selectedIds=null;
						String saveString = EvernoteUtils.stripNoteHTMLContent(noteContent);
						args.putString("noteContent", saveString);				
						args.putStringArrayList("Attachment",selectedIds );
						changeScreen(new NotepriseFragment("RecordsList", SalesforceRecordsList.class, args));
					}
					else
					{
						exceedFieldLength = Html.fromHtml(publishString).length()-baseActivity.SELECTED_FIELD_LENGTH ;
						truncateContentDialog = new CommonCustomDialog(R.layout.note_content_truncate_dialog, this, SF_TRUNCATE_DIALOG_TAG);
						truncateContentDialog.show(getFragmentManager(), "TruncateContentDialog");
					}
				}
				else
				{
					baseActivity.saveToSFButton.showContextMenu();
				}		
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
			deleteDialog = new CommonCustomDialog(R.layout.delete_note_dialog_layout, this, DELETE_DIALOG_TAG);
			deleteDialog.show(getFragmentManager(), "DeleteNoteDialog");
		}
		else if (view == baseActivity.editButton)
		{
			if(note.getAttributes().getContentClass()==null ||note.getAttributes().getContentClass().equals(""))
			{
				Bundle args = new Bundle();
			    args.putString("noteGuid", noteGuid);
				changeScreen(new NotepriseFragment("NoteEditScreen", NoteEditScreen.class,args));				
			}
			else
			{	//showToastNotification(getString(R.string.read_only_note_message));
			readOnlyDialog = new CommonCustomDialog(R.layout.read_only_dialog_layout, this,READ_ONLY_DIALOG_TAG);
			readOnlyDialog.show(getFragmentManager(), "ReadOnlyDialog");
			}	
			
			
		}
		else if (view == baseActivity.publishToChatterButton)			
		{			
			if (Html.fromHtml(publishString).length() > 1000)
			{
				TASK = TRUNCATE_NOTE;
				exceedLength = Html.fromHtml(publishString).length()-1000;
				chatterTruncateDialog = new CommonCustomDialog(R.layout.common_yes_no_dialog_layout, this, CHATTER_TRUNCATE_DIALOG_TAG);
				chatterTruncateDialog.show(getFragmentManager(), "TruncateContentDialog");	
			
			
			}
			else
			{
				baseActivity.publishToChatterButton.showContextMenu();
			}			
		}
		else if (view == deleteDialogNoButton)
		{
			deleteDialog.dismiss();
		}
		else if (view == deleteDialogYesButton)
		{
			deleteDialog.dismiss();
			TASK = DELETE_NOTE;
			showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_note_delete_message));
			executeAsyncTask();
		}
		else if(view == readOnlyDialogOkButton)
		{
			readOnlyDialog.dismiss();
		}
		else if (view == okayButton)
		{
			String saveString;
			imageAttachDialog.dismiss();
			selectedIds = listAdapter.getCheckedItemsList();
			Bundle args = new Bundle();
			if(TASK == ATTACHMENT_ONLY)
			{
				saveString=null;
			}
			else		
			{
				saveString = EvernoteUtils.stripNoteHTMLContent(noteContent);
			}			
			args.putString("noteContent", saveString);
			args.putString("noteGuid", noteGuid);
			args.putStringArrayList("Attachment",selectedIds );
			changeScreen(new NotepriseFragment("RecordsList", SalesforceRecordsList.class, args));	
			}
		else if (view == okayChatterButton)
		{
			
			
			attachmentName = listAdapterChatterImage.getCheckedItemsListName();
			NotepriseLogger.logMessage("Slectes ids"+selectedIds);
			if(attachmentName !=null)
			{
			chatterAttachmentListDialog.dismiss();				
			if(TASK == ATTACHMENT_ONLY)
			{
				saveString=null;
			}
			else		
			{
				saveString = EvernoteUtils.stripNoteHTMLContent(noteContent);
			}
			showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_user_self_feed_message));
			executeAsyncTask();
			}
			else 
			{
				showToastNotification(getString(R.string.chatter_select_attachment_error_message));
			}
				
		}
		else if (view == okayUserChatterButton)
		{
			
			
			attachmentName = listAdapterUserChatterImage.getCheckedItemsListName();
			NotepriseLogger.logMessage("Slectes ids"+selectedIds);
			if(attachmentName !=null)
			{
			chatterAttachmentListDialog.dismiss();				
			if(TASK == ATTACHMENT_ONLY)
			{
				saveString=null;
			}
			else		
			{
				saveString = EvernoteUtils.stripNoteHTMLContent(noteContent);
			}
			final String filepath = SD_CARD + Constants.IMAGE_PATH + noteTitle + "_" +attachmentName;	
			Bundle args = new Bundle();			
		    args.putString("publishString", saveString);
		    args.putString("publishTask", "USER_FEED");
		    args.putString("filePath", filepath);
			changeScreen(new NotepriseFragment("PublishToChatterRecordsList", PublishToChatterRecordsListScreen.class, args));
			}
			else 
			{
				showToastNotification(getString(R.string.chatter_select_attachment_error_message));
			}
				
		}
		else if (view == okayGroupChatterButton)
		{
			
			
			attachmentName = listAdapterGroupChatterImage.getCheckedItemsListName();
			NotepriseLogger.logMessage("Slectes ids"+selectedIds);
			if(attachmentName !=null)
			{
			chatterAttachmentListDialog.dismiss();				
			if(TASK == ATTACHMENT_ONLY)
			{
				saveString=null;
			}
			else		
			{
				saveString = EvernoteUtils.stripNoteHTMLContent(noteContent);
			}
			final String filepath = SD_CARD + Constants.IMAGE_PATH + noteTitle + "_" +attachmentName;	
			Bundle args = new Bundle();			
		    args.putString("publishString", saveString);
		    args.putString("publishTask", "GROUP_FEED");
		    args.putString("filePath", filepath);
			changeScreen(new NotepriseFragment("PublishToChatterRecordsList", PublishToChatterRecordsListScreen.class, args));
			}
			else 
			{
				showToastNotification(getString(R.string.chatter_select_attachment_error_message));
			}
				
		}
		else if(view == truncateDialogYesButton)
		{
			truncateContentDialog.dismiss();
			publishString = EvernoteUtils.stripNoteHTMLContent(noteContent);
			String publishStringForWall;
			if (publishString.length() > baseActivity.SELECTED_FIELD_LENGTH)
			{
				publishStringForWall = publishString.substring(0, baseActivity.SELECTED_FIELD_LENGTH-1);
				if(note.getResources()!= null)
				{
					showImageDialog();
				}
				else 
				{
					Bundle args = new Bundle();
					args.putString("noteContent", publishStringForWall);
					args.putStringArrayList("Attachment",selectedIds );
					changeScreen(new NotepriseFragment("RecordsList", SalesforceRecordsList.class, args));
				}
			}			
		}
		else if (view == truncateDialogNoButton)
		{
			truncateContentDialog.dismiss();
		}
		else if (view == chatterTruncateDialogYesButton)
		{
			chatterTruncateDialog.dismiss();
			baseActivity.publishToChatterButton.showContextMenu();
		}
		else if (view == chatterTruncateDialogNoButton)
		{
			chatterTruncateDialog.dismiss();
		}
	}
	
	public void showImageDialog()
	{
		imageAttachDialog= new CommonCustomDialog(R.layout.attachimage_salesforce_object_diolog_layout, this, SF_ATTACHMENTS_DIALOG_TAG);
		imageAttachDialog.show(getFragmentManager(), "ImageAttachDialog");
	}
	
	public void showChatterImageDialog()
	{
		NotepriseLogger.logMessage("In chatter image dialog only");
		chatterAttachmentListDialog= new CommonCustomDialog(R.layout.attachimage_salesforce_object_diolog_layout, this, CHATTER_ATTACHMENT_LIST_DIALOG_TAG);
		chatterAttachmentListDialog.show(getFragmentManager(), "chatterAttachmentListDialog");
	}
	
	public void showUserChatterImageDialog()
	{
		NotepriseLogger.logMessage("In chatter image dialog only");
		chatterAttachmentListDialog= new CommonCustomDialog(R.layout.attachimage_salesforce_object_diolog_layout, this, CHATTER_USER_ATTACHMENT_LIST_DIALOG_TAG);
		chatterAttachmentListDialog.show(getFragmentManager(), "chatterAttachmentListDialog");
	}
	
	public void showGroupChatterImageDialog()
	{
		NotepriseLogger.logMessage("In chatter image dialog only");
		chatterAttachmentListDialog= new CommonCustomDialog(R.layout.attachimage_salesforce_object_diolog_layout, this, CHATTER_GROUP_ATTACHMENT_LIST_DIALOG_TAG);
		chatterAttachmentListDialog.show(getFragmentManager(), "chatterAttachmentListDialog");
	}
	@Override
	public void instantiateCustomDialog(View view) 
	{
		super.instantiateCustomDialog(view);
		if (view.getTag() != null && (String) view.getTag() == DELETE_DIALOG_TAG)
		{
			deleteDialogYesButton = (Button) view.findViewById(R.id.delete_note_yes_button);
			deleteDialogYesButton.setOnClickListener(this);
			deleteDialogNoButton = (Button) view.findViewById(R.id.delete_note_no_button);
			deleteDialogNoButton.setOnClickListener(this);
		}
		if (view.getTag() != null && (String) view.getTag() == READ_ONLY_DIALOG_TAG)
		{
			readOnlyDialogOkButton = (Button) view.findViewById(R.id.read_only_ok_button);
			readOnlyDialogOkButton.setOnClickListener(this);
			
		}
		
		
		else if((view.getTag() != null && (String) view.getTag() == SF_ATTACHMENTS_DIALOG_TAG))				
		{
			listView = (ListView)view.findViewById(R.id.notes_list_view);			
			if (listItems != null && listItems.size() > 0)
			{				
				listAdapter = new CommonListAdapter(this, inflater, listItems);				
				listAdapter.changeOrdering(Constants.SORT_BY_LABEL);
				listView.setAdapter(listAdapter);
				listView.setOnItemClickListener(this);
				listAdapter.showCheckList();
			}				
			okayButton = (Button) view.findViewById(R.id.okay_button);
			okayButton.setOnClickListener(this);					
		}
		else if((view.getTag() != null && (String) view.getTag() ==  CHATTER_ATTACHMENT_DIALOG_TAG))				
		{
			listViewChatter = (ListView)view.findViewById(R.id.notes_list_view);
			listitemsChatter = new ArrayList<CommonListItems>();
			CommonListItems dialogItem = new CommonListItems();
			dialogItem.setLabel("Text");
			dialogItem.setId("Text");
			listitemsChatter.add(dialogItem);
			dialogItem = new CommonListItems();
			dialogItem.setLabel("Attachment");
			dialogItem.setId("Attachment");
			listitemsChatter.add(dialogItem);
			dialogItem = new CommonListItems();
			dialogItem.setLabel("Text and Attachment");
			dialogItem.setId("Text and Attachment");
			listitemsChatter.add(dialogItem);
			if (listitemsChatter != null && listitemsChatter.size() > 0)
			{				
				listAdapterChatter = new CommonListAdapter(this, inflater, listitemsChatter);				
				listViewChatter.setAdapter(listAdapterChatter);
				listViewChatter.setOnItemClickListener(this);
			}					
		}
		
		else if((view.getTag() != null && (String) view.getTag() ==  CHATTER_USER_ATTACHMENT_DIALOG_TAG))				
		{
			listViewUserChatter = (ListView)view.findViewById(R.id.notes_list_view);
			listitemsUserChatter = new ArrayList<CommonListItems>();
			CommonListItems dialogItem = new CommonListItems();
			dialogItem.setLabel("Text");
			dialogItem.setId("Text");
			listitemsUserChatter.add(dialogItem);
			dialogItem = new CommonListItems();
			dialogItem.setLabel("Attachment");
			dialogItem.setId("Attachment");
			listitemsUserChatter.add(dialogItem);
			dialogItem = new CommonListItems();
			dialogItem.setLabel("Text and Attachment");
			dialogItem.setId("Text and Attachment");
			listitemsUserChatter.add(dialogItem);
			if (listitemsUserChatter != null && listitemsUserChatter.size() > 0)
			{				
				listAdapterUserChatter = new CommonListAdapter(this, inflater, listitemsUserChatter);				
				listViewUserChatter.setAdapter(listAdapterUserChatter);
				listViewUserChatter.setOnItemClickListener(this);
			}					
		}
		else if((view.getTag() != null && (String) view.getTag() ==  CHATTER_GROUP_ATTACHMENT_DIALOG_TAG))				
		{
			listViewGroupChatter = (ListView)view.findViewById(R.id.notes_list_view);
			listitemsGroupChatter = new ArrayList<CommonListItems>();
			CommonListItems dialogItem = new CommonListItems();
			dialogItem.setLabel("Text");
			dialogItem.setId("Text");
			listitemsGroupChatter.add(dialogItem);
			dialogItem = new CommonListItems();
			dialogItem.setLabel("Attachment");
			dialogItem.setId("Attachment");
			listitemsGroupChatter.add(dialogItem);
			dialogItem = new CommonListItems();
			dialogItem.setLabel("Text and Attachment");
			dialogItem.setId("Text and Attachment");
			listitemsGroupChatter.add(dialogItem);
			if (listitemsGroupChatter != null && listitemsGroupChatter.size() > 0)
			{				
				listAdapterGroupChatter = new CommonListAdapter(this, inflater, listitemsGroupChatter);				
				listViewGroupChatter.setAdapter(listAdapterGroupChatter);
				listViewGroupChatter.setOnItemClickListener(this);
			}					
		}
		else if((view.getTag() != null && (String) view.getTag() ==  CHATTER_ATTACHMENT_LIST_DIALOG_TAG))				
		{
			listViewChatterImage = (ListView)view.findViewById(R.id.notes_list_view);			
			if (listItems != null && listItems.size() > 0)
			{				
				listAdapterChatterImage = new CommonListAdapter(this, inflater, listItems);				
				listAdapterChatterImage.changeOrdering(Constants.SORT_BY_LABEL);
				listViewChatterImage.setAdapter(listAdapterChatterImage);
				listViewChatterImage.setOnItemClickListener(this);
				listAdapterChatterImage.showCheckList();
				
			}				
			okayChatterButton = (Button) view.findViewById(R.id.okay_button);
			okayChatterButton.setOnClickListener(this);	
		}
		
		else if((view.getTag() != null && (String) view.getTag() ==  CHATTER_USER_ATTACHMENT_LIST_DIALOG_TAG))				
		{
			listViewUserChatterImage = (ListView)view.findViewById(R.id.notes_list_view);			
			if (listItems != null && listItems.size() > 0)
			{				
				listAdapterUserChatterImage = new CommonListAdapter(this, inflater, listItems);				
				listAdapterUserChatterImage.changeOrdering(Constants.SORT_BY_LABEL);
				listViewUserChatterImage.setAdapter(listAdapterUserChatterImage);
				listViewUserChatterImage.setOnItemClickListener(this);
				listAdapterUserChatterImage.showCheckList();
				
			}				
			okayUserChatterButton = (Button) view.findViewById(R.id.okay_button);
			okayUserChatterButton.setOnClickListener(this);	
		}
		
		else if((view.getTag() != null && (String) view.getTag() ==  CHATTER_GROUP_ATTACHMENT_LIST_DIALOG_TAG))				
		{
			listViewGroupChatterImage = (ListView)view.findViewById(R.id.notes_list_view);			
			if (listItems != null && listItems.size() > 0)
			{				
				listAdapterGroupChatterImage = new CommonListAdapter(this, inflater, listItems);				
				listAdapterGroupChatterImage.changeOrdering(Constants.SORT_BY_LABEL);
				listViewGroupChatterImage.setAdapter(listAdapterGroupChatterImage);
				listViewGroupChatterImage.setOnItemClickListener(this);
				listAdapterGroupChatterImage.showCheckList();
				
			}				
			okayGroupChatterButton = (Button) view.findViewById(R.id.okay_button);
			okayGroupChatterButton.setOnClickListener(this);	
		}
		
		else if (view.getTag() != null && (String) view.getTag() == SF_TRUNCATE_DIALOG_TAG)
		{
			
			truncateDialogYesButton = (Button) view.findViewById(R.id.delete_note_yes_button);
			truncateDialogYesButton.setOnClickListener(this);
			sfTruncateDialogMessage = (TextView) view.findViewById(R.id.commondialog_message_text);
			sfTruncateDialogMessage.setText(getString(R.string.note_content_truncate_message1)+" "+ String.valueOf(exceedFieldLength) +" "+"char "+getString(R.string.note_content_truncate_message2));
			truncateDialogNoButton = (Button) view.findViewById(R.id.delete_note_no_button);
			truncateDialogNoButton.setOnClickListener(this);
		}
		else if (view != null && (String) view.getTag() == CHATTER_TRUNCATE_DIALOG_TAG)
		{
			
			chatterTruncateDialogHeaderText = (TextView) view.findViewById(R.id.common_yes_no_dialog_header_text);
			chatterTruncateDialogHeaderText.setText(getString(R.string.chatter_truncate_dialog_title));
			chatterTruncateDialogMessage = (TextView) view.findViewById(R.id.common_yes_no_dialog_message_text);
			chatterTruncateDialogMessage.setText(getString(R.string.chatter_truncate_dialog_message1)+" "+String.valueOf(exceedLength) +" "+"char " + getString(R.string.chatter_truncate_dialog_message2));
			chatterTruncateDialogYesButton = (Button) view.findViewById(R.id.common_yes_no_dialog_yes_button);
			chatterTruncateDialogYesButton.setOnClickListener(this);
			chatterTruncateDialogNoButton = (Button) view.findViewById(R.id.common_yes_no_dialog_no_button);
			chatterTruncateDialogNoButton.setOnClickListener(this);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) 
	{
		if (view == baseActivity.publishToChatterButton)
		{
			MenuInflater inflater = baseActivity.getMenuInflater();
		    inflater.inflate(R.menu.chatter_context_menu, menu);
            menu.setHeaderView(this.inflater.inflate(R.layout.chatter_menu_header_view_layout, null));
        } 
		else if (view == baseActivity.saveToSFButton)
        {    		
    		MenuInflater inflater = baseActivity.getMenuInflater();
    		inflater.inflate(R.menu.salesforce_object_menu, menu);
            menu.setHeaderView(this.inflater.inflate(R.layout.salesforce_menu_header_layout, null));            	
        }
		super.onCreateContextMenu(menu, view, menuInfo);
	}		
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) 
	{
		
		if(adapter.getAdapter() == listAdapterChatter)
		{
			if(position == 0)
			{
				TASK = PUBLISH_TO_MY_CHATTER_FEED;
				showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_user_self_feed_message));
				executeAsyncTask();
			}
			else if (position ==1 ){
				NotepriseLogger.logMessage("In Attachment only");
				TASK= ATTACHMENT_ONLY;
				showChatterImageDialog();
				chatterImageDialog.dismiss();
			}
			else if(position == 2)
			{
				TASK= TEXT_ATTACHMENT;
				showChatterImageDialog();
				chatterImageDialog.dismiss();
			}
		}
		
		else if(adapter.getAdapter() == listAdapterUserChatter)
		{
			if(position == 0)
			{
				chatterUserImageDialog.dismiss();
				Bundle args = new Bundle();			
			    args.putString("publishString", publishString);
			    args.putString("publishTask", "USER_FEED");
				changeScreen(new NotepriseFragment("PublishToChatterRecordsList", PublishToChatterRecordsListScreen.class, args));
			}
			else if (position ==1 ){
				NotepriseLogger.logMessage("In Attachment only");
				TASK= ATTACHMENT_ONLY;
				showUserChatterImageDialog();
				chatterUserImageDialog.dismiss();
			}
			else if(position == 2)
			{
				TASK= TEXT_ATTACHMENT;
				showUserChatterImageDialog();
				chatterUserImageDialog.dismiss();
			}
		}
		else if(adapter.getAdapter() == listAdapterGroupChatter)
		{
			if(position == 0)
			{
				chatterUserImageDialog.dismiss();
				Bundle args = new Bundle();			
			    args.putString("publishString", publishString);
			    args.putString("publishTask", "GROUP_FEED");
				changeScreen(new NotepriseFragment("PublishToChatterRecordsList", PublishToChatterRecordsListScreen.class, args));
			}
			else if (position ==1 ){
				NotepriseLogger.logMessage("In Attachment only");
				TASK= ATTACHMENT_ONLY;
				showGroupChatterImageDialog();
				chatterUserImageDialog.dismiss();
			}
			else if(position == 2)
			{
				TASK= TEXT_ATTACHMENT;
				showGroupChatterImageDialog();
				chatterUserImageDialog.dismiss();
			}
		}
		
		else if(adapter.getAdapter() == listAdapterChatterImage)
		{
			NotepriseLogger.logMessage("filename from list"+listAdapterChatterImage.getListItemText(position));
			if(listAdapterChatterImage.isCheckListMode())
			{
				if(checkedItemPosition != -1)
				{
					
					listAdapterChatterImage.setUnChecedkItem(checkedItemPosition);
				}
				listAdapterChatterImage.setChecedkCurrentItem(position);
				checkedItemPosition= position;
			}	
		}
		else if(adapter.getAdapter() == listAdapterUserChatterImage)
		{
			NotepriseLogger.logMessage("filename from list"+listAdapterUserChatterImage.getListItemText(position));
			if(listAdapterUserChatterImage.isCheckListMode())
			{
				if(checkedItemPosition != -1)
				{
					
					listAdapterUserChatterImage.setUnChecedkItem(checkedItemPosition);
				}
				listAdapterUserChatterImage.setChecedkCurrentItem(position);
				checkedItemPosition= position;
			}	
		}
		else if(adapter.getAdapter() == listAdapterGroupChatterImage)
		{
			NotepriseLogger.logMessage("filename from list"+listAdapterGroupChatterImage.getListItemText(position));
			if(listAdapterGroupChatterImage.isCheckListMode())
			{
				if(checkedItemPosition != -1)
				{
					
					listAdapterGroupChatterImage.setUnChecedkItem(checkedItemPosition);
				}
				listAdapterGroupChatterImage.setChecedkCurrentItem(position);
				checkedItemPosition= position;
			}	
		}
		else
			if(listAdapter.isCheckListMode())
		{
			listAdapter.setChecedkCurrentItem(position);
			selection[position]=true;
		}		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		if (item.getItemId() == R.id.chatter_menu_post_my_feed)
		{	
			if(note.getResources()!= null)
			{
				chatterImageDialog= new CommonCustomDialog(R.layout.note_chatter_wall, this, CHATTER_ATTACHMENT_DIALOG_TAG);
				chatterImageDialog.show(getFragmentManager(), "chatterImageDialog");
			}
			else
			{
			TASK = PUBLISH_TO_MY_CHATTER_FEED;
			showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_user_self_feed_message));
			executeAsyncTask();
			}
		}
		else if (item.getItemId() == R.id.chatter_menu_post_user_feed)
		{			
			if(note.getResources()!= null)
			{
				chatterUserImageDialog= new CommonCustomDialog(R.layout.note_chatter_wall, this, CHATTER_USER_ATTACHMENT_DIALOG_TAG);
				chatterUserImageDialog.show(getFragmentManager(), "chatterUserImageDialog");
			}
			else
			{
			Bundle args = new Bundle();			
		    args.putString("publishString", publishString);
		    args.putString("publishTask", "USER_FEED");
			changeScreen(new NotepriseFragment("PublishToChatterRecordsList", PublishToChatterRecordsListScreen.class, args));
		}
		
		}
		else if (item.getItemId() == R.id.chatter_menu_post_group_feed)
		{			
			if(note.getResources()!= null)
			{
				chatterUserImageDialog= new CommonCustomDialog(R.layout.note_chatter_wall, this, CHATTER_GROUP_ATTACHMENT_DIALOG_TAG);
				chatterUserImageDialog.show(getFragmentManager(), "chatterUserImageDialog");
			}
			else
			{
			Bundle args = new Bundle();
		    args.putString("publishString", publishString);
		    args.putString("publishTask", "GROUP_FEED");
			changeScreen(new NotepriseFragment("PublishToChatterRecordsList", PublishToChatterRecordsListScreen.class, args));
		}		
		
		}		
		else if (item.getItemId() == R.id.salesforce_menu_post_text)
		{	
			if(baseActivity.SELECTED_FIELD_LENGTH >=Html.fromHtml(publishString).length())
			{
				Bundle args = new Bundle();
				selection=null;
				String saveString = EvernoteUtils.stripNoteHTMLContent(noteContent);				
				args.putString("noteContent", saveString);				
				args.putBooleanArray("Attachment",selection );
				changeScreen(new NotepriseFragment("RecordsList", SalesforceRecordsList.class, args));
			}
			else
			{
				exceedFieldLength = Html.fromHtml(publishString).length() - baseActivity.SELECTED_FIELD_LENGTH;
				truncateContentDialog = new CommonCustomDialog(R.layout.note_content_truncate_dialog, this, SF_TRUNCATE_DIALOG_TAG);
				truncateContentDialog.show(getFragmentManager(), "TruncateContentDialog");
			}
		}		
		else if (item.getItemId() == R.id.salesforce_menu_post_attachment)
		{	
			TASK= ATTACHMENT_ONLY;			
			showImageDialog();
		}		
		else if (item.getItemId() == R.id.salesforce_menu_post_text_attachment)
		{	
			TASK=TEXT_ATTACHMENT;
		  	showImageDialog();			
		}		
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		baseActivity.deleteNoteButton.setVisibility(View.VISIBLE);
		TASK = GET_NOTE_DATA;
		showProgresIndicator();
		executeAsyncTask();			
	}
	
	@Override
	public void onStop() 
	{
		super.onStop();
		baseActivity.saveToSFButton.setVisibility(View.GONE);
		baseActivity.editButton.setVisibility(View.GONE);
		baseActivity.publishToChatterButton.setVisibility(View.GONE);
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
			
			note =	EvernoteUtils.getNotedata(evernoteSession,noteGuid,true);
			
		    }
			
		}
		else if (TASK == DELETE_NOTE)
		{
			deletionId = EvernoteUtils.deleteNote(authToken, client, noteGuid);
		}
		else if (TASK == PUBLISH_TO_MY_CHATTER_FEED)
		{
			publishString = EvernoteUtils.stripNoteHTMLContent(noteContent);
			String publishStringForWall = publishString;
			if (publishString.length() > 1000)
			{
				publishStringForWall = publishString.substring(0, 999);
			}			
					
			publishResponse = SalesforceUtils.publishNoteContentToMyChatterFeed(salesforceRestClient, publishStringForWall, SF_API_VERSION, null, null, null);			
			
		}
		
		else if(TASK == ATTACHMENT_ONLY || TASK == TEXT_ATTACHMENT)
		{
			
			final String filepath = SD_CARD + Constants.IMAGE_PATH + noteTitle + "_" +attachmentName;			
			File file = new File(filepath);
			response = SalesforceUtils.publishNoteToMyChatterFeed(salesforceRestClient, saveString, SF_API_VERSION, file,attachmentName, "Chatter!");
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
			noteContent = note.getContent();
			setHeaderTitle(noteTitle);
			List<Resource> res = new ArrayList<Resource>();
			res = note.getResources();
			mediaString = noteContent;
			if (res != null && noteContent.indexOf("<en-media") != -1) 
			{   
				int index=0;
				mediaContent=true;
				listItems = new ArrayList<CommonListItems>();
				_options = new String[res.size()];
				selection = new boolean[res.size()];
				for (Iterator<Resource> iterator = res.iterator(); iterator.hasNext();) 
				{
					Resource resource = iterator.next();				
					CommonListItems resourceItem = new CommonListItems();
					String resFileName = resource.getAttributes().getFileName();
					Integer length = 0;
					if (resFileName != null)
					{
						length = resFileName.length();
					}
					resourceItem.setLabel(resource.getAttributes().getFileName());
					_options[index]=resource.getAttributes().getFileName()+resource.getMime();
					resourceItem.setId(resource.getAttributes().getFileName());
					resourceItem.setAttachmentLength(length.toString());
					resourceItem.setShowListArrow(false);
					listItems.add(resourceItem);					
					if(resource.getMime().equalsIgnoreCase("image/jpeg") || resource.getMime().equalsIgnoreCase("image/png"))
					{
						bitmap = BitmapFactory.decodeByteArray(resource.getData().getBody(), 0, resource.getData().getBody().length);	
						if(resource.getAttributes().getFileName()!=null)
						{
							fileName=resource.getAttributes().getFileName();
						}
						saveImageToExternalStorage(bitmap, resource.getAttributes().getFileName(), noteTitle,resource.getMime());					
						mediaString = EvernoteUtils.getMediaStringFromNote(noteContent,resource.getMime());
						final String fileName = "file://" + SD_CARD + Constants.IMAGE_PATH + noteTitle + "_" + resource.getAttributes().getFileName();
						final String html = "<img src=\"" + fileName + "\">";
						if(mediaString != null)
						{
							noteContent = noteContent.replace(mediaString, html);
						}					
					}else
					{
						saveFileToExternalStorage(resource.getData().getBody(), resource.getAttributes().getFileName(), noteTitle,resource.getMime());
					}
					index++;
				}
			}
			if (res != null && mediaContent)
			{
				noteContentWebView.loadDataWithBaseURL(SD_CARD + Constants.IMAGE_PATH + Constants.APP_PATH_SD_CARD, noteContent, "text/html", "utf-8", "");
			}				
			else
			{
				noteContentWebView.loadData(noteContent, "text/html", "utf-8");
			}	
			
			baseActivity.editButton.setVisibility(View.VISIBLE);			
			baseActivity.saveToSFButton.setVisibility(View.VISIBLE);
			baseActivity.publishToChatterButton.setVisibility(View.VISIBLE);
			publishString = EvernoteUtils.stripNoteHTMLContent(noteContent);
			
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
					String response = publishResponse.asString();
					NotepriseLogger.logMessage(response);
					if (response.contains("errorCode"))
					{
						JSONArray res = new JSONArray(response);
						JSONObject obj = res.getJSONObject(0);
						String message  = obj.getString("message");
						showToastNotification(message);
					}
					else
					{
						showToastNotification(getString(R.string.salesforce_chatter_post_self_success_message));
					}
				} 
				catch (ParseException e) 
				{
					e.printStackTrace();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				} 
				catch (JSONException e) 
				{
					e.printStackTrace();
				}				
			}
			else
			{
				showToastNotification(getString(R.string.some_error_ocurred_message));
			}
		}
		else if ( TASK == ATTACHMENT_ONLY || TASK == TEXT_ATTACHMENT)
		{
			TASK = GET_NOTE_DATA;
			hideFullScreenProgresIndicator();
			if (response == 201)
			{
				showToastNotification(getString(R.string.salesforce_chatter_post_self_success_message));		
			}
			else
			{
				showToastNotification(getString(R.string.some_error_ocurred_message));
			}
		}
	}

	public boolean saveImageToExternalStorage(Bitmap image, String imageName, String noteTitle,String type) 
	{
	
		try 
		{
			File dir = new File(SD_CARD + Constants.IMAGE_PATH);
			if (!dir.exists()) 
			{
				dir.mkdirs();
			}
			OutputStream fOut = null;
			File file = new File(SD_CARD + Constants.IMAGE_PATH, noteTitle + "_" + imageName);
			if (file.exists()) 
			{
				file.delete();				
			}
			file.createNewFile();
			fOut = new FileOutputStream(file);
			if(type.equalsIgnoreCase("image/png"))
			{
				image.compress(Bitmap.CompressFormat.PNG, ImageLoader.REQ_SIZE_100, fOut);
			}
			else if (type.equalsIgnoreCase("image/jpeg"))
			{
				image.compress(Bitmap.CompressFormat.JPEG, ImageLoader.REQ_SIZE_100, fOut);
			}			
			fOut.flush();
			fOut.close();
			MediaStore.Images.Media.insertImage(baseActivity.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
			return true;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean saveFileToExternalStorage(byte[] fileData  , String imageName, String noteTitle,String type) 
	{
	
		try 
		{
			File dir = new File(SD_CARD + Constants.IMAGE_PATH);
			if (!dir.exists()) 
			{
				dir.mkdirs();
			}
			OutputStream fOut = null;
			File file = new File(SD_CARD + Constants.IMAGE_PATH, noteTitle + "_" + imageName);
			if (file.exists()) 
			{
				file.delete();				
			}
			file.createNewFile();
			fOut = new FileOutputStream(file);
			fOut.write(fileData);	
			fOut.flush();
			fOut.close();
			return true;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	
	
	@Override
	public void onClick(DialogInterface dialog, int which) 
	{
		if(TASK == TRUNCATE_NOTE && which == -1)
		{
			baseActivity.publishToChatterButton.showContextMenu();
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