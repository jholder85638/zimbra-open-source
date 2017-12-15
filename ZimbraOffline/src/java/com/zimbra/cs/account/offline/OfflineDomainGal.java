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
package com.zimbra.cs.account.offline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbOfflineDirectory;
import com.zimbra.cs.offline.common.OfflineConstants;

/**
 * directory:
 *          entry_id | entry_type | entry_name  | zimbraId 
 *              dd   |   Gal      | xxx.com     |  aaaa-bbbb-cccc-dddd
 *
 * directory_attrs:
 *          entry_id | name                     | value
 *              dd   | cn                       | xxx.com
 *              dd   | objectClass              | domainGalEntry
 *              dd   | zimbraId                 | aaaa-bbbb-cccc-dddd
 *              dd   | offlineGalRetryEnabled   | TRUE
 *              dd   | offlineGalAccountId      | 1234-2323-232-3323 (Gal Account zimbraId)
 *              dd   | offlineUsingGalAccountId | 1111-2222-3333-4444 (Account which uses this as their GAL)
 *              dd   | offlineUsingGalAccountId | 1111-2222-3333-5555 (Account which uses this as their GAL)
 *
 */
public class OfflineDomainGal extends NamedEntry {

    protected OfflineDomainGal(String name, String id, Map<String, Object> attrs, Provisioning prov) {
        super(name, id, attrs, null, prov);
    }

    public static Map<String, OfflineDomainGal> instantiateAll(OfflineProvisioning prov) {
        Map<String, OfflineDomainGal> map = new HashMap<String, OfflineDomainGal>();
        try {
            List<String> ids = DbOfflineDirectory.listAllDirectoryEntries(OfflineProvisioning.EntryType.GAL);
            for (String id : ids) {
                Map<String, Object> attrs = DbOfflineDirectory.readDirectoryEntry(OfflineProvisioning.EntryType.GAL,
                        Provisioning.A_zimbraId, id);
                if (attrs == null)
                    continue;
                String domainName = (String) attrs.get(Provisioning.A_cn);
                if (domainName != null)
                    map.put(domainName.toLowerCase(), new OfflineDomainGal(domainName, id, attrs, prov));
            }
            return map;
        } catch (ServiceException e) {
            // throw RuntimeException because we're being called at startup...
            throw new RuntimeException("failure instantiating offlineDomainGal", e);
        }
    }

    public String[] getAttachedToGalAccountIds() {
        return getMultiAttr(OfflineProvisioning.A_offlineUsingGalAccountId);
    }

    public String getDomain() {
        return getAttr(Provisioning.A_cn);
    }

    public String getId() {
        return getAttr(Provisioning.A_zimbraId);
    }

    public Account getGalAccount() throws ServiceException {
        return OfflineProvisioning.getOfflineInstance().getAccountById(getGalAccountId());
    }

    public String getGalAccountId() {
        return getAttr(OfflineConstants.A_offlineGalAccountId);
    }

    public boolean isRetryEnabled() {
        return getBooleanAttr(OfflineProvisioning.A_offlineGalRetryEnabled, true);
    }
}
