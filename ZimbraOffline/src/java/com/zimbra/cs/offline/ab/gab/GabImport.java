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
package com.zimbra.cs.offline.ab.gab;

import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.offline.OfflineDataSource;
import com.zimbra.cs.datasource.DataSourceManager;
import com.zimbra.cs.mailbox.SyncExceptionHandler;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.offline.OfflineLog;
import com.zimbra.cs.offline.ab.LocalData;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;

import java.util.List;

public class GabImport implements DataSource.DataImport {
    private final OfflineDataSource ds;
    private SyncSession session;

    private static final Log LOG = OfflineLog.gab;

    private static final String ERROR = "Google address book synchronization failed";

    public GabImport(DataSource ds) {
        this.ds = (OfflineDataSource) ds;
    }

    public void test() throws ServiceException {
        session = new SyncSession(ds);
    }

    public void importData(List<Integer> folderIds, boolean fullSync)
        throws ServiceException {
         // Only sync contacts if full sync or there are local contact changes
        if (!fullSync && !new LocalData(ds).hasLocalChanges()) {
            return;
        }
        LOG.info("Importing contacts for data source '%s'", ds.getName());
        DataSourceManager.getInstance().getMailbox(ds).beginTrackingSync();
        if (session == null) {
            session = new SyncSession(ds);
        }
        try {
            session.sync();
        } catch (Exception e) {
            if (!SyncExceptionHandler.isRecoverableException(null, 1, ERROR, e)) {
                ds.reportError(Mailbox.ID_FOLDER_CONTACTS, ERROR, e);
            }
        }
        LOG.info("Finished importing contacts for data source '%s'", ds.getName());
    }
}
