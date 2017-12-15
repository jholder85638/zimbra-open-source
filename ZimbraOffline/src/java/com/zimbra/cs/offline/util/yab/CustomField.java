/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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

public class CustomField extends SimpleField {
    private String title;

    private static final String TITLE = "title";
    
    public static CustomField custom(String title, String value) {
        return new CustomField(title, value);
    }

    public CustomField() {
        super(CUSTOM);
    }
    
    public CustomField(String title, String value) {
        super(CUSTOM, value);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public Element toXml(Document doc, String tag) {
        Element e = super.toXml(doc, tag);
        e.setAttribute(TITLE, title);
        return e;
    }

    @Override
    protected void parseXml(Element e) {
        super.parseXml(e);
        title = e.getAttribute(TITLE);
    }
}
