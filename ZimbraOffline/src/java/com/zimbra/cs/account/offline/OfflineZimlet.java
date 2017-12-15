/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014 Zimbra, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.account.offline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Zimlet;
import com.zimbra.cs.account.offline.OfflineProvisioning.EntryType;
import com.zimbra.cs.db.DbOfflineDirectory;

class OfflineZimlet extends Zimlet {
    OfflineZimlet(String name, String id, Map<String, Object> attrs, Provisioning prov) {
        super(name, id, attrs, prov);
    }

    static Map<String,Zimlet> instantiateAll(Provisioning prov) {
        Map<String,Zimlet> zmap = new HashMap<String,Zimlet>();
        try {
            List<String> ids = DbOfflineDirectory.listAllDirectoryEntries(OfflineProvisioning.EntryType.ZIMLET);
            for (String id : ids) {
                Map<String, Object> attrs = DbOfflineDirectory.readDirectoryEntry(OfflineProvisioning.EntryType.ZIMLET, Provisioning.A_zimbraId, id);
                if (attrs == null)
                    continue;
                String name = (String) attrs.get(Provisioning.A_cn);
                if (name != null)
                    zmap.put(name.toLowerCase(), new OfflineZimlet(name, id, attrs, prov));
            }
            return zmap;
        } catch (ServiceException e) {
            // throw RuntimeException because we're being called at startup...
            throw new RuntimeException("failure instantiating zimlets", e);
        }
    }
}