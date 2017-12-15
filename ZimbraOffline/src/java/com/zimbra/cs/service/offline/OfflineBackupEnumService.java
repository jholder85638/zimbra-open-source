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

import java.util.Map;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.offline.backup.AccountBackupInfo;
import com.zimbra.cs.offline.backup.AccountBackupProducer;
import com.zimbra.cs.offline.backup.BackupInfo;
import com.zimbra.cs.offline.common.OfflineConstants;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;

public class OfflineBackupEnumService extends DocumentHandler {
    
    @Override
    public Element handle(Element request, Map<String, Object> context)
            throws ServiceException {
        Set<AccountBackupInfo> info = AccountBackupProducer.getInstance().getStoredBackups();
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Element response = zsc.createElement(OfflineConstants.ACCOUNT_BACKUP_ENUM_RESPONSE);
        for (AccountBackupInfo acctInfo : info) {
            Element acctElem = response.addElement(AdminConstants.E_ACCOUNT);
            acctElem.addAttribute(AdminConstants.E_ID, acctInfo.getAccountId());
            for (BackupInfo backup : acctInfo.getBackups()) {
                Element backupElem = acctElem.addElement(OfflineConstants.E_BACKUP);
                backupElem.addAttribute(AdminConstants.A_TIME, backup.getTimestamp());
                backupElem.addAttribute(AdminConstants.A_FILE_SIZE, backup.getFile().length());
            }
        }
        return response;
    }
}
