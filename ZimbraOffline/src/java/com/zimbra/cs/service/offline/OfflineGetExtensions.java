/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010, 2013, 2014, 2016 Synacor, Inc.
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

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.offline.common.OfflineConstants;
import com.zimbra.cs.util.ZimbraApplication;
import com.zimbra.soap.DocumentHandler;


public class OfflineGetExtensions extends DocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        List<String> extensionNames = ZimbraApplication.getInstance().getExtensionNames();
        Element response = getZimbraSoapContext(context).createElement(OfflineConstants.GET_EXTENSIONS_RESPONSE);
        if (extensionNames != null) {
            for (String ext : extensionNames)
                response.addElement(OfflineConstants.EXTENSION).addAttribute(OfflineConstants.EXTENSION_NAME, ext);
        }
        return response;
    }

    @Override
    public boolean needsAuth(Map<String, Object> context) {
        return false;
    }

    @Override
    public boolean needsAdminAuth(Map<String, Object> context) {
        return false;
    }
}
