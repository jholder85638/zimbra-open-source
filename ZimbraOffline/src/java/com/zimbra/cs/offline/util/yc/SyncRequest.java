/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.offline.util.yc;

import com.zimbra.cs.offline.OfflineLog;
import com.zimbra.cs.offline.util.yc.oauth.OAuthGetContactsRequest;
import com.zimbra.cs.offline.util.yc.oauth.OAuthRequest;
import com.zimbra.cs.offline.util.yc.oauth.OAuthToken;

public class SyncRequest extends Request {

    private int reqRev = 0;

    public SyncRequest(OAuthToken token) {
        super(token);
    }

    public SyncRequest(OAuthToken token, int rev) {
        super(token);
        this.reqRev = rev;
    }

    @Override
    public SyncResponse send() throws YContactException {
        OAuthRequest req = new OAuthGetContactsRequest(this.getToken(), this.reqRev);
        String resp = req.send();

        OfflineLog.yab.debug(resp);

        return new SyncResponse(200, resp);
    }

}
