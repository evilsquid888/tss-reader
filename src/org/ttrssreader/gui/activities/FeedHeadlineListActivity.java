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

package org.ttrssreader.gui.activities;

import org.ttrssreader.R;
import org.ttrssreader.controllers.Controller;
import org.ttrssreader.gui.IRefreshEndListener;
import org.ttrssreader.gui.IUpdateEndListener;
import org.ttrssreader.model.Refresher;
import org.ttrssreader.model.Updater;
import org.ttrssreader.model.article.ArticleReadStateUpdater;
import org.ttrssreader.model.feedheadline.FeedHeadlineListAdapter;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class FeedHeadlineListActivity extends ListActivity implements IRefreshEndListener, IUpdateEndListener {
	
	private static final int ACTIVITY_SHOW_FEED_ITEM = 0;
	
	private static final int MENU_REFRESH = Menu.FIRST;
	private static final int MENU_MARK_ALL_READ = Menu.FIRST + 1;
	private static final int MENU_MARK_ALL_UNREAD = Menu.FIRST + 2;
	
	public static final String FEED_ID = "FEED_ID";
	public static final String FEED_TITLE = "FEED_TITLE";
	
	private String mFeedId;
	private String mFeedTitle;
	
	private ListView mFeedHeadlineListView;
	private FeedHeadlineListAdapter mAdapter = null;
	
	private ProgressDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedlist);
		
		Controller.getInstance().initializeXmlRpcController(this);

		mFeedHeadlineListView = getListView();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mFeedId = extras.getString(FEED_ID);
			mFeedTitle = extras.getString(FEED_TITLE);
		} else if (savedInstanceState != null) {
			mFeedId = savedInstanceState.getString(FEED_ID);
			mFeedTitle = savedInstanceState.getString(FEED_TITLE);
		} else {
			mFeedId = "-1";
			mFeedTitle = null;
		}		
	}
	
	@Override
	protected void onResume() {		
		doRefresh();
		super.onResume();
	}

	private void doRefresh() {		 
		
		Controller.getInstance().setRefreshNeeded(false);
		
		mProgressDialog = ProgressDialog.show(this, "Refreshing", this.getResources().getString(R.string.Commons_PleaseWait));

		if (mAdapter == null) {
			mAdapter = new FeedHeadlineListAdapter(this, mFeedId);	
			mFeedHeadlineListView.setAdapter(mAdapter);
		}
		new Refresher(this, mAdapter);		 
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Intent i = new Intent(this, ArticleActivity.class);
		i.putExtra(ArticleActivity.ARTICLE_ID, mAdapter.getFeedItemId(position));
		i.putExtra(ArticleActivity.FEED_ID, mFeedId);

		startActivityForResult(i, ACTIVITY_SHOW_FEED_ITEM);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item;
    	
    	item = menu.add(0, MENU_REFRESH, 0, R.string.Main_RefreshMenu);
        item.setIcon(R.drawable.refresh32);    
        
        item = menu.add(0, MENU_MARK_ALL_READ, 0, R.string.FeedHeadlinesListActivity_MarkAllRead);
        
        item = menu.add(0, MENU_MARK_ALL_UNREAD, 0, R.string.FeedHeadlinesListActivity_MarkAllUnread);
        
    	return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch(item.getItemId()) {
    	case MENU_REFRESH:
    		doRefresh();
            return true;  	
    	case MENU_MARK_ALL_READ:
    		setReadState();
            return true;
    	case MENU_MARK_ALL_UNREAD:
    		setUnreadState();
            return true;
    	}
    	
    	return super.onMenuItemSelected(featureId, item);
    }
	
	private void setReadState() {
		
		mProgressDialog = ProgressDialog.show(this,
				this.getResources().getString(R.string.Commons_UpdateReadState),
				this.getResources().getString(R.string.Commons_PleaseWait));
		
		new Updater(this, new ArticleReadStateUpdater(mAdapter.getUnreadIdList(), 1));
	}
	
	private void setUnreadState() {
		
		mProgressDialog = ProgressDialog.show(this,
				this.getResources().getString(R.string.Commons_UpdateReadState),
				this.getResources().getString(R.string.Commons_PleaseWait));
		
		new Updater(this, new ArticleReadStateUpdater(mAdapter.getReadIdList(), 0));
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

			if (mFeedTitle != null) {
				this.setTitle(this.getResources().getString(R.string.ApplicationName) + " - " + mFeedTitle + " (" + mAdapter.getUnreadCount() + ")");
			} else {
				this.setTitle(this.getResources().getString(R.string.ApplicationName) + " (" + mAdapter.getUnreadCount() + ")");
			}
		} else {
			openConnectionErrorDialog(Controller.getInstance().getXmlRpcConnector().getLastError());
		}
		
		mProgressDialog.dismiss();		
	}

	@Override
	public void onUpdateEnd() {
		mProgressDialog.dismiss();
		doRefresh();
	}	

}
