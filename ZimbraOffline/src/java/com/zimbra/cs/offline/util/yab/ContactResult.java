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

public class ContactResult extends Result {
    private AddAction addAction;
    private Contact contact;

    public static final String TAG = "contact";

    private ContactResult() {}

    @Override
    public boolean isError() {
        return false;
    }
    
    public boolean isAdded() {
        return addAction == AddAction.ADD;
    }

    public boolean isMerged() {
        return addAction == AddAction.MERGE;
    }

    public Contact getContact() {
        return contact;
    }
    
    public static ContactResult fromXml(Element e) {
        return new ContactResult().parseXml(e);
    }

    private ContactResult parseXml(Element e) {
        assert e.getTagName().equals(TAG);
        addAction = AddAction.fromXml(e);
        if (addAction == null) {
            throw new IllegalArgumentException("Missing add-action element");
        }
        contact = Contact.fromXml(e);
        return this;
    }

    @Override
    public Element toXml(Document doc) {
        Element e = contact.toXml(doc, TAG);
        addAction.setAttribute(e);
        return e;
    }
}
