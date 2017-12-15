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

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.zimbra.cs.mime.ParsedContact;

public class ContactRemove implements ContactOperation {

    private int itemId;
    private String remoteId;

    public ContactRemove(String contactId) {
        this.remoteId = contactId;
    }

    @Override
    public String getRemoteId() {
        return this.remoteId;
    }

    @Override
    public Action getOp() {
        return Action.REMOVE;
    }

    @Override
    public ParsedContact getParsedContact() {
        throw new UnsupportedOperationException("remove contact operation doesnt need parse contact");
    }

    @Override
    public boolean isPushOperation() {
        throw new UnsupportedOperationException("remove contact operation is push neutral");
    }

    @Override
    public Contact getYContact() {
        throw new UnsupportedOperationException("remove contact operation doesnt need ycontact");
    }

    @Override
    public int compareTo(ContactOperation contactOp) {
        return this.itemId - contactOp.getItemId();
    }

    @Override
    public String toString() {
        ToStringHelper helper = Objects.toStringHelper(this);
        return helper.add("remoteId", this.remoteId).add("itemId", this.itemId).add("Op", this.getOp().name())
                .toString();
    }

    @Override
    public void setItemId(int id) {
        this.itemId = id;
    }

    @Override
    public int getItemId() {
        return this.itemId;
    }
}
