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

package org.ttrssreader.model.article;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ttrssreader.controllers.Controller;
import org.ttrssreader.controllers.DataController;
import org.ttrssreader.model.IUpdatable;
import org.ttrssreader.model.feed.FeedItem;

public class ArticleReadStateUpdater implements IUpdatable {
	
	private List<String> mArticleIdList;
	private String mFeedId;
	private int mArticleState;
	
	public ArticleReadStateUpdater(String feedId, List<String> articleIdList, int articleState) {
		mFeedId = feedId;
		mArticleIdList = articleIdList;
		mArticleState = articleState;
	}
	
	public ArticleReadStateUpdater(String feedId, String articleId, int articleState) {
		mFeedId = feedId;
		mArticleIdList = new ArrayList<String>();
		mArticleIdList.add(articleId);
		mArticleState = articleState;
	}

	@Override
	public void update() {
		String articleId;				
		Iterator<String> iter = mArticleIdList.iterator();
		while (iter.hasNext()) {
			articleId = iter.next();
			
			// TODO: implement set article read
			//Controller.getInstance().getTTRSSConnector().setArticleRead(articleId, mArticleState);
			
			//DataController.getInstance().getSingleArticleForFeedsHeadlines(mFeedId, articleId).setUnread(mArticleState == 0 ? true : false);			
		}
		
		int deltaUnread = mArticleState == 0 ? mArticleIdList.size() : - mArticleIdList.size();
		
		FeedItem feed = DataController.getInstance().getFeed(mFeedId);
		feed.setDeltaUnreadCount(deltaUnread);
		
		DataController.getInstance().getCategory(feed.getCategoryId()).setDeltaUnreadCount(deltaUnread);
		DataController.getInstance().getVirtualCategory("-4").setDeltaUnreadCount(deltaUnread);
		
		Controller.getInstance().setRefreshNeeded(true);
	}

}
