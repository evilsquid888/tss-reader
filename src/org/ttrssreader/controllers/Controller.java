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

import android.content.Context;
import android.preference.PreferenceManager;

public class Controller {
	
	private final static String JSON_END_URL = "api/";
	
	private boolean mIsControllerInitialized = false;
	private ITTRSSConnector mTTRSSConnector;
	
	private boolean mRefreshNeeded = false;
	
	private boolean mAlwaysFullRefresh = false;
	
	private static Controller mInstance = null;
	
	private Controller() {}
	
	public static Controller getInstance() {
		if (mInstance == null) {
			mInstance = new Controller();
		}
		return mInstance;
	}
	
	public void initializeController(Context context) {
		String url = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesConstants.CONNECTION_URL, "http://localhost/");
		
		if (!url.endsWith(JSON_END_URL)) {
			if (!url.endsWith("/")) {
				url += "/";
			}
			url += JSON_END_URL;
		}
		
		String userName = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesConstants.CONNECTION_USERNAME, "admin");
		String password = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesConstants.CONNECTION_PASSWORD, "password");
		
		boolean showUnreadInVirtualFeeds = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesConstants.MISC_SHOW_VIRTUAL_UNREAD, false);
		
		mAlwaysFullRefresh = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesConstants.MISC_ALWAYS_FULL_REFRESH, false);
		
		mTTRSSConnector = new TTRSSJsonConnector(url, userName, password, showUnreadInVirtualFeeds);
		
		mRefreshNeeded = true;
	}
	
	public void checkAndInitializeController(final Context context) {
		if (!mIsControllerInitialized) {					

			initializeController(context);

			mIsControllerInitialized = true;
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
	
	public boolean isAlwaysPerformFullRefresh() {
		return mAlwaysFullRefresh;
	}

}
