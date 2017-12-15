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

/**
 * Represents a Contact field change.
 */
public class FieldChange extends Entity {
    private final Type type;
    private final Field field;
    private final int fid;

    public static enum Type {
        ADD, UPDATE, REMOVE
    }

    public static FieldChange add(Field field) {
        return new FieldChange(Type.ADD, field, -1);
    }

    public static FieldChange remove(int fid) {
        return new FieldChange(Type.REMOVE, null, fid);
    }

    public static FieldChange update(Field field) {
        return new FieldChange(Type.UPDATE, field, field.getId());
    }
    
    private FieldChange(Type type, Field field, int fid) {
        this.type = type;
        this.field = field;
        this.fid = fid;
    }

    public Type getType() {
        return type;
    }

    public Field getField() {
        return field;
    }

    public int getFieldId() {
        return fid;
    }

    @Override
    public Element toXml(Document doc) {
        switch (type) {
        case ADD:
            return field.toXml(doc, "add-" + field.getName());
        case UPDATE:
            return field.toXml(doc, "update-" + field.getName());
        case REMOVE:
            Element e = doc.createElement("remove-field");
            e.setAttribute(Field.FID, String.valueOf(fid));
            return e;
        }
        return null;
    }
}
