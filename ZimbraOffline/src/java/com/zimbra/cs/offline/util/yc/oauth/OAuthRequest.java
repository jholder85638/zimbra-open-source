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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.cs.offline.OfflineLog;

public abstract class OAuthRequest {

    protected Map<String, String> params = new HashMap<String, String>();
    private OAuthToken token;
    protected static HttpClient httpClient = ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient();

    public OAuthRequest(OAuthToken token) {
        this.token = token;
    }

    public Map<String, String> getParams() {
        return params;
    }

    protected void addParam(String key, String value) {
        this.params.put(key, value);
    }

    protected void removeParam(String key) {
        this.params.remove(key);
    }

    public OAuthToken getToken() {
        return token;
    }

    protected abstract String getEndpointURL();

    protected abstract String getHttpMethod();

    protected String getVerifier() {
        return "";
    }

    protected void fillParams() throws OAuthException {
        doFillSpecificParams();
        doSign();
    }

    protected abstract void doFillSpecificParams();

    protected abstract String getStep();

    private void doSign() throws OAuthException {
        try {
            String signature = OAuthHelper.getSignature(
                    OAuthHelper.getBaseString(this.params, getEndpointURL(), getHttpMethod(), getStep()),
                    OAuthConstants.OAUTH_CONSUMER_SECRET_VALUE, getToken().getTokenSecret());
            this.addParam(OAuthConstants.OAUTH_SIGNATURE, signature);
        } catch (com.zimbra.cs.offline.util.yc.oauth.OAuthException e) {
            throw new OAuthException("Generate signature error at " + getStep(), "", false, null, null);
        }
    }

    public String send() throws OAuthException {

        try {
            fillParams();

            HttpMethod httpMethod = "POST".equals(getHttpMethod()) ? new PostMethod(getEndpointURL()) : new GetMethod(
                    getEndpointURL());
            OfflineLog.yab.info("[Yahoo OAuth] sending request [%s], token {%s}", getEndpointURL(), getToken());
            String header = OAuthHelper.extractHeader(this.params);
            httpMethod.setRequestHeader("Authorization", header);
            int code = HttpClientUtil.executeMethod(httpClient, httpMethod);
            if (code != 200) {
                throw OAuthException.handle(code, getStep());
            }
            InputStream stream = httpMethod.getResponseBodyAsStream();
            String resp = OAuthHelper.getStreamContents(stream);
            return resp;
        } catch (Exception e) {
            OfflineLog.yab.error("sending oauth request error", e);
            StringBuilder msg = new StringBuilder();
            msg.append("error when sending req at ").append(getStep());
            if (e instanceof OAuthException) {
                msg.append(" Internal reason: " + e.getMessage());
            }
            throw new OAuthException(msg.toString(), "", false, e, null);
        }
    }
}
