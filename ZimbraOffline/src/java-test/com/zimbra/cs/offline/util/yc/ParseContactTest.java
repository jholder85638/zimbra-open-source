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

public class ParseContactTest {

    @Test
    public void testAddContact() {
        try {
            InputStream stream = this.getClass().getClassLoader().getResourceAsStream("yahoo_contacts_server_add.xml");
            DocumentBuilder docBuilder = Xml.newDocumentBuilder();
            Document doc = docBuilder.parse(stream);
            Element root = doc.getDocumentElement();
            Assert.assertEquals("contactsync", root.getNodeName());

            Contact contact = new Contact();
            contact.extractFromXml(Xml.getChildren(root).get(1));
            Assert.assertEquals(13, contact.getAllFields().size());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testDeleteContact() {
        try {
            InputStream stream = this.getClass().getClassLoader()
                    .getResourceAsStream("yahoo_contacts_server_remove.xml");
            DocumentBuilder docBuilder = Xml.newDocumentBuilder();
            Document doc = docBuilder.parse(stream);
            Element root = doc.getDocumentElement();
            Assert.assertEquals("contactsync", root.getNodeName());

            Contact contact = new Contact();
            contact.extractFromXml(Xml.getChildren(root).get(1));
            Assert.assertEquals("remove", contact.getOp().name().toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateContact() {
        try {
            InputStream stream = this.getClass().getClassLoader()
                    .getResourceAsStream("yahoo_contacts_server_update.xml");
            DocumentBuilder docBuilder = Xml.newDocumentBuilder();
            Document doc = docBuilder.parse(stream);
            Element root = doc.getDocumentElement();
            Assert.assertEquals("contactsync", root.getNodeName());

            Contact contact = new Contact();
            contact.extractFromXml(Xml.getChildren(root).get(1));
            Assert.assertEquals("update", contact.getOp().name().toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testParseDbXml() {
        try {
            InputStream stream = this.getClass().getClassLoader().getResourceAsStream("yahoo_contacts_dummy_saved.xml");
            DocumentBuilder docBuilder = Xml.newDocumentBuilder();
            Document doc = docBuilder.parse(stream);
            Element root = doc.getDocumentElement();

            Contact contact = new Contact();
            contact.extractFromXml(root);
            Assert.assertEquals("add", contact.getOp().name().toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
