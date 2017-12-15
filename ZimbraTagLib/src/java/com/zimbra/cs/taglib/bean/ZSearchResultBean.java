/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014 Zimbra, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.taglib.bean;

import com.zimbra.client.*;
import com.zimbra.client.ZSearchResult.ZConversationSummary;

import java.util.ArrayList;
import java.util.List;

public class ZSearchResultBean {
    
    private ZSearchResult mResult;
    private ArrayList<ZSearchHitBean> mHits;
    private int mLimit;
    private int mOffset;
    private int mPrevOffset;
    private int mNextOffset;
    
    public ZSearchResultBean(ZSearchResult result, ZSearchParams params) {
        mResult = result;
        mLimit = params.getLimit();
        mOffset = params.getOffset();
        mPrevOffset = mLimit > mOffset ? 0 : mOffset - mLimit;
        mNextOffset = mOffset + mLimit;
    }

    public int getSize() { return mResult.getHits().size(); }

    public ZConversationSummary getConversationSummary() { return mResult.getConversationSummary(); }
    
    public synchronized ZMessageBean getFetchedMessage() {
        for (ZSearchHitBean hit : getHits()) {
            if (hit.getIsMessage()) {
                ZMessageBean msg = hit.getMessageHit().getMessage();
                if (msg != null) return msg;
            }
        }
        return null;
    }

    /**
     *
     * @return index of first message in result that matched, or -1.
     * 
     */
    public synchronized int getFetchedMessageIndex() {
        int i = 0;
        for (ZSearchHitBean hit : getHits()) {
            if (hit.getIsMessage()) {
                ZMessageBean msg = hit.getMessageHit().getMessage();
                if (msg != null) return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * @return ZSearchHit objects from search
     */
    public synchronized List<ZSearchHitBean> getHits() {
        if (mHits == null) {
            mHits = new ArrayList<ZSearchHitBean>();
            for (ZSearchHit hit : mResult.getHits()) {
                if (hit instanceof ZConversationHit) {
                    mHits.add(new ZConversationHitBean((ZConversationHit)hit));
                } else if (hit instanceof ZMessageHit) {
                    mHits.add(new ZMessageHitBean((ZMessageHit)hit));
                } else if (hit instanceof ZContactHit) {
                    mHits.add(new ZContactHitBean((ZContactHit)hit));
                } else if (hit instanceof ZVoiceMailItemHit) {
                    mHits.add(new ZVoiceMailItemHitBean((ZVoiceMailItemHit)hit));
                } else if (hit instanceof ZCallHit) {
                    mHits.add(new ZCallHitBean((ZCallHit)hit));
                } else if (hit instanceof ZTaskHit) {
                    mHits.add(new ZTaskHitBean((ZTaskHit)hit));
                } else if (hit instanceof ZDocumentHit) {
                    mHits.add(new ZDocumentHitBean((ZDocumentHit)hit));
                } else if (hit instanceof ZAppointmentHit) {
                    mHits.add(new ZAppointmentHitBean((ZAppointmentHit)hit));
                }else if (hit instanceof ZWikiHit) {
                    mHits.add(new ZWikiHitBean((ZWikiHit)hit));
                }
            }
        }
        return mHits;
    }

    /**
     * @return true if there are more search results on the server
     */
    public boolean getHasMore() { return mResult.hasMore(); }  

    public boolean getHasNextPage() { return mResult.hasMore(); }
    public boolean getHasPrevPage() { return mOffset > 0; }
    
    /**
     * @return the sort by value
     */
    public String getSortBy() { return mResult.getSortBy(); }

    /**
     * @return offset used for the search.
     */
    public int getOffset() { return mOffset; }

    public int getPrevOffset() { return mPrevOffset; }
    
    public int getNextOffset() { return mNextOffset; }

    /** @return limit used with search */
    public int getLimit() { return mLimit; }
}
