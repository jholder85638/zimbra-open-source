/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
import java.util.Map;
import java.util.UUID;

import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.offline.OfflineLC;

class OfflineLocalServer extends Server {
    private OfflineLocalServer(OfflineConfig oconfig, Map<String, Object> attrs, Provisioning prov) {
        super((String) attrs.get(Provisioning.A_cn), (String) attrs.get(Provisioning.A_zimbraId), attrs, oconfig.getServerDefaults(), prov);
    }

    static OfflineLocalServer instantiate(OfflineConfig oconfig, Provisioning prov) {
        Map<String, Object> attrs = new HashMap<String, Object>(12);
        attrs.put(Provisioning.A_objectClass, "zimbraServer");
        attrs.put(Provisioning.A_cn, "localhost");
        attrs.put(Provisioning.A_zimbraServiceHostname, "localhost");
        attrs.put(Provisioning.A_zimbraSmtpHostname, "localhost");
        attrs.put(Provisioning.A_zimbraSmtpPort, "25");
        attrs.put(Provisioning.A_zimbraSmtpTimeout, "60000");        
        attrs.put(Provisioning.A_zimbraSmtpSendPartial, "false");
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        attrs.put("zimbraServiceEnabled", "mailbox");
        attrs.put("zimbraServiceInstalled", "mailbox");
        attrs.put(Provisioning.A_zimbraMailPort, LC.zimbra_admin_service_port.value()); //in offline both are the same
        attrs.put(Provisioning.A_zimbraAdminPort, LC.zimbra_admin_service_port.value());
        attrs.put(Provisioning.A_zimbraMailMode, "http");
        attrs.put(Provisioning.A_zimbraLmtpNumThreads, "1");
        attrs.put(Provisioning.A_zimbraLmtpBindPort, "7635");
        attrs.put(Provisioning.A_zimbraFileUploadMaxSize, OfflineLC.zdesktop_upload_size_limit.value());
        return new OfflineLocalServer(oconfig, attrs, prov);
    }
}
