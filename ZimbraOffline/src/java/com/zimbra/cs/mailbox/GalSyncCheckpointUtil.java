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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;

/**
 * A checkpoint consists of two parts, GalSyncToken and lastSyncedItemId.
 * we do checkpointing for every batch of contacts we persist. If full sync failed,
 * all synced items will be skipped.
 * SyncToken only get persisted after a successful full sync.
 * 
 */
public final class GalSyncCheckpointUtil {

    private static final String OFFLINE_GAL_CHECKPOINT = "offline_gal_sync_checkpoint";
    private static final String OFFLINE_GAL_ITEMS = "offline_gal_items";
    private static final String CHECKPOINT_SEPARATOR = "@";
    private static final GalSyncCheckpoint INVALID_CHECKPOINT = new GalSyncCheckpoint("x:x:x", -1);

    private GalSyncCheckpointUtil() {
    }

    static String getCheckpointGalAccountId(Mailbox mbox) throws ServiceException {
        Metadata md = mbox.getConfig(null, OFFLINE_GAL_CHECKPOINT);
        if (md == null) {
            md = new Metadata();
            md.put(mbox.getAccountId(), INVALID_CHECKPOINT);
            mbox.setConfig(null, OFFLINE_GAL_CHECKPOINT, md);
        }
        String checkpt = md.get(mbox.getAccountId());
        return checkpt.split(":")[1];
    }
    
    static String getCheckpointToken(Mailbox mbox) throws ServiceException {
        Metadata md = mbox.getConfig(null, OFFLINE_GAL_CHECKPOINT);
        if (md == null) {
            md = new Metadata();
            md.put(mbox.getAccountId(), INVALID_CHECKPOINT);
            mbox.setConfig(null, OFFLINE_GAL_CHECKPOINT, md);
        }
        String checkpt = md.get(mbox.getAccountId());
        return checkpt.split(CHECKPOINT_SEPARATOR)[0];
    }

    static void removeCheckpoint(Mailbox mbox) throws ServiceException {
        Metadata md = mbox.getConfig(null, OFFLINE_GAL_CHECKPOINT);
        if (md == null) {
            md = new Metadata();
        }
        md.put(mbox.getAccountId(), INVALID_CHECKPOINT);
        mbox.setConfig(null, OFFLINE_GAL_CHECKPOINT, md);

        md = mbox.getConfig(null, OFFLINE_GAL_ITEMS);
        if (md == null) {
            md = new Metadata();
        }
        md.put(mbox.getAccountId(), "");
        mbox.setConfig(null, OFFLINE_GAL_ITEMS, md);
    }

    static void checkpoint(Mailbox mbox, String token, String galAcctId, String items) throws ServiceException {
        Metadata md = mbox.getConfig(null, OFFLINE_GAL_CHECKPOINT);
        int syncedItemId = Integer.parseInt(items.substring(items.lastIndexOf(":")+1));
        if (StringUtil.isNullOrEmpty(token)) {
            StringBuilder ckpt = new StringBuilder(md.get(mbox.getAccountId()));
            md.put(mbox.getAccountId(), ckpt.replace(ckpt.lastIndexOf(CHECKPOINT_SEPARATOR)+1, ckpt.length(), ""+syncedItemId));
        } else {
            md.put(mbox.getAccountId(), new GalSyncCheckpoint(token, syncedItemId));
        }

        mbox.setConfig(null, OFFLINE_GAL_CHECKPOINT, md);
    }

    static int getLastSyncedItemId(Mailbox mbox) throws ServiceException {
        Metadata md = mbox.getConfig(null, OFFLINE_GAL_CHECKPOINT);
        if (md == null) {
            md = new Metadata();
            md.put(mbox.getAccountId(), INVALID_CHECKPOINT);
            mbox.setConfig(null, OFFLINE_GAL_CHECKPOINT, md);
        }
        String checkpt = md.get(mbox.getAccountId());
        return Integer.parseInt(checkpt.split(CHECKPOINT_SEPARATOR)[1]);
    }
    
    public static boolean hasCheckpoint(Mailbox mbox) throws ServiceException {
        Metadata md = mbox.getConfig(null, OFFLINE_GAL_CHECKPOINT);
        if (md == null) {
            md = new Metadata();
            md.put(mbox.getAccountId(), INVALID_CHECKPOINT);
            mbox.setConfig(null, OFFLINE_GAL_CHECKPOINT, md);
            return false;
        }
        String checkpt = md.get(mbox.getAccountId());
        return Integer.parseInt(checkpt.split(CHECKPOINT_SEPARATOR)[1]) != -1;
    }
    
    public static void persistItemIds(Mailbox mbox, List<Integer> ids) throws ServiceException {
        if (!ids.isEmpty()) {
            Metadata md = mbox.getConfig(null, OFFLINE_GAL_ITEMS);
            if (md == null) {
                md = new Metadata();
                md.put(mbox.getAccountId(), "");
                mbox.setConfig(null, OFFLINE_GAL_ITEMS, md);
            }
            StringBuilder buf = new StringBuilder();
            boolean isFirst = true;
            for (Integer id : ids) {
                if (!isFirst) {
                    buf.append(",");
                }
                buf.append(id);
                isFirst = false;
            }
            md.put(mbox.getAccountId(), buf.toString());
            mbox.setConfig(null, OFFLINE_GAL_ITEMS, md);    
        }
    }
    
    public static List<Integer> retrieveItemIds(Mailbox mbox) throws ServiceException {
        Metadata md = mbox.getConfig(null, OFFLINE_GAL_ITEMS);
        String ids = md.get(mbox.getAccountId());
        if (StringUtil.isNullOrEmpty(ids)) {
            return Collections.EMPTY_LIST;
        } else {
            List<Integer> list = new ArrayList<Integer>();
            String[] idArray = ids.split(",");
            for (String id : idArray) {
                list.add(Integer.parseInt(id));
            }
            return list;
        }
    }

    private static final class GalSyncCheckpoint {
        String token;
        int lastSyncedId;

        GalSyncCheckpoint(String token, int lastId) {
            this.token = token;
            this.lastSyncedId = lastId;
        }

        public String toString() {
            return this.token + CHECKPOINT_SEPARATOR + this.lastSyncedId;
        }
    }
}