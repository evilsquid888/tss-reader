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

package org.ttrssreader.model.category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ttrssreader.R;
import org.ttrssreader.controllers.Controller;
import org.ttrssreader.model.IRefreshable;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CategoryListAdapter extends BaseAdapter implements IRefreshable {
	
	private Context mContext;
	
	private List<CategoryItem> mCategories;
	
	public CategoryListAdapter(Context context) {
		mContext = context;			
		mCategories = new ArrayList<CategoryItem>();
	}
	
	@Override
	public int getCount() {		
		return mCategories.size();
	}

	@Override
	public Object getItem(int position) {		
		return mCategories.get(position);
	}

	@Override
	public long getItemId(int position) {		
		return position;
	}
	
	public String getCategoryId(int position) {
		return mCategories.get(position).getId();
	}
	
	public String getCategoryTitle(int position) {
		return mCategories.get(position).getTitle();
	}
	
	public int getUnreadCount(int position) {
		return mCategories.get(position).getUnreadCount();
	}

	private String formatTitle(String title, int unread) {
		if (unread > 0) {
			return title + " (" + unread + ")";
		} else {
			return title;
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CategoryListView sv;
        if (convertView == null) {
            sv = new CategoryListView(mContext, mCategories.get(position).getTitle(),
            		mCategories.get(position).getId(),
            		mCategories.get(position).getUnreadCount());
        } else {
            sv = (CategoryListView) convertView;
            sv.setIcon(mCategories.get(position).getId(), mCategories.get(position).getUnreadCount() > 0);
            sv.setBoldTitleIfNecessary(mCategories.get(position).getUnreadCount() > 0);
            sv.setTitle(formatTitle(mCategories.get(position).getTitle(), mCategories.get(position).getUnreadCount()));
        }

        return sv;

	}
	
	private class CategoryListView extends LinearLayout {
        public CategoryListView(Context context, String title, String id, int unreadCount) {
            super(context);

            this.setOrientation(HORIZONTAL);

            // Here we build the child views in code. They could also have
            // been specified in an XML file.
            
            mIcon = new ImageView(context);
            setIcon(id, unreadCount > 0);
            addView(mIcon, new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));

            mTitle = new TextView(context);
            mTitle.setGravity(Gravity.CENTER_VERTICAL);  
            setBoldTitleIfNecessary(unreadCount > 0);
            mTitle.setText(formatTitle(title, unreadCount));
            
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
            layoutParams.setMargins(10, 0, 0, 0);
            
            addView(mTitle, layoutParams);
                        
        }

        /**
         * Convenience method to set the title of a SpeechView
         */
        public void setTitle(String title) {
            mTitle.setText(title);
        }
        
        public void setBoldTitleIfNecessary(boolean isUnread) {
        	if (isUnread) {
        		mTitle.setTypeface(mTitle.getTypeface(), 1);
        	} else {
        		mTitle.setTypeface(mTitle.getTypeface(), 0);
        	}
        }
        
        public void setIcon(String id, boolean hasUnread) {
        	if (id.equals("-1")) {
        		mIcon.setImageResource(R.drawable.star48);
        	} else if (id.equals("-2")) {
        		mIcon.setImageResource(R.drawable.published48);
        	} else if (id.equals("-3")) {
        		mIcon.setImageResource(R.drawable.fresh48);
        	} else if (id.equals("-4")) {
        		mIcon.setImageResource(R.drawable.all48);
        	} else {
        		if (hasUnread) {
        			mIcon.setImageResource(R.drawable.categoryunread48);
        		} else {
        			mIcon.setImageResource(R.drawable.categoryread48);
        		}
        	}
        }       

        private ImageView mIcon;
        private TextView mTitle;
    }

	public void refreshData() {
		mCategories = new ArrayList<CategoryItem>();		
		
		List<CategoryItem> tempList = new ArrayList<CategoryItem>();
		
		Iterator<Map<?, ?>> iter;
		CategoryItem categoryItem;
		Map<?, ?> item;
		
		List<Map<?, ?>> virtualCategories = Controller.getInstance().getXmlRpcConnector().getVirtualFeeds();
		if (virtualCategories != null) {
			iter = virtualCategories.iterator();
			while (iter.hasNext()) {
				item = iter.next();
				categoryItem = new CategoryItem(item.get("id").toString(), item.get("title").toString(), new Integer(item.get("unread").toString()).intValue());
				mCategories.add(categoryItem);
			}
			
			Collections.sort(mCategories, new VirtualCategoryItemComparator());

			List<Map<?, ?>> categories = Controller.getInstance().getXmlRpcConnector().getCategories();
			iter = categories.iterator();
			while (iter.hasNext()) {
				item = iter.next();
				
				// "unread" is not in ttrss 1.3.4 by default.
				if (item.containsKey("unread")) {
					categoryItem = new CategoryItem(item.get("id").toString(), item.get("title").toString(), new Integer(item.get("unread").toString()).intValue());
				} else {
					categoryItem = new CategoryItem(item.get("id").toString(), item.get("title").toString(), 0);
				}
				
				tempList.add(categoryItem);
			}				

			Collections.sort(tempList, new CategoryItemComparator());

			for (CategoryItem category : tempList) {
				mCategories.add(category);
			}
		}
	}

}
