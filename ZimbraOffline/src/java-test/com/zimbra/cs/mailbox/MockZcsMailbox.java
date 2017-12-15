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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailItem.UnderlyingData;

public class MockZcsMailbox extends ZcsMailbox {
    
    private Account account;
    private Map<String, Metadata> metadata = new HashMap<String, Metadata>();
    
    private Map<String, Tag> tagsByName = new HashMap<String, Tag>();

    MockZcsMailbox(Account account, MailboxData data) throws ServiceException {
        super(data);
        this.account = account;
    }

    @Override
    public Metadata getConfig(OperationContext octxt, String section) {
        return metadata.get(section);
    }

    @Override
    public void setConfig(OperationContext octxt, String section,
            Metadata config) {
        metadata.put(section, config);
    }

    @Override
    public Account getAccount() throws ServiceException {
        if (account == null) {
            return super.getAccount();
        } else {
            return account;
        }
    }

    @Override
    public String getAccountId() {
        if (account == null) {
            return super.getAccountId();
        } else {
            return account.getId();
        }
    }

    @Override
    public synchronized List<Tag> getTagList(OperationContext octxt)
                    throws ServiceException {
        return new ArrayList<Tag>();

    }
    
    @Override
    public synchronized Tag getTagByName(String name) throws ServiceException {
        return tagsByName.get(name);
    }
    
    public void addStubTag(String name, Integer id) throws ServiceException {
        UnderlyingData data = new UnderlyingData();
        data.id          = id;
        data.type        = MailItem.Type.TAG.toByte();
        data.name        = name;
//        data.subject     = name;
        Tag tag = new Tag(this, data);
        tagsByName.put(name, tag);
    }
}
