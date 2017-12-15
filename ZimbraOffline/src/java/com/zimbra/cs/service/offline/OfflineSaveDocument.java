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

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.offline.OfflineProvisioning;
import com.zimbra.cs.service.util.ItemId;

public class OfflineSaveDocument extends OfflineDocumentHandlers.SaveDocument {
    
    @Override
    protected Element proxyRequest(Element request, Map<String, Object> context, ItemId iidRequested, ItemId iidResolved)
        throws ServiceException {
        Element eUpload = request.getElement(MailConstants.E_DOC).getOptionalElement(MailConstants.E_UPLOAD);
        if (eUpload != null && OfflineProvisioning.getOfflineInstance().isMountpointAccount(iidResolved.getAccountId())) {
            String id = eUpload.getAttribute(MailConstants.A_ID);
            String acctId = iidRequested.getAccountId();       
            eUpload.addAttribute(MailConstants.A_ID, OfflineDocumentHandlers.uploadOfflineDocument(id, acctId));
        }
        return super.proxyRequest(request, context, iidRequested, iidResolved);
    }
}
