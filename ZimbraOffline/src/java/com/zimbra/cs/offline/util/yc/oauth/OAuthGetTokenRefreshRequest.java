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

public class OAuthGetTokenRefreshRequest extends OAuthGetTokenRequest {

    public OAuthGetTokenRefreshRequest(OAuthToken token) {
        super(token, null);
    }

    @Override
    protected void doFillSpecificParams() {
        super.doFillSpecificParams();
        this.removeParam(OAuthConstants.OAUTH_VERIFIER);
        this.addParam(OAuthConstants.OAUTH_SESSION_HANDLE, getToken().getSessionHandle());
    }

    @Override
    protected String getVerifier() {
        return "n";
    }

    @Override
    protected String getStep() {
        return "(step 4, refresh)";
    }
}
