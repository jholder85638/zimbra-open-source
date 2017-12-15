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

public class ContactChange {
    private int cid;
    private final List<FieldChange> fieldChanges;
    private final List<CategoryChange> categoryChanges;

    private static final String CID = "cid";

    public ContactChange() {
        fieldChanges = new ArrayList<FieldChange>();
        categoryChanges = new ArrayList<CategoryChange>();
    }

    public void setId(int cid) {
        this.cid = cid;
    }
    
    public int getId() {
        return cid;
    }
    
    public void addFieldChange(FieldChange change) {
        fieldChanges.add(change);
    }

    public List<FieldChange> getFieldChanges() {
        return fieldChanges;
    }

    public void addCategoryChange(CategoryChange change) {
        categoryChanges.add(change);
    }

    public List<CategoryChange> getCategoryChanges() {
        return categoryChanges;
    }

    public boolean isEmpty() {
        return fieldChanges.isEmpty() && categoryChanges.isEmpty();
    }

    public Element toXml(Document doc, String tag) {
        Element e = doc.createElement(tag);
        if (cid != -1) {
            e.setAttribute(CID, String.valueOf(cid));
        }
        for (FieldChange change : fieldChanges) {
            e.appendChild(change.toXml(doc));
        }
        for (CategoryChange change : categoryChanges) {
            e.appendChild(change.toXml(doc));
        }
        return e;
    }
}
