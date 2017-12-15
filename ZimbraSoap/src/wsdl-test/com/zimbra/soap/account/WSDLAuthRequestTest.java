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
package com.zimbra.soap.account;

import generated.zcsclient.account.testAuthRequest;
import generated.zcsclient.account.testAuthResponse;
import generated.zcsclient.ws.service.ZcsPortType;
import generated.zcsclient.zm.testAccountBy;
import generated.zcsclient.zm.testAccountSelector;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.soap.Utility;

public class WSDLAuthRequestTest {

    private static ZcsPortType eif;

    @BeforeClass
    public static void init() throws Exception {
        eif = Utility.getZcsSvcEIF();
    }

    /**
     * Current assumption : user1 exists with password test123
     */
    @Test
    public void simple() throws Exception {
        testAuthRequest authReq = new testAuthRequest();
        testAccountSelector acct = new testAccountSelector();
        acct.setBy(testAccountBy.NAME);
        acct.setValue("user1");
        authReq.setAccount(acct);
        authReq.setPassword("test123");
        authReq.setPreauth(null);
        authReq.setAuthToken(null);
        testAuthResponse authResponse = eif.authRequest(authReq);
        Assert.assertNotNull("authResponse object", authResponse);
        String authToken = authResponse.getAuthToken();
        Assert.assertNotNull("authToken", authToken);
        Assert.assertTrue(String.format("AuthToken length %d should be greater than 10",  authToken.length()),
                authToken.length() > 10);
        long lifetime = authResponse.getLifetime();
        Assert.assertTrue(String.format("lifetime %d should be > 0", lifetime), lifetime > 0);
        Assert.assertNull("refer should be null", authResponse.getRefer());
        Assert.assertEquals("Skin name", "harmony", authResponse.getSkin());  // If the default changes, this might change too?
    }

    /**
     * Current assumption : user1 exists with password test123
     */
    @Test
    public void badPasswd() throws Exception {
        try {
            Utility.getAccountServiceAuthToken("user1", "BAD-PASSWORD");
            Assert.fail("Should have had a fault resulting in an exception being thrown");
        } catch (SOAPFaultException sfe) {
            Assert.assertTrue(sfe.getMessage().startsWith("authentication failed for "));
        }
    }
}
