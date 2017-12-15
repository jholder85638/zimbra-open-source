/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2013, 2014, 2015, 2016 Synacor, Inc.
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
package com.zimbra.cs.security.openid.consumer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.UrlIdentifier;
import org.openid4java.discovery.XriIdentifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;
import org.openid4java.util.HttpClientFactory;
import org.openid4java.util.ProxyProperties;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.extension.ExtensionHttpHandler;
import com.zimbra.cs.extension.ZimbraExtension;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.servlet.ZimbraServlet;

/**
 * OpenID Consumer HTTP Handler
 */
public class OpenIDConsumerHandler extends ExtensionHttpHandler {

    private static final String COOKIE_ZM_OPENID_DISCOVERY_INFO = "ZM_OPENID_DISCOVERY_INFO";

    private ConsumerManager manager;

    @Override
    public void init(ZimbraExtension ext) throws ServiceException {
        super.init(ext);
        configureHttpProxy();
        try {
            manager = new ConsumerManager();
            if (Provisioning.getInstance().getLocalServer().isOpenidConsumerStatelessModeEnabled()) {
                manager.setMaxAssocAttempts(0);
            } else {
                manager.setAssociations(new MemcachedConsumerAssociationStore());
                // set nonce timestamp expiry to 5 min
                manager.setNonceVerifier(new MemcachedNonceVerifier(300));
            }
        } catch (Exception e) {
            ZimbraLog.extensions.error("OpenID error", e);
            throw ServiceException.FAILURE("Error in initializing OpenID ConsumerManager", e);
        }
    }

    private static void configureHttpProxy() throws ServiceException {
        String url = Provisioning.getInstance().getLocalServer().getAttr(Provisioning.A_zimbraHttpProxyURL, null);
        if (url == null)
            return;

        ProxyProperties proxyProps = new ProxyProperties();
        URI sProxyUri;
        try {
            sProxyUri = new URI(url);
        } catch (URISyntaxException e) {
            ZimbraLog.extensions.error(e.getMessage());
            throw ServiceException.FAILURE("invalid zimbraHttpProxyURL", e);
        }
        proxyProps.setProxyHostName(sProxyUri.getHost());
        proxyProps.setProxyPort(sProxyUri.getPort());
        String userInfo = sProxyUri.getUserInfo();
        if (userInfo != null) {
            int i = userInfo.indexOf(':');
            if (i != -1) {
                proxyProps.setUserName(userInfo.substring(0, i));
                proxyProps.setPassword(userInfo.substring(i + 1));
            }
        }
        HttpClientFactory.setProxyProperties(proxyProps);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        doPost(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if ("true".equals(req.getParameter("is_return"))) {
            processReturn(req, resp);
        } else {
            String userSuppliedId = req.getParameter("openid_identifier");
            if (userSuppliedId == null)
                throw new ServletException("Invalid request");
            else
                authRequest(userSuppliedId, req, resp);
        }
    }

    private void processReturn(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        VerificationResult verification;
        DiscoveryInformation discovered;
        try {
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response = new ParameterList(req.getParameterMap());

            // retrieve the previously stored discovery information
            String serializedDiscInfo = getCookieValue(req, COOKIE_ZM_OPENID_DISCOVERY_INFO);
            if (serializedDiscInfo == null) {
                throw new ServletException("Request missing discovery information");
            }
            discovered = deserializeDiscoveryInfo(serializedDiscInfo);

            // extract the receiving URL from the HTTP request
            StringBuffer receivingURL = req.getRequestURL();
            String queryString = req.getQueryString();
            if (queryString != null && queryString.length() > 0)
                receivingURL.append("?").append(req.getQueryString());

            // verify the response
            verification = manager.verify(receivingURL.toString(), response, discovered);
        } catch (OpenIDException e) {
            ZimbraLog.extensions.error("OpenID error code %s", e.getErrorCode(), e);
            throw new ServletException(e.getMessage());
        } catch (ServiceException e) {
            throw new ServletException(e.getMessage());
        }

        Identifier identifier = verification.getVerifiedId();
        if (identifier == null) {
            throw new ServletException("Authentication failed");
        }
        ZimbraLog.extensions.debug("claimed identifier: %s", identifier);
        String openId = identifier.getIdentifier();
        try {
            Provisioning prov = Provisioning.getInstance();
            AuthToken authToken = getZimbraAuthToken(req);
            if (authToken == null) {
                // lookup the account corresponding to the open-id
                Account acct = prov.getAccountByForeignPrincipal("openid:" + openId);
                if (acct == null) {
                    throw new ServletException("No user account found corresponding to OpenID " + openId);
                }

                // add a zimbra cookie to the response
                authToken = AuthProvider.getAuthToken(acct);
                authToken.encode(resp, false, req.getScheme().equals("https"));

                // redirect to the correct URL
                Server server = acct.getServer();
                if (server == null) {
                    throw new ServletException("Server not found corresponding to account " + acct.getName());
                }
                resp.sendRedirect(server.getMailURL());
            } else {
                // user already has a valid login session => this request is for
                // "linking" open-id with the user account
                Account acct = prov.getAccountById(authToken.getAccountId());

                // check whether OP Endpoint URL is an allowed one
                Domain domain = prov.getDomain(acct);
                String[] allowedOPURLs = domain.getOpenidConsumerAllowedOPEndpointURL();
                if (allowedOPURLs == null)
                    throw new ServletException("There are no allowed OP Endpoint URLs");
                if (!Arrays.asList(allowedOPURLs).contains(discovered.getOPEndpoint().toString()))
                    throw new ServletException("OP Endpoint URL " + discovered.getOPEndpoint() + " is not allowed");

                acct.addForeignPrincipal("openid:" + openId);
                resp.getOutputStream().print("Success");
            }
        } catch (ServiceException e) {
            ZimbraLog.extensions.warn("Unexpected error after having verified OpenID authentication response", e);
            throw new ServletException(e.getMessage());
        }
    }

    private void authRequest(String userSuppliedId, HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        try {
            // configure the return_to URL where your application will receive
            // the authentication responses from the OpenID provider
            String returnToUrl = req.getRequestURL().toString() + "?is_return=true";

            // perform discovery on the user-supplied identifier
            List discoveries = manager.discover(userSuppliedId);

            // if nothing was discovered then assume userSuppliedId to be the OP
            // endpoint URL
            if (discoveries.isEmpty()) {
                ZimbraLog.extensions.debug("No OP endpoints discovered");
                try {
                    discoveries.add(new DiscoveryInformation(new URL(userSuppliedId)));
                } catch (MalformedURLException e) {
                    throw new ServletException("Expected supplied identifier to be OP Endpoint URL");
                }
            }

            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            DiscoveryInformation discovered = manager.associate(discoveries);
            if (discovered == null) {
                throw new ServletException("No OP endpoints discovered");
            }

            // store the discovery information in a cookie
            Cookie cookie = new Cookie(COOKIE_ZM_OPENID_DISCOVERY_INFO, serialize(discovered));
            cookie.setPath("/");
            resp.addCookie(cookie);

            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

            if (!discovered.isVersion2()) {
                // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
                // The only method supported in OpenID 1.x
                // redirect-URL usually limited ~2048 bytes
                resp.sendRedirect(authReq.getDestinationUrl(true));
            } else {
                // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)
                req.setAttribute("message", authReq);
                HttpServlet servlet = ZimbraServlet.getServlet("ExtensionDispatcherServlet");
                servlet.getServletContext().getContext("/zimbra").getRequestDispatcher("/public/formredirection.jsp")
                        .forward(req, resp);
            }
        } catch (OpenIDException e) {
            ZimbraLog.extensions.debug("OpenID error code %s", e.getErrorCode(), e);
            throw new ServletException(e.getMessage());
        } catch (ServiceException e) {
            throw new ServletException(e.getMessage());
        }
    }

    /**
     * Returns a valid auth token if the request contains one.
     *
     * @param req
     * @return
     */
    private static AuthToken getZimbraAuthToken(HttpServletRequest req) {
        String encodedToken = getCookieValue(req, ZimbraCookie.COOKIE_ZM_AUTH_TOKEN);
        if (encodedToken == null)
            return null;
        AuthToken authToken;
        try {
            authToken = AuthProvider.getAuthToken(encodedToken);
        } catch (AuthTokenException e) {
            // invalid token, no problem
            return null;
        }
        return authToken.isExpired() ? null : authToken;
    }

    private static String getCookieValue(HttpServletRequest req, String cookieName) {
        Cookie cookies[] = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName))
                    return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public String getPath() {
        return "/openid/consumer";
    }

    public static String serialize(DiscoveryInformation discInfo) throws IOException, ServiceException {
        JSONObject jsonSerialized = new JSONObject();
        try {
            // serialize identifier into JSON
            Identifier claimedID = discInfo.getClaimedIdentifier();
            if (claimedID != null) {
                String claimedIdentifierString = claimedID.getIdentifier();
                if (claimedIdentifierString != null) {
                    JSONObject claimedIDJSON = new JSONObject();
                    claimedIDJSON.put("identifier", claimedIdentifierString);
                    if (claimedID instanceof XriIdentifier) {
                        // need identifier, iriNormalForm and uriNormalForm
                        claimedIDJSON.put("idType", "XRI");
                        claimedIDJSON.put("iriNormalForm", ((XriIdentifier) claimedID).toIRINormalForm());
                        claimedIDJSON.put("uriNormalForm", ((XriIdentifier) claimedID).toURINormalForm());
                    } else {
                        // need only string value
                        claimedIDJSON.put("idType", "URL");
                    }
                    jsonSerialized.put("claimedID", claimedIDJSON);
                }
            }
            URL opEndpoint = discInfo.getOPEndpoint();
            if (opEndpoint != null) {
                jsonSerialized.put("opEndpoint", opEndpoint.toString());
            }
            jsonSerialized.put("hasDelegateIdentifier", discInfo.hasDelegateIdentifier());
            if (discInfo.hasDelegateIdentifier()) {
                jsonSerialized.put("delegateIdentifier", discInfo.getDelegateIdentifier());
            }
            jsonSerialized.put("version", discInfo.getVersion());
            @SuppressWarnings("rawtypes")
            Set discoveryTypes = discInfo.getTypes();
            JSONArray discoveryTypesJSON = new JSONArray();
            for (Object type : discoveryTypes) {
                discoveryTypesJSON.put(type);
            }
            jsonSerialized.put("discoveryTypes", discoveryTypesJSON);
            ZimbraLog.extensions.debug("Serialized open id %s", jsonSerialized.toString());
        } catch (JSONException e) {
            ZimbraLog.extensions.error("Failed to serialize DiscoveryInformation %s", e.getMessage(), e);
            throw ServiceException.FAILURE(null, e);
        }
        return Base64.encodeBase64String(jsonSerialized.toString().getBytes());
    }

    public static DiscoveryInformation deserializeDiscoveryInfo(String serializedObj) throws ServletException,
            IOException, ServiceException {
        String jsonString = new String(Base64.decodeBase64(serializedObj));
        ZimbraLog.extensions.debug("Deserializing open id %s", jsonString);
        try {
            JSONObject jsonSerialized = new JSONObject(jsonString);
            Identifier claimedID = null;
            if (jsonSerialized.has("claimedID")) {
                JSONObject claimedIDJSON = jsonSerialized.getJSONObject("claimedID");
                String idType = claimedIDJSON.getString("idType");
                if ("URL".equalsIgnoreCase(idType)) {
                    String identifier = claimedIDJSON.getString("identifier");
                    claimedID = new UrlIdentifier(identifier);
                } else if ("XRI".equalsIgnoreCase(idType)) {
                    String identifier = claimedIDJSON.getString("identifier");
                    String iriNormalForm = claimedIDJSON.getString("iriNormalForm");
                    String uriNormalForm = claimedIDJSON.getString("uriNormalForm");
                    claimedID = new XriIdentifier(identifier, iriNormalForm, uriNormalForm);
                } else {
                    throw ServiceException
                            .INVALID_REQUEST("Failed to deserialize cookie. Wrong idType " + idType, null);
                }
            }

            String opEndpointString = jsonSerialized.getString("opEndpoint");
            boolean hasDelegateIdentifier = jsonSerialized.getBoolean("hasDelegateIdentifier");
            String delegateIdentifier = null;
            if (hasDelegateIdentifier) {
                delegateIdentifier = jsonSerialized.getString("delegateIdentifier");
            }
            String version = jsonSerialized.getString("version");
            JSONArray discoveryTypesJSON = jsonSerialized.getJSONArray("discoveryTypes");
            int numTypes = discoveryTypesJSON.length();
            Set discoveryTypes = new HashSet();
            for (int i = 0; i < numTypes; i++) {
                discoveryTypes.add(discoveryTypesJSON.get(i));
            }
            DiscoveryInformation info = new DiscoveryInformation(new URL(opEndpointString), claimedID,
                    delegateIdentifier, version, discoveryTypes);
            return info;
        } catch (JSONException | DiscoveryException e) {
            ZimbraLog.extensions.error("Failed to deserialize cookie %s", e.getMessage(), e);
            throw ServiceException.FAILURE(null, e);
        }
    }
}
