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

import java.util.Map;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.offline.OfflineAccount;
import com.zimbra.cs.account.offline.OfflineProvisioning;
import com.zimbra.cs.mailbox.GalSync;
import com.zimbra.cs.offline.OfflineLog;
import com.zimbra.cs.offline.common.OfflineConstants;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;

public class OfflineResetGal extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        String accountId = request.getAttribute(AdminConstants.E_ID);
        OfflineProvisioning prov = OfflineProvisioning.getOfflineInstance();
        OfflineAccount account = (OfflineAccount) prov.get(AccountBy.id, accountId);
        if (account.isFeatureGalEnabled() && account.isFeatureGalSyncEnabled()) {
            OfflineAccount galAccount = (OfflineAccount) prov.getGalAccountByAccount(account);
            if (GalSync.isFullSynced(galAccount)) {
                boolean isReset = GalSync.getInstance().resetGal(galAccount);
                if (!isReset) {
                    OfflineLog.offline.debug("reseting gal for account %s -- Skipped because GAL is recently synced",
                            account.getName());
                }
            } else {
                OfflineLog.offline.debug(
                        "reseting gal for account %s -- Skipped because GAL has not finished initial sync",
                        account.getName());
            }
        } else {
            OfflineLog.offline.debug("Offline GAL sync is disabled for %s, resetting skipped.", account.getName());
        }

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Element resp = zsc.createElement(OfflineConstants.RESET_GAL_ACCOUNT_RESPONSE);
        return resp;
    }
}
