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

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;

import com.zimbra.common.service.ServiceException;

public class OAuthGetRequestTokenTest {

    static {
        BasicConfigurator.configure();
    }

    @Test
    public void getRequestTokenTest() {
        OAuthRequest req = new OAuthGetRequestTokenRequest(new OAuthToken());
        String respStr = null;
        try {
            respStr = req.send();
        } catch (Exception e) {
            Assert.fail("OAuth get request token failed");
        }
        Assert.assertNotNull("failed to get response", respStr);
        OAuthResponse resp = null;
        try {
            resp = new OAuthGetRequestTokenResponse(respStr);
        } catch (ServiceException e) {
            Assert.fail("OAuth create response failed");
        }
        Assert.assertNotNull("failed to create OAuthResponse", resp);
        Assert.assertNotNull(resp.getToken().getToken());
        Assert.assertNotNull(resp.getToken().getTokenSecret());
    }

    public static void main(String[] args) {
        OAuthGetRequestTokenTest test = new OAuthGetRequestTokenTest();
        test.getRequestTokenTest();
    }
}
