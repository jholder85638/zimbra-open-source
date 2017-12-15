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
package com.zimbra.cs.offline;

import com.zimbra.cs.account.offline.OfflineDataSource;
import com.zimbra.cs.account.DataSource;
import com.zimbra.common.service.ServiceException;

import java.util.List;
import java.util.ArrayList;

public class YMailImport implements DataSource.DataImport {
    private final OfflineImport imapImport;
    private OfflineImport ycImport;
    private OfflineImport calDavImport;
    
    public YMailImport(OfflineDataSource ds) throws ServiceException {
        imapImport = OfflineImport.imapImport(ds);
        if (ds.isContactSyncEnabled()) {
            //ycImport = OfflineImport.yabImport(ds.getContactSyncDataSource());
            ycImport = OfflineImport.ycImport(ds.getContactSyncDataSource());
        }
        if (ds.isCalendarSyncEnabled()) {
            calDavImport = OfflineImport.ycalImport(ds.getCalendarSyncDataSource());
        }
    }

    public void test() throws ServiceException {
        if (ycImport != null) {
            ycImport.test();
        }
        if (calDavImport != null) {
            calDavImport.test();
        }
        imapImport.test();
    }

    public void importData(List<Integer> folderIds, boolean fullSync)
        throws ServiceException {
        List<ServiceException> errors = new ArrayList<ServiceException>();
        try {
            imapImport.importData(folderIds, fullSync);
        } catch (ServiceException e) {
            errors.add(e);
        }
        if (ycImport != null) {
            try {
                ycImport.importData(folderIds, fullSync);
            } catch (ServiceException e) {
                errors.add(e);
            }
        }
        if (calDavImport != null) {
            try {
                calDavImport.importData(null, fullSync);
            } catch (ServiceException e) {
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            throw errors.get(0);
        }
    }
}
