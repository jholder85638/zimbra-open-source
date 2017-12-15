/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2008, 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.offline.ab;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public class Change {
    public enum Type { ADD, UPDATE, DELETE }

    private final Type type;
    private final int itemId;

    public static Change add(int itemId) {
        return new Change(Type.ADD, itemId);
    }

    public static Change update(int itemId) {
        return new Change(Type.UPDATE, itemId);
    }

    public static Change delete(int itemId) {
        return new Change(Type.DELETE, itemId);
    }
    
    private Change(Type type, int itemId) {
        this.type = type;
        this.itemId = itemId;
    }

    public Type getType() { return type; }
    public int getItemId() { return itemId; }

    public boolean isAdd() { return type == Type.ADD; }
    public boolean isUpdate() { return type == Type.UPDATE; }
    public boolean isDelete() { return type == Type.DELETE; }

    @Override
    public String toString() {
        ToStringHelper helper = Objects.toStringHelper(this);
        return helper.add("operation", this.type.name())
            .add("itemId", this.itemId).toString();
    }
}
