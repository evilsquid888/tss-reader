/*
 * Tiny Tiny RSS Reader for Android
 * 
 * Copyright (C) 2009 J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.ttrssreader.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ttrssreader.model.article.ArticleItem;
import org.ttrssreader.model.category.CategoryItem;
import org.ttrssreader.model.feed.FeedItem;
import org.ttrssreader.utils.Utils;

public class TTRSSJsonConnector implements ITTRSSConnector {

	private static final String OP_LOGIN = "?op=login&user=%s&password=%s";
	private static final String OP_GET_UNREAD = "?op=getUnread&sid=%s";
	private static final String OP_GET_CATEGORIES = "?op=getCategories&sid=%s";
	private static final String OP_GET_FEEDS = "?op=getFeeds&sid=%s";
	private static final String OP_GET_FEEDHEADLINES = "?op=getHeadlines";
	
	private static final String ERROR_NAME = "{\"error\":";
	private static final String SESSION_ID = "session_id";
	private static final String ID_NAME = "id";
	private static final String TITLE_NAME = "title";
	private static final String UNREAD_NAME = "unread";
	
	private String mServerUrl;
	private String mUserName;
	private String mPassword;
	
	private String mSessionId;
	
	private String mLastError = "";
	private boolean mHasLastError = false;
	
	public TTRSSJsonConnector(String serverUrl, String userName, String password) {
		mServerUrl = serverUrl;
		mUserName = userName;
		mPassword = password;
		
		mSessionId = null;
	}
	
	private String doRequest(String url) {
		String strResponse = null;
		
		HttpClient httpclient = new DefaultHttpClient();		 				
		HttpPost httpPost = new HttpPost(url);
 
        // Execute the request
        HttpResponse response;
        
        try {
        	
            response = httpclient.execute(httpPost);
            
            HttpEntity entity = response.getEntity();
            
            if (entity != null) {
            	InputStream instream = entity.getContent();
            	
            	strResponse = Utils.convertStreamToString(instream);
            	
            	if (strResponse.startsWith(ERROR_NAME)) {
            		mHasLastError = true;
            		mLastError = strResponse;
            	}
            }
        } catch (ClientProtocolException e) {
        	mHasLastError = true;
    		mLastError = e.getMessage();
        } catch (IOException e) {
        	mHasLastError = true;
    		mLastError = e.getMessage();
        }
        
        return strResponse;
	}
	
	private JSONArray getJSONResponseAsArray(String url) {
		mHasLastError = false;
		mLastError = "";
		
		JSONArray result = null;
		
		String strResponse = doRequest(url);
		
		try {
			result = new JSONArray(strResponse);
		} catch (JSONException e) {
			mHasLastError = true;
    		mLastError = e.getMessage();
		}
		
		return result;
	}
	
	private TTRSSJsonResult getJSONResponse(String url) {
		
		mHasLastError = false;
		mLastError = "";
		
		TTRSSJsonResult result = null;
		
		String strResponse = doRequest(url);
		
		try {
			
			result = new TTRSSJsonResult(strResponse);
			
		} catch (JSONException e) {
			mHasLastError = true;
    		mLastError = e.getMessage();
		}
        
        return result;
	}
	
	private boolean login() {
		boolean result = true;
		mSessionId = null;
		
		String url = mServerUrl + String.format(OP_LOGIN, mUserName, mPassword);
		
		TTRSSJsonResult jsonResult = getJSONResponse(url);
		
		if (!mHasLastError) {
			
			int i = 0;
			boolean stop = false;
			
			try {
				while ((i < jsonResult.getNames().length()) &&
						(!stop)) {

					if (jsonResult.getNames().getString(i).equals(SESSION_ID)) {
						stop = true;
						mSessionId = jsonResult.getValues().getString(i);
					} else {
						i++;
					}

				}
			} catch (JSONException e) {
				result = false;
				mHasLastError = true;
	    		mLastError = e.getMessage();
			}
			
		} else {
			result = false;
		}
		
		return result;
	}
	
	public void testConnection() {
		HttpClient httpclient = new DefaultHttpClient();
		 
		String url = mServerUrl + String.format(OP_LOGIN, mUserName, mPassword); 
		
		HttpPost httpPost = new HttpPost(url);
 
        // Execute the request
        HttpResponse response;
        
        try {
        	
            response = httpclient.execute(httpPost);
            
            HttpEntity entity = response.getEntity();
            
            if (entity != null) {
            	InputStream instream = entity.getContent();
            	
            	String result = Utils.convertStreamToString(instream);
            	
            	JSONObject json = new JSONObject(result);
            	
            	JSONArray nameArray = json.names();
                JSONArray valArray = json.toJSONArray(nameArray);
            	
            	System.out.println(result);
            }
            
            
            
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


	}
	
	@Override
	public Map<?, ?> getArticle(int articleId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CategoryItem> getCategories() {
		
		List<CategoryItem> finalResult = new ArrayList<CategoryItem>();;
		
		if (mSessionId == null) {
			login();
			
			if (mHasLastError) {
				return null;
			}
		}
		
		String url = mServerUrl + String.format(OP_GET_CATEGORIES, mSessionId);
		
		JSONArray jsonResult = getJSONResponseAsArray(url);
				
		int i = 0;
		JSONObject object;
		
		CategoryItem categoryItem;
		
		String id = null;
		String title = null;
		int unread = 0;
		
		try {
			while (i < jsonResult.length()) {
				object = jsonResult.getJSONObject(i);

				JSONArray names = object.names();
				JSONArray values = object.toJSONArray(names);
				
				int j = 0;
				while (j < names.length()) {
					if (names.getString(j).equals(ID_NAME)) {
						id = values.getString(j);
					} else if (names.getString(j).equals(TITLE_NAME)) {
						title = values.getString(j);
					} else  if (names.getString(j).equals(UNREAD_NAME)) {
						unread = values.getInt(j);
					}
					
					j++;
				}
				categoryItem = new CategoryItem(id,
						title,
						unread);
				
				finalResult.add(categoryItem);
				
				i++;
			}
		} catch (JSONException e) {
			mHasLastError = true;
			mLastError = e.getMessage();
		}
		
		return finalResult;
	}

	@Override
	public List<ArticleItem> getFeedHeadlines(int feedId, int limit, int filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLastError() {
		return mLastError;
	}

	@Override
	public Map<String, List<FeedItem>> getSubsribedFeeds() {
		
		Map<String, List<FeedItem>> finalResult = new HashMap<String, List<FeedItem>>();;
		
		if (mSessionId == null) {
			login();
			
			if (mHasLastError) {
				return null;
			}
		}
		
		String url = mServerUrl + String.format(OP_GET_FEEDS, mSessionId);
		
		JSONArray jsonResult = getJSONResponseAsArray(url);
				
		int i = 0;
		JSONObject object;
		
		FeedItem feedItem;
		List<FeedItem> feedItemList;
		
		String categoryId = null;		
		String id = null;
		String title = null;
		String feedUrl = null;
		int unread = 0;
		
		try {
			while (i < jsonResult.length()) {
				object = jsonResult.getJSONObject(i);
				
				JSONArray names = object.names();
				JSONArray values = object.toJSONArray(names);
				
				int j = 0;
				while (j < names.length()) {
					if (names.getString(j).equals("cat_id")) {
						categoryId = values.getString(j);
					} else if (names.getString(j).equals(ID_NAME)) {
						id = values.getString(j);
					} else  if (names.getString(j).equals(TITLE_NAME)) {
						title = values.getString(j);
					} else  if (names.getString(j).equals("feed_url")) {
						feedUrl = values.getString(j);
					}else  if (names.getString(j).equals(UNREAD_NAME)) {
						unread = values.getInt(j);
					}
					
					j++;
				}
				
				feedItem = new FeedItem(categoryId,
						id,
						title,
						feedUrl,
						unread);
				
				feedItemList = finalResult.get(categoryId);
				if (feedItemList == null) {
					feedItemList = new ArrayList<FeedItem>();
					finalResult.put(categoryId, feedItemList);
				}
				
				feedItemList.add(feedItem);
				
				i++;
			}
		} catch (JSONException e) {
			mHasLastError = true;
			mLastError = e.getMessage();
		}
		
		return finalResult;
	}

	@Override
	public int getTotalUnread() {
		if (mSessionId == null) {
			login();
			
			if (mHasLastError) {
				return -1;
			}
		}
		
		String url = mServerUrl + String.format(OP_GET_UNREAD, mSessionId);
		
		TTRSSJsonResult jsonResult = getJSONResponse(url);
		
		if (mHasLastError) {
			return -1;
		}
		
		int result = -1;
		int i = 0;
		boolean stop = false;
		
		try {
			while ((i < jsonResult.getNames().length()) &&
					(!stop)) {
				if (jsonResult.getNames().getString(i).equals(UNREAD_NAME)) {

					stop = true;
					result = jsonResult.getValues().getInt(i);

				} else {
					i++;
				}
			}
		} catch (JSONException e) {
			mHasLastError = true;
    		mLastError = e.getMessage();
		}
		
		return result;
	}

	@Override
	public List<CategoryItem> getVirtualFeeds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasLastError() {		
		return mHasLastError;
	}

}
