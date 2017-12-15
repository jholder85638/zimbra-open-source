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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zimbra.cs.offline.util.yc.YContactException;

public abstract class OAuthResponse {

    private String rawResp;
    private OAuthToken token;
    
    public OAuthResponse(String resp) throws YContactException {
        this.rawResp = resp;
        handleResponse();
    }
    
    protected abstract void handleResponse() throws YContactException;

    protected String getRawResponse() {
        return this.rawResp;
    }
    
    public OAuthToken getToken() {
        return this.token;
    }
    
    public void setToken(OAuthToken token) {
        this.token = token;
    }
    
    protected String getByKey(String key) {
        Pattern p = Pattern.compile(key+"=([^&]+)");
        Matcher matcher = p.matcher(this.rawResp);
        if (matcher.find() && matcher.groupCount() > 0) {
            return matcher.group(1);
        }
        return "";
    }
    
    public String toString() {
        return this.rawResp;
    }
}
