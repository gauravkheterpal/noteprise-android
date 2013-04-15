package com.metacube.noteprise.salesforce;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ParseException;

import com.metacube.noteprise.common.CommonListItems;
import com.metacube.noteprise.common.Constants;
import com.metacube.noteprise.util.NotepriseLogger;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest.RestMethod;
import com.salesforce.androidsdk.rest.RestResponse;

public class SalesforceUtils 
{
	
	public static Boolean checkObjectItem(JSONObject object)
	{
		if (		object.optString("triggerable").equalsIgnoreCase("true")
				&& 	object.optString("searchable").equalsIgnoreCase("true")
				&& 	object.optString("queryable").equalsIgnoreCase("true")
			)
		{
			return true;
		}		
		return false;
	}
	
	public static Boolean filterObjectFieldForStringType(JSONObject field)
	{
		if (field.optString("type").equalsIgnoreCase("string") || field.optString("type").equalsIgnoreCase("textarea"))
		{
			if (field.optString("updateable").equalsIgnoreCase("true"))
			{
				return true;
			}			
		}		
		return false;
	}
	
	public static int publishNoteToMyChatterFeed(RestClient salesforceRestClient, String noteContent, String SF_API_VERSION, File imageFile, String fileName, String imageTitle)
	{
		int publishResponse = 0;
		if (salesforceRestClient != null)
		{
			try 
			{
				if (imageFile != null && fileName != null && imageTitle != null)
				{
					PostMethod postMethod = null;		
					String url = salesforceRestClient.getClientInfo().instanceUrl + "/services/data/" + SF_API_VERSION + "/chatter/feeds/news/me/feed-items";
					if(noteContent!=null)
					{
					Part[] parts = { 
										new StringPart("desc", "Description"),
										new StringPart("fileName", fileName),
										new StringPart("text", noteContent),																				
										new FilePart("feedItemFileUpload", imageFile),
									};
					    postMethod = new PostMethod(url);
						postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
						}
						else
						{
							Part[] parts = { 
									new StringPart("desc", "Description"),
									new StringPart("fileName", fileName),																
									new FilePart("feedItemFileUpload", imageFile),
								};
							postMethod = new PostMethod(url);
							postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
						}
					
					
					postMethod.setRequestHeader("Authorization", "OAuth " + salesforceRestClient.getAuthToken());								
					org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
					publishResponse = client.executeMethod(postMethod);
				}
				else
				{
					
				}				
			}
			catch (UnsupportedEncodingException e) 
			{
				NotepriseLogger.logError("UnsupportedEncodingException while publishing chatter feed.", NotepriseLogger.ERROR, e);
			} 
			catch (IOException e) 
			{
				NotepriseLogger.logError("IOException while publishing chatter feed.", NotepriseLogger.ERROR, e);
			}		
		}
		return publishResponse;
	}
	
	
	public static RestResponse publishNoteContentToMyChatterFeed(RestClient salesforceRestClient, String noteContent, String SF_API_VERSION, File imageFile, String fileName, String imageTitle)
	{
		RestResponse publishResponse = null;
		String encodedText = null;
		try {
			encodedText = URLEncoder.encode(noteContent, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}		
		try {
			publishResponse = salesforceRestClient.sendSync(RestMethod.POST, "/services/data/" + SF_API_VERSION + "/chatter/feeds/news/me/feed-items?text=" + encodedText, null);
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return publishResponse;
	}
	
	public static RestResponse getUserFollowingData(RestClient salesforceRestClient, String SF_API_VERSION)
	{
		RestResponse publishResponse = null;
		if (salesforceRestClient != null)
		{
			try 
			{	
				publishResponse = salesforceRestClient.sendSync(RestMethod.GET, "/services/data/" + SF_API_VERSION + "/chatter/users/me/following?filterType=005&pageSize=" + Constants.USER_PAGE_BATCH_SIZE, null);
			} 
			catch (UnsupportedEncodingException e) 
			{
				NotepriseLogger.logError("UnsupportedEncodingException while getting following data.", NotepriseLogger.ERROR, e);
			} 
			catch (IOException e) 
			{
				NotepriseLogger.logError("IOException while getting following data.", NotepriseLogger.ERROR, e);
			}		
		}
		return publishResponse;
	}
	
	public static ArrayList<CommonListItems> getListItemsFromUserFollowingResponse(RestResponse restResponse)
	{
		ArrayList<CommonListItems> responseList = new ArrayList<CommonListItems>();
		try 
		{
			String response = restResponse.asString();
			NotepriseLogger.logMessage(response);
			JSONArray responseArray = new JSONObject(response).getJSONArray("following");
			for (int i = 0; i < responseArray.length(); i++)
			{
				JSONObject subject = responseArray.getJSONObject(i).getJSONObject("subject");
				CommonListItems item = new CommonListItems();				
				item.setId(subject.optString("id"));
				item.setLabel(subject.optString("name"));
				item.setLeftUserImageURL(subject.getJSONObject("photo").optString("smallPhotoUrl"));
				responseList.add(item);
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
		return responseList;
	}
	
	public static ArrayList<CommonListItems> getListAttachmentItem(String[] restResponse)
	{
		ArrayList<CommonListItems> responseList = new ArrayList<CommonListItems>();
		try 
		{
			
			for (int i = 0; i < restResponse.length; i++)
			{				
				CommonListItems item = new CommonListItems();								
				item.setLabel(restResponse[i]);				
				responseList.add(item);
			}
		} 
		catch (ParseException e) 
		{				
			e.printStackTrace();
		} 
				
		return responseList;
	}
	
	public static int publishNoteWithUserMentions(RestClient salesforceRestClient, String noteContent, String SF_API_VERSION, ArrayList<String> selectedIds, File imageFile, String fileName, String imageTitle)
	{
		
		int publishResponse =0;
		if (salesforceRestClient != null)
		{
			try 
			{
				String stringBody = generateJSONBodyForChatterFeed(noteContent, selectedIds, null, fileName, imageTitle);				
				String url = salesforceRestClient.getClientInfo().instanceUrl + "/services/data/" + SF_API_VERSION + "/chatter/feeds/news/me/feed-items";
	
				if (imageFile != null && fileName != null && imageTitle != null)
				{
						PostMethod postMethod = null;	
						postMethod = new PostMethod(url);	
						StringPart jsonPart = new StringPart("json",stringBody);
						jsonPart.setContentType("application/json");
						FilePart filePart= new FilePart("feedItemFileUpload", imageFile);
						filePart.setContentType("image/png");
						
						Part[] parts = { 								
								jsonPart,																
								filePart,
								new StringPart("text", "noteContent"),
							};
						postMethod = new PostMethod(url);
						postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
						NotepriseLogger.logMessage("sandeep");
						
					
				
				
				postMethod.setRequestHeader("Authorization", "OAuth " + salesforceRestClient.getAuthToken());
		
				
				org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
				publishResponse =client.executeMethod(postMethod);
					
				}
	
								
			} 
			catch (UnsupportedEncodingException e) 
			{
				NotepriseLogger.logError("UnsupportedEncodingException while publishing chatter feed.", NotepriseLogger.ERROR, e);
			} 
			catch (IOException e) 
				{
				NotepriseLogger.logError("IOException while publishing chatter feed.", NotepriseLogger.ERROR, e);
			}	
		}
		return publishResponse;
	}
	
	public static RestResponse publishNoteTextWithUserMentions(RestClient salesforceRestClient, String noteContent, String SF_API_VERSION, ArrayList<String> selectedIds)
	{
		
		RestResponse publishResponse = null;
		if (salesforceRestClient != null)
		{
			try 
			{
				String stringBody = generateJSONBodyForChatterFeed(noteContent, selectedIds, null, null,null);	
				String url = salesforceRestClient.getClientInfo().instanceUrl + "/services/data/" + SF_API_VERSION + "/chatter/feeds/news/me/feed-items";		
					StringEntity stringEntity = new StringEntity(stringBody);
					stringEntity.setContentType("application/json");
					publishResponse = salesforceRestClient.sendSync(RestMethod.POST, url, stringEntity);
						
			} 
			catch (UnsupportedEncodingException e) 
			{
				NotepriseLogger.logError("UnsupportedEncodingException while publishing chatter feed.", NotepriseLogger.ERROR, e);
			} 
			catch (IOException e) 
			{
				NotepriseLogger.logError("IOException while publishing chatter feed.", NotepriseLogger.ERROR, e);
			}	
		}
		return publishResponse;
	}
	public static String generateJSONBodyForChatterFeed(String content, ArrayList<String> mentionIds, String imageDescription, String fileName, String imageTitle)
	{
		JSONArray msg = new JSONArray();
		String bodyString = null;
		JSONObject requestJSON=null;
		try 
		{			
			if (mentionIds != null)
			{
				for (int i = 0; i < mentionIds.size(); i++)
				{
					JSONObject mention = new JSONObject();				
					mention.put("type", "mention");
					mention.put("id", mentionIds.get(i));
					msg.put(mention);
				}				
			}
			if (content != null)
			{
				JSONObject text = new JSONObject();
				text.put("type", "text");
				
				text.put("text", " " + content);
				msg.put(text);
			}	
			JSONObject attachment = null;
			if (fileName != null && imageTitle != null)
			{
				attachment = new JSONObject();
				attachment.putOpt("desc", imageDescription);
				attachment.putOpt("fileName", fileName);
				attachment.putOpt("title", imageTitle);
			}
			requestJSON= new JSONObject();
			requestJSON.putOpt("body", new JSONObject().put("messageSegments", msg));
			if (attachment != null)
			{
				requestJSON.putOpt("attachment", attachment);
			}
			bodyString = requestJSON.toString();
			NotepriseLogger.logMessage("Json"+requestJSON);			
		}  
		catch (JSONException e) 
		{
			e.printStackTrace();
		} 
	
		return bodyString;
	}
	
	public static RestResponse getUserGroupData(RestClient salesforceRestClient, String SF_API_VERSION)
	{
		RestResponse publishResponse = null;
		if (salesforceRestClient != null)
		{
			try 
			{	
				publishResponse = salesforceRestClient.sendSync(RestMethod.GET, "/services/data/" + SF_API_VERSION + "/chatter/users/me/groups?pageSize=" + Constants.GROUP_PAGE_BATCH_SIZE, null);
			} 
			catch (UnsupportedEncodingException e) 
			{
				NotepriseLogger.logError("UnsupportedEncodingException while getting group data.", NotepriseLogger.ERROR, e);
			} 
			catch (IOException e) 
			{
				NotepriseLogger.logError("IOException while getting group data.", NotepriseLogger.ERROR, e);
			}		
		}
		return publishResponse;
	}
	
	public static ArrayList<CommonListItems> getListItemsFromUserGroupResponse(RestResponse restResponse)
	{
		ArrayList<CommonListItems> responseList = new ArrayList<CommonListItems>();
		try 
		{
			String response = restResponse.asString();
			NotepriseLogger.logMessage(response);
			JSONArray responseArray = new JSONObject(response).getJSONArray("groups");
			for (int i = 0; i < responseArray.length(); i++)
			{
				JSONObject group = responseArray.getJSONObject(i);
				CommonListItems item = new CommonListItems();				
				item.setId(group.optString("id"));
				item.setLabel(group.optString("name"));
				item.setLeftUserImageURL(group.getJSONObject("photo").optString("smallPhotoUrl"));
				responseList.add(item);
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
		return responseList;
	}
	public static int publishNoteToUserGroupWithImage(RestClient salesforceRestClient, String groupId, String noteContent, String SF_API_VERSION,File imageFile)
	{
		
		int publishResponse =0;
		if (salesforceRestClient != null)
		{
			try 
			{	
				String url = salesforceRestClient.getClientInfo().instanceUrl + "/services/data/" + SF_API_VERSION + "/chatter/feeds/record/" + groupId + "/feed-items";
				PostMethod postMethod = null;	
				postMethod = new PostMethod(url);
				if(noteContent!=null)
				{
				Part[] parts = { 
						new StringPart("desc", ""),
						new StringPart("fileName", "sandeep"),
						new StringPart("text", noteContent),										
						new FilePart("feedItemFileUpload", imageFile),					
					};
				 postMethod = new PostMethod(url);
					postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));	
				}
				else 
				{
					Part[] parts = { 
							new StringPart("desc", ""),
							new StringPart("fileName", "sandeep"),								
							new FilePart("feedItemFileUpload", imageFile),
						};
					 postMethod = new PostMethod(url);
						postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));	
				}
				   
					postMethod.setRequestHeader("Authorization", "OAuth " + salesforceRestClient.getAuthToken());								
					org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
					publishResponse = client.executeMethod(postMethod);		

				NotepriseLogger.logMessage("sandeep"+publishResponse +"response"+postMethod.getResponseBodyAsString());
			
	
								
			} 
			catch (UnsupportedEncodingException e) 
			{
				NotepriseLogger.logError("UnsupportedEncodingException while publishing chatter feed.", NotepriseLogger.ERROR, e);
			} 
			catch (IOException e) 
			{
				NotepriseLogger.logError("IOException while publishing chatter feed.", NotepriseLogger.ERROR, e);
			}	
		}
		return publishResponse;
		/*RestResponse publishResponse = null;
		if (salesforceRestClient != null)
		{
			try 
			{				
				StringEntity stringEntity = new StringEntity(generateJSONBodyForChatterFeed(noteContent, null, null, null, null));
				stringEntity.setContentType("application/json");
				String url = "/services/data/" + SF_API_VERSION + "/chatter/feeds/record/" + groupId + "/feed-items";
				NotepriseLogger.logMessage(url);
				publishResponse = salesforceRestClient.sendSync(RestMethod.POST, url, stringEntity);
			} 
			catch (UnsupportedEncodingException e) 
			{
				NotepriseLogger.logError("UnsupportedEncodingException while publishing chatter feed to group.", NotepriseLogger.ERROR, e);
			} 
			catch (IOException e) 
			{
				NotepriseLogger.logError("IOException while publishing chatter feed to group.", NotepriseLogger.ERROR, e);
			}	
		}
		return publishResponse;*/
	}	
	
	public static RestResponse publishNoteToUserGroup(RestClient salesforceRestClient, String groupId, String noteContent, String SF_API_VERSION)
	{
		RestResponse publishResponse = null;
		if (salesforceRestClient != null)
		{
			try 
			{				
				StringEntity stringEntity = new StringEntity(generateJSONBodyForChatterFeed(noteContent, null, null, null, null));
				stringEntity.setContentType("application/json");
				String url = "/services/data/" + SF_API_VERSION + "/chatter/feeds/record/" + groupId + "/feed-items";
				NotepriseLogger.logMessage(url);
				publishResponse = salesforceRestClient.sendSync(RestMethod.POST, url, stringEntity);
			} 
			catch (UnsupportedEncodingException e) 
			{
				NotepriseLogger.logError("UnsupportedEncodingException while publishing chatter feed to group.", NotepriseLogger.ERROR, e);
			} 
			catch (IOException e) 
			{
				NotepriseLogger.logError("IOException while publishing chatter feed to group.", NotepriseLogger.ERROR, e);
			}	
		}
		return publishResponse;
	}	
}