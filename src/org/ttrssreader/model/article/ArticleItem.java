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

import java.util.Date;
import java.util.Map;

import org.ttrssreader.controllers.Controller;
import org.ttrssreader.model.IRefreshable;

public class ArticleItem implements IRefreshable {

	private String mId;
	private String mTitle;
	private String mFeedId;
	private boolean mIsUnread;
	private String mContent;
	private String mArticleUrl;
	private String mArticleCommentUrl;
	private Date mUpdateDate;
	private boolean mIsContentLoaded;		

	public ArticleItem(String feedId, String id) {
		this(feedId, id, null, false, null);
	}
	
	public ArticleItem(String feedId, String id, String title, boolean isUnread, Date updateDate) {
		mId = id;
		mTitle = title;
		mFeedId = feedId;
		mIsUnread = isUnread;
		mUpdateDate = updateDate;
		mContent = "";
		mArticleUrl = null;
		mArticleCommentUrl = null;
		mIsContentLoaded = false;
	}
	
	public String getId() {
		return mId;
	}
	
	public String getFeedId() {
		return mFeedId;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public boolean isUnread() {
		return mIsUnread;
	}
	
	public String getContent() {
		return mContent;
	}
	
	public String getArticleUrl() {
		return mArticleUrl;
	}
	
	public String getArticleCommentUrl() {
		return mArticleCommentUrl;
	}
	
	public void setContent(String value) {
		mContent = value;
	}
	
	public Date getUpdateDate() {
		return mUpdateDate;
	}
	
	public boolean isContentLoaded() {
		return mIsContentLoaded;
	}

	@Override
	public void refreshData() {
		Map<?, ?> result = Controller.getInstance().getXmlRpcConnector().getArticle(new Integer(mId).intValue());
		if (result != null) {
			mContent = result.get("content").toString();
			
			if ((mTitle == null) ||
					(mTitle.length() == 0)) {
				mTitle = result.get("title").toString();
			}
			
			mArticleUrl = result.get("link").toString();
			mArticleCommentUrl = result.get("comments").toString();
			
			if (mUpdateDate == null) {
				String updatedDate = result.get("updated").toString();
				if ((updatedDate != null) &&
						(updatedDate.length() > 0)) {
					// PHP strtotime gives timestamp in seconds.
					mUpdateDate = new Date(new Long(result.get("updated").toString() + "000").longValue());
				} else {
					mUpdateDate = null;
				}
			}
		}		
		mIsContentLoaded = true;
	}	
	
}
