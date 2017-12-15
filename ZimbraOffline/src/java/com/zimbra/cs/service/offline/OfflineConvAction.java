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
package com.zimbra.cs.service.offline;

import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.ZcsMailbox;
import com.zimbra.cs.mailbox.OfflineServiceException;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.mail.ConvAction;
import com.zimbra.cs.service.mail.ItemAction;
import com.zimbra.cs.account.offline.OfflineProvisioning;
import com.zimbra.cs.account.offline.OfflineAccount;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.ZimbraSoapContext;

public class OfflineConvAction extends ConvAction {
    
    private static String OFFLINECONV_MBOX = "OfflineConv_MBox";
    private static String OFFLINECONV_TOACCT = "OfflineConv_ToAcct";
    
    @Override
    public void preProxy(Element request, Map<String, Object> context) throws ServiceException {
        Element act = request.getOptionalElement(MailConstants.E_ACTION);
        String op;
        if (act == null || (op = act.getAttribute(MailConstants.A_OPERATION, null)) == null ||
            (!op.equals(ItemAction.OP_MOVE) && !op.equals(ItemAction.OP_COPY)))
            return;

        String id;
        int pos;
        OfflineProvisioning prov = OfflineProvisioning.getOfflineInstance();
        if ((id = act.getAttribute(MailConstants.A_ID, null)) == null || (pos = id.indexOf(":")) <= 0)
            return;
        String fromAcctId = id.substring(0, pos);
        Account fromAcct = prov.get(Key.AccountBy.id, fromAcctId);
        if (fromAcct == null || !prov.isMountpointAccount(fromAcct)) // source is not a mountpoint...
            return;
        
        id = act.getAttribute(MailConstants.A_FOLDER, null);
        if (id == null || (pos = id.indexOf(":")) <= 0)
            return;
        String toAcctId = id.substring(0, pos);
        int folderId = Integer.parseInt(id.substring(pos + 1));
        OfflineAccount toAcct = (OfflineAccount)prov.get(Key.AccountBy.id, toAcctId);
        if (toAcct == null || !prov.isZcsAccount(toAcct))
            return;
        context.put(OFFLINECONV_TOACCT, toAcct);
                      
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(toAcct);
        if (!(mbox instanceof ZcsMailbox))
            throw OfflineServiceException.MISCONFIGURED("incorrect mailbox class: " + mbox.getClass().getSimpleName());
        ZcsMailbox ombx = (ZcsMailbox)mbox;
        context.put(OFFLINECONV_MBOX, ombx);
        
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        OperationContext octxt = getOperationContext(zsc, context);
        Folder folder = ombx.getFolderById(octxt, folderId);
        if (ombx.pushNewFolder(octxt, folderId, false, zsc)) {
            String newId = Integer.toString(ombx.getFolderByName(octxt, folder.getParentId(), folder.getName()).getId());
            act.addAttribute(MailConstants.A_FOLDER, toAcctId + ":" + newId);
        }
    }
    
    @Override
    public void postProxy(Element request, Element response, Map<String, Object> context) throws ServiceException {
        ZcsMailbox ombx = (ZcsMailbox)context.get(OFFLINECONV_MBOX);
        if (ombx == null)
            return;
        ombx.sync(true, ((OfflineAccount)context.get(OFFLINECONV_TOACCT)).isDebugTraceEnabled());
    }
}
