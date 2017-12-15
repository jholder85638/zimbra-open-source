/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.service.ServiceException;

public class OfflineProxyHelper {
    public static void uploadAttachments(Element request, String acctId) throws ServiceException {
        Element eAttach = request.getElement(MailConstants.E_MSG).getOptionalElement(MailConstants.E_ATTACH);
        if (eAttach != null) {
            String aid = eAttach.getAttribute(MailConstants.A_ATTACHMENT_ID, null);
            if (aid == null)
                return;
            
            String[] ids = aid.split(",");
            String newAid = "";
            for (String id : ids) {
                 String newId = OfflineDocumentHandlers.uploadOfflineDocument(id, acctId);
                 if (newAid.length() > 0)
                     newAid += ",";
                 newAid += newId;
            }
            eAttach.addAttribute(MailConstants.A_ATTACHMENT_ID, newAid);
        }
    }
}
