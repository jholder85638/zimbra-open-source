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

import com.zimbra.cs.offline.util.yc.YContactException;


public class OAuthGetRequestTokenResponse extends OAuthResponse {

    public OAuthGetRequestTokenResponse(String resp) throws YContactException {
        super(resp);
    }

    @Override
    protected void handleResponse() {
        String token = getByKey(OAuthConstants.OAUTH_TOKEN);
        String tokenSecret = getByKey(OAuthConstants.OAUTH_TOKEN_SECRET);
        String url = getByKey(OAuthConstants.OAUTH_REQUEST_AUTH_URL);
        OAuthToken otoken = new OAuthToken(token, tokenSecret);
        otoken.setNextUrl(url);
        this.setToken(otoken);
    }

}
