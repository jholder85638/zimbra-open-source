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
package com.zimbra.cs.service.offline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.service.mail.GetMiniCal;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.ZimbraSoapContext;

public class OfflineGetMiniCal extends GetMiniCal {
    
    @Override
    protected Element proxyIfNecessary(Element request,
                    Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account reqAcct = getRequestedAccount(zsc);
        List<ItemId> itemIds = new ArrayList<ItemId>();
        boolean needToProxy = false;
        for (Iterator<Element> foldersIter = request.elementIterator(MailConstants.E_FOLDER); foldersIter.hasNext(); ) {
            Element fElem = foldersIter.next();
            ItemId iidFolder = new ItemId(fElem.getAttribute(MailConstants.A_ID), zsc);
            if (!iidFolder.belongsTo(reqAcct)) {
                //at least one folder does not belong to the req'd acct; need to get each folder separately
                needToProxy = true;
            }
            itemIds.add(iidFolder);
        }
        if (needToProxy) {
            Element response = getResponseElement(zsc);

            Element tzElem = request.getOptionalElement(MailConstants.E_CAL_TZ);
            long rangeStart = request.getAttributeLong(MailConstants.A_CAL_START_TIME);
            long rangeEnd = request.getAttributeLong(MailConstants.A_CAL_END_TIME);

            for (ItemId iid : itemIds) {
                Element newRequest = zsc.createElement(MailConstants.GET_MINI_CAL_REQUEST);
                newRequest.addAttribute(MailConstants.A_CAL_START_TIME, rangeStart);
                newRequest.addAttribute(MailConstants.A_CAL_END_TIME, rangeEnd);
                if (tzElem != null) {
                    newRequest.addElement(tzElem.detach());
                }
                newRequest.addElement(MailConstants.E_FOLDER).addAttribute(MailConstants.A_ID,iid.toString());
                Element proxyResp = proxyRequest(newRequest, context, iid, iid);
                for (Iterator<Element> dateIter = proxyResp.elementIterator(MailConstants.E_CAL_MINICAL_DATE); dateIter.hasNext(); ) {
                    response.addElement(dateIter.next().detach());
                }
            }
            return response;
        } else {
            return super.proxyIfNecessary(request, context);
        }
    }

}
