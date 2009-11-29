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
import org.ttrssreader.model.IUpdatable;

public class ArticleReadStateUpdater implements IUpdatable {
	
	private List<String> mArticleIdList;
	private int mArticleState;
	
	public ArticleReadStateUpdater(List<String> articleIdList, int articleState) {
		mArticleIdList = articleIdList;
		mArticleState = articleState;
	}
	
	public ArticleReadStateUpdater(String articleId, int articleState) {
		mArticleIdList = new ArrayList<String>();
		mArticleIdList.add(articleId);
		mArticleState = articleState;
	}

	@Override
	public void update() {
		Iterator<String> iter = mArticleIdList.iterator();
		while (iter.hasNext()) {
			Controller.getInstance().getXmlRpcConnector().setArticleRead(iter.next(), mArticleState);
		}
		Controller.getInstance().setRefreshNeeded(true);
	}

}
