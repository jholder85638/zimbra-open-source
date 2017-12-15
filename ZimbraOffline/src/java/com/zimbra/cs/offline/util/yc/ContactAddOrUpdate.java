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

public class ContactAddOrUpdate implements ContactOperation {

    private ParsedContact pc;
    private Contact ycontact;
    private int itemId = Integer.MAX_VALUE;
    private boolean isPush;

    public ContactAddOrUpdate(ParsedContact pc, Contact ycontact, boolean isPush) {
        this.pc = pc;
        this.ycontact = ycontact;
        this.isPush = isPush;
    }

    @Override
    public String getRemoteId() {
        return this.ycontact.getId();
    }

    @Override
    public Action getOp() {
        return this.ycontact.getOp();
    }

    @Override
    public ParsedContact getParsedContact() {
        return this.pc;
    }

    @Override
    public boolean isPushOperation() {
        return this.isPush;
    }

    @Override
    public Contact getYContact() {
        return this.ycontact;
    }

    @Override
    public int compareTo(ContactOperation contactOp) {
        return this.itemId - contactOp.getItemId();
    }

    @Override
    public String toString() {
        ToStringHelper helper = Objects.toStringHelper(this);
        return helper.add("remoteId", this.getRemoteId())
                .add("itemId", this.itemId)
                .add("Op", this.getOp().name())
                .add("isPush", this.isPushOperation()).toString();
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
