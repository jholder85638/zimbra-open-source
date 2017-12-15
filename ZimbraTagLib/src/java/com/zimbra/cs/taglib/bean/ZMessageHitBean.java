/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2009, 2010, 2011, 2013, 2014 Zimbra, Inc.
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

import com.zimbra.client.ZEmailAddress;
import com.zimbra.client.ZMessageHit;

import java.util.Date;
import java.util.List;

public class ZMessageHitBean extends ZSearchHitBean {

    private ZMessageHit mHit;
    
    public ZMessageHitBean(ZMessageHit hit) {
        super(hit, HitType.message);
        mHit = hit;
    }

    public String getFlags() { return mHit.getFlags(); } 
    
    public String getFolderId() { return mHit.getFolderId(); }

    public long getSize() { return mHit.getSize(); }

    public Date getDate() { return new Date(mHit.getDate()); }
    
    public String getConversationId() { return mHit.getConversationId(); }

    public boolean getIsInvite() { return mHit.getIsInvite(); }
    
    public boolean getIsUnread() { return mHit.isUnread(); }

    public boolean getIsFlagged() { return mHit.isFlagged(); }

    public boolean getIsHighPriority() { return mHit.isHighPriority(); }

    public boolean getIsLowPriority() { return mHit.isLowPriority(); }

    public boolean getHasAttachment() { return mHit.hasAttachment(); }

    public boolean getIsRepliedTo() { return mHit.isRepliedTo(); }

    public boolean getIsSentByMe() { return mHit.isSentByMe(); }

    public boolean getIsForwarded() { return mHit.isForwarded(); } 

    public boolean getIsDraft() { return mHit.isDraft(); }

    public boolean getIsDeleted() { return mHit.isDeleted(); }

    public boolean getIsNotificationSent() { return mHit.isNotificationSent(); }
    
    /**
     * @return comma-separated list of tag ids
     */
    public String getTagIds() { return mHit.getTagIds(); }

    public String getSubject() { return mHit.getSubject(); }
    
    public boolean getHasFlags() { return mHit.hasFlags(); }
    
    public boolean getHasTags() { return mHit.hasTags(); }
    
    public String getFragment() { return mHit.getFragment(); }
    
    public ZEmailAddress getSender() { return mHit.getSender(); }

    public String getDisplaySender() { return BeanUtils.getAddr(mHit.getSender()); }

    public List<ZEmailAddress> getAddresses() { return mHit.getAddresses(); }

    public String getDisplayAddresses() { return BeanUtils.getAddrs(mHit.getAddresses()); }    

    public boolean getContentMatched() { return mHit.getContentMatched(); }

    public boolean getMessageMatched() { return mHit.getContentMatched() || mHit.getMimePartHits().size() > 0; }

    public ZMessageBean getMessage() {
        if (mHit.getMessage() != null)
            return new ZMessageBean(mHit.getMessage());
        else
            return null;
    }

    /**
     *  @return names (1.2.3...) of mime part(s) that matched, or empty list.
     */
    public List<String> getMimePartHits() { return mHit.getMimePartHits(); }

    public String getStatusImage() {
        if (getIsInvite())
            return "startup/ImgAppointment.png";
        else if (getIsUnread())
            return "startup/ImgMsgStatusUnread.png";
        else if (getIsDraft())
            return "startup/ImgMsgStatusDraft.png";
        else if (getIsRepliedTo())
            return "startup/ImgMsgStatusReply.png";
        else if (getIsForwarded())
            return "startup/ImgMsgStatusForward.png";
        else if (getIsSentByMe())
            return "startup/ImgMsgStatusSent.png";
        else
            return "startup/ImgMsgStatusRead.png";
    }

    public String getStatusImageAltKey() {
        if (getIsInvite())
            return "ALT_MSG_STATUS_APPT";
        else if (getIsUnread())
            return "ALT_MSG_STATUS_UNREAD";
        else if (getIsDraft())
            return "ALT_MSG_STATUS_DRAFT";
        else if (getIsRepliedTo())
            return "ALT_MSG_STATUS_REPLIEDTO";
        else if (getIsForwarded())
            return "ALT_MSG_STATUS_FORWARDED";
        else if (getIsSentByMe())
            return "ALT_MSG_STATUS_SENTBYME";
        else
            return "ALT_MSG_STATUS_READ";
    }

}

