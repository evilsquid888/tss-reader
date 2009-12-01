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

package org.ttrssreader;

import org.ttrssreader.controllers.Controller;
import org.ttrssreader.controllers.DataController;
import org.ttrssreader.gui.IRefreshEndListener;
import org.ttrssreader.gui.activities.AboutActivity;
import org.ttrssreader.gui.activities.ConnectionErrorActivity;
import org.ttrssreader.gui.activities.FeedHeadlineListActivity;
import org.ttrssreader.gui.activities.FeedListActivity;
import org.ttrssreader.gui.activities.PreferencesActivity;
import org.ttrssreader.model.Refresher;
import org.ttrssreader.model.category.CategoryListAdapter;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class TtrssReaderMainActivity extends ListActivity implements IRefreshEndListener {
	
	private static final int ACTIVITY_SHOW_FEEDS = 0;
	
	private static final int MENU_REFRESH = Menu.FIRST;
	private static final int MENU_SHOW_PREFERENCES = Menu.FIRST + 1;
	private static final int MENU_SHOW_ABOUT = Menu.FIRST + 2;
	
	private ListView mCategoryListView;
	private CategoryListAdapter mAdapter = null;
	
	private ProgressDialog mProgressDialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Controller.getInstance().initializeXmlRpcController(this);
        
        mCategoryListView = getListView();                                                              
    }
        
    @Override
	protected void onResume() {    	
		doRefresh();
		super.onResume();
	}  
    
    private void doRefresh() {
    	
    	Controller.getInstance().setRefreshNeeded(false);
    	
    	mProgressDialog = ProgressDialog.show(this, "Refreshing", this.getResources().getString(R.string.Commons_PleaseWait));
    	
    	int totalUnread = Controller.getInstance().getXmlRpcConnector().getTotalUnread();
    	
    	if (totalUnread > 0) {
    		this.setTitle(this.getResources().getString(R.string.ApplicationName) + " (" + totalUnread + ")");
    	} else { 
    		this.setTitle(this.getResources().getString(R.string.ApplicationName));
    	}
    	
    	if (mAdapter == null) {
    		mAdapter = new CategoryListAdapter(this);
    		mCategoryListView.setAdapter(mAdapter);
    	}
    	new Refresher(this, mAdapter);    	
    }    
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        String categoryId = mAdapter.getCategoryId(position); 
        Intent i;
        
        if ((categoryId.equals("-1")) ||
        		(categoryId.equals("-2")) ||
        		(categoryId.equals("-3")) ||
        		(categoryId.equals("-4"))) {
        	// Virtual feeds
        	i = new Intent(this, FeedHeadlineListActivity.class);
        	i.putExtra(FeedHeadlineListActivity.FEED_ID, categoryId);
        	i.putExtra(FeedHeadlineListActivity.FEED_TITLE, mAdapter.getCategoryTitle(position));
        } else {
        	// Categories
        	i = new Intent(this, FeedListActivity.class);
        	i.putExtra(FeedListActivity.CATEGORY_ID, categoryId);
        	i.putExtra(FeedListActivity.CATEGORY_TITLE, mAdapter.getCategoryTitle(position));        	
        }                
    	
    	startActivityForResult(i, ACTIVITY_SHOW_FEEDS);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item;
    	
    	item = menu.add(0, MENU_REFRESH, 0, R.string.Main_RefreshMenu);
        item.setIcon(R.drawable.refresh32);
    	
        item = menu.add(0, MENU_SHOW_PREFERENCES, 0, R.string.Main_ShowPreferencesMenu);
        item.setIcon(R.drawable.preferences32);                
        
        item = menu.add(0, MENU_SHOW_ABOUT, 0, R.string.Main_ShowAboutMenu);
        item.setIcon(R.drawable.about32);
    	
    	return true;
    }        
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch(item.getItemId()) {
    	case MENU_REFRESH:
    		doForceRefresh();
            return true;
    	case MENU_SHOW_PREFERENCES:
    		openPreferences();
            return true;
    	case MENU_SHOW_ABOUT:
    		openAboutDialog();
            return true;
    	}
    	
    	return super.onMenuItemSelected(featureId, item);
    }
    
    private void doForceRefresh() {
		DataController.getInstance().forceFullRefresh();
		doRefresh();
	}
    
    private void openPreferences() {
		Intent preferencesActivity = new Intent(this, PreferencesActivity.class);
  		startActivity(preferencesActivity);
	}
    
    private void openAboutDialog() {
		Intent i = new Intent(this, AboutActivity.class);
		startActivity(i);
	}
    
    private void openConnectionErrorDialog(String errorMessage) {
		Intent i = new Intent(this, ConnectionErrorActivity.class);
		i.putExtra(ConnectionErrorActivity.ERROR_MESSAGE, errorMessage);
		startActivity(i);
	}

	@Override
	public void onRefreshEnd() {
		if (!Controller.getInstance().getXmlRpcConnector().hasLastError()) {			
			mAdapter.notifyDataSetChanged();
		} else {
			openConnectionErrorDialog(Controller.getInstance().getXmlRpcConnector().getLastError());
		}
    	mProgressDialog.dismiss();		
	}	 
    
}