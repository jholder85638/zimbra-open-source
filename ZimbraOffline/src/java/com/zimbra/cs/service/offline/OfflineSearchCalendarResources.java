/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2012, 2013, 2014, 2016 Synacor, Inc.
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

import java.util.Iterator;
import java.util.Map;

import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.offline.OfflineAccount;
import com.zimbra.cs.index.LuceneFields;
import com.zimbra.cs.service.account.SearchCalendarResources;
import com.zimbra.soap.ZimbraSoapContext;

public class OfflineSearchCalendarResources extends SearchCalendarResources {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext ctxt = getZimbraSoapContext(context);
        Account acct = getRequestedAccount(ctxt);
        boolean needFullName = false;
        boolean needEmail = false;
        if (acct instanceof OfflineAccount) {
            OfflineAccount oAcct = (OfflineAccount) acct;
            if (!oAcct.getRemoteServerVersion().isAtLeast7xx())
            {
                String attrs = request.getAttribute(AccountConstants.E_ATTRS, null);
                if (attrs != null) {
                    if (attrs.matches("(^|.*,)("+ContactConstants.A_fullName+")($|,.*)")) {
                        request.addAttribute(AccountConstants.E_ATTRS, attrs+","+Provisioning.A_displayName);
                        needFullName = true;
                    }
                    if (attrs.matches("(^|.*,)("+ContactConstants.A_email+")($|,.*)")) {
                        needEmail = true;
                    }
                }
                Element nameEl = request.getOptionalElement(AccountConstants.E_NAME);
                if (nameEl != null) {
                    String name = nameEl.getText();
                    Element filterElem = request.getOptionalElement(AccountConstants.E_ENTRY_SEARCH_FILTER);
                    if (filterElem != null) {
                        Element termElem = filterElem.getOptionalElement(AccountConstants.E_ENTRY_SEARCH_FILTER_MULTICOND);
                        if (termElem != null) {
                            boolean nameExists = false;
                            for (Iterator<Element> iter = termElem.elementIterator(); iter.hasNext();) {
                                Element cond = iter.next();
                                if (Provisioning.A_displayName.equals(cond.getAttribute(AccountConstants.A_ENTRY_SEARCH_FILTER_ATTR, null))) {
                                    nameExists = true;
                                    break;
                                }
                            }
                            if (!nameExists) {
                                Element nameCond = termElem.addElement(AccountConstants.E_ENTRY_SEARCH_FILTER_SINGLECOND);
                                nameCond.addAttribute(AccountConstants.A_ENTRY_SEARCH_FILTER_ATTR, Provisioning.A_displayName);
                                nameCond.addAttribute(AccountConstants.A_ENTRY_SEARCH_FILTER_OP, LuceneFields.L_OBJECTS);
                                nameCond.addAttribute(AccountConstants.A_ENTRY_SEARCH_FILTER_VALUE, name);
                            }
                        }
                    } 
                }
            }
        }
        Element response = OfflineServiceProxy.SearchCalendarResources().handle(request, context);
        if (needEmail || needFullName) {
            Iterator<Element> calResources = response.elementIterator(AccountConstants.E_CALENDAR_RESOURCE);
            while (calResources.hasNext()) {
                Element calResource = calResources.next();
                //copy email from name if needed
                Element attrs = calResource.getElement("_attrs");
                if (needEmail && calResource.getAttribute(ContactConstants.A_email, null) == null && calResource.getAttribute(AccountConstants.A_NAME, null) != null) {
                    attrs.addAttribute(ContactConstants.A_email, calResource.getAttribute(AccountConstants.A_NAME));
                }
                //copy fullName from displayName if needed
                if (needFullName && calResource.getAttribute(ContactConstants.A_fullName, null) == null && calResource.getAttribute(Provisioning.A_displayName, null) != null) {
                    attrs.addAttribute(ContactConstants.A_fullName, calResource.getAttribute(Provisioning.A_displayName));
                }
            }
        }
        return response;
    }
}
