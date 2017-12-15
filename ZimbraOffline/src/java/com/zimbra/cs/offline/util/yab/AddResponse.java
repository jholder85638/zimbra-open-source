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

import com.zimbra.cs.offline.util.Xml;

public class AddResponse extends Response {
    private final List<Result> results;

    private static final String TAG = "add-response";

    private AddResponse() {
        results = new ArrayList<Result>();
    }

    public List<Result> getResults() {
        return results;
    }

    public static AddResponse fromXml(Element e) {
        return new AddResponse().parseXml(e);
    }

    private AddResponse parseXml(Element e) {
        if (!e.getTagName().equals(TAG)) {
            throw new IllegalArgumentException(
                "Not an '" + TAG + "' element: " + e.getTagName());
        }
        for (Element child : Xml.getChildren(e)) {
            String tag = child.getTagName();
            if (tag.equals(ContactResult.TAG)) {
                results.add(ContactResult.fromXml(child));
            } else if (tag.equals(ErrorResult.TAG)) {
                results.add(ErrorResult.fromXml(child));
            } else {
                throw new IllegalArgumentException(
                    "Unexpected result element: " + tag);
            }
        }
        if (results.isEmpty()) {
            throw new IllegalArgumentException(
                "Expected at least one result element");
        }
        return this;
    }

    @Override
    public Element toXml(Document doc) {
        Element e = doc.createElement(TAG);
        for (Result result : results) {
            e.appendChild(result.toXml(doc));
        }
        return e;
    }
}
