/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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

package com.zimbra.cs.datasource.offline;

import com.zimbra.soap.admin.type.DataSourceType;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.DataSource.DataImport;
import com.zimbra.cs.account.offline.OfflineDataSource;
import com.zimbra.cs.datasource.DataSourceManager;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.offline.GMailImport;
import com.zimbra.cs.offline.OfflineImport;
import com.zimbra.cs.offline.YMailImport;
import com.zimbra.cs.offline.ab.yc.YContactImport;


public class OfflineDataSourceManager extends DataSourceManager {

    @Override
    public boolean isSyncCapable(DataSource ds, Folder folder) {
        if (ds.isSyncInboxOnly())
            return folder.getId() == Mailbox.ID_FOLDER_INBOX;
        return (folder.getFlagBitmask() & Flag.BITMASK_SYNCFOLDER) != 0;
    }

    @Override
    public boolean isSyncEnabled(DataSource ds, Folder folder) {
        if (ds.isSyncInboxOnly() && folder.getId() != Mailbox.ID_FOLDER_INBOX) {
            return false;
        }
        int bits = folder.getFlagBitmask();
        return (bits & Flag.BITMASK_SYNCFOLDER) != 0 && (bits & Flag.BITMASK_SYNC) != 0;
    }

    @Override
    public DataImport getDataImport(DataSource ds) throws ServiceException {
        if (ds instanceof OfflineDataSource) {
            OfflineDataSource ods = (OfflineDataSource) ds;
            if (ds.getType() == DataSourceType.imap) {
                if (ods.isYahoo()) {
                    return new YMailImport(ods);
                } else if (ods.isGmail()) {
                    return new GMailImport(ods);
                } else {
                    return OfflineImport.imapImport(ods);
                }
            } else if (ds.getType() == DataSourceType.yab) {
//                return new YabImport(ods);
                return new YContactImport(ods);
            }
        }
        return super.getDataImport(ds);
    }
}
