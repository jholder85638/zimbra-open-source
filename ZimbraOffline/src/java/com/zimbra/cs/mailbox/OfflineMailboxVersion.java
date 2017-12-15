/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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

import com.zimbra.common.service.ServiceException;

public class OfflineMailboxVersion {

    private static final short CURRENT_VERSION = 3;

    private short version;

    static OfflineMailboxVersion CURRENT() {
        return new OfflineMailboxVersion();
    }
                    
    OfflineMailboxVersion() {
        version = CURRENT_VERSION;
    }
    
    OfflineMailboxVersion(short version) {
        this.version = version;
    }
    
    OfflineMailboxVersion(OfflineMailboxVersion other) {
        version = other.version;
    }

    static OfflineMailboxVersion fromMetadata(Metadata md) throws ServiceException {
        short ver = 1; // unknown version are set to 1
        if (md != null)
            ver = (short) md.getLong("ver", 1);
        return new OfflineMailboxVersion(ver);
    }

    void writeToMetadata(Metadata md) {
        md.put("ver", version);
    }
    
    public boolean atLeast(int version) {
        return this.version >= version;
    }

    public boolean atLeast(OfflineMailboxVersion b) {
        return atLeast(b.version);
    }

    public boolean isLatest() {
        return version == CURRENT_VERSION;
    }

    public boolean tooHigh() {
        return version >CURRENT_VERSION;
    }

    public String toString() {
        return Short.toString(version);
    }
}
