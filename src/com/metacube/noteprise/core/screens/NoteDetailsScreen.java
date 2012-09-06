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
import org.apache.thrift.transport.TTransportException;

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

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
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
	String fileName;
	Bitmap bitmap;
	ArrayList<CommonListItems> listItems=null;
	CommonListAdapter listAdapter;
	ListView listView;
	byte[] byteimage;
	ArrayList<String> selectedIds = null;
	boolean mediaContent= false;
	RestResponse publishResponse;
	Integer GET_NOTE_DATA = 0, DELETE_NOTE = 1, PUBLISH_TO_MY_CHATTER_FEED = 2, TASK = 0, deletionId = null, TRUNCATE_NOTE = 3,TEXT_ONLY=4,ATTACHMENT_ONLY=5,TEXT_ATTACHMENT=6;
	public String SD_CARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	protected String[] _options =null;
	protected boolean[] selection=null;
	Button deleteDialogYesButton, deleteDialogNoButton, okayButton,truncateDialogYesButton,truncateDialogNoButton, chatterTruncateDialogYesButton, chatterTruncateDialogNoButton;
	CommonCustomDialog deleteDialog, imageAttachDialog, truncateContentDialog, chatterTruncateDialog;
	String CHATTER_TRUNCATE_DIALOG_TAG = "CHATTER_TRUNCATE_DIALOG_TAG", DELETE_DIALOG_TAG = "DELETE_DIALOG_TAG", SF_TRUNCATE_DIALOG_TAG = "SF_TRUNCATE_DIALOG_TAG";
	
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
						truncateContentDialog = new CommonCustomDialog(R.layout.note_content_truncate_dialog, this);
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
			deleteDialog = new CommonCustomDialog(R.layout.delete_note_dialog_layout, this);
			deleteDialog.show(getFragmentManager(), "DeleteNoteDialog");
		}
		else if (view == baseActivity.editButton)
		{
		    Bundle args = new Bundle();
		    args.putString("noteGuid", noteGuid);
			changeScreen(new NotepriseFragment("NoteEditScreen", NoteEditScreen.class,args));
		}
		else if (view == baseActivity.publishToChatterButton)			
		{			
			if (Html.fromHtml(publishString).length() > 1000)
			{
				TASK = TRUNCATE_NOTE;
				/*truncateContentDialog = new CommonCustomDialog(R.layout.note_content_truncate_dialog, this);
				truncateContentDialog.show(getFragmentManager(), "TruncateContentDialog");*/	
				commonMessageDialog.showContentTruncateDialog(this);
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
		else if(view == truncateDialogYesButton)
		{
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
			baseActivity.publishToChatterButton.showContextMenu();
		}
		else if (view == chatterTruncateDialogNoButton)
		{
			chatterTruncateDialog.dismiss();
		}
	}
	
	public void showImageDialog()
	{
		imageAttachDialog= new CommonCustomDialog(R.layout.attachimage_salesforce_object_diolog_layout, this);
		imageAttachDialog.show(getFragmentManager(), "ImageAttachDialog");
	}
	
	@Override
	public void instantiateCustomDialog(View view) 
	{
		super.instantiateCustomDialog(view);
		if (view.getTag() != null && ((Integer) view.getTag()) == R.layout.delete_note_dialog_layout)
		{
			//Dialog is delete note dialog.
			deleteDialogYesButton = (Button) view.findViewById(R.id.delete_note_yes_button);
			deleteDialogYesButton.setOnClickListener(this);
			deleteDialogNoButton = (Button) view.findViewById(R.id.delete_note_no_button);
			deleteDialogNoButton.setOnClickListener(this);
		}
		else if((view.getTag() != null && ((Integer) view.getTag()) == R.layout.attachimage_salesforce_object_diolog_layout))				
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
		else if (view.getTag() != null && ((Integer) view.getTag()) == R.layout.note_content_truncate_dialog)
		{
			truncateDialogYesButton = (Button) view.findViewById(R.id.delete_note_yes_button);
			truncateDialogYesButton.setOnClickListener(this);
			truncateDialogNoButton = (Button) view.findViewById(R.id.delete_note_no_button);
			truncateDialogNoButton.setOnClickListener(this);
		}
		/*else if (view != null && (String) view.getTag() == CHATTER_TRUNCATE_DIALOG_TAG)
		{
			
		}*/
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
		if (listAdapter.isCheckListMode())
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
			TASK = PUBLISH_TO_MY_CHATTER_FEED;
			showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_user_self_feed_message));
			executeAsyncTask();
		}
		else if (item.getItemId() == R.id.chatter_menu_post_user_feed)
		{			
			Bundle args = new Bundle();			
		    args.putString("publishString", publishString);
		    args.putString("publishTask", "USER_FEED");
			changeScreen(new NotepriseFragment("PublishToChatterRecordsList", PublishToChatterRecordsListScreen.class, args));
		}
		else if (item.getItemId() == R.id.chatter_menu_post_group_feed)
		{			
			Bundle args = new Bundle();
		    args.putString("publishString", publishString);
		    args.putString("publishTask", "GROUP_FEED");
			changeScreen(new NotepriseFragment("PublishToChatterRecordsList", PublishToChatterRecordsListScreen.class, args));
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
				truncateContentDialog = new CommonCustomDialog(R.layout.note_content_truncate_dialog, this);
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
		baseActivity.createNewNoteButton.setVisibility(View.GONE);
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
		        	note = client.getNote(authToken, noteGuid, true, true, true, true);
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
			publishString = EvernoteUtils.stripNoteHTMLContent(noteContent);
			String publishStringForWall = publishString;
			if (publishString.length() > 1000)
			{
				publishStringForWall = publishString.substring(0, 999);
			}			
					
			publishResponse = SalesforceUtils.publishNoteToMyChatterFeed(salesforceRestClient, publishStringForWall, SF_API_VERSION, null, null, null);			
			//File file = new File("/mnt/sdcard/plus_icon.png");
			//publishResponse = SalesforceUtils.publishNoteToMyChatterFeed(salesforceRestClient, publishStringForWall, SF_API_VERSION, file, "image", "Chatter!");
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