/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010, 2011, 2012, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.session;

import java.util.Iterator;
import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.offline.OfflineSyncManager;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;

public class OfflineSoapSession extends SoapSession {

    public OfflineSoapSession(ZimbraSoapContext zsc) {
        super(zsc);
    }

    @Override
    public void addRemoteNotifications(RemoteNotifications rns) {
        removeUnqualifiedRemoteNotifications(rns);
        super.addRemoteNotifications(rns);
    }

    private void removeUnqualifiedRemoteNotifications(RemoteNotifications rns) {
        if (rns == null || rns.count == 0) {
            return;
        }
        removeUnqualifiedRemoteNotifications(rns.created);
        removeUnqualifiedRemoteNotifications(rns.modified);
    }

    private void removeUnqualifiedRemoteNotifications(List<Element> notifs) {
        if (notifs == null) {
            return;
        }
        //we don't want to add acct id, just want to see if formatting *requires* id to be added
        ItemIdFormatter ifmt = new ItemIdFormatter("any","any", false);
        Iterator<Element> it = notifs.iterator();
        while (it.hasNext()) {
            Element elt = it.next();
            String itemIdStr = null;
            try {
                itemIdStr = elt.getAttribute(A_ID);
            } catch (ServiceException se) {
                continue;
            }
            ItemId item = null;
            try {
                item = new ItemId(itemIdStr,ifmt.getAuthenticatedId());
            } catch (ServiceException e) {
                continue;
            }
            if (item != null && !item.toString().equals(itemIdStr)) {
                it.remove();
            }
        }
    }

    @Override
    public RegisterNotificationResult registerNotificationConnection(final PushChannel sc) throws ServiceException {
        RegisterNotificationResult result = super.registerNotificationConnection(sc);
        if (result == RegisterNotificationResult.BLOCKING && OfflineSyncManager.getInstance().hasPendingStatusChanges()) {
            //if any pending sync state changes make it DATA_READY
            result = RegisterNotificationResult.DATA_READY;
        }
        return result;
    }
}
