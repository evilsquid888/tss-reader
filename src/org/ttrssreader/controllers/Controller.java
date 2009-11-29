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

import org.ttrssreader.preferences.PreferencesConstants;
import org.ttrssreader.xmlrpc.TtrssXmlRpcConnector;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class Controller {
	
	private final static String XML_RPC_END_URL = "xml-rpc.php";
	
	private boolean mIsXmlRpcClientLibInitialized = false;
	private TtrssXmlRpcConnector mXmlRpcConnector;
	
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
		
		if (!url.endsWith(XML_RPC_END_URL)) {
			if (!url.endsWith("/")) {
				url += "/";
			}
			url += XML_RPC_END_URL;
		}
		
		String userName = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesConstants.CONNECTION_USERNAME, "admin");
		String password = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesConstants.CONNECTION_PASSWORD, "password");
		
		mXmlRpcConnector = new TtrssXmlRpcConnector(url, userName, password);
		mRefreshNeeded = true;
	}
	
	public void initializeXmlRpcController(final Context context) {
		if (!mIsXmlRpcClientLibInitialized) {		
			PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
				@Override
				public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
					if ((key.equals(PreferencesConstants.CONNECTION_URL)) ||
							(key.equals(PreferencesConstants.CONNECTION_USERNAME)) ||
							(key.equals(PreferencesConstants.CONNECTION_PASSWORD))) {
						initializeXmlRpcConnector(context);
					}
				}			
			});

			initializeXmlRpcConnector(context);

			mIsXmlRpcClientLibInitialized = true;
		}
	}
	
	public TtrssXmlRpcConnector getXmlRpcConnector() {
		return mXmlRpcConnector;		
	}

	public boolean isRefreshNeeded() {
		return mRefreshNeeded;
	}

	public void setRefreshNeeded(boolean mRefreshNeeded) {
		this.mRefreshNeeded = mRefreshNeeded;
	}

}
