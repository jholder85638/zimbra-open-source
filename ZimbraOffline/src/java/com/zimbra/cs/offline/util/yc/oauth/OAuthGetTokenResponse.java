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
package com.zimbra.cs.offline.util.yc.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.zimbra.cs.offline.util.yc.YContactException;

public class OAuthGetTokenResponse extends OAuthResponse {

    public OAuthGetTokenResponse(String resp) throws YContactException {
        super(resp);
    }

    @Override
    protected void handleResponse() throws YContactException {
        String token;
        try {
            token = URLDecoder.decode(getByKey(OAuthConstants.OAUTH_TOKEN), "UTF-8");
            String tokenSecret = URLDecoder.decode(getByKey(OAuthConstants.OAUTH_TOKEN_SECRET), "UTF-8");
            OAuthToken otoken = new OAuthToken(token, tokenSecret);
            otoken.setSessionHandle(getByKey(OAuthConstants.OAUTH_SESSION_HANDLE));
            otoken.setGuid(getByKey(OAuthConstants.OAUTH_YAHOO_GUID));
            this.setToken(otoken);
        } catch (UnsupportedEncodingException e) {
            throw new OAuthException("error when decoding token", "", false, e, null);
        }
    }
}
