package com.metacube.noteprise.salesforce;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.metacube.noteprise.common.CommonListItems;
import com.metacube.noteprise.common.Constants;
import com.metacube.noteprise.util.NotepriseLogger;
import com.metacube.noteprise.util.Utilities;
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
	
	public static RestResponse publishNoteToMyChatterFeed(RestClient salesforceRestClient, String noteContent, String SF_API_VERSION, File imageFile, String fileName, String imageTitle)
	{
		RestResponse publishResponse = null;
		if (salesforceRestClient != null)
		{
			try 
			{
				if (imageFile != null && fileName != null && imageTitle != null)
				{
					//String stringBody = generateJSONBodyForChatterFeed(noteContent, null, null, fileName, imageTitle);					
					//String url = "/services/data/" + SF_API_VERSION + "/chatter/feeds/news/me/feed-items";
					String url ="https://ap1.salesforce.com/services/data/v25.0/chatter/feeds/news/me/feed-items";
					
					HttpClient httpclient = new DefaultHttpClient();
			        HttpPost httppost;
			        MultipartEntity reqEntity;
			        httppost = new HttpPost(url);
			        reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			        File file = new File("/mnt/sdcard/image.png"); 
			        NotepriseLogger.logMessage("File can read=="+file.canRead() + "--" + file.length());
			       /* File imageFile1 = new File(
			        		"E:\\NotePrise\\Salesforce\\menu.jpg");*/
			        FileBody bin = new FileBody(file);
			        reqEntity.addPart("feedItemFileUpload", bin);
			        String fileName1 = "Chatter Image";
			        // file name can be text plain only, though using text/html doesn't breaks
			        reqEntity.addPart("fileName", new StringBody(fileName1, "text/plain",
			                Charset.defaultCharset()));
			        // Sending text/html doesn't helps as HTML will be printed, though using text/html doesn't breaks
			        reqEntity.addPart("text", new StringBody("Hello World", "text/plain",
			                Charset.defaultCharset()));
			        reqEntity.addPart("feedItemFileUpload", new FileBody(file,
			                fileName1, "application/octet-stream", Charset.defaultCharset()
			                        .toString()));
			        httppost.setEntity(reqEntity);

			        httppost.setHeader("Authorization", "OAuth " + salesforceRestClient.getAuthToken());
			        HttpResponse resp = httpclient.execute(httppost);
			        HttpEntity response = resp.getEntity();
			        NotepriseLogger.logMessage("Response for image"+resp.getStatusLine().toString() + "--" + Utilities.convertStreamToString(response.getContent()));
					/*NotepriseLogger.logMessage(url);
					MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
					multipartEntity.addPart("fileName", new StringBody(fileName, "text/html", Charset.defaultCharset()));
					multipartEntity.addPart("text", new StringBody(noteContent, "text/html", Charset.defaultCharset()));
					multipartEntity.addPart("feedItemFileUpload", new FileBody(imageFile, fileName, "application/octet-stream", Charset.defaultCharset().toString()));
					publishResponse = salesforceRestClient.sendSync(RestMethod.POST, url, multipartEntity);
					Part[] parts = { 
										new StringPart("fileName", fileName),
										new StringPart("text", noteContent),
										new FilePart("feedItemFileUpload", imageFile),
									};
					String url = salesforceRestClient.getClientInfo().instanceUrl + "/services/data/" + SF_API_VERSION + "/chatter/feeds/news/me/feed-items";
					NotepriseLogger.logMessage(url);
					PostMethod postMethod = new PostMethod(url);
					postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
					postMethod.setRequestHeader("Authorization", "OAuth " + salesforceRestClient.getAuthToken());
					postMethod.addRequestHeader("X-PrettyPrint", "1");
					postMethod.setRequestHeader("Content-Type", "multipart/form-data");
					org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
					client.executeMethod(postMethod);
					String responseBody = postMethod.getResponseBodyAsString();
					NotepriseLogger.logMessage(responseBody);*/
				}
				else
				{
					String encodedText = URLEncoder.encode(noteContent, "UTF-8");		
					publishResponse = salesforceRestClient.sendSync(RestMethod.POST, "/services/data/" + SF_API_VERSION + "/chatter/feeds/news/me/feed-items?text=" + encodedText, null);
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
	
	public static RestResponse publishNoteWithUserMentions(RestClient salesforceRestClient, String noteContent, String SF_API_VERSION, ArrayList<String> selectedIds, File imageFile, String fileName, String imageTitle)
	{
		RestResponse publishResponse = null;
		if (salesforceRestClient != null)
		{
			try 
			{
				String stringBody = generateJSONBodyForChatterFeed(noteContent, selectedIds, null, fileName, imageTitle);				
				String url = "/services/data/" + SF_API_VERSION + "/chatter/feeds/news/me/feed-items";
				NotepriseLogger.logMessage(url);
				if (imageFile != null && fileName != null && imageTitle != null)
				{
					MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
					multipartEntity.addPart("data", new StringBody(stringBody, "application/json", Charset.defaultCharset()));
					multipartEntity.addPart("file", new FileBody(imageFile, fileName, "image/png", Charset.defaultCharset().toString()));
					NotepriseLogger.logMessage(multipartEntity.toString());
					//publishResponse = salesforceRestClient.sendSync(RestMethod.POST, url, multipartEntity);
				}
				else
				{
					StringEntity stringEntity = new StringEntity(stringBody);
					stringEntity.setContentType("application/json");
					publishResponse = salesforceRestClient.sendSync(RestMethod.POST, url, stringEntity);
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
	
	public static String generateJSONBodyForChatterFeed(String content, ArrayList<String> mentionIds, String imageDescription, String fileName, String imageTitle)
	{
		JSONArray msg = new JSONArray();
		String bodyString = null;
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
				//content = URLEncoder.encode(content, "UTF-8");
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
			JSONObject requestJSON = new JSONObject();
			requestJSON.putOpt("body", new JSONObject().put("messageSegments", msg));
			if (attachment != null)
			{
				requestJSON.putOpt("attachment", attachment);
			}
			bodyString = requestJSON.toString();
			NotepriseLogger.logMessage(bodyString);			
		}  
		catch (JSONException e) 
		{
			e.printStackTrace();
		} 
		/*catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}*/ 
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