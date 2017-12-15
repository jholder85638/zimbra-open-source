/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010, 2011, 2012, 2013, 2014, 2016 Synacor, Inc.
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.SystemUtil;
import com.zimbra.common.util.Version;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.AttributeCardinality;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.AttributeFlag;
import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeOrder;
import com.zimbra.cs.account.AttributeServerType;
import com.zimbra.cs.account.AttributeType;
import com.zimbra.cs.offline.OfflineLog;

/**
 */
public class OfflineAttributeInfo extends AttributeInfo {

    public OfflineAttributeInfo(
            String attrName, int id, String parentId, int groupId, AttributeCallback callback,
            AttributeType type, AttributeOrder order, String value, boolean immutable, String min,
            String max, AttributeCardinality cardinality, Set<AttributeClass> requiredIn,
            Set<AttributeClass> optionalIn, Set<AttributeFlag> flags, List<String> globalConfigValues,
            List<String> defaultCOSValues, List<String> globalConfigValuesUpgrade,
            List<String> defaultCOSValuesUpgrade, String description, List<AttributeServerType> requiresRestart,
            List<Version> since, Version deprecatedSince) {
        super(attrName, id, parentId, groupId, callback, type, order, value, immutable, min, max, cardinality,
              requiredIn, optionalIn, flags, globalConfigValues, defaultCOSValues, null, globalConfigValuesUpgrade,
              defaultCOSValuesUpgrade, description, requiresRestart, since, deprecatedSince);
    }

    @Override
    protected void checkValue(String value, Map attrsToModify) throws ServiceException {
        try {
            super.checkValue(value, attrsToModify);
        } catch (AccountServiceException e) {
            if (AccountServiceException.INVALID_ATTR_VALUE.equals(e.getCode()) && mType == AttributeType.TYPE_ENUM) {
                // use the default value and ignore exception
                if (mDefaultCOSValues != null && !mDefaultCOSValues.isEmpty()) {
                    if (mDefaultCOSValues.size() == 1)
                        attrsToModify.put(mName, mDefaultCOSValues.get(0));
                    else
                        attrsToModify.put(mName, mDefaultCOSValues.toArray());
                    OfflineLog.offline.warn(SystemUtil.getStackTrace(e));
                    OfflineLog.offline.warn("Using default value '" + mDefaultCOSValues + "' for attribute '" + mName + "'");
                    return;
                }
            }
            throw e;
        }
    }
}
