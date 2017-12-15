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
package com.zimbra.cs.mailbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.offline.OfflineAccount;
import com.zimbra.cs.account.offline.OfflineGal;
import com.zimbra.cs.account.offline.OfflineProvisioning;
import com.zimbra.cs.offline.OfflineLC;
import com.zimbra.cs.offline.OfflineLog;

public final class GalSyncRetry {

    private Map<Mailbox, MailboxGalSyncRetry> mboxRetryMap = new ConcurrentHashMap<Mailbox, MailboxGalSyncRetry>();

    private GalSyncRetry() {}

    private static final class LazyHolder {
        static GalSyncRetry instance = new GalSyncRetry();
    }

    private synchronized MailboxGalSyncRetry getRetry(OfflineAccount galAccount) throws ServiceException {
        Mailbox galMbox = MailboxManager.getInstance().getMailboxByAccount(galAccount);
        if (!this.mboxRetryMap.containsKey(galMbox)) {
            try {
                this.mboxRetryMap.put(galMbox, new MailboxGalSyncRetry(galMbox));
            } catch (ServiceException e) {
                OfflineLog.offline.error("GAL retry get retry failed", e);
            }
        }
        return this.mboxRetryMap.get(galMbox);
    }

    private synchronized MailboxGalSyncRetry getExistingRetry(Mailbox galMbox) {
        return this.mboxRetryMap.get(galMbox);
    }

    public static void checkpoint(OfflineAccount galAccount, List<String> retryContactIds)
    throws ServiceException {
        LazyHolder.instance.getRetry(galAccount).checkpoint(retryContactIds);
    }

    public static void retry(OfflineAccount galAccount, List<String> retryContactIds)
    throws ServiceException, IOException {
        LazyHolder.instance.getRetry(galAccount).retry(retryContactIds);
    }

    public static boolean remove(Mailbox galMbox) throws ServiceException {
        MailboxGalSyncRetry retry = LazyHolder.instance.getExistingRetry(galMbox);
        if (retry != null) {
            retry.remove();
            return true;
        }
        return false;
    }

    private static final class MailboxGalSyncRetry {

        private static String OfflineGalSyncRetry = "offline_gal_retry";
        private Set<String> retryIds = new HashSet<String>();

        private Mailbox galMbox;
        private Metadata md;
        private long lastRetry;
        private OperationContext context;
        private int syncFolder;
        private DataSource ds;

        MailboxGalSyncRetry(Mailbox galMbox) throws ServiceException {
            this.galMbox = galMbox;
            this.context = new OperationContext(this.galMbox);
            this.syncFolder = OfflineGal.getSyncFolder(this.galMbox, this.context, false).getId();
            this.ds = OfflineProvisioning.getOfflineInstance().getDataSource(galMbox.getAccount());
            
            this.md = this.galMbox.getConfig(null, OfflineGalSyncRetry);
            if (this.md == null) {
                this.md = new Metadata();
                this.md.put(this.galMbox.getAccountId(), "");
                this.galMbox.setConfig(null, OfflineGalSyncRetry, md);
                this.lastRetry = System.currentTimeMillis();
            } else {
                //load
                String retryIds = this.md.get(this.galMbox.getAccountId());
                if (!StringUtil.isNullOrEmpty(retryIds)) {
                    this.retryIds.addAll(Arrays.asList(retryIds.split(",")));
                }
                this.lastRetry = 0;
            }
        }

        private void addRetryIds(List<String> ids) {
            this.retryIds.addAll(ids);
        }

        private String getRetryItems() {
            StringBuilder builder = new StringBuilder();
            boolean isFirst = true;
            for (String id : this.retryIds) {
                if (!isFirst) {
                    builder.append(",");
                }
                isFirst = false;
                builder.append(id);
            }
            return builder.toString();
        }

        private boolean needsRetry() {
            boolean result = (this.retryIds.size() != 0);
            if (result && this.lastRetry == 0) {
                this.lastRetry = System.currentTimeMillis();
            }
            return result;
        }

        private boolean needsRetryNow() {
            return (((System.currentTimeMillis() - this.lastRetry) / 1000)
                    > OfflineLC.zdesktop_gal_sync_retry_interval_secs.longValue());
        }

        void retry(List<String> retryingIds) throws ServiceException, IOException {
            if (!retryingIds.isEmpty()) {
                this.retryIds.addAll(retryingIds);
                retryingIds.clear();
            }
            if (needsRetry()) {
                if (needsRetryNow()) {
                    if (!this.retryIds.isEmpty()) {
                        OfflineLog.offline.debug("Offline GAL sync retry " + this.retryIds.size() + " items");
                        List<String> newRetryIds = new ArrayList<String>();
                        GalSyncUtil.fetchContacts(galMbox, context, syncFolder, getRetryItems(),
                                false, ds, newRetryIds, "", this.galMbox.getAccountId());
                        this.retryIds.clear();
                        addRetryIds(newRetryIds);
                        checkpoint();
                        this.lastRetry = System.currentTimeMillis();
                        OfflineLog.offline.debug("Offline GAL sync retry finished");
                    } else {
                        OfflineLog.offline.debug("Offline GAL sync retry passed, no retry items");
                    }
                } else {
                    OfflineLog.offline.debug("Offline GAL sync retry skipped, not yet time to do it");
                }
            } else {
                OfflineLog.offline.debug("Offline GAL sync retry bypassed, no retry items");
            }
        }

        void checkpoint(List<String> ids) throws ServiceException {
            addRetryIds(ids);
            checkpoint();
        }

        void checkpoint() throws ServiceException {
            this.md.put(this.galMbox.getAccountId(), getRetryItems());
            this.galMbox.setConfig(null, OfflineGalSyncRetry, this.md);
        }

        void remove() throws ServiceException {
            this.md = null;
            this.galMbox.setConfig(null, OfflineGalSyncRetry, this.md);
        }

        public String toString() {
            return getRetryItems();
        }
    }
}
