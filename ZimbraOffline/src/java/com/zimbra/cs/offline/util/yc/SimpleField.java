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

import org.w3c.dom.Element;

import com.zimbra.cs.offline.util.yc.Fields.Type;

public class SimpleField extends FieldValue {
    private String value;
    private Fields.Type type;
    
    public SimpleField(Type type, String value) {
        this.type = type;
        this.value = value;
    }
    
    public SimpleField(String type) {
        if (Fields.NICKNAME.equals(type)) {
            this.type = Fields.Type.nickname;
        } else if (Fields.EMAIL.equals(type)) {
            this.type = Fields.Type.email;
        } else if (Fields.YAHOOID.equals(type)) {
            this.type = Fields.Type.yahooid;
        } else if (Fields.PHONE.equals(type)) {
            this.type = Fields.Type.phone;
        } else if (Fields.COMPANY.equals(type)) {
            this.type = Fields.Type.company;
        } else if (Fields.GUID.equals(type)) {
            this.type = Fields.Type.guid;
        } else if (Fields.JOBTITLE.equalsIgnoreCase(type)) {
            this.type = Fields.Type.jobTitle;
        } else if (Fields.LINK.equals(type)) {
            this.type = Fields.Type.link;
        } else if (Fields.NOTES.equals(type)) {
            this.type = Fields.Type.notes;
        } else if (Fields.OTHERID.equals(type)) {
            this.type = Fields.Type.otherid;
        }else if (Fields.CUSTOM.equals(type)) {
            this.type = Fields.Type.custom;
        } else {
            throw new IllegalArgumentException("error, Yahoo contacts type " + type + " doesn't exist !");
        }
    }

    public String getValue() {
        return this.value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    protected void appendValues(Element parent) {
        parent.setTextContent(this.value);
    }

    @Override
    public void extractFromXml(Element parent) {
        this.value = parent.getTextContent();
    }
    @Override
    public Type getType() {
        return this.type;
    }
}
