package com.metacube.noteprise.core.screens;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.metacube.noteprise.R;
import com.metacube.noteprise.common.BaseFragment;
import com.metacube.noteprise.common.CommonCustomDialog;
import com.metacube.noteprise.common.CommonListItems;
import com.metacube.noteprise.common.CommonSpinnerAdapter;
import com.metacube.noteprise.common.Constants;
import com.metacube.noteprise.salesforce.SalesforceUtils;
import com.metacube.noteprise.util.NotepriseLogger;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;

public class SalesforceObjectChooser extends BaseFragment implements OnClickListener, AsyncRequestCallback, OnItemClickListener 
{
	Button objectSpinnerButton, fieldSpinnerButton;
	RestRequest sobjectsRequest, fieldsRequest;
	CommonSpinnerAdapter objectSpinnerAdapter, fieldSpinnerAdapter;
	String OBJECT_SPINNER_TAG = "OBJECT_SPINNER", FIELD_SPINNER_TAG = "FIELD_SPINNER";
	CommonCustomDialog objectSpinnerDialog, fieldSpinnerDialog;
	RelativeLayout objectSpinnerLayout, fieldSpinnerLayout;
	ListView objectSpinnerList, fieldSpinnerList;
	String objectName, objectLabel, fieldName, fieldLabel;
	Integer fieldLength;
	TextView objectSpinnerDialogPromptText, fieldSpinnerDialogPromptText;
	
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        objectSpinnerDialog = new CommonCustomDialog(R.layout.custom_spinner_dropdown_layout, this, OBJECT_SPINNER_TAG);
        fieldSpinnerDialog = new CommonCustomDialog(R.layout.custom_spinner_dropdown_layout, this, FIELD_SPINNER_TAG);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	clearContainer(container);
    	View contentView = inflater.inflate(R.layout.salesforce_object_chooser_layout, container);
    	objectSpinnerButton = (Button) contentView.findViewById(R.id.object_list_spinner_button);        
        objectSpinnerButton.setOnClickListener(this);
        objectSpinnerButton.setEnabled(Boolean.FALSE);        
        objectSpinnerLayout = (RelativeLayout) contentView.findViewById(R.id.object_list_spinner_layout);
        objectSpinnerLayout.setVisibility(View.GONE);
        fieldSpinnerButton = (Button) contentView.findViewById(R.id.field_list_spinner_button);
        fieldSpinnerButton.setOnClickListener(this);
        fieldSpinnerButton.setEnabled(Boolean.FALSE);
        fieldSpinnerLayout = (RelativeLayout) contentView.findViewById(R.id.field_list_spinner_layout);
        fieldSpinnerLayout.setVisibility(View.GONE);        
        baseActivity.saveButton.setOnClickListener(this);
    	return super.onCreateView(inflater, container, savedInstanceState);
    }

	@Override
	public void onClick(View view) 
	{
		if (view == baseActivity.saveButton)
		{
			noteprisePreferences.saveUserSalesforceObjectFieldMapping(objectName, objectLabel, fieldName, fieldName, fieldLength);			
			baseActivity.SELECTED_OBJECT_NAME = objectName;
			baseActivity.SELECTED_OBJECT_LABEL = objectLabel;
			baseActivity.SELECTED_FIELD_NAME = fieldName;
			baseActivity.SELECTED_FIELD_LABEL = fieldLabel;
			baseActivity.SELECTED_FIELD_LENGTH = fieldLength;
			finishScreen();
		}
		else if (view == objectSpinnerButton)
		{
			if (objectSpinnerDialog != null)
			{
				objectSpinnerDialog.show(getFragmentManager(), "ObjectSpinnerDialog");
			}
		}
		else if (view == fieldSpinnerButton)
		{
			if (fieldSpinnerDialog != null)
			{
				fieldSpinnerDialog.show(getFragmentManager(), "FieldSpinnerDialog");
			}
		}
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		if (salesforceRestClient != null)
		{
			showProgresIndicator();			
			sobjectsRequest = RestRequest.getRequestForDescribeGlobal(SF_API_VERSION);
			salesforceRestClient.sendAsync(sobjectsRequest, this);			
		}
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
		if (view != null && (String) view.getTag() == OBJECT_SPINNER_TAG)
		{
			objectSpinnerDialogPromptText = (TextView) view.findViewById(R.id.custom_spinner_prompt_text);
			objectSpinnerDialogPromptText.setText(R.string.choose_object_prompt);
			objectSpinnerList = (ListView) view.findViewById(R.id.custom_spinner_list_view);
			objectSpinnerList.setAdapter(objectSpinnerAdapter);
			objectSpinnerList.setOnItemClickListener(this);
		}
		else if (view != null && (String) view.getTag() == FIELD_SPINNER_TAG)
		{
			fieldSpinnerDialogPromptText = (TextView) view.findViewById(R.id.custom_spinner_prompt_text);
			fieldSpinnerDialogPromptText.setText(R.string.choose_field_prompt);
			fieldSpinnerList = (ListView) view.findViewById(R.id.custom_spinner_list_view);
			fieldSpinnerList.setAdapter(fieldSpinnerAdapter);
			fieldSpinnerList.setOnItemClickListener(this);
		}
	}

	@Override
	public void onSuccess(RestRequest request, RestResponse response) 
	{
		JSONObject responseObject;
		if (request == sobjectsRequest)
		{
			try 
			{
				responseObject = response.asJSONObject();
				NotepriseLogger.logMessage(responseObject.toString());
				ArrayList<CommonListItems> items = new ArrayList<CommonListItems>();
				JSONArray sobjects = responseObject.getJSONArray("sobjects");
				for (int i=0; i < sobjects.length(); i++)
				{
					CommonListItems item = new CommonListItems();
					JSONObject object = sobjects.getJSONObject(i);
					if (SalesforceUtils.checkObjectItem(object))
					{
						item.setLabel(object.optString("label"));
						item.setName(object.optString("name"));						
						items.add(item);
					}					
				}
				objectSpinnerAdapter = new CommonSpinnerAdapter(inflater, items);
				objectSpinnerAdapter.changeOrdering(Constants.SORT_BY_LABEL);
				objectSpinnerButton.setEnabled(Boolean.TRUE);
				objectSpinnerLayout.setVisibility(View.VISIBLE);
				hideProgresIndicator();
			} 
			catch (ParseException e) 
			{
				e.printStackTrace();
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else if (request == fieldsRequest)
		{
			try 
			{
				responseObject = response.asJSONObject();
				NotepriseLogger.logMessage("fields"+responseObject.toString());
				ArrayList<CommonListItems> items = new ArrayList<CommonListItems>();
				JSONArray fields = responseObject.getJSONArray("fields");
				for (int i=0; i < fields.length(); i++)
				{
					CommonListItems item = new CommonListItems();
					JSONObject field = fields.getJSONObject(i);
					if (SalesforceUtils.filterObjectFieldForStringType(field))
					{
						item.setLabel(field.optString("label"));
						item.setName(field.optString("name"));
						item.setFieldLength(field.optInt("length"));
						items.add(item);
					}
				}
				fieldSpinnerAdapter = new CommonSpinnerAdapter(inflater, items);
				fieldSpinnerAdapter.changeOrdering(Constants.SORT_BY_LABEL);
				fieldSpinnerButton.setEnabled(Boolean.TRUE);
				hideProgresIndicator();
			} 
			catch (ParseException e) 
			{
				e.printStackTrace();
			} 
			catch (JSONException e) 
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
	public void onError(Exception exception) 
	{
		NotepriseLogger.logError("Exception getting response in objectChooser fragment.", NotepriseLogger.ERROR, exception);
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) 
	{		
		if (adapter.getAdapter() == objectSpinnerAdapter)
		{
			CommonListItems item = objectSpinnerAdapter.getListItem(position);
			objectLabel = item.getLabel();
			objectName = item.getName();
			objectSpinnerDialog.dismiss();
			fieldSpinnerLayout.setVisibility(View.VISIBLE);
			objectSpinnerButton.setText(objectLabel);			
			if (salesforceRestClient != null)
			{
				showProgresIndicator();
				baseActivity.saveButton.setVisibility(View.GONE);
				fieldsRequest = RestRequest.getRequestForDescribe(SF_API_VERSION, item.getName());
				salesforceRestClient.sendAsync(fieldsRequest, this);			
			}
		}
		else if (adapter.getAdapter() == fieldSpinnerAdapter)
		{
			CommonListItems item = fieldSpinnerAdapter.getListItem(position);
			fieldLabel = item.getLabel();
			fieldName = item.getName();
			fieldLength = item.getFieldLength();
			fieldSpinnerDialog.dismiss();
			baseActivity.saveButton.setVisibility(View.VISIBLE);
			fieldSpinnerButton.setText(fieldLabel);
		}		
	}	
}