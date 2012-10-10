package com.metacube.noteprise.core.screens;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.ParseException;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore.Client;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;
import com.metacube.noteprise.R;
import com.metacube.noteprise.common.BaseFragment;
import com.metacube.noteprise.common.CommonListAdapter;
import com.metacube.noteprise.common.CommonListItems;
import com.metacube.noteprise.salesforce.CommonSOQL;
import com.metacube.noteprise.util.NotepriseLogger;
import com.metacube.noteprise.util.Utilities;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;

public class SalesforceRecordsList extends BaseFragment implements OnItemClickListener, AsyncRequestCallback, OnClickListener
{
	ListView listView;
	String noteContent,filePath,encodedImage,noteGuid,authToken,name, contentType,id,recordId;
	Client client;
	Note note;
	RestRequest recordsRequest, updateRecordRequest,createAttachment;
	CommonListAdapter recordsAdapter;
	TextView noResultsTextView;
	int totalRequests = 0;
	ArrayList<String> imageids;
	public static final Integer GET_NOTE_DATA = 1;
	Integer TASK = -1;


	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
	}

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);        
        noteContent = Utilities.getStringFromBundle(getArguments(), "noteContent");  
        NotepriseLogger.logMessage("notecontrent in image" +noteContent);
        noteGuid = Utilities.getStringFromBundle(getArguments(), "noteGuid");
        imageids = getArguments().getStringArrayList("Attachment");
        NotepriseLogger.logMessage("imageids" +imageids);
        authToken = evernoteSession.getAuthToken();
       
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	clearContainer(container);
    	View contentView = inflater.inflate(R.layout.common_list_layout, container);    	
    	listView = (ListView) contentView.findViewById(R.id.common_list_view);
    	baseActivity.editButton.setOnClickListener(this);
    	baseActivity.saveButton.setOnClickListener(this);
    	noResultsTextView = (TextView) contentView.findViewById(R.id.common_list_no_results);
    	noResultsTextView.setVisibility(View.GONE);
    	return super.onCreateView(inflater, container, savedInstanceState);
    }

	@Override
	public void onResume() 
	{
		super.onResume();

		 if (salesforceRestClient != null)
			{
				List<String> fieldList = new ArrayList<String>();
				fieldList.add("id");
				fieldList.add("name");
				try 
				{	
					showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_salesforce_getting_record_list_message));
					recordsRequest = RestRequest.getRequestForQuery(SF_API_VERSION, CommonSOQL.getQueryForObject(selectedObjectName));				
				} 
				catch (UnsupportedEncodingException e) 
				{
					e.printStackTrace();
				}
				salesforceRestClient.sendAsync(recordsRequest, this);			
			}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) 
	{
		if (recordsAdapter.isCheckListMode())
		{
			recordsAdapter.setChecedkCurrentItem(position);
		}
		else
		{
			showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_salesforce_record_updating_message));
			recordId = recordsAdapter.getListItemId(position);
			if(imageids != null)
			sendCreateRequest(recordId);
			else 
				sendUpdateRequest(recordId);

		}		
	}

	@Override
	public void doTaskInBackground() 
	{
		super.doTaskInBackground();
		try 
    	{
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
    	catch (EDAMNotFoundException e) 
    	{			
			e.printStackTrace();
		} 
    	catch (TException e) 
    	{			
			e.printStackTrace();
		}
	}

	@Override
	public void onTaskFinished() 
	{
		super.onTaskFinished();
		hideFullScreenProgresIndicator();
	}

	public void sendUpdateRequest(String recordId)
	{
		Map<String, Object> fields = new LinkedHashMap<String, Object>();
		fields.put(selectedFieldName, noteContent);

		try 
		{
			updateRecordRequest = RestRequest.getRequestForUpdate(SF_API_VERSION, selectedObjectName, recordId, fields);
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		salesforceRestClient.sendAsync(updateRecordRequest, this);
	}

	public void sendCreateRequest(String recordId)
	{
		List<Resource> res = new ArrayList<Resource>();
		res = note.getResources();
		//int i=0;
		Map<String, Object> fields = new LinkedHashMap<String, Object>();
		String objectType = "Attachment";
		fields.put("ParentID",recordId);
		for (Iterator<Resource> iterator = res.iterator(); iterator.hasNext();) 
		{  
			Resource resource = iterator.next();
			for (Iterator<String> ids = imageids.iterator(); ids.hasNext();) 
			{ 
				String id = ids.next();
		        if(id.equals(resource.getAttributes().getFileName()))		        	
		        {
		        	encodedImage = Base64.encodeToString( resource.getData().getBody(), Base64.DEFAULT);						   
		        	fields.put("Body",encodedImage);
		        	fields.put("Name", resource.getAttributes().getFileName());
		        	fields.put("ContentType", resource.getMime());
		        	try 
		        	{
		        		createAttachment = RestRequest.getRequestForCreate(SF_API_VERSION, objectType, fields);
		        		//updateRecordRequest = RestRequest.getRequestForUpdate(SF_API_VERSION, objectType, recordId, fields);
		        	} 
		        	catch (UnsupportedEncodingException e) 
		        	{
		        		e.printStackTrace();
		        	} 
		        	catch (IOException e) 
		        	{
		        		e.printStackTrace();
		        	}
		        	salesforceRestClient.sendAsync(createAttachment, this);
		        }					
			}			
		}		
	}

	@Override
	public void onSuccess(RestRequest request, RestResponse response) 
	{
		if (request == recordsRequest)
		{
			try 
			{
				NotepriseLogger.logMessage(response.asString());
				JSONObject responseObject = response.asJSONObject();
				JSONArray records = responseObject.getJSONArray("records");
				ArrayList<CommonListItems> items = new ArrayList<CommonListItems>();
				for (int i = 0; i < records.length(); i++)
				{
					CommonListItems item = new CommonListItems();
					JSONObject object = records.getJSONObject(i);
					item.setLabel(object.optString("Name"));
					item.setId(object.optString("Id"));
					item.setLeftImage(R.drawable.record_icon);
					items.add(item);
				}
				//hideFullScreenProgresIndicator();

				if (items != null && items.size() > 0)
				{
					baseActivity.editButton.setVisibility(View.VISIBLE);
					recordsAdapter = new CommonListAdapter(inflater, items);
					listView.setAdapter(recordsAdapter);
					listView.setOnItemClickListener(this);
					 if (imageids != null)
				        {
				        	TASK = GET_NOTE_DATA;
				        	//showFullScreenProgresIndicator();
				        	executeAsyncTask();
				        }
					 else 
					 {
						 hideFullScreenProgresIndicator();
					 }
				}
				else
				{
					noResultsTextView.setVisibility(View.VISIBLE);
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
		else if (request == updateRecordRequest)
		{
			try 
			{
				NotepriseLogger.logMessage("In return message"+response.asString() + "response" +response.getStatusCode());
				if (response.getStatusCode() == 204 && !response.asString().contains("errorCode"))
				{
					if (totalRequests > 0)
					{
						hideFullScreenProgresIndicator();	
						showToastNotification(getString(R.string.progress_dialog_salesforce_record_updated_success_message));						
						clearScreen();

					}
					else
					{
						hideFullScreenProgresIndicator();
						showToastNotification(getString(R.string.progress_dialog_salesforce_record_updated_success_message));												
						clearScreen();

					}					
				}
				else if (response.asString().contains("errorCode"))
				{
					hideFullScreenProgresIndicator();
					showToastNotification(getString(R.string.salesforce_record_saving_failed_message));
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
		}	
		else if(request == createAttachment)
		{
			try 
			{
				NotepriseLogger.logMessage("In return message"+response.asString() + "response" +response.getStatusCode());
				if (response.getStatusCode() == 201 && !response.asString().contains("errorCode"))
				{
					if (totalRequests > 0)
					{
						hideFullScreenProgresIndicator();
						//showToastNotification(getString(R.string.progress_dialog_salesforce_record_updated_success_message));
						if(imageids !=null )
						{													
								showToastNotification(getString(R.string.progress_dialog_salesforce_record_created_success_message));
							    if(noteContent != null)
								sendUpdateRequest(recordId);
							    else
							    	clearScreen();
						}
					}
					else
					{
						//showToastNotification(getString(R.string.progress_dialog_salesforce_record_updated_success_message));
						hideFullScreenProgresIndicator();
						if(imageids !=null )
						{													
								showToastNotification(getString(R.string.progress_dialog_salesforce_record_created_success_message));
							    if(noteContent != null)
								sendUpdateRequest(recordId);
							    else
							    	clearScreen();
						}
					}					
				}
				else if (response.asString().contains("errorCode"))
				{
					hideFullScreenProgresIndicator();
					showToastNotification(getString(R.string.salesforce_record_saving_failed_message));
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
		}
	}

	@Override
	public void onStop() 
	{
		super.onStop();
		baseActivity.editButton.setVisibility(View.GONE);
		baseActivity.saveButton.setVisibility(View.GONE);
	}

	@Override
	public void onError(Exception exception) 
	{
		hideFullScreenProgresIndicator();
		NotepriseLogger.logError("Exception getting response for records list.", NotepriseLogger.ERROR, exception);	
		//commonMessageDialog.showMessageDialog(getString(R.string.some_error_ocurred_message));
		commonMessageDialog.showMessageDialog(exception.getMessage().toString());
	}

	@Override
	public void onClick(View view) 
	{
		if (view == baseActivity.editButton)
		{
			baseActivity.editButton.setVisibility(View.GONE);
			baseActivity.saveButton.setVisibility(View.VISIBLE);
			recordsAdapter.showCheckList();
		}
		else if (view == baseActivity.saveButton)
		{			
			ArrayList<String> selectedRecords = recordsAdapter.getCheckedItemsList();
			if (selectedRecords.size() > 0)
			{
				showFullScreenProgresIndicator(getString(R.string.progress_dialog_title),getString(R.string.progress_dialog_salesforce_record_updating_message));
				totalRequests = selectedRecords.size();
				for (int i = 0; i < selectedRecords.size(); i++)
				{
					recordId=selectedRecords.get(i);
					if(imageids!=null)
					sendCreateRequest(selectedRecords.get(i));
					else
					sendUpdateRequest(recordId);


				}
			}
			else
			{
				showToastNotification(getString(R.string.salesforce_no_records_selected_message));
			}
		}
	}
}