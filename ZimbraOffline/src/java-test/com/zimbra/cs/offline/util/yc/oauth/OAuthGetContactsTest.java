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

import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;

public class OAuthGetContactsTest {

    @Test
    public void getContacts() {
        try {
            OAuthRequest req = new OAuthGetRequestTokenRequest(new OAuthToken());
            String resp = req.send();
            OAuthResponse response = new OAuthGetRequestTokenResponse(resp);
            System.out.println("paste it into browser and input the highlighted codes below: "
                    + response.getToken().getNextUrl());

            System.out.print("Verifier: ");
            Scanner scan = new Scanner(System.in);
            String verifier = scan.nextLine();
            req = new OAuthGetTokenRequest(response.getToken(), verifier);
            resp = req.send();
            response = new OAuthGetTokenResponse(resp);

            // use 0 for get all contacts, use other revision number (< server
            // rev) to retrieve the delta part
            req = new OAuthGetContactsRequest(response.getToken(), 0);
            resp = req.send();

            System.out.println(resp);
            Assert.assertNotNull(resp);

            response = new OAuthGetContactsResponse(resp);
        } catch (Exception e) {
            Assert.fail();
        }
    }
}
