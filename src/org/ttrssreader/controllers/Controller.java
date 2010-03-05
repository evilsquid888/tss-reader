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

package org.ttrssreader.controllers;

import org.ttrssreader.net.ITTRSSConnector;
import org.ttrssreader.net.TTRSSJsonConnector;
import org.ttrssreader.preferences.PreferencesConstants;
import org.ttrssreader.xmlrpc.TtrssXmlRpcConnector;

import android.content.Context;
import android.preference.PreferenceManager;

public class Controller {
	
	private final static String XML_RPC_END_URL = "xml-rpc.php";
	
	private final static String JSON_END_URL = "api/";
	
	private boolean mIsXmlRpcClientLibInitialized = false;
	private ITTRSSConnector mTTRSSConnector;
	
	private boolean mRefreshNeeded = false;
	
	private static Controller mInstance = null;
	
	private Controller() {}
	
	public static Controller getInstance() {
		if (mInstance == null) {
			mInstance = new Controller();
		}
		return mInstance;
	}
	
	public void initializeXmlRpcConnector(Context context) {
		String url = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesConstants.CONNECTION_URL, "http://localhost/");
		
		/*
		if (!url.endsWith(XML_RPC_END_URL)) {
			if (!url.endsWith("/")) {
				url += "/";
			}
			url += XML_RPC_END_URL;
		}
		*/
		if (!url.endsWith(JSON_END_URL)) {
			if (!url.endsWith("/")) {
				url += "/";
			}
			url += JSON_END_URL;
		}
		
		String userName = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesConstants.CONNECTION_USERNAME, "admin");
		String password = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesConstants.CONNECTION_PASSWORD, "password");
		
		//mTTRSSConnector = new TtrssXmlRpcConnector(url, userName, password);
		mTTRSSConnector = new TTRSSJsonConnector(url, userName, password);
		
		//((TTRSSJsonConnector) mTTRSSConnector).testConnection();
		
		mRefreshNeeded = true;
	}
	
	public void initializeXmlRpcController(final Context context) {
		if (!mIsXmlRpcClientLibInitialized) {					

			initializeXmlRpcConnector(context);

			mIsXmlRpcClientLibInitialized = true;
		}
	}
	
	public ITTRSSConnector getTTRSSConnector() {
		return mTTRSSConnector;		
	}

	public boolean isRefreshNeeded() {
		return mRefreshNeeded;
	}

	public void setRefreshNeeded(boolean mRefreshNeeded) {
		this.mRefreshNeeded = mRefreshNeeded;
	}

}
