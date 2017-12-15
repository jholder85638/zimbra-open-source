/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.offline.util.yab;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.List;
import java.util.ArrayList;

public class AddRequest extends Request {
    private final List<Contact> contacts;

    public static final String ACTION = "addContacts";
    public static final String TAG = "add-request";
    
    public AddRequest(Session session) {
        super(session);
        contacts = new ArrayList<Contact>();
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }

    @Override
    protected String getAction() {
        return ACTION;
    }

    @Override
    public Element toXml(Document doc) {
        if (contacts.size() == 0) {
            throw new IllegalStateException(
                "AddRequest must contain at least one contact");
        }
        Element e = doc.createElement(TAG);
        for (Contact contact : contacts) {
            e.appendChild(contact.toXml(doc));
        }
        return e;
    }

    @Override
    protected Response parseResponse(Document doc) {
        return AddResponse.fromXml(doc.getDocumentElement());
    }
}
