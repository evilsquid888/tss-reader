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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ttrssreader.model.article.ArticleItem;
import org.ttrssreader.model.category.CategoryItem;
import org.ttrssreader.model.category.CategoryItemComparator;
import org.ttrssreader.model.category.VirtualCategoryItemComparator;
import org.ttrssreader.model.feed.FeedItem;

public class DataController {
	
	private static DataController mInstance = null;
	
	private boolean mForceFullRefresh = false;
	
	private Map<String, List<ArticleItem>> mFeedsHeadlines;	
	private Map<String, List<FeedItem>> mSubscribedFeeds;
	private List<CategoryItem> mVirtualCategories;
	private List<CategoryItem> mCategories;
	
	private DataController() {
		mFeedsHeadlines = new HashMap<String, List<ArticleItem>>();
		mSubscribedFeeds = null;
		mVirtualCategories = null;
		mCategories = null;
	}
	
	public static DataController getInstance() {
		if (mInstance == null) {
			mInstance = new DataController();
		}
		return mInstance;
	}
	
	public void forceFullRefresh() {
		mForceFullRefresh = true;
	}
	
	private boolean needFullRefresh() {
		return mForceFullRefresh || Controller.getInstance().isAlwaysPerformFullRefresh();
	}
	
	private List<CategoryItem> internalGetVirtualCategories() {
		if ((mVirtualCategories == null) ||
				(needFullRefresh())) {
			mVirtualCategories = Controller.getInstance().getTTRSSConnector().getVirtualFeeds();
			mForceFullRefresh = false;
		}
		return mVirtualCategories;
	}
	
	private List<CategoryItem> internalGetCategories() {
		if ((mCategories == null) ||
				(needFullRefresh())) {
			mCategories = Controller.getInstance().getTTRSSConnector().getCategories();
			mForceFullRefresh = false;
		}
		return mCategories;
	}
	
	public CategoryItem getVirtualCategory(String categoryId) {		
		CategoryItem result = null;
		
		CategoryItem item;
		
		Iterator<CategoryItem> iter = internalGetVirtualCategories().iterator();
		while ((iter.hasNext()) && 
				(result == null)) {
			item = iter.next();
			
			if (item.getId().equals(categoryId)) {
				result = item;
			}
		}
		
		return result;
	}
	
	public CategoryItem getCategory(String categoryId) {		
		CategoryItem result = null;
		
		CategoryItem item;
		
		Iterator<CategoryItem> iter = internalGetCategories().iterator();
		while ((iter.hasNext()) && 
				(result == null)) {
			item = iter.next();
			
			if (item.getId().equals(categoryId)) {
				result = item;
			}
		}
		
		return result;
	}
	
	public List<CategoryItem> getCategories(boolean withVirtuals) {
		boolean tmpForceFullRefresh = mForceFullRefresh;
		
		List<CategoryItem> finalList = new ArrayList<CategoryItem>();
		
		if (withVirtuals) {
			List<CategoryItem> virtualList = internalGetVirtualCategories();

			if (virtualList != null) {
				Iterator<CategoryItem> iter = virtualList.iterator();
				while (iter.hasNext()) {
					finalList.add(iter.next());
				}		
				Collections.sort(finalList, new VirtualCategoryItemComparator());
			}
		}
		
		mForceFullRefresh = tmpForceFullRefresh;
		
		List<CategoryItem> tempList = new ArrayList<CategoryItem>();
		
		List<CategoryItem> categoryList = internalGetCategories();
		
		if (categoryList != null) {
			Iterator<CategoryItem> iter = categoryList.iterator();
			while (iter.hasNext()) {
				tempList.add(iter.next());
			}
			Collections.sort(tempList, new CategoryItemComparator());
		}
		
		for (CategoryItem category : tempList) {
			finalList.add(category);
		}
		
		return finalList;
	}
	
	public Map<String, List<FeedItem>> getSubscribedFeeds() {
		if ((mSubscribedFeeds == null) ||
				(needFullRefresh())) {
			mSubscribedFeeds = Controller.getInstance().getTTRSSConnector().getSubsribedFeeds();
			mForceFullRefresh = false;
		}
		return mSubscribedFeeds;
	}
	
	public List<FeedItem> getSubscribedFeedsByCategory(String categoryId) {				
		return getSubscribedFeeds().get(categoryId);
	}
	
	public FeedItem getFeed(String categoryId, String feedId) {
		List<FeedItem> feedList = getSubscribedFeedsByCategory(categoryId);
		
		FeedItem result = null;
		
		FeedItem tempFeed;
		Iterator<FeedItem> iter = feedList.iterator();
		while ((result == null) &&
				(iter.hasNext())) {
			
			tempFeed = iter.next();
			
			if (tempFeed.getId().equals(feedId)) {
				result = tempFeed;
			}
		}
		
		return result;
	}
	
	/**
	 * Same as getFeed(String categoryId, String feedId), but slower as it as to look through categories.
	 * @param feedId
	 * @return
	 */
	public FeedItem getFeed(String feedId) {
		Map<String, List<FeedItem>> feedsList = getSubscribedFeeds();
		
		FeedItem result = null;
		
		Set<String> categories = feedsList.keySet();
		
		Iterator<String> categoryIter = categories.iterator();
		while ((categoryIter.hasNext()) &&
				(result == null)) {
			result = getFeed(categoryIter.next(), feedId);
		}
		
		return result;
	}
	
	public List<ArticleItem> getArticlesForFeedsHeadlines(String feedId) {
		List<ArticleItem> result = mFeedsHeadlines.get(feedId);
		
		if ((result == null) ||
				(needFullRefresh())) {
			result = Controller.getInstance().getTTRSSConnector().getFeedHeadlines(new Integer(feedId).intValue(), 100, 0);
			mFeedsHeadlines.put(feedId, result);
			mForceFullRefresh = false;
		}
		
		return result;
	}
	
	public ArticleItem getSingleArticleForFeedsHeadlines(String feedId, String articleId) {
		List<ArticleItem> articlesList = getArticlesForFeedsHeadlines(feedId);
		
		ArticleItem result = null;
		
		ArticleItem tempArticle;
		Iterator<ArticleItem> iter = articlesList.iterator();
		while ((result == null) &&
				(iter.hasNext())) {
			
			tempArticle = iter.next();
			
			if (tempArticle.getId().equals(articleId)) {
				result = tempArticle;
			}
		}
		
		return result;
	}
	
	/**
	 * Same has getSingleArticleForFeedsHeadlines(), except that it load full article content if necessary.
	 * @param feedId
	 * @param articleId
	 * @return
	 */
	public ArticleItem getSingleArticleWithFullContentLoaded(String feedId, String articleId) {
		
		ArticleItem result = getSingleArticleForFeedsHeadlines(feedId, articleId);
		
		if (result != null) {
			if (!result.isContentLoaded()) {
				result.doLoadContent();
			}
		}
		
		return result;
	}

}
