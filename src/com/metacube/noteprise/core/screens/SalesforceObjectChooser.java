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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

import com.metacube.noteprise.R;
import com.metacube.noteprise.common.BaseFragment;
import com.metacube.noteprise.common.CommonListItems;
import com.metacube.noteprise.common.CommonSpinnerAdapter;
import com.metacube.noteprise.common.Constants;
import com.metacube.noteprise.salesforce.SalesforceUtils;
import com.metacube.noteprise.util.NotepriseLogger;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;

public class SalesforceObjectChooser extends BaseFragment implements OnClickListener, AsyncRequestCallback, OnItemSelectedListener 
{
	Spinner objectSpinner, fieldSpinner;
	RestRequest sobjectsRequest, fieldsRequest;
	CommonSpinnerAdapter objectsSpinnerAdapter, fieldsSpinnerAdapter;
	
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	clearContainer(container);
    	View contentView = inflater.inflate(R.layout.salesforce_object_chooser_layout, container);
    	objectSpinner = (Spinner) contentView.findViewById(R.id.object_list_spinner);
        fieldSpinner = (Spinner) contentView.findViewById(R.id.field_list_spinner);
        baseActivity.saveButton.setOnClickListener(this);
    	return super.onCreateView(inflater, container, savedInstanceState);
    }

	@Override
	public void onClick(View view) 
	{
		if (view == baseActivity.saveButton)
		{
			String objectName = ((CommonListItems) objectSpinner.getSelectedItem()).getName();
			String objectLabel = ((CommonListItems) objectSpinner.getSelectedItem()).getLabel();
			String fieldName = ((CommonListItems) fieldSpinner.getSelectedItem()).getName();
			String fieldLabel = ((CommonListItems) fieldSpinner.getSelectedItem()).getLabel();		
			Integer fieldLength = ((CommonListItems) fieldSpinner.getSelectedItem()).getFieldLength();
			noteprisePreferences.saveUserSalesforceObjectFieldMapping(objectName, objectLabel, fieldName, fieldName, fieldLength);			
			baseActivity.SELECTED_OBJECT_NAME = objectName;
			baseActivity.SELECTED_OBJECT_LABEL = objectLabel;
			baseActivity.SELECTED_FIELD_NAME = fieldName;
			baseActivity.SELECTED_FIELD_LABEL = fieldLabel;
			baseActivity.SELECTED_FIELD_LENGTH = fieldLength;
			baseActivity.salesforceObjectsButton.setVisibility(View.VISIBLE);
			finishScreen();
		}
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		baseActivity.salesforceObjectsButton.setVisibility(View.GONE);
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
		baseActivity.salesforceObjectsButton.setVisibility(View.VISIBLE);
		baseActivity.saveButton.setVisibility(View.GONE);
	}
	
	@Override
	public void onDetach() 
	{
		super.onDetach();
		
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
				objectsSpinnerAdapter = new CommonSpinnerAdapter(inflater, items);
				objectsSpinnerAdapter.changeOrdering(Constants.SORT_BY_LABEL);
				objectSpinner.setAdapter(objectsSpinnerAdapter);
				objectSpinner.setOnItemSelectedListener(this);
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
				fieldsSpinnerAdapter = new CommonSpinnerAdapter(inflater, items);
				fieldsSpinnerAdapter.changeOrdering(Constants.SORT_BY_LABEL);
				fieldSpinner.setAdapter(fieldsSpinnerAdapter);
				baseActivity.saveButton.setVisibility(View.VISIBLE);
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
		NotepriseLogger.logError("Exception getting response for accounts.", NotepriseLogger.ERROR, exception);
	}

	@Override
	public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) 
	{
		CommonListItems item = (CommonListItems) objectsSpinnerAdapter.getItem(position);	
		if (salesforceRestClient != null)
		{
			showProgresIndicator();
			baseActivity.saveButton.setVisibility(View.GONE);
			fieldsRequest = RestRequest.getRequestForDescribe(SF_API_VERSION, item.getName());
			salesforceRestClient.sendAsync(fieldsRequest, this);			
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) 
	{		
	}	
}