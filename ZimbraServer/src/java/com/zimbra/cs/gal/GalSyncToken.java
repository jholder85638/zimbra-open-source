/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2009, 2010, 2011, 2013, 2014, 2015, 2016 Synacor, Inc.
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
package com.zimbra.cs.gal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.ldap.LdapDateUtil;
import com.zimbra.cs.ldap.LdapUtil;

public class GalSyncToken {
    public GalSyncToken(String token) {
        parse(token);
    }

    public GalSyncToken(String ldapTs, String accountId, int changeId) {
        mLdapTimestamp = ldapTs;
        mChangeIdMap = new HashMap<String,String>();
        mChangeIdMap.put(accountId, "" + changeId);
    }

    private String mLdapTimestamp;
    private HashMap<String,String> mChangeIdMap;

    private void parse(String token) {
        mChangeIdMap = new HashMap<String,String>();
        int pos = token.indexOf(':');
        if (pos == -1) {
            // old style LDAP timestamp token
            mLdapTimestamp = token;
            return;
        }
        mLdapTimestamp = token.substring(0, pos);
        boolean finished = false;
        while (!finished) {
            token = token.substring(pos+1);
            int sep = token.indexOf(':');
            if (sep == -1)
                return;
            String key = token.substring(0, sep);
            String value = null;
            pos = token.indexOf(':', sep+1);
            if (pos == -1) {
                finished = true;
                value = token.substring(sep+1);
            } else {
                value = token.substring(sep+1, pos);
            }
            mChangeIdMap.put(key, value);
        }
    }

    public String getLdapTimestamp() {
        return mLdapTimestamp;
    }

    public String getLdapTimestamp(String format) throws ServiceException {
        // mLdapTimestamp should be always in this format
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        Date ts = LdapDateUtil.parseGeneralizedTime(mLdapTimestamp);
        if (ts == null) {
            return mLdapTimestamp;
        } else {
            if (format.endsWith("'Z'")) {
                //make sure we're returning the correct timezone
                //we previously used SimpleDateFormat on both ends; so we were using local timezone but ignoring it
                fmt.setTimeZone(TimeZone.getTimeZone("Zulu"));
            }
            return fmt.format(ts);
        }
    }

    public int getChangeId(String accountId) {
        String cid = mChangeIdMap.get(accountId);
        if (cid != null)
            return Integer.parseInt(cid);
        return 0;
    }

    public boolean doMailboxSync() {
        return mLdapTimestamp.length() == 0 || mChangeIdMap.size() > 0;
    }

    public boolean isEmpty() {
        return mLdapTimestamp.length() == 0 && mChangeIdMap.size() == 0;
    }

    public void merge(GalSyncToken that) {
        ZimbraLog.gal.debug("merging token "+this+" with "+that);
        mLdapTimestamp = LdapUtil.getEarlierTimestamp(this.mLdapTimestamp, that.mLdapTimestamp);
        for (String aid : that.mChangeIdMap.keySet())
            mChangeIdMap.put(aid, that.mChangeIdMap.get(aid));
        ZimbraLog.gal.debug("result: "+this);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(mLdapTimestamp);
        for (String aid : mChangeIdMap.keySet())
            buf.append(":").append(aid).append(":").append(mChangeIdMap.get(aid));
        return buf.toString();
    }
}
