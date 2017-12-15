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
package com.zimbra.cs.offline.util.yc;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.zimbra.cs.offline.util.Xml;
import com.zimbra.cs.offline.util.yc.oauth.OAuthGetRequestTokenRequest;
import com.zimbra.cs.offline.util.yc.oauth.OAuthGetRequestTokenResponse;
import com.zimbra.cs.offline.util.yc.oauth.OAuthGetTokenRequest;
import com.zimbra.cs.offline.util.yc.oauth.OAuthGetTokenResponse;
import com.zimbra.cs.offline.util.yc.oauth.OAuthHelper;
import com.zimbra.cs.offline.util.yc.oauth.OAuthPutContactRequest;
import com.zimbra.cs.offline.util.yc.oauth.OAuthRequest;
import com.zimbra.cs.offline.util.yc.oauth.OAuthResponse;
import com.zimbra.cs.offline.util.yc.oauth.OAuthToken;

public class AddContactTest {

    @Test
    public void testAddContactWithDiff() throws Exception {
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
        OAuthToken token = response.getToken();

        try {
            InputStream stream = this.getClass().getClassLoader()
                    .getResourceAsStream("yahoo_contacts_client_add_dummy.xml");
            String content = OAuthHelper.getStreamContents(stream);

            req = new OAuthPutContactRequest(token, content);
            resp = req.send();
            System.out.println("resp:" + resp);
            DocumentBuilder builder = Xml.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(resp)));
            Element syncResult = doc.getDocumentElement();
            int rev = Xml.getIntAttribute(syncResult, "yahoo:rev");
            System.out.println("rev: " + rev);
            Element result = Xml.getChildren(syncResult).get(0);
            Element contacts = Xml.getChildren(result).get(0);
            boolean success = false;
            for (Element child : Xml.getChildren(contacts)) {
                if ("response".equals(child.getNodeName())) {
                    Assert.assertEquals("success", child.getTextContent());
                    success = true;
                }
                if ("id".equals(child.getNodeName())) {
                    String id = child.getTextContent();
                    System.out.println("get new contact id: " + id);
                }
            }
            if (!success) {
                Assert.fail();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
