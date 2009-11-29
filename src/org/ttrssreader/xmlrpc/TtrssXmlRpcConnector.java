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

package org.ttrssreader.xmlrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.external.xmlrpc.android.XMLRPCClient;
import org.external.xmlrpc.android.XMLRPCException;

import android.util.Log;

public class TtrssXmlRpcConnector {

	private static final String METHOD_GET_VERSION = "rss.getVersion";
	private static final String METHOD_GET_TOTAL_UNREAD = "rss.getTotalUnread";
	private static final String METHOD_GET_SUBSRIBES_FEEDS = "rss.getSubscribedFeeds";
	private static final String METHOD_GET_ARTICLE = "rss.getArticle";
	private static final String METHOD_GET_VIRTUAL_FEEDS = "rss.getVirtualFeeds";
	private static final String METHOD_GET_CATEGORIES = "rss.getCategories";
	private static final String METHOD_GET_FEEDS_HEADLINES = "rss.getFeedHeadlines";
	private static final String METHOD_SET_ARTICLE_READ = "rss.setArticleRead";
	
	private String mServerUrl;
	private String mUserName;
	private String mPassword;
	
	private String mLastError = "";
	private boolean mHasLastError = false;;
	
	//private Map<String, String> mFeedCategoryMap = null;
	
	private XMLRPCClient mClient;
	
	public TtrssXmlRpcConnector(String serverUrl, String userName, String password) {
		mServerUrl = serverUrl;
		mUserName = userName;
		mPassword = password;
		//mFeedCategoryMap = null;
		internalBuildClient();
	}
	
	public TtrssXmlRpcConnector(String serverUrl) {
		this(serverUrl, "", "");
	}
	
	private void internalBuildClient() {
			mClient = new XMLRPCClient(mServerUrl);
	}
	
	public boolean hasLastError() {
		return mHasLastError;
	}
	
	public String getLastError() {
		return mLastError;
	}
	
	/*
	public Map<String, String> getFeedCategoryMap() {
		return mFeedCategoryMap;
	}
	*/
	
	@SuppressWarnings("unchecked")
	private List getNewParamListWithAuthentication() {
		List params = new ArrayList();
		params.add(mUserName);
		params.add(mPassword);
		
		return params;
	}
	
	private List<Map<?, ?>> convertResultArray(Object input) {
		if (input != null) {
			
			Object[] objectResult = (Object[]) input;
			
			List<Map<?, ?>> resultList = new ArrayList<Map<?, ?>>();
			
			for (int i = 0; i < objectResult.length; i++) {			
				resultList.add((HashMap<?, ?>) objectResult[i]);
			}
			
			return resultList;
		} else {
			return null;
		}
	}
	
	private Object[] convertListToArray(List<?> params) {
		Object result[] = new Object[params.size()];
		
		int counter = 0;
		Iterator<?> iter = params.iterator();
		while (iter.hasNext()) {
			result[counter] = iter.next();
			counter++;
		}
		
		return result;
	}
	
	private Object internalExecute(String methodName, List<?> params) {
		
		mHasLastError = false;
		mLastError = "";
		
		try {
			
			Object result = mClient.call(methodName, convertListToArray(params));
			
			if (result.toString().equals("Login failed.")) {
				mHasLastError = true;
				mLastError = result.toString();
			}
			
			return result;

		} catch (XMLRPCException e) {
			mHasLastError = true;
			mLastError = e.getMessage();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public String getVersion() {
		Object result;
		
		result = internalExecute(METHOD_GET_VERSION, new ArrayList());
		
		if (result != null) {
			return result.toString();
		} else {
			return "";
		}
	}
		
	@SuppressWarnings("unchecked")
	public int getTotalUnread() {
		List params = getNewParamListWithAuthentication();
		
		Object result = internalExecute(METHOD_GET_TOTAL_UNREAD, params);
		
		if (result != null) {
			return new Integer(result.toString()).intValue();
		} else {
			return -1;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<?, ?>> getSubsribedFeeds() {
		List params = getNewParamListWithAuthentication();
		
		Object result = internalExecute(METHOD_GET_SUBSRIBES_FEEDS, params);

		List<Map<?, ?>> convertedResult;
		if (!mHasLastError) {		
			convertedResult = convertResultArray(result);
		} else {
			convertedResult = null;
		}
		
		return convertedResult;
	}
	
	@SuppressWarnings("unchecked")
	public Map<?, ?> getArticle(int articleId) {
		List params = getNewParamListWithAuthentication();
		params.add(new Integer(articleId));
		
		Object result = internalExecute(METHOD_GET_ARTICLE, params);
		
		if (result != null) {
			return (Map<?, ?>) result;
		} else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<?, ?>> getVirtualFeeds() {
		List params = getNewParamListWithAuthentication();
		
		Object result = internalExecute(METHOD_GET_VIRTUAL_FEEDS, params);
		
		return convertResultArray(result);
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<?, ?>> getCategories() {
		List params = getNewParamListWithAuthentication();
		
		Object result = internalExecute(METHOD_GET_CATEGORIES, params);
		
		return convertResultArray(result);		
	}
	
	/**
	 * 
	 * @param feedId
	 * @param limit
	 * @param filter 0: No filtering; 1: Only unread; 2: Only starred; 3: Adaptive; 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<?, ?> getFeedHeadlines(int feedId, int limit, int filter) {
		List params = getNewParamListWithAuthentication();
		params.add(new Integer(feedId));
		params.add(new Integer(limit));
		params.add(new Integer(filter));
		
		Object result = internalExecute(METHOD_GET_FEEDS_HEADLINES, params);
		
		return (Map<?, ?>) result;
	}
	
	@SuppressWarnings("unchecked")
	public Object setArticleRead(String articleId, int articleState) {
		List params = getNewParamListWithAuthentication();
		params.add(new Integer(articleId));
		params.add(articleState);
		
		Object result = internalExecute(METHOD_SET_ARTICLE_READ, params);
		
		return result;
	}
	
	public void selfTest() {
		
		int ARTICLE_TEST_ID = 42;
		int FEED_TEST_ID = 46;
		int FEED_TEST_LIMIT = 100;
		int FEED_TEST_FILTER = 0;
		
		Log.d("TTRssXmlRpcLib.selfTest()", "selfTest() started.");
		Log.d("TTRssXmlRpcLib.selfTest()", "Server URL: " + mServerUrl);
		Log.d("TTRssXmlRpcLib.selfTest()", "User name: " + mUserName);
		Log.d("TTRssXmlRpcLib.selfTest()", "Password: [Not shown]");
		
		Log.d("TTRssXmlRpcLib.selfTest()", "getVersion: " + this.getVersion());
		Log.d("TTRssXmlRpcLib.selfTest()", "getTotalUnread: " + this.getTotalUnread());
		
		List<Map<?, ?>> result = this.getSubsribedFeeds();
		if (result != null) {
			Log.d("TTRssXmlRpcLib.selfTest()", "getSubsribedFeeds: ");
			
			Iterator<Map<?, ?>> iter = result.iterator();
			while (iter.hasNext()) {
				Log.d("TTRssXmlRpcLib.selfTest()", iter.next().toString());
			}
		}
		
		
		Log.d("TTRssXmlRpcLib.selfTest()", "getArticle (id: " + ARTICLE_TEST_ID + "): ");
		Map<?, ?> articleResult =  this.getArticle(ARTICLE_TEST_ID);
		if (articleResult != null) {
			Log.d("TTRssXmlRpcLib.selfTest()", articleResult.toString());
		} else {
			Log.d("TTRssXmlRpcLib.selfTest()", "Not found, maybe try another id?");
		}
		
		List<Map<?, ?>> virtualFeedsResult = this.getVirtualFeeds();
		if (result != null) {
			Log.d("TTRssXmlRpcLib.selfTest()", "getVirtualFeeds: ");
			
			Iterator<Map<?, ?>> iter = virtualFeedsResult.iterator();
			while (iter.hasNext()) {
				Log.d("TTRssXmlRpcLib.selfTest()", iter.next().toString());
			}
		}
		
		List<Map<?, ?>> categoriesResult = this.getCategories();
		if (result != null) {
			Log.d("TTRssXmlRpcLib.selfTest()", "getCategories: ");
			
			Iterator<Map<?, ?>> iter = categoriesResult.iterator();
			while (iter.hasNext()) {
				Log.d("TTRssXmlRpcLib.selfTest()", iter.next().toString());
			}
		}
		
		Log.d("TTRssXmlRpcLib.selfTest()", "getFeedHeadlines (id: " + FEED_TEST_ID + ", limit: " + FEED_TEST_LIMIT + ", filter: " + FEED_TEST_FILTER + "): ");
		Map<?, ?> feedHeadlinesResult = this.getFeedHeadlines(FEED_TEST_ID, FEED_TEST_LIMIT, FEED_TEST_FILTER);
		Log.d("TTRssXmlRpcLib.selfTest()", "Feed title: " + feedHeadlinesResult.get("title"));
		Log.d("TTRssXmlRpcLib.selfTest()", "Feed Content: ");
		Object[] headlines = (Object[]) feedHeadlinesResult.get("headlines");
		for (int i = 0; i < headlines.length; i++) {
			Log.d("TTRssXmlRpcLib.selfTest()", headlines[i].toString());
		}
	}
	
}
