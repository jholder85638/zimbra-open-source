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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.zimbra.cs.offline.util.Xml;

public class SyncResponse extends Response {

    private static DocumentBuilder docBuilder = Xml.newDocumentBuilder();

    public SyncResponse(int retCode, String resp) {
        super(retCode, resp);
    }

    public boolean extract(ContactSync cont) throws YContactException {
        Document doc;
        Element root;

        try {
            doc = docBuilder.parse(new InputSource(new StringReader(getResp())));
            root = doc.getDocumentElement();
        } catch (Exception e) {
            throw new YContactException("parsing response error", "", false, e, null);
        }

        int yahooRev = Integer.parseInt(root.getAttribute("yahoo:rev"));
        if (yahooRev == cont.getClientRev()) {
            return false;
        }
        cont.setYahooRev(yahooRev);
        cont.setClientRev(Integer.parseInt(root.getAttribute("yahoo:clientrev")));

        // parse contacts
        List<Element> children = Xml.getChildren(root);
        List<Contact> contacts = new ArrayList<Contact>();
        for (Element e : children) {
            if (Contact.TAG_NAME.equals(e.getNodeName())) {
                com.zimbra.cs.offline.util.yc.Contact contact = new com.zimbra.cs.offline.util.yc.Contact();
                contact.extractFromXml(e);
                contacts.add(contact);
            }
        }
        if (!contacts.isEmpty()) {
            cont.setContacts(contacts);
        }
        return true;
    }

}
