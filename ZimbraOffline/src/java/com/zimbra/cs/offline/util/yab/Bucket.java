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
import com.zimbra.cs.offline.util.Xml;

import java.util.List;

public class Bucket extends Entity {
    private int id;
    private int count;
    private Contact start;
    private Contact end;

    public static final String TAG = "bucket";

    private static final String ID = "id";
    private static final String CONTACT_COUNT = "contact-count";
    
    private Bucket() {}

    public int getId() { return id; }
    public int getContactCount() { return count; }
    public Contact getStartContact() { return start; }
    public Contact getEndContact() { return end; }

    public static Bucket fromXml(Element e) {
        return new Bucket().parseXml(e);
    }

    private Bucket parseXml(Element e) {
        if (!e.getTagName().equals(TAG)) {
            throw new IllegalArgumentException(
                "Not a '" + TAG + "' element: " + e.getTagName());
        }
        id = Xml.getIntAttribute(e, "id");
        count = Xml.getIntAttribute(e, "contact-count");
        List<Element> children = Xml.getChildren(e);
        if (children.size() != 2) {
            throw new IllegalArgumentException("Invalid '" + TAG + "' element");
        }
        start = Contact.fromXml(children.get(0));
        end = Contact.fromXml(children.get(1));
        return this;
    }

    @Override
    public Element toXml(Document doc) {
        Element e = doc.createElement(TAG);
        Xml.appendElement(e, ID, String.valueOf(id));
        Xml.appendElement(e, CONTACT_COUNT, String.valueOf(count));
        e.appendChild(start.toXml(doc));
        e.appendChild(end.toXml(doc));
        return e;
    }
}
