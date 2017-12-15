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

/**
 * Category reference by name or id.
 */
public final class Category extends Entity {
    private String name;
    private int id = -1;

    public static final String TAG = "category";
    
    private static final String CATID = "catid";

    public Category() {}

    public Category(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public Category(String name) {
        this.name = name;
    }

    public Category(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    public static Category fromXml(Element e) {
        Category cat = new Category();
        cat.parseXml(e);
        return cat;
    }

    @Override
    public Element toXml(Document doc) {
        return toXml(doc, TAG);
    }
    
    public Element toXml(Document doc, String tag) {
        Element e = doc.createElement(tag);
        if (id != -1) {
            e.setAttribute(CATID, String.valueOf(id));
        }
        if (name != null) {
            Xml.appendText(e, name);
        }
        return e;
    }

    private void parseXml(Element e) {
        id = Xml.getIntAttribute(e, CATID);
        name = Xml.getTextValue(e);
    }
}
