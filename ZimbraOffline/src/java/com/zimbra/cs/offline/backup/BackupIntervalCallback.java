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
package com.zimbra.cs.offline.backup;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.callback.CallbackContext;
import com.zimbra.cs.offline.OfflineLog;

/**
 * Attribute change callback. Triggered by AttributeManager when interval is changed.
 *
 */
public class BackupIntervalCallback extends AttributeCallback {

    @SuppressWarnings("unchecked")
    @Override
    public void postModify(CallbackContext context, String attrName, Entry entry) {
        try {
            BackupTimer.updateInterval();
        } catch (ServiceException e) {
            OfflineLog.offline.error("Exception while updating backup interval",e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void preModify(CallbackContext context, String attrName, Object attrValue,
            Map attrsToModify, Entry entry)
            throws ServiceException {
        //do nada
    }

}
