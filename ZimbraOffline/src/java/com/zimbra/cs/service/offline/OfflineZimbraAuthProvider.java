/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010, 2013, 2014, 2016 Synacor, Inc.
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

/*
 * Created on Aug 20, 2010
 */
package com.zimbra.cs.service.offline;

import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.ZimbraAuthToken;
import com.zimbra.cs.service.AuthProviderException;
import com.zimbra.cs.service.ZimbraAuthProvider;

public class OfflineZimbraAuthProvider extends ZimbraAuthProvider {
    
    public static final String PROVIDER_NAME = "offline";

    public OfflineZimbraAuthProvider() {
        super(PROVIDER_NAME);
    }
    
    protected AuthToken genAuthToken(String encodedAuthToken) throws AuthProviderException, AuthTokenException {
        if (StringUtil.isNullOrEmpty(encodedAuthToken))
            throw AuthProviderException.NO_AUTH_DATA();
        AuthToken at = ZimbraAuthToken.getAuthToken(encodedAuthToken);
        if (at instanceof ZimbraAuthToken) {
           try {
               return (AuthToken)((ZimbraAuthToken)at).clone();
           } catch (CloneNotSupportedException e) {
               ZimbraLog.system.error("Unable to clone zimbra auth token",e);
               return at;
           }
        } else {
            return at;
        }
    }
}
