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

package org.ttrssreader.model.feedheadline;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.ttrssreader.R;
import org.ttrssreader.controllers.DataController;
import org.ttrssreader.model.IRefreshable;
import org.ttrssreader.model.article.ArticleItem;
import org.ttrssreader.utils.DateUtils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FeedHeadlineListAdapter extends BaseAdapter implements IRefreshable {
	
	private Context mContext;
	
	private String mFeedId;
	
	private List<ArticleItem> mFeeds = null;
	
	public FeedHeadlineListAdapter(Context context, String feedId) {
		mContext = context;		
		mFeedId = feedId;
		mFeeds = new ArrayList<ArticleItem>();
	}
	
	@Override
	public int getCount() {
		return mFeeds.size();
	}

	@Override
	public Object getItem(int position) {
		return mFeeds.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public String getFeedItemId(int position) {
		return mFeeds.get(position).getId();
	}
	
	public int getUnreadCount() {
		int result = 0;
		
		Iterator<ArticleItem> iter = mFeeds.iterator();
		while (iter.hasNext()) {
			if (iter.next().isUnread()) {
				result++;
			}
		}		
		
		return result;
	}
	
	public List<ArticleItem> getArticleReadList() {
		List<ArticleItem> result = new ArrayList<ArticleItem>();
		
		ArticleItem item;
		Iterator<ArticleItem> iter = mFeeds.iterator();
		while (iter.hasNext()) {
			item = iter.next();
			if (!item.isUnread()) {
				result.add(item);
			}
		}
		
		return result;
	}
	
	public List<ArticleItem> getArticleUnreadList() {
		List<ArticleItem> result = new ArrayList<ArticleItem>();
		
		ArticleItem item;
		Iterator<ArticleItem> iter = mFeeds.iterator();
		while (iter.hasNext()) {
			item = iter.next();
			if (item.isUnread()) {
				result.add(item);
			}
		}
		
		return result;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FeedHeadlineListView sv;
        if (convertView == null) {
            sv = new FeedHeadlineListView(mContext,
            		position,
            		mFeeds.get(position).getTitle(),
            		mFeeds.get(position).getId(),
            		mFeeds.get(position).isUnread(),
            		mFeeds.get(position).getContent(),
            		mFeeds.get(position).getUpdateDate());
        } else {
            sv = (FeedHeadlineListView) convertView;
            
            sv.setIcon(mFeeds.get(position).isUnread());
            sv.setBoldTitleIfNecessary(mFeeds.get(position).isUnread());
            sv.setTitle(mFeeds.get(position).getTitle());   
            sv.setUpdateDate(mContext, mFeeds.get(position).getUpdateDate());
        }

        return sv;
	}		
	
	private class FeedHeadlineListView extends LinearLayout {
        public FeedHeadlineListView(Context context, int position, String title, String id, boolean isUnread, String content, Date updatedDate) {
            super(context);

            this.setOrientation(HORIZONTAL);

            // Here we build the child views in code. They could also have
            // been specified in an XML file.

            mIcon = new ImageView(context);
            setIcon(isUnread);            
            addView(mIcon, new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
            
            LinearLayout textLayout = new LinearLayout(context);
            textLayout.setOrientation(VERTICAL);
            textLayout.setGravity(Gravity.CENTER_VERTICAL);
            
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
            layoutParams.setMargins(10, 0, 0, 0);
            
            addView(textLayout, layoutParams);
            
            mTitle = new TextView(context);            
            setBoldTitleIfNecessary(isUnread);
            mTitle.setText(title);            
            textLayout.addView(mTitle, new LinearLayout.LayoutParams(
            		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            
            mUpdateDate = new TextView(context);
            mUpdateDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);            
            setUpdateDate(context, updatedDate);
            textLayout.addView(mUpdateDate, new LinearLayout.LayoutParams(
            		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }

        /**
         * Convenience method to set the title of a SpeechView
         */
        public void setTitle(String title) {
            mTitle.setText(title);
        }
        
        public void setBoldTitleIfNecessary(boolean isUnread) {
        	if (isUnread) {
        		mTitle.setTypeface(Typeface.DEFAULT_BOLD, 1);
        	} else {
        		mTitle.setTypeface(Typeface.DEFAULT, 0);
        	}
        }
        
        public void setIcon(boolean isUnread) {
        	if (isUnread) {        	
            	mIcon.setImageResource(R.drawable.articleunread48);
            } else {
            	mIcon.setImageResource(R.drawable.articleread48);
            }
        }    
        
        public void setUpdateDate(Context context, Date updatedDate) {
        	mUpdateDate.setText(DateUtils.getDisplayDate(context, updatedDate));
        }

        private ImageView mIcon;
        private TextView mTitle;
        private TextView mUpdateDate;
    }
	
	@Override
	public void refreshData() {
		mFeeds = DataController.getInstance().getArticlesForFeedsHeadlines(mFeedId);				
	}

}
