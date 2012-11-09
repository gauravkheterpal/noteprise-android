package com.metacube.noteprise.salesforce;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.metacube.noteprise.common.Constants;

public class CommonSOQL 
{
	public static final String SELECT_QUERY_PREFIX = "SELECT id, ";
	public static final String DISPLAY_LABEL_FIELD = "name";
	public static final String FROM = " from ";
	public static final String ORDER_BY_SUFFIX = " ORDER BY ";
	public static final String LIMIT = " LIMIT ";
	public static final String OFFSET_VALUE = " OFFSET ";
			
	public static String getQueryForObject(String object, String offsetValue,Boolean getObjectCount)
	{
		HashMap<String, String> OBJECT_FIELD_MAP = new LinkedHashMap<String, String>();	   
		OBJECT_FIELD_MAP.put("Case", "CaseNumber");
		OBJECT_FIELD_MAP.put("CaseComment", "ParentId");
		OBJECT_FIELD_MAP.put("ContentVersion", "ContentDocumentId");
		OBJECT_FIELD_MAP.put("Contract", "ContractNumber");
		OBJECT_FIELD_MAP.put("Event", "Subject");
		OBJECT_FIELD_MAP.put("FeedComment", "FeedItemId");
		OBJECT_FIELD_MAP.put("Idea", "Title");
		OBJECT_FIELD_MAP.put("Note", "Title");
		OBJECT_FIELD_MAP.put("Solution", "SolutionName");
		OBJECT_FIELD_MAP.put("Task", "Subject");		
		
		String displayField = DISPLAY_LABEL_FIELD;
		if (OBJECT_FIELD_MAP.get(object) != null)
		{
			displayField = OBJECT_FIELD_MAP.get(object);
		}
		
		String query="";
		
		if(getObjectCount)
		{
			query = SELECT_QUERY_PREFIX + displayField + FROM + object + ORDER_BY_SUFFIX + displayField ;
		}
		else
		{
			if(offsetValue==null)
				query = SELECT_QUERY_PREFIX + displayField + FROM + object + ORDER_BY_SUFFIX + displayField + LIMIT + Constants.RECORD_LIMIT;
			else
				query = SELECT_QUERY_PREFIX + displayField + FROM + object + ORDER_BY_SUFFIX + displayField + LIMIT + Constants.RECORD_LIMIT + OFFSET_VALUE + offsetValue;
		}
		
		return query;
	}
	
	public static boolean getSupportedObject(String object)
	{
		HashMap<String, String> OBJECT_FIELD_MAP = new LinkedHashMap<String, String>();	
		boolean result= false;
		OBJECT_FIELD_MAP.put("Account", "Account");
		OBJECT_FIELD_MAP.put("Asset", "Asset");
		OBJECT_FIELD_MAP.put("Campaign", "Campaign");
		OBJECT_FIELD_MAP.put("Case", "Case");
		OBJECT_FIELD_MAP.put("Contact", "Contact");
		OBJECT_FIELD_MAP.put("Contract", "Contract");
		OBJECT_FIELD_MAP.put("Custom objects", "Custom objects");
		OBJECT_FIELD_MAP.put("EmailMessage", "EmailMessage");
		OBJECT_FIELD_MAP.put("EmailTemplate", "EmailTemplate");
		OBJECT_FIELD_MAP.put("Event", "Event");		
		OBJECT_FIELD_MAP.put("Lead", "Lead");
		OBJECT_FIELD_MAP.put("Opportunity", "Opportunity");
		OBJECT_FIELD_MAP.put("Product2", "Product2");
		OBJECT_FIELD_MAP.put("Solution", "Solution");
		OBJECT_FIELD_MAP.put("Task", "Task");
		
		if (OBJECT_FIELD_MAP.get(object) != null)
		{
			result = true;
		}		
		return result;
	}
}
