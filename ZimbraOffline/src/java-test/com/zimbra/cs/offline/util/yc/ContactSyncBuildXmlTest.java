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

import javax.xml.parsers.DocumentBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.zimbra.cs.offline.util.Xml;

public class ContactSyncBuildXmlTest {

    @Test
    public void testBuildXml() {
        DocumentBuilder builder = Xml.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("contactsync");
        try {
            InputStream stream = this.getClass().getClassLoader().getResourceAsStream("yahoo_contacts_server_add.xml");
            Document contactDoc = builder.parse(stream);
            Element contactRoot = contactDoc.getDocumentElement();
            Assert.assertEquals("contactsync", contactRoot.getNodeName());

            Contact contact = new Contact();
            contact.extractFromXml(Xml.getChildren(contactRoot).get(1));

            Element contactEle = contact.toXml(root.getOwnerDocument());
            root.appendChild(contactEle);

            System.out.println(Xml.toString(root));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
