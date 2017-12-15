/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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

/*
 * Created on Jul 30, 2010
 */
package com.zimbra.cs.service.offline;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.cs.offline.common.OfflineConstants;
import com.zimbra.cs.service.admin.AdminService;
import com.zimbra.soap.DocumentDispatcher;

public class OfflineAdminService extends AdminService {

    @Override
    public void registerHandlers(DocumentDispatcher dispatcher) {
        super.registerHandlers(dispatcher);
        dispatcher.registerHandler(AdminConstants.DELETE_MAILBOX_REQUEST, new OfflineDeleteMailbox());
        dispatcher.registerHandler(AdminConstants.DELETE_ACCOUNT_REQUEST, new OfflineDeleteAccount());
        dispatcher.registerHandler(OfflineConstants.RESET_GAL_ACCOUNT_REQUEST, new OfflineResetGal());
    }

}
