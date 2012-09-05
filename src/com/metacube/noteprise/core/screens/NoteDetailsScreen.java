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
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.PopupWindow.OnDismissListener;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore.Client;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;
import com.metacube.noteprise.R;
import com.metacube.noteprise.common.BaseFragment;
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

public class NoteDetailsScreen extends BaseFragment implements OnClickListener, android.content.DialogInterface.OnClickListener, OnDismissListener
{
	String authToken;
	Client client;
	WebView noteContentWebView;
	String noteTitle, noteContent, noteGuid, mediaString, publishString,encodeImage;
	Note note;
	Bitmap bitmap;
	byte[] byteimage;
	boolean mediaContent= false;
	RestResponse publishResponse;
	Integer GET_NOTE_DATA = 0, DELETE_NOTE = 1, PUBLISH_TO_MY_CHATTER_FEED = 2, TASK = 0, deletionId = null, TRUNCATE_NOTE = 3;
	public String SD_CARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	protected CharSequence[] _options ;
	protected boolean[] _selections ;
	
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
				
				
				if(baseActivity.SELECTED_FIELD_LENGTH >= Html.fromHtml(noteContent).length())
				{
					
					if(CommonSOQL.getSupportedObject(baseActivity.SELECTED_OBJECT_NAME)){
						//args.putString("encodeImage",encodeImage);
						showToastNotification("Object is supporting the atatchment");
					}
					//onCreateDialog().show();
					
				}
				else
				{
					showToastNotification(getString(R.string.salesforce_select_object_field_length_message));	
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
			commonMessageDialog.showDeleteNoteDialog(authToken, client, this);
		}
		else if (view == baseActivity.editButton)
		{
		    Bundle args = new Bundle();
		    args.putString("noteGuid", noteGuid);
			changeScreen(new NotepriseFragment("NoteEditScreen", NoteEditScreen.class,args));
		}
		else if (view == baseActivity.publishToChatterButton)			
		{
			NotepriseLogger.logMessage("Length" + Html.fromHtml(noteContent).length());
			if (Html.fromHtml(publishString).length() > 1000)
			{
				TASK = TRUNCATE_NOTE;
				commonMessageDialog.showContentTruncateDialog(this);
			}
			else 
			baseActivity.publishToChatterButton.showContextMenu();
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
		super.onCreateContextMenu(menu, view, menuInfo);
	}
	
/*	@Override
	protected Dialog onCreateDialog() 
	{
		return 
		new AlertDialog.Builder(baseActivity)
        	.setTitle( "Attachment" )
        	.setMultiChoiceItems( _options, _selections, new DialogSelectionClickHandler() )
        	.setPositiveButton( "OK", new DialogButtonClickHandler() )
        	.create();
	}
	
	public class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener
	{
		public void onClick( DialogInterface dialog, int clicked, boolean selected )
		{
			NotepriseLogger.logMessage("ClickItems"+ _options[ clicked ] + " selected: " + selected );
			_selections[clicked]= selected;
			//Log.i( "ME", _options[ clicked ] + " selected: " + selected );
		}
	}
	

	public class DialogButtonClickHandler implements DialogInterface.OnClickListener
	{
		public void onClick( DialogInterface dialog, int clicked )
		{
			switch( clicked )
			{
				case DialogInterface.BUTTON_POSITIVE:
					printSelectedPlanets();
					break;
			}
		}
	}*/
	
	/*protected void printSelectedPlanets(){
		if(onCreateDialog()!=null)
			onCreateDialog().dismiss();
		Bundle args = new Bundle();
		String saveString = EvernoteUtils.stripNoteHTMLContent(noteContent);
		NotepriseLogger.logMessage("Saving string==" + saveString);
		args.putString("noteContent", saveString);
		args.putString("noteGuid", noteGuid);
		args.putBooleanArray("Attachment", _selections);
		
		changeScreen(new NotepriseFragment("RecordsList", SalesforceRecordsList.class, args));
		
		}*/
	
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
			NotepriseLogger.logMessage("Response" + publishResponse + "Content" + publishStringForWall);			
			publishResponse = SalesforceUtils.publishNoteToMyChatterFeed(salesforceRestClient, publishStringForWall, SF_API_VERSION, null, null, null);			
			File file = new File("/mnt/sdcard/plus_icon.png");
			publishResponse = SalesforceUtils.publishNoteToMyChatterFeed(salesforceRestClient, publishStringForWall, SF_API_VERSION, file, "image", "Chatter!");
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
			NotepriseLogger.logMessage("Note Content" + noteContent);
			List<Resource> res = new ArrayList<Resource>();
			res = note.getResources();
			//NotepriseLogger.logMessage("Resources" + res.toString());
			mediaString = noteContent;
			if (res != null && noteContent.indexOf("<en-media") != -1) 
			{   int index=0;
				mediaContent=true;
				_options = new CharSequence[res.size()];
				_selections = new boolean[res.size()];
				for (Iterator<Resource> iterator = res.iterator(); iterator.hasNext();) 
				{
					Resource resource = iterator.next();				
					NotepriseLogger.logMessage("File Name" + resource.getData().getBody()+"mime" + resource.getMime() +index);	
					_options[index]= resource.getAttributes().getFileName();
					
					if(resource.getMime().equalsIgnoreCase("image/jpeg") || resource.getMime().equalsIgnoreCase("image/png")){
					//encodeImage = Base64.encodeToString( resource.getData().getBody(), Base64.DEFAULT);	
					bitmap = BitmapFactory.decodeByteArray(resource.getData().getBody(), 0, resource.getData().getBody().length);
					if(resource.getMime().equalsIgnoreCase("image/jpeg"))
					saveImageToExternalStorage(bitmap, resource.getAttributes().getFileName(), noteTitle,resource.getMime());
					else if(resource.getMime().equalsIgnoreCase("image/png")){
					saveImageToExternalStorage(bitmap, resource.getAttributes().getFileName(), noteTitle,resource.getMime());	
					}
					mediaString = EvernoteUtils.getMediaStringFromNote(noteContent,resource.getMime());
					final String fileName = "file://" + SD_CARD + Constants.IMAGE_PATH + noteTitle + "_" + resource.getAttributes().getFileName();
					final String html = "<img src=\"" + fileName + "\">";
					noteContent = noteContent.replace(mediaString, html);
					}					
					NotepriseLogger.logMessage("HTML" + noteContent);
					index++;
				}
			}
			if (res != null && mediaContent)
			{
				NotepriseLogger.logMessage("Image URL");
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
			NotepriseLogger.logMessage("PublishString" + publishString);
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
				NotepriseLogger.logMessage("Deleting file" + file.delete() + "filename" + file.getName());
			}
			file.createNewFile();
			fOut = new FileOutputStream(file);
			if(type.equalsIgnoreCase("image/png")){
				image.compress(Bitmap.CompressFormat.PNG, ImageLoader.REQ_SIZE_100, fOut);
			}else if (type.equalsIgnoreCase("image/jpeg")){
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
		else if (which == -1) // For Positive Button
		{
			TASK = DELETE_NOTE;
			showFullScreenProgresIndicator(getString(R.string.progress_dialog_title),getString(R.string.progress_dialog_note_delete_message));
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