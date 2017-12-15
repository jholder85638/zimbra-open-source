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
package com.zimbra.cs.offline.ab.yc;

import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.offline.OfflineDataSource;
import com.zimbra.cs.account.offline.OfflineProvisioning;
import com.zimbra.cs.mailbox.OfflineServiceException;
import com.zimbra.cs.mailbox.YContactSync;
import com.zimbra.cs.offline.OfflineLog;
import com.zimbra.cs.offline.ab.LocalData;
import com.zimbra.cs.offline.util.yc.YContactException;
import com.zimbra.cs.offline.util.yc.oauth.OAuthManager;


public class YContactImport implements DataSource.DataImport {

    private final OfflineDataSource ds;
    private YContactSync session;
    
    public YContactImport(OfflineDataSource ds) throws YContactException {
        this.ds = ds;
        //TODO it could be a good place to load oauth token, not a good place for persist it as it's 
        //before OfflineProvisiong's createAccount in code path
    }
    @Override
    public void test() throws ServiceException {
    }

    @Override
    public void importData(List<Integer> folderIds, boolean fullSync) throws ServiceException {
        if (!fullSync && !new LocalData(ds).hasLocalChanges()) {
            return;
        }

        try {
            OfflineLog.yab.info("Start importing Yahoo contacts for account '%s'", ds.getName());
            if (!OAuthManager.hasOAuthToken(ds.getAccountId())) {
                OAuthManager.persistCredential(ds.getAccountId(), (String) ds.getAttr(OfflineProvisioning.A_offlineYContactToken),
                                (String) ds.getAttr(OfflineProvisioning.A_offlineYContactTokenSecret),
                                (String) ds.getAttr(OfflineProvisioning.A_offlineYContactTokenSessionHandle),
                                (String) ds.getAttr(OfflineProvisioning.A_offlineYContactGuid),
                                (String) ds.getAttr(OfflineProvisioning.A_offlineYContactTokenTimestamp),
                                (String) ds.getAttr(OfflineProvisioning.A_offlineYContactVerifier));
            }
            if (session == null) {
                session = new YContactSync(ds);
            }
            session.sync();
        } catch (Exception e) {
            OfflineLog.yab.error("Failed to import Yahoo contacts for account '%s'", ds.getName(), e);
            throw OfflineServiceException.YCONTACT_NEED_VERIFY();
        }
        OfflineLog.yab.info("Finished importing Yahoo contacts for account '%s'", ds.getName());
    }
}