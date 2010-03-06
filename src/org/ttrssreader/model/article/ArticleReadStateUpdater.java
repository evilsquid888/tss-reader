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

public class ArticleReadStateUpdater implements IUpdatable {
	
	private List<ArticleItem> mArticleList;
	private String mFeedId;
	private int mArticleState;
	
	public ArticleReadStateUpdater(String feedId, List<ArticleItem> articleList, int articleState) {
		mFeedId = feedId;
		mArticleList = articleList;
		mArticleState = articleState;
	}
	
	public ArticleReadStateUpdater(String feedId, ArticleItem article, int articleState) {
		mFeedId = feedId;
		mArticleList = new ArrayList<ArticleItem>();
		mArticleList.add(article);
		mArticleState = articleState;
	}

	@Override
	public void update() {
		ArticleItem article;				
		Iterator<ArticleItem> iter = mArticleList.iterator();
		
		String idList = "";
		
		while (iter.hasNext()) {
			article = iter.next();
			
			// Build a list of articles id to update.
			if (idList.length() > 0) {
				idList += ",";
			}
			
			idList += article.getId();
			
			DataController.getInstance().getSingleArticleForFeedsHeadlines(article.getFeedId(), article.getId()).setUnread(mArticleState == 1 ? true : false);
			DataController.getInstance().getFeed(article.getFeedId()).setDeltaUnreadCount(mArticleState == 1 ? 1 : -1);
			DataController.getInstance().getCategory(DataController.getInstance().getFeed(article.getFeedId()).getCategoryId()).setDeltaUnreadCount(mArticleState == 1 ? 1 : -1);
			
			// If on a virtual feeds, also update article state in it.
			if ((mFeedId.equals("-1")) ||
					(mFeedId.equals("-2")) ||
					(mFeedId.equals("-3")) ||
					(mFeedId.equals("-4"))) {
				DataController.getInstance().getSingleArticleForFeedsHeadlines(mFeedId, article.getId()).setUnread(mArticleState == 1 ? true : false);
				DataController.getInstance().getVirtualCategory(mFeedId).setDeltaUnreadCount(mArticleState == 1 ? 1 : -1);
			}
		}
		
		Controller.getInstance().getTTRSSConnector().setArticleRead(idList, mArticleState);
				
		int deltaUnread = mArticleState == 1 ? mArticleList.size() : - mArticleList.size();
		DataController.getInstance().getVirtualCategory("-4").setDeltaUnreadCount(deltaUnread);
		
		Controller.getInstance().setRefreshNeeded(true);
	}

}
