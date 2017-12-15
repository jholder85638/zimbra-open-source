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

import com.zimbra.cs.offline.OfflineLC;

public class OAuthConstants {

    public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    public static final String OAUTH_CONSUMER_SECRET = "oauth_consumer_secret";
    
    public static final String OAUTH_CONSUMER_KEY_VALUE = OfflineLC.zdesktop_yoauth_consumer_key.value();
    public static final String OAUTH_CONSUMER_SECRET_VALUE = OfflineLC.zdesktop_yoauth_consumer_secret.value();
    
    public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    public static final String OAUTH_SIGNATURE = "oauth_signature";

    public static final String OAUTH_NONCE = "oauth_nonce";
    public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    
    
    public static final String OAUTH_VERSION = "oauth_version";
    
    public static final String OAUTH_TOKEN = "oauth_token";
    public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
    public static final String OAUTH_REQUEST_AUTH_URL = "xoauth_request_auth_url";
    public static final String OUT_OF_BAND = "oob";
    public static final String OAUTH_VERIFIER = "oauth_verifier";
//    public static final String OAUTH_LANG_PREF = "xoauth_lang_pref";
    public static final String OAUTH_CALLBACK = "oauth_callback";
    
    public static final String OAUTH_SESSION_HANDLE = "oauth_session_handle";
    public static final String OAUTH_SESSION_HANDLE_EXPIRE_IN = "oauth_authorization_expires_in";
    public static final String OAUTH_YAHOO_GUID = "xoauth_yahoo_guid";
    
    public static final String HMAC_SHA1 = "HMAC-SHA1";
    public static final String POST_METHOD = "POST";
    public static final String GET_METHOD = "GET";
    public static final String PUT_METHOD = "PUT";
    
    public static final String OAUTH_GET_REQ_TOKEN_URL = OfflineLC.zdesktop_yoauth_get_req_token_url.value();
    public static final String OAUTH_GET_TOKEN_URL = OfflineLC.zdesktop_yoauth_get_token_url.value();
    public static final String OAUTH_GET_CONTACTS_URL = OfflineLC.zdesktop_yoauth_get_contacts_url.value();
    public static final String OAUTH_PUT_CONTACTS_URL = OfflineLC.zdesktop_yoauth_put_contacts_url.value();
    
    public static final long OAUTH_TOKEN_EXPIRE_PERIOD = OfflineLC.zdesktop_yoauth_token_expire_period.longValue();
}
