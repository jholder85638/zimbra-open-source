/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;

class OutboxTracker {
    /*
     * Outgoing messages in Outbox.  This is an in memory cache so that we don't have to run derby queries all the time
     *
     * key: mailbox id
     * value: a map of item-id -> last time triesd and failed (0 means never tried)
     */
    private static final Map<Integer, Map<Integer, Long>> sOutboxMessageMap = Collections.synchronizedMap(new HashMap<Integer, Map<Integer, Long>>());

    static void invalidate(Mailbox mbox) {
        synchronized (sOutboxMessageMap) {
            sOutboxMessageMap.remove(mbox.getId());
        }
    }

    static Iterator<Integer> iterator(Mailbox mbox, long retryDelay) throws ServiceException {
        Map<Integer, Long> outboxMap = null;
        mbox.lock.lock();
        try {
            synchronized (sOutboxMessageMap) {
                outboxMap = sOutboxMessageMap.get(mbox.getId());
                if (outboxMap == null) {
                    refresh(mbox);
                    outboxMap = sOutboxMessageMap.get(mbox.getId());
                }
            }
        } finally {
            mbox.lock.release();
        }
        List<Integer> msgList = new ArrayList<Integer>();
        long now = System.currentTimeMillis();
        for (Map.Entry<Integer, Long> e : outboxMap.entrySet()) {
            if (now - e.getValue() > retryDelay)
                msgList.add(e.getKey());
        }
        Collections.sort(msgList, new Comparator<Integer>() {
            @Override public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        return msgList.iterator();
    }

    static void recordFailure(Mailbox mbox, int itemId) {
        synchronized (sOutboxMessageMap) {
            Map<Integer, Long> outboxMap = sOutboxMessageMap.get(mbox.getId());
            if (outboxMap != null)
                outboxMap.put(itemId, System.currentTimeMillis());
        }
    }

    static void remove(Mailbox mbox, int itemId) {
        synchronized (sOutboxMessageMap) {
            Map<Integer, Long> outboxMap = sOutboxMessageMap.get(mbox.getId());
            if (outboxMap != null)
                outboxMap.remove(itemId);
        }
    }

    private static void refresh(Mailbox mbox) throws ServiceException {
        List<Integer> pendingSends = mbox.listItemIds(new OperationContext(mbox), MailItem.Type.MESSAGE,
                DesktopMailbox.ID_FOLDER_OUTBOX);
        synchronized (sOutboxMessageMap) {
            Map<Integer, Long> oldMap = sOutboxMessageMap.get(mbox.getId());
            Map<Integer, Long> newMap = new HashMap<Integer, Long>();
            for (int id : pendingSends) {
                newMap.put(id, (oldMap == null ? 0L : (oldMap.get(id) == null ? 0L : oldMap.get(id))));
            }
            sOutboxMessageMap.put(mbox.getId(), newMap);
        }
    }
}
