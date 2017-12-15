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
package com.zimbra.cs.offline.jsp;

import java.util.Arrays;

public interface JspConstants {

    public enum JspVerb {
        add, del, exp, imp, mod, rst, idx, rstgal;

        public boolean isAdd() {
            return this == add;
        }

        public boolean isDelete() {
            return this == del;
        }

        public boolean isExport() {
            return this == exp;
        }

        public boolean isImport() {
            return this == imp;
        }

        public boolean isModify() {
            return this == mod;
        }

        public boolean isReset() {
            return this == rst;
        }

        public boolean isReindex() {
            return this == idx;
        }

        public boolean isResetGal() {
            return this == rstgal;
        }

        public static JspVerb fromString(String s) {
            try {
                return s == null ? null : JspVerb.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("invalid type: " + s + ", valid values: " + Arrays.asList(JspVerb.values()),
                        e);
            }
        }
    }

    public static final String LOCAL_ACCOUNT = "local@host.local";
    public static final String MASKED_PASSWORD = "********";
    public static final String DUMMY_PASSWORD = "topsecret";

    public static final String OFFLINE_REMOTE_HOST = "offlineRemoteHost";
    public static final String OFFLINE_REMOTE_PORT = "offlineRemotePort";
    public static final String OFFLINE_REMOTE_SSL = "offlineRemoteSsl";
}
