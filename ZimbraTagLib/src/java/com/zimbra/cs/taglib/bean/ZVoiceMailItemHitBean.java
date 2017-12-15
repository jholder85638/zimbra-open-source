/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.taglib.bean;

import com.zimbra.client.ZPhone;
import com.zimbra.client.ZVoiceMailItemHit;
import com.zimbra.common.service.ServiceException;

import java.util.Date;

public class ZVoiceMailItemHitBean extends ZSearchHitBean {

    private ZVoiceMailItemHit mHit;

    public ZVoiceMailItemHitBean(ZVoiceMailItemHit hit) {
        super(hit, HitType.voiceMailItem);
        mHit = hit;
    }

    public static ZVoiceMailItemHitBean deserialize(String value, String phone) throws ServiceException {
        return new ZVoiceMailItemHitBean(ZVoiceMailItemHit.deserialize(value, phone));
    }

    public String toString() { return mHit.toString(); }

    public boolean getIsFlagged() { return mHit.isFlagged(); }

    public boolean getIsUnheard() { return mHit.isUnheard(); }

    public boolean getIsPrivate() { return mHit.isPrivate(); }

    public ZPhone getCaller() { return mHit.getCaller(); }

    public String getDisplayCaller() { return mHit.getDisplayCaller(); }

    public String getSoundUrl() { return mHit.getSoundUrl(); }

    public Date getDate() { return new Date(mHit.getDate()); }

    public long getDuration() { return mHit.getDuration(); }

    public String getSerialize() { return  mHit.serialize(); }
}

