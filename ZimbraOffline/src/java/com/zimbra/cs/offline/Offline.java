/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.offline;

import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapTransport;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.offline.OfflineAccount;
import com.zimbra.cs.account.offline.OfflineProvisioning;

public class Offline {
    static {
    	//If we don't set this, DNS resolution is by default cached forever.  If one starts the offline server from one
    	//location and then moves to another network, the sync target address may change due to different route.
    	java.security.Security.setProperty("networkaddress.cache.ttl" , OfflineLC.dns_cache_ttl.value());

    	//Set a couple of HttpClient connection/socket parameters for offline specific tuning
    	DefaultHttpParams.getDefaultParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, OfflineLC.http_so_timeout.intValue());
    	DefaultHttpParams.getDefaultParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, OfflineLC.http_connection_timeout.intValue());
    	
    	if (LC.client_use_system_proxy.booleanValue())
    	    System.setProperty("java.net.useSystemProxies","true");
    	
    	System.setProperty("http.agent", OfflineLC.zdesktop_name.value() + " " + OfflineLC.getFullVersion()); //for httpclient
    }

    public static class OfflineDebugListener implements SoapTransport.DebugListener {
    	OfflineAccount account;
    	
    	public OfflineDebugListener() {}
    	public OfflineDebugListener(OfflineAccount account) { this.account = account; }
    	
        public void sendSoapMessage(Element envelope) {
        	if (account == null || account.isDebugTraceEnabled())
        		OfflineLog.request.debug(getPayload(envelope));
        }
        public void receiveSoapMessage(Element envelope) {
        	if (account == null || account.isDebugTraceEnabled())
        		OfflineLog.response.debug(getPayload(envelope));
        }

        private Element getPayload(Element soap) {
            Element body = soap.getOptionalElement("Body");
            if (body != null && !body.listElements().isEmpty()) {
                Element elt = body.listElements().get(0);
                if (elt.getName().equals(AccountConstants.AUTH_REQUEST.getName())) {
                    elt = elt.clone();
                    Element eltPswd = elt.getOptionalElement(AccountConstants.E_PASSWORD);
                    if (eltPswd != null)
                        eltPswd.setText("*");
                }
                return elt;
            }
            if (body == null && soap.getOptionalElement("Fault") != null)  return soap.getOptionalElement("Fault");
            return soap;
        }
    }

    public static String getServerURI(Account acct, String service) {
        return getServerURI(acct.getAttr(OfflineProvisioning.A_offlineRemoteServerUri), service);
    }

    public static String getServerURI(String baseUri, String service) {
        if (baseUri == null)
            return null;
        else if (baseUri.endsWith("/") && service.startsWith("/"))
            return baseUri + service.substring(1);
        else if (!baseUri.endsWith("/") && !service.startsWith("/"))
            return baseUri + '/' + service;
        else
            return baseUri + service;
    }
}
