package com.metacube.noteprise.core.screens;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.ParseException;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.metacube.noteprise.R;
import com.metacube.noteprise.common.BaseFragment;
import com.metacube.noteprise.common.CommonListAdapter;
import com.metacube.noteprise.common.CommonListItems;
import com.metacube.noteprise.salesforce.SalesforceUtils;
import com.metacube.noteprise.util.NotepriseLogger;
import com.metacube.noteprise.util.Utilities;
import com.salesforce.androidsdk.rest.RestResponse;

public class PublishToChatterRecordsListScreen extends BaseFragment implements OnClickListener, OnItemClickListener
{
	String publishString, publishTask, groupId;
	RestResponse dataResponse = null, publishResponse;
	Button publishToChatterButton;
	public static final int GET_FOLLOWING_DATA = 0, PUBLISH_TO_CHATTER_USER = 1, GET_GROUP_DATA = 2, PUBLISH_TO_CHATTER_GROUP = 3;
	Integer TASK = -1, TASK_TYPE = -1;
	ArrayList<CommonListItems> listItems;
	CommonListAdapter listAdapter;
	ListView listView;
	ArrayList<String> selectedIds = null;
	LinearLayout editRecordSelectionButton, saveRecordSelectionButton;
	
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);        
        publishString = Utilities.getStringFromBundle(getArguments(), "publishString");    
        publishTask = Utilities.getStringFromBundle(getArguments(), "publishTask"); 
        if (publishTask.equalsIgnoreCase("USER_FEED"))
        {
        	TASK_TYPE = GET_FOLLOWING_DATA;
        	TASK = GET_FOLLOWING_DATA;
        } 
        else if (publishTask.equalsIgnoreCase("GROUP_FEED"))
        {
        	TASK_TYPE = GET_GROUP_DATA;
        	TASK = GET_GROUP_DATA;
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	clearContainer(container);
    	View contentView = inflater.inflate(R.layout.common_list_layout, container);
    	listView = (ListView) contentView.findViewById(R.id.common_list_view);
    	editRecordSelectionButton = (LinearLayout) addViewToBaseHeaderLayout(inflater, R.layout.common_edit_button_layout, R.id.common_edit_button);
    	saveRecordSelectionButton = (LinearLayout) addViewToBaseHeaderLayout(inflater, R.layout.common_save_button_layout, R.id.common_save_button);
    	editRecordSelectionButton.setOnClickListener(this);
    	saveRecordSelectionButton.setOnClickListener(this);
    	return super.onCreateView(inflater, container, savedInstanceState);
    }
	
	@Override
	public void onResume() 
	{
		super.onResume();
		showFullScreenProgresIndicator();
		executeAsyncTask();
	}
	
	@Override
	public void doTaskInBackground() 
	{
		super.doTaskInBackground();
		if (salesforceRestClient != null)
		{
			switch(TASK)
			{
				case GET_FOLLOWING_DATA:
				{
					dataResponse = SalesforceUtils.getUserFollowingData(salesforceRestClient, SF_API_VERSION);
					break;
				}
				case PUBLISH_TO_CHATTER_USER:
				{
					publishResponse = SalesforceUtils.publishNoteWithUserMentions(salesforceRestClient, publishString, SF_API_VERSION, selectedIds);
					break;
				}
				case GET_GROUP_DATA:
				{
					dataResponse = SalesforceUtils.getUserGroupData(salesforceRestClient, SF_API_VERSION);
					break;
				}
				case PUBLISH_TO_CHATTER_GROUP:
				{
					publishResponse = SalesforceUtils.publishNoteToUserGroup(salesforceRestClient, groupId, publishString, SF_API_VERSION);
					break;
				}
			}
		}
	}
	
	public void publishNoteToChatterFeed()
	{
		TASK = PUBLISH_TO_CHATTER_USER;
		showFullScreenProgresIndicator();
		executeAsyncTask();
	}
	
	@Override
	public void onTaskFinished() 
	{
		super.onTaskFinished();
		hideFullScreenProgresIndicator();	
		if (TASK == GET_FOLLOWING_DATA)
		{
			if (dataResponse != null)
			{
				listItems = SalesforceUtils.getListItemsFromUserFollowingResponse(dataResponse);
				if (listItems != null && listItems.size() > 0)
				{
					listAdapter = new CommonListAdapter(this, inflater, listItems);
					listView.setAdapter(listAdapter);
					listView.setOnItemClickListener(this);
					editRecordSelectionButton.setVisibility(View.VISIBLE);
				}
			}
		}
		else if (TASK == GET_GROUP_DATA)
		{
			if (dataResponse != null)
			{
				listItems = SalesforceUtils.getListItemsFromUserGroupResponse(dataResponse);
				if (listItems != null && listItems.size() > 0)
				{
					listAdapter = new CommonListAdapter(this, inflater, listItems);
					listView.setAdapter(listAdapter);
					listView.setOnItemClickListener(this);
				}
			}
		}
		else if (TASK == PUBLISH_TO_CHATTER_USER)
		{
			TASK = -1;
			try 
			{			
				if (publishResponse.getStatusCode() == 200 || publishResponse.getStatusCode() == 201)
				{
					showToastNotification(getString(R.string.salesforce_chatter_post_user_success_message));
					finishScreen();
				}
				else
				{
					showToastNotification(getString(R.string.some_error_ocurred_message));
					String response = publishResponse.asString();
					NotepriseLogger.logMessage(response);
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
		else if (TASK == PUBLISH_TO_CHATTER_GROUP)
		{
			TASK = -1;
			try 
			{			
				if (publishResponse.getStatusCode() == 200 || publishResponse.getStatusCode() == 201)
				{
					showToastNotification(getString(R.string.salesforce_chatter_post_group_success_message));
					finishScreen();
				}
				else
				{
					showToastNotification(getString(R.string.some_error_ocurred_message));
					String response = publishResponse.asString();
					NotepriseLogger.logMessage(response);
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
		removeViewFromBaseHeaderLayout(editRecordSelectionButton);
		removeViewFromBaseHeaderLayout(saveRecordSelectionButton);
	}

	@Override
	public void onClick(View view) 
	{
		if (view == publishToChatterButton)
		{
			publishNoteToChatterFeed();
		}
		else if (view == editRecordSelectionButton)
		{
			editRecordSelectionButton.setVisibility(View.GONE);
			saveRecordSelectionButton.setVisibility(View.VISIBLE);
			listAdapter.showCheckList();
		}
		else if (view == saveRecordSelectionButton)
		{
			selectedIds = listAdapter.getCheckedItemsList();
			if (selectedIds.size() > 0)
			{
				showFullScreenProgresIndicator();
				TASK = PUBLISH_TO_CHATTER_USER;
				showFullScreenProgresIndicator();
				executeAsyncTask();
			}
			else
			{
				showToastNotification(getString(R.string.salesforce_no_records_selected_message));
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) 
	{
		if (listAdapter.isCheckListMode())
		{
			listAdapter.setChecedkCurrentItem(position);
		}
		else if (listItems != null)
		{
			if (TASK_TYPE == GET_FOLLOWING_DATA)
			{
				String recordId = listItems.get(position).getId();
				selectedIds = new ArrayList<String>();
				selectedIds.add(recordId);
				TASK = PUBLISH_TO_CHATTER_USER;
				showFullScreenProgresIndicator();
				executeAsyncTask();
			}
			else if (TASK_TYPE == GET_GROUP_DATA)
			{
				groupId = listItems.get(position).getId();
				TASK = PUBLISH_TO_CHATTER_GROUP;
				showFullScreenProgresIndicator();
				executeAsyncTask();
			}			
		}
	}
}