package com.metacube.noteprise.core.screens;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
	public static final int GET_FOLLOWING_USER_LIST = 0, PUBLISH_TO_CHATTER_USER_WITH_MENTIONS = 1, GET_GROUPS_LIST = 2, PUBLISH_TO_CHATTER_GROUP = 3;
	Integer TASK = -1, TASK_TYPE = -1;
	int count = 0, numberOfResponse = 0;
	ArrayList<CommonListItems> listItems;
	CommonListAdapter listAdapter;
	ListView listView;
	ArrayList<String> selectedIds = null;
	
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
        	TASK_TYPE = GET_FOLLOWING_USER_LIST;
        	TASK = GET_FOLLOWING_USER_LIST;
        } 
        else if (publishTask.equalsIgnoreCase("GROUP_FEED"))
        {
        	TASK_TYPE = GET_GROUPS_LIST;
        	TASK = GET_GROUPS_LIST;
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	clearContainer(container);
    	View contentView = inflater.inflate(R.layout.common_list_layout, container);
    	listView = (ListView) contentView.findViewById(R.id.common_list_view);
    	baseActivity.editButton.setOnClickListener(this);
    	baseActivity.saveButton.setOnClickListener(this);
    	return super.onCreateView(inflater, container, savedInstanceState);
    }
	
	@Override
	public void onResume() 
	{
		super.onResume();
		if (TASK_TYPE == GET_FOLLOWING_USER_LIST)
		{
			showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_getting_following_data_message));
		}
		else if (TASK_TYPE == GET_GROUPS_LIST)
		{
			showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_getting_group_data_message));
		}
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
				case GET_FOLLOWING_USER_LIST:
				{
					dataResponse = SalesforceUtils.getUserFollowingData(salesforceRestClient, SF_API_VERSION);
					break;
				}
				case PUBLISH_TO_CHATTER_USER_WITH_MENTIONS:
				{
					if (publishString.length() > 950)
					{
						publishString = publishString.substring(0, 949);
					}
					publishResponse = SalesforceUtils.publishNoteWithUserMentions(salesforceRestClient, publishString, SF_API_VERSION, selectedIds, null, null, null);
					break;
				}
				case GET_GROUPS_LIST:
				{
					dataResponse = SalesforceUtils.getUserGroupData(salesforceRestClient, SF_API_VERSION);
					break;
				}
				case PUBLISH_TO_CHATTER_GROUP:
				{	
					if (publishString.length() > 1000)
					{
						publishString = publishString.substring(0, 999);
					}
					if (selectedIds != null && selectedIds.size() > 1)
					{
						for (int i = 0; i < selectedIds.size(); i++)
						{	
							groupId = selectedIds.get(i);
							publishResponse = SalesforceUtils.publishNoteToUserGroup(salesforceRestClient, groupId, publishString, SF_API_VERSION);
							if (publishResponse.getStatusCode() == 200 || publishResponse.getStatusCode() == 201)
							{
								NotepriseLogger.logMessage("Post successful for groupId=" + groupId + " at position=" + i);
							}
						}
					}
					else
					{
						groupId = selectedIds.get(0);
						publishResponse = SalesforceUtils.publishNoteToUserGroup(salesforceRestClient, groupId, publishString, SF_API_VERSION);
					}
					break;
				}
			}
		}
	}
	
	public void publishNoteToChatterFeed()
	{
		TASK = PUBLISH_TO_CHATTER_USER_WITH_MENTIONS;
		showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_user_self_feed_with_mentions_message));
		executeAsyncTask();
	}
	
	@Override
	public void onTaskFinished() 
	{
		super.onTaskFinished();		
		hideFullScreenProgresIndicator();
		if (TASK == GET_FOLLOWING_USER_LIST)
		{
			if (dataResponse != null)
			{
				listItems = SalesforceUtils.getListItemsFromUserFollowingResponse(dataResponse);
				if (listItems != null && listItems.size() > 0)
				{
					listAdapter = new CommonListAdapter(this, inflater, listItems);
					listView.setAdapter(listAdapter);
					listView.setOnItemClickListener(this);
					baseActivity.editButton.setVisibility(View.VISIBLE);
				}
			}
		}
		else if (TASK == GET_GROUPS_LIST)
		{
			if (dataResponse != null)
			{
				listItems = SalesforceUtils.getListItemsFromUserGroupResponse(dataResponse);
				if (listItems != null && listItems.size() > 0)
				{
					listAdapter = new CommonListAdapter(this, inflater, listItems);
					listView.setAdapter(listAdapter);
					listView.setOnItemClickListener(this);
					baseActivity.editButton.setVisibility(View.VISIBLE);
				}
			}
		}
		else if (TASK == PUBLISH_TO_CHATTER_USER_WITH_MENTIONS)
		{
			TASK = -1;
			try 
			{	
				if (publishResponse.getStatusCode() == 200 || publishResponse.getStatusCode() == 201)
				{
					showToastNotification(getString(R.string.salesforce_chatter_post_user_with_mentions_success_message));
					finishScreen();
				}
				else
				{
					showToastNotification(getString(R.string.some_error_ocurred_message));
					String response = publishResponse.asString();
					NotepriseLogger.logMessage(response);
				}
			}			
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else if (TASK == PUBLISH_TO_CHATTER_GROUP)
		{
			try 
			{					
				TASK = -1;
				if (publishResponse.getStatusCode() == 200 || publishResponse.getStatusCode() == 201)
				{
					showToastNotification(getString(R.string.salesforce_chatter_post_group_success_message));
					finishScreen();
				}
				else
				{
					showToastNotification(getString(R.string.some_error_ocurred_message));
					NotepriseLogger.logMessage(publishResponse.asString());
				}
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
	public void onClick(View view) 
	{
		if (view == baseActivity.editButton)
		{
			baseActivity.editButton.setVisibility(View.GONE);
			baseActivity.saveButton.setVisibility(View.VISIBLE);
			listAdapter.showCheckList();
		}
		else if (view == baseActivity.saveButton)
		{			
			selectedIds = listAdapter.getCheckedItemsList();
			if (TASK_TYPE == GET_FOLLOWING_USER_LIST)
			{
				if (selectedIds.size() > 0)
				{
					if (selectedIds.size() > 25)
					{
						showToastNotification(getString(R.string.salesforce_selected_user_exceed_message));	
					}
					else 
					{
						Integer calculatedLimit = 1000 - listAdapter.getCheckedItemsUserNameLength() - selectedIds.size() * 12;
						if (publishString.length() > calculatedLimit)
						{
							publishString = publishString.substring(0, calculatedLimit - 1);	
						}										
						TASK = PUBLISH_TO_CHATTER_USER_WITH_MENTIONS;
						showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_user_self_feed_with_mentions_message));
						executeAsyncTask();
					}
				}
				else
				{
					showToastNotification(getString(R.string.salesforce_no_records_selected_message));
				}
			}
			else if (TASK_TYPE == GET_GROUPS_LIST)
			{
				if (selectedIds.size() > 0)
				{
					if (publishString.length() > 1000)
					{
						publishString = publishString.substring(0, 999);
					}					
					showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_group_feed_message));
					TASK = PUBLISH_TO_CHATTER_GROUP;
					executeAsyncTask();
				}
				else
				{
					showToastNotification(getString(R.string.salesforce_no_records_selected_message));
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) 
	{
		if (listAdapter.isCheckListMode())
		{
			if (TASK_TYPE == GET_FOLLOWING_USER_LIST && !(listAdapter.getCheckedItemsList().size() < 25) && !listAdapter.isItemChecked(position))
			{
				showToastNotification(getString(R.string.salesforce_selected_user_exceed_message));	
				return;
			}
			listAdapter.setChecedkCurrentItem(position);
		}
		else if (listItems != null)
		{
			if (TASK_TYPE == GET_FOLLOWING_USER_LIST)
			{
				String recordId = listItems.get(position).getId();
				selectedIds = new ArrayList<String>();
				selectedIds.add(recordId);
				TASK = PUBLISH_TO_CHATTER_USER_WITH_MENTIONS;
				showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_user_self_feed_with_mentions_message));
				executeAsyncTask();
			}
			else if (TASK_TYPE == GET_GROUPS_LIST)
			{
				groupId = listItems.get(position).getId();
				TASK = PUBLISH_TO_CHATTER_GROUP;
				showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_group_feed_message));
				executeAsyncTask();
			}			
		}
	}
}