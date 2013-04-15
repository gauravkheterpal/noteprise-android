package com.metacube.noteprise.core.screens;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.metacube.noteprise.R;
import com.metacube.noteprise.R.drawable;
import com.metacube.noteprise.common.BaseFragment;
import com.metacube.noteprise.common.CommonCustomDialog;
import com.metacube.noteprise.common.CommonListAdapter;
import com.metacube.noteprise.common.CommonListItems;
import com.metacube.noteprise.salesforce.SalesforceUtils;
import com.metacube.noteprise.util.NotepriseLogger;
import com.metacube.noteprise.util.Utilities;
import com.salesforce.androidsdk.rest.RestResponse;

public class PublishToChatterRecordsListScreen extends BaseFragment implements OnClickListener, OnItemClickListener
{
	String publishString, publishTask, groupId,filePath;
	RestResponse dataResponse = null, publishResponse;
	public static final int GET_FOLLOWING_USER_LIST = 0, PUBLISH_TO_CHATTER_USER_WITH_MENTIONS = 1, GET_GROUPS_LIST = 2, PUBLISH_TO_CHATTER_GROUP = 3;
	Integer TASK = -1, TASK_TYPE = -1;
	int count = 0, numberOfResponse = 0,responseCode;
	ArrayList<CommonListItems> listItems;
	CommonListAdapter listAdapter;
	ListView listView;
	TextView textView;
	int pos;
	ArrayList<String> selectedIds = null;
	CommonCustomDialog postToUser,postToGroup;
	Button   postToUserYes,postToUserNo, postToGroupNo, postToGroupYes;
	BaseFragment baseFragment;
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
        filePath =	Utilities.getStringFromBundle(getArguments(), "filePath");
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
    	textView=(TextView)contentView.findViewById(R.id.common_list_no_results);
    	baseActivity.editButton.setOnClickListener(this);
    	baseActivity.saveButton.setOnClickListener(this);
    	baseActivity.editButton.setImageDrawable(getResources().getDrawable(R.drawable.edit_button_normal));
       
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
					if(publishString!=null)
					{
					if (publishString.length() > 950)
					{
						publishString = publishString.substring(0, 949);
					}
					}
					if(filePath !=null)
					{
						File file = new File(filePath);
						responseCode = SalesforceUtils.publishNoteWithUserMentions(salesforceRestClient, publishString, SF_API_VERSION, selectedIds, file, "sandeep","sandeep");
					}
						
					else
					{
						publishResponse = SalesforceUtils.publishNoteTextWithUserMentions(salesforceRestClient, publishString, SF_API_VERSION, selectedIds);
					}
					break;
				}
				case GET_GROUPS_LIST:
				{
					dataResponse = SalesforceUtils.getUserGroupData(salesforceRestClient, SF_API_VERSION);
					break;
				}
				case PUBLISH_TO_CHATTER_GROUP:
				{	
					if(publishString!=null)
					{
					if (publishString.length() > 1000)
					{
						publishString = publishString.substring(0, 999);
					}
					}
					if (selectedIds != null && selectedIds.size() > 1)
					{
						for (int i = 0; i < selectedIds.size(); i++)
						{	
							groupId = selectedIds.get(i);
							NotepriseLogger.logMessage("Post successful for groupId=" + groupId + " at position=" + i);
							if(filePath !=null)
							{
								File file = new File(filePath);
								responseCode = SalesforceUtils.publishNoteToUserGroupWithImage(salesforceRestClient, groupId, publishString, SF_API_VERSION,file);
							}
								
							else
							{
							publishResponse = SalesforceUtils.publishNoteToUserGroup(salesforceRestClient, groupId, publishString, SF_API_VERSION);
							if (publishResponse.getStatusCode() == 200 || publishResponse.getStatusCode() == 201)
							{
								NotepriseLogger.logMessage("Post successful for groupId=" + groupId + " at position=" + i);
							}
						}
							
							
						}
					}
					else
					{
						if(filePath !=null)
						{
							NotepriseLogger.logMessage("single group"+filePath);
							File file = new File(filePath);
							responseCode = SalesforceUtils.publishNoteToUserGroupWithImage(salesforceRestClient, groupId, publishString, SF_API_VERSION,file);
						}
							
						else
						{
						publishResponse = SalesforceUtils.publishNoteToUserGroup(salesforceRestClient, groupId, publishString, SF_API_VERSION);
					}
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
				else{
					textView.setText(R.string.no_chatter_user);
					textView.setVisibility(View.VISIBLE);
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
				    hideFullScreenProgresIndicator();
				    }
				  else{
				        
				      hideFullScreenProgresIndicator();
				      textView.setText(R.string.no_chatter_group);
				      textView.setVisibility(View.VISIBLE);
				        }
			}
		}
		else if (TASK == PUBLISH_TO_CHATTER_USER_WITH_MENTIONS)
		{
			TASK = -1;
			try 
			{	
				if(filePath !=null)
				{
					if(responseCode == 201)
					{
						showToastNotification(getString(R.string.salesforce_chatter_post_user_with_mentions_success_message));
						finishScreen();
					}
				}
				
				else if (publishResponse.getStatusCode() == 200 || publishResponse.getStatusCode() == 201)
				{
					showToastNotification(getString(R.string.salesforce_chatter_post_user_with_mentions_success_message));
					finishScreen();
				}
				else
				{
					//showToastNotification(getString(R.string.some_error_ocurred_message));
					showToastNotification("Error when publishing note on specific Chatter user feed");
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
				if(filePath !=null)
				{
					if(responseCode == 201)
					{
						showToastNotification(getString(R.string.salesforce_chatter_post_group_success_message));
						finishScreen();
					}
				}
				
				else if (publishResponse.getStatusCode() == 200 || publishResponse.getStatusCode() == 201)
				{
					showToastNotification(getString(R.string.salesforce_chatter_post_group_success_message));
					finishScreen();
				}
				else
				{
					//showToastNotification(getString(R.string.some_error_ocurred_message));
					showToastNotification("Error when publishing note on Chatter Group");
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
		baseActivity.editButton.setImageDrawable(getResources().getDrawable(R.drawable.edit_button_normal));

	}

	public void instantiateCustomDialog(View view) {
		  super.instantiateCustomDialog(view);
		  if(TASK_TYPE == GET_GROUPS_LIST){
			   postToGroupYes = (Button) view.findViewById(R.id.confirm_note_yes_button);//"done by me"
			  postToGroupYes.setOnClickListener(this);
			   postToGroupNo = (Button) view.findViewById(R.id.confirm_note_no_button);
			   postToGroupNo.setOnClickListener(this);
			  }
			   else if(TASK_TYPE == GET_FOLLOWING_USER_LIST)
			   {
		   postToUserYes = (Button) view//"done by me"
		     .findViewById(R.id.post_note_yes_button);
		  postToUserYes.setOnClickListener(this);
		   postToUserNo = (Button) view
		     .findViewById(R.id.post_note_no_button);
		   postToUserNo.setOnClickListener(this);}
		  
		}
	@Override
	public void onClick(View view) 
	{
		
		 if (view == baseActivity.editButton)
		{
			
			baseActivity.saveButton.setVisibility(View.VISIBLE);
				if (listAdapter.isCheckListMode())
					{
					baseActivity.editButton.setImageDrawable(getResources().getDrawable(R.drawable.edit_button_normal));
					listAdapter.clearCheckedItems(listItems);
					listAdapter.hideCheckList();
					baseActivity.saveButton.setVisibility(View.GONE);
					}
					else{
		
						baseActivity.editButton.setImageDrawable(getResources().getDrawable(R.drawable.cancel_button_normal));
						baseActivity.saveButton.setVisibility(View.VISIBLE);
						listAdapter.showCheckList();
					}
		}
		else if (view == baseActivity.saveButton)
		{	
		/*	if (listAdapter.isCheckListMode())
		{
		*/	
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
					if(publishString !=null)
					{
					if (publishString.length() > 1000)
					{
						publishString = publishString.substring(0, 999);
					}					
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
		
		
		else if(view==postToUserYes)
		{
			showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_user_self_feed_with_mentions_message));
			executeAsyncTask();
			postToUser.dismiss();
		}
		else if(view==postToUserNo)
			postToUser.dismiss();
		else if (view == postToGroupYes) {
			   groupId = listItems.get(pos).getId();
			   showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_group_feed_message));
			      executeAsyncTask();
			      postToGroup.dismiss();
			             
			     }else if(view==postToGroupNo){
			   postToGroup.dismiss();
			    }//done by me
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) 
	{
		 pos=position;
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
				postToUser=new CommonCustomDialog(R.layout.post_to_user_confirm_dialog, this);
				postToUser.show(getFragmentManager(), "CONFIRM_POST_TO_USER");
				//showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_user_self_feed_with_mentions_message));
				//executeAsyncTask();
				
				
				
			}
			else if (TASK_TYPE == GET_GROUPS_LIST)
			{
				groupId = listItems.get(position).getId();
				TASK = PUBLISH_TO_CHATTER_GROUP;
				postToGroup = new CommonCustomDialog(R.layout.post_to_group_confirm_dialog, this);//to confirm post to group
		          postToGroup.show(getFragmentManager(), "CONFIRM_POST_TO_GROUP");
		    
				/*showFullScreenProgresIndicator(getString(R.string.progress_dialog_title), getString(R.string.progress_dialog_chatter_publish_to_group_feed_message));
				executeAsyncTask();*/
			}			
		}
	}
}