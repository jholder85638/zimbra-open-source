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
package com.zimbra.cs.offline.ab.yab;

import com.zimbra.cs.offline.ab.SyncException;

public class RemoteId {
    private final Type type;
    private final int value;

    private enum Type { CONTACT, CATEGORY }

    public static final String CONTACT_PREFIX = "contact:";
    public static final String CATEGORY_PREFIX = "category:";

    public static RemoteId contactId(int id) {
        return new RemoteId(Type.CONTACT, id);
    }

    public static RemoteId categoryId(int id) {
        return new RemoteId(Type.CATEGORY, id);
    }

    public static RemoteId parse(String s) throws SyncException {
        try {
            if (s.startsWith(CONTACT_PREFIX)) {
                String id = s.substring(CONTACT_PREFIX.length());
                return contactId(Integer.parseInt(id));
            } else if (s.startsWith(CATEGORY_PREFIX)) {
                String id = s.substring(CATEGORY_PREFIX.length());
                return categoryId(Integer.parseInt(id));
            }
        } catch (NumberFormatException e) {
        }
        throw new SyncException("Invalid ID syntax: " + s);
    }
    
    private RemoteId(Type type, int value) {
        assert value > 0 : "Invalid id value: " + value;
        this.type = type;
        this.value = value;
    }

    public int getId() { return value; }

    public boolean isContact() {
        return type == Type.CONTACT;
    }

    public boolean isCategory() {
        return type == Type.CATEGORY;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == RemoteId.class) {
            RemoteId rid = (RemoteId) obj;
            return type == rid.type && value == rid.value;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ Integer.valueOf(value).hashCode();
    }

    @Override
    public String toString() {
        switch (type) {
        case CONTACT:
            return CONTACT_PREFIX + String.valueOf(value);
        case CATEGORY:
            return CATEGORY_PREFIX + String.valueOf(value);
        default:
            throw new AssertionError();
        }
    }
}
