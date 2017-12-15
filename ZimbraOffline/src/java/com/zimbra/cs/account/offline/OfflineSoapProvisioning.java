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
package com.zimbra.cs.account.offline;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.offline.common.OfflineConstants;

public class OfflineSoapProvisioning extends SoapProvisioning {

    public void resetGal(String accountId) throws ServiceException {
        XMLElement req = new XMLElement(OfflineConstants.RESET_GAL_ACCOUNT_REQUEST);
        req.addElement(AdminConstants.E_ID).setText(accountId);
        invoke(req);
    }
}
