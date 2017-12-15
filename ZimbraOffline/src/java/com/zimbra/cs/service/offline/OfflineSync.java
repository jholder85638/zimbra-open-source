/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014 Zimbra, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.service.offline;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.offline.DirectorySync;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.SyncMailbox;
import com.zimbra.cs.mailbox.OfflineMailboxManager;
import com.zimbra.cs.mailbox.OfflineServiceException;
import com.zimbra.cs.mailbox.ZcsMailbox;
import com.zimbra.cs.offline.OfflineLog;
import com.zimbra.cs.offline.OfflineSyncManager;
import com.zimbra.cs.offline.common.OfflineConstants;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;

public class OfflineSync extends DocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        MailboxManager mmgr = MailboxManager.getInstance();
        if (!(mmgr instanceof OfflineMailboxManager))
            throw OfflineServiceException.MISCONFIGURED("incorrect mailbox manager class: " + mmgr.getClass().getSimpleName());

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        
        boolean isDebugTraceOn = request.getAttributeBool("debug", false);
        if (OfflineSyncManager.getInstance().isConnectionDown()) {
            OfflineSyncManager.getInstance().setConnectionDown(false);
        }
        if (OfflineSyncManager.getInstance().isUILoading()) {
            //previously missed an update from UI; no way user can press send/receive while UI is loading
            OfflineSyncManager.getInstance().setUILoading(false);
        }
        try {
            if (mbox instanceof SyncMailbox)
            	((SyncMailbox)mbox).sync(true, isDebugTraceOn);
            
            if (mbox instanceof ZcsMailbox) {
                DirectorySync.getInstance().sync(mbox.getAccount(), true);
            }
        } catch (ServiceException se) {
            if (ServiceException.INTERRUPTED.equals(se.getCode())) {
                OfflineLog.offline.debug("Sync interrupted. Network status changed to down during sync");
            } else {
                throw se;
            }
        }
        return zsc.createElement(OfflineConstants.SYNC_RESPONSE);
    }
}
