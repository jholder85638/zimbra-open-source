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
package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.offline.OfflineAccount;
import com.zimbra.cs.gal.GalGroup.GroupInfo;
import com.zimbra.cs.gal.GalGroupInfoProvider;
import com.zimbra.cs.offline.OfflineLog;

/**
 * Provide GroupInfo from entries in OfflineGal
 * 
 */
public class OfflineGalGroupInfoProvider extends GalGroupInfoProvider {

    @Override
    public GroupInfo getGroupInfo(String addr, boolean needCanExpand, Account requestedAcct, Account authedAcct) {
        OfflineAccount reqAccount = (OfflineAccount) requestedAcct;
        if (reqAccount.isZcsAccount() && reqAccount.isFeatureGalEnabled() && reqAccount.isFeatureGalSyncEnabled()) {
            try {
                Contact con = GalSyncUtil.getGalDlistContact(reqAccount, addr);
                if (con != null && con.isGroup()) {
                    return needCanExpand ? GroupInfo.CAN_EXPAND : GroupInfo.IS_GROUP;
                }
            } catch (ServiceException e) {
                OfflineLog.offline.error("Unable to find group %s addr due to exception", e, addr);
            }
        }
        return null;
    }

}
