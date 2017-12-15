/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2008, 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.offline.ab;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Metadata;

public class SyncState {
    private int lastModSequence;
    private String lastRevisionContact;
    private String lastRevisionGroup;
    private String lastContactURL;

    private static final String VERSION = "7";

    private static final String KEY_VERSION = "VERSION";
    private static final String KEY_SEQUENCE = "SEQUENCE";
    private static final String KEY_REVISION = "REVISION";
    private static final String KEY_GROUP_REVISION = "REVISIONGRP";
    private static final String KEY_CONTACTURL = "CONTACTURL";

    static boolean isCompatibleVersion(Metadata md) throws ServiceException {
        return md == null || VERSION.equals(md.get(KEY_VERSION));
    }

    void load(Metadata md) throws ServiceException {
        if (md == null) return;
        if (!isCompatibleVersion(md)) {
            throw new IllegalStateException("Incompatible sync version");
        }
        lastModSequence = (int) md.getLong(KEY_SEQUENCE);
        lastRevisionContact = md.get(KEY_REVISION, null);
        lastRevisionGroup = md.get(KEY_GROUP_REVISION, null);
        lastContactURL = md.get(KEY_CONTACTURL, null);
    }

    Metadata getMetadata() {
        Metadata md = new Metadata();
        md.put(KEY_VERSION, VERSION);
        md.put(KEY_SEQUENCE, lastModSequence);
        if (lastRevisionContact != null)
            md.put(KEY_REVISION, lastRevisionContact);
        if (lastRevisionGroup != null)
            md.put(KEY_GROUP_REVISION, lastRevisionGroup);
        if (lastContactURL != null)
            md.put(KEY_CONTACTURL, lastContactURL);
        return md;
    }

    public String getLastRevision() { return lastRevisionContact; }
    public String getlastRevisionGroup() { return lastRevisionGroup; }
    public String getlastContactURL() { return lastContactURL; }
    public int getLastModSequence() { return lastModSequence; }

    public void setLastRevision(String lastRevisionContact) {
        this.lastRevisionContact = lastRevisionContact;
    }

    public void setLastRevisionGroup(String lastRevisionGroup) {
        this.lastRevisionGroup = lastRevisionGroup;
    }

    public void setLastModSequence(int lastModSequence) {
        this.lastModSequence = lastModSequence;
    }

    public void setLastContactURL(String lastContactURL) {
        this.lastContactURL = lastContactURL;
    }

    @Override
    public String toString() {
        return String.format(
            "{lastModSequence=%d, lastRevisionContact=%s, lastRevisionGroup=%s, lastContactURL=%s}", lastModSequence,
            lastRevisionContact, lastRevisionGroup, lastContactURL);
    }
}
