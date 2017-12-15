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
package com.zimbra.cs.mailbox;

import com.zimbra.cs.account.Account;
import com.zimbra.soap.ZimbraSoapContext;

public class OfflineGalContactAutoComplete extends ContactAutoComplete {

    public OfflineGalContactAutoComplete(Account acct, OperationContext octxt) {
        super(acct, octxt);
    }

    public OfflineGalContactAutoComplete(Account acct, ZimbraSoapContext zsc,
                    OperationContext octxt) {
        super(acct, zsc, octxt);
    }

    @Override
    protected void addEntry(ContactEntry entry, AutoCompleteResult result) {
        if (entry.isGroup() && result.entries.contains(entry)) {
            //duplicate non-group added; addEntry rejects duplicates so we need to manually set the flag
            //this occurs because GAL search in ZD uses mailbox search; there can be multiple entries for one addr
            //for example corporate GAL has server-team@zimbra.com as type=account and Zimbra GAL has server-team@zimbra.com as type=group
            for (ContactEntry exist : result.entries) {
                if (entry.getKey().equals(exist.getKey()) && !exist.isGroup()) {
                    exist.setIsGalGroup(true);
                }
            }
        } else {
            result.addEntry(entry);
        }
    }
}
