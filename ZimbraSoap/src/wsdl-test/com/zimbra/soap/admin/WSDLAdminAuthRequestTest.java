/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010, 2011, 2012, 2013, 2014 Zimbra, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.soap.admin;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.xml.ws.developer.WSBindingProvider;

import generated.zcsclient.zm.testAccountBy;
import generated.zcsclient.zm.testAccountSelector;
import generated.zcsclient.admin.testAuthRequest;
import generated.zcsclient.admin.testAuthResponse;
import generated.zcsclient.admin.testDelegateAuthRequest;
import generated.zcsclient.admin.testDelegateAuthResponse;
import generated.zcsclient.ws.service.ZcsAdminPortType;

import com.zimbra.soap.Utility;

public class WSDLAdminAuthRequestTest {

    private static ZcsAdminPortType eif;

    @BeforeClass
    public static void init() throws Exception {
        Utility.setUpToAcceptAllHttpsServerCerts();
        eif =Utility.getAdminSvcEIF();
    }

    @Test
    public void simple() throws Exception {
        testAuthRequest authReq = new testAuthRequest();
        testAccountSelector acct = new testAccountSelector();
        acct.setBy(testAccountBy.NAME);
        acct.setValue("admin");
        authReq.setAccount(acct);
        authReq.setPassword("test123");
        authReq.setAuthToken(null);
        testAuthResponse authResponse = eif.authRequest(authReq);
        Assert.assertNotNull(authResponse);
        String authToken = authResponse.getAuthToken();
        Assert.assertNotNull(authToken);
        int len = authToken.length();
        Assert.assertTrue("authToken length should be at least 10 actual value=" + len, len >= 10);
        long lifetime = authResponse.getLifetime();
        Assert.assertTrue("lifetime value should be +ve", lifetime > 0);
    }

    @Test
    public void badPasswd() throws Exception {
        testAuthRequest authReq = new testAuthRequest();
        testAccountSelector acct = new testAccountSelector();
        acct.setBy(testAccountBy.NAME);
        acct.setValue("admin");
        authReq.setAccount(acct);
        authReq.setPassword("BAD-ONE");
        authReq.setAuthToken(null);
        // Invoke the methods.
        try {
            @SuppressWarnings("unused")
            testAuthResponse authResponse = eif.authRequest(authReq);
            Assert.fail("Should have had a fault resulting in an exception being thrown");
        } catch (SOAPFaultException sfe) {
            Assert.assertTrue(
                    sfe.getMessage() + "should start with <authentication failed for >",
                    sfe.getMessage().startsWith("authentication failed for "));
        }
    }

    @Test
    public void nonAdminUser() throws Exception {
        testAuthRequest authReq = new testAuthRequest();
        testAccountSelector acct = new testAccountSelector();
        acct.setBy(testAccountBy.NAME);
        acct.setValue("user1");
        authReq.setAccount(acct);
        authReq.setPassword("test123");
        authReq.setAuthToken(null);
        // Invoke the methods.
        try {
            @SuppressWarnings("unused")
            testAuthResponse authResponse = eif.authRequest(authReq);
            Assert.fail("Should have had a fault resulting in an exception being thrown");
        } catch (SOAPFaultException sfe) {
            Assert.assertEquals("SOAP fault message - ",
                    "permission denied: not an admin account",
                    sfe.getMessage());
        }
    }

    @Test
    public void delegateAuth() throws Exception {
        Utility.addSoapAdminAuthHeader((WSBindingProvider) eif);
        testDelegateAuthRequest delegateAuthReq = new testDelegateAuthRequest();
        testAccountSelector delegateAcct = new testAccountSelector();
        delegateAcct.setBy(testAccountBy.NAME);
        delegateAcct.setValue("admin");
        delegateAuthReq.setAccount(delegateAcct);
        testDelegateAuthResponse delegateAuthResp = eif.delegateAuthRequest(delegateAuthReq);
        Assert.assertNotNull(delegateAuthResp);
        String authToken = delegateAuthResp.getAuthToken();
        Assert.assertNotNull(authToken);
        int len = authToken.length();
        Assert.assertTrue("authToken length should be at least 10 actual value=" + len, len >= 10);
        long lifetime = delegateAuthResp.getLifetime();
        Assert.assertEquals("lifetime value", 0, lifetime);
    }
}
