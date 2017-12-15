/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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

import com.zimbra.common.account.ProvisioningConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.offline.OfflineProvisioning.EntryType;
import com.zimbra.cs.db.DbOfflineDirectory;
import com.zimbra.cs.offline.OfflineLC;
import com.zimbra.cs.offline.OfflineLog;

class OfflineConfig extends Config {
    private OfflineConfig(Map<String, Object> attrs, Provisioning provisioning) {
        super(attrs, provisioning);
    }


    static synchronized OfflineConfig instantiate(Provisioning provisioning) {
        try {
            Map<String, Object> attrs = DbOfflineDirectory.readDirectoryEntry(OfflineProvisioning.EntryType.CONFIG, OfflineProvisioning.A_offlineDn, "config");
            if (attrs == null) {
                attrs = new HashMap<String, Object>(2);
                attrs.put(Provisioning.A_cn, "config");
                attrs.put(Provisioning.A_objectClass, "zimbraGlobalConfig");
                try {
                    DbOfflineDirectory.createDirectoryEntry(OfflineProvisioning.EntryType.CONFIG, "config", attrs, false);
                } catch (ServiceException x) {
                    OfflineLog.offline.error("can't save config", x); //shouldn't really happen.  see bug 34567
                }
            }
            String[] skins = OfflineLC.zdesktop_skins.value().split("\\s*,\\s*");
            attrs.put(Provisioning.A_zimbraInstalledSkin, skins);
            if (!OfflineLC.zdesktop_redolog_enabled.booleanValue()) {
                attrs.put(Provisioning.A_zimbraRedoLogEnabled, OfflineLC.zdesktop_redolog_enabled.booleanValue() ? ProvisioningConstants.TRUE : ProvisioningConstants.FALSE);
                attrs.put(Provisioning.A_zimbraRedoLogFsyncIntervalMS, 0);
            }
            attrs.put(Provisioning.A_zimbraSmtpSendAddMailer, ProvisioningConstants.FALSE);
            attrs.put(Provisioning.A_zimbraNotebookAccount, "local@host.local");
            attrs.put(Provisioning.A_zimbraMtaMaxMessageSize, OfflineLC.zdesktop_upload_size_limit.value());

            attrs.put(Provisioning.A_zimbraBatchedIndexingSize, OfflineLC.zdesktop_batched_indexing_size.value());
            attrs.put(Provisioning.A_zimbraMailDiskStreamingThreshold, OfflineLC.zdesktop_mail_disk_streaming_threshold.value());
            attrs.put(Provisioning.A_zimbraMailFileDescriptorCacheSize, OfflineLC.zdesktop_mail_file_descriptor_cache_size.value());
            attrs.put(Provisioning.A_zimbraMessageCacheSize, OfflineLC.zdesktop_message_cache_size.value());
            attrs.put(Provisioning.A_zimbraMessageIdDedupeCacheSize, "0");
            attrs.put(Provisioning.A_zimbraNotebookPageCacheSize, "96");

            return new OfflineConfig(attrs, provisioning);
        } catch (ServiceException e) {
            // throw RuntimeException because we're being called at startup...
            throw new RuntimeException("failure instantiating global Config: " + e.getMessage(), e);
        }
    }
}
