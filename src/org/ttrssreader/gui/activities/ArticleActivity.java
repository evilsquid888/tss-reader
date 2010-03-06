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
import org.ttrssreader.model.article.ArticleItem;
import org.ttrssreader.model.article.ArticleItemAdapter;
import org.ttrssreader.model.article.ArticleReadStateUpdater;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class ArticleActivity extends Activity implements IRefreshEndListener, IUpdateEndListener {
	
	public static final String ARTICLE_ID = "ARTICLE_ID";
	public static final String FEED_ID = "FEED_ID";
	
	private static final int MENU_MARK_READ = Menu.FIRST;
	private static final int MENU_MARK_UNREAD = Menu.FIRST + 1;
	private static final int MENU_OPEN_LINK = Menu.FIRST + 2;
	private static final int MENU_OPEN_COMMENT_LINK = Menu.FIRST + 3;	
	
	private String mArticleId;
	private String mFeedId;
	
	private ArticleItem mArticleItem = null;
	
	private ArticleItemAdapter mAdapter = null;
	
	private WebView webview;	
	private ProgressDialog mProgressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.articletem);
		
		Controller.getInstance().initializeXmlRpcController(this);
		
		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);				
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mArticleId = extras.getString(ARTICLE_ID);
			mFeedId = extras.getString(FEED_ID);
		} else if (savedInstanceState != null) {
			mArticleId = savedInstanceState.getString(ARTICLE_ID);
			mFeedId = savedInstanceState.getString(FEED_ID);
		} else {
			mArticleId = "-1";
			mFeedId = "-1";
		}		
	}
	
	@Override
	protected void onResume() {		
		doRefresh();
		super.onResume();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item;
    	
    	item = menu.add(0, MENU_MARK_READ, 0, R.string.ArticleActivity_MarkRead);
    	
    	item = menu.add(0, MENU_MARK_UNREAD, 0, R.string.ArticleActivity_MarkUnread);
    	
    	item = menu.add(0, MENU_OPEN_LINK, 0, R.string.ArticleActivity_OpenLink);
        item.setIcon(R.drawable.link32);
    	
        item = menu.add(0, MENU_OPEN_COMMENT_LINK, 0, R.string.ArticleActivity_OpenCommentLink);
        item.setIcon(R.drawable.commentlink32);                
    	
    	return true;
    }
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case MENU_MARK_READ:
			changeUnreadState(0);
			return true;
		case MENU_MARK_UNREAD:
			changeUnreadState(1);
			return true;
		case MENU_OPEN_LINK:
			openLink();
			return true;
		case MENU_OPEN_COMMENT_LINK:
			openCommentLink();
			return true;    	
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void doRefresh() {		
		
		Controller.getInstance().setRefreshNeeded(false);
		
		mProgressDialog = ProgressDialog.show(this, "Refreshing", this.getResources().getString(R.string.Commons_PleaseWait));

		mAdapter = new ArticleItemAdapter(mFeedId, mArticleId);
		new Refresher(this, mAdapter);
	}
	
	private void changeUnreadState(int articleState) {
		
		mProgressDialog = ProgressDialog.show(this,
				this.getResources().getString(R.string.Commons_UpdateReadState),
				this.getResources().getString(R.string.Commons_PleaseWait));
		
		new Updater(this, new ArticleReadStateUpdater(mFeedId, mArticleId, articleState));
	}
	
	private void openUrl(String url) {		
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}
	
	private void openLink() {
		if (mArticleItem != null) {
			String url = mArticleItem.getArticleUrl();
			if ((url != null) &&
					(url.length() > 0)) {
				openUrl(url);
			}
		}
	}
	
	private void openCommentLink() {
		if (mArticleItem != null) {
			String url = mArticleItem.getArticleCommentUrl();
			if ((url != null) &&
					(url.length() > 0)) {
				openUrl(url);
			}
		}
	}
	
	private void openConnectionErrorDialog(String errorMessage) {
		Intent i = new Intent(this, ConnectionErrorActivity.class);
		i.putExtra(ConnectionErrorActivity.ERROR_MESSAGE, errorMessage);
		startActivity(i);
	}	

	@Override
	public void onRefreshEnd() {
		if (!Controller.getInstance().getTTRSSConnector().hasLastError()) {
			mArticleItem = mAdapter.getArticle();
			
			if (mArticleItem != null) {
				// Use if loadDataWithBaseURL, 'cause loadData is buggy (encoding error & don't support "%" in html).				
				webview.loadDataWithBaseURL (null, mArticleItem.getContent(), "text/html", "utf-8", "about:blank"); 														
				
				if (mArticleItem.getTitle() != null) {
					this.setTitle(this.getResources().getString(R.string.ApplicationName) + " - " + mArticleItem.getTitle());
				} else {
					this.setTitle(this.getResources().getString(R.string.ApplicationName));
				}
			}
		} else {
			openConnectionErrorDialog(Controller.getInstance().getTTRSSConnector().getLastError());
		}
		
		mProgressDialog.dismiss();
	}

	@Override
	public void onUpdateEnd() {
		if (Controller.getInstance().getTTRSSConnector().hasLastError()) {
			openConnectionErrorDialog(Controller.getInstance().getTTRSSConnector().getLastError());
		}
		
		mProgressDialog.dismiss();
	}

}
