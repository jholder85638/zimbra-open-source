/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.ldaputils;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.LDAPUtilsConstants;
import com.zimbra.soap.ZimbraSoapContext;
/**
 * @author Greg Solovyev
 */
public class RenameLDAPEntry extends AdminDocumentHandler {

    public Element handle(Element request, Map<String, Object> context)
            throws ServiceException {

        ZimbraSoapContext lc = getZimbraSoapContext(context);
        String dn = request.getAttribute(LDAPUtilsConstants.E_DN);
        String newDN = request.getAttribute(LDAPUtilsConstants.E_NEW_DN);

        NamedEntry ne = LDAPUtilsHelper.getInstance().renameLDAPEntry(dn,  newDN);

        ZimbraLog.security.info(ZimbraLog.encodeAttrs(new String[] { "cmd",
                "RenameLDAPEntry", "dn", dn,"new_dn", newDN}, null));

        Element response = lc
                .createElement(LDAPUtilsConstants.RENAME_LDAP_ENTRY_RESPONSE);
        ZimbraLDAPUtilsService.encodeLDAPEntry(response, ne);

        return response;
    }
}
