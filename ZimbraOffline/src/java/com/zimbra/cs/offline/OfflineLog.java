/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2013, 2014 Zimbra, Inc.
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
package com.zimbra.cs.offline;

import com.zimbra.common.util.LogFactory;

public class OfflineLog {
    /** The "zimbra.offline" logger. For offline sync logs. */
    public static final com.zimbra.common.util.Log offline = LogFactory.getLog("zimbra.offline");

    /** The "zimbra.offline.request" logger. For recording SOAP traffic
     *  sent to the remote server. */
    public static final com.zimbra.common.util.Log request = LogFactory.getLog("zimbra.offline.request");

    /** The "zimbra.offline.response" logger. For recording SOAP traffic
     *  received from the remote server. */
    public static final com.zimbra.common.util.Log response = LogFactory.getLog("zimbra.offline.response");

    /** The "zimbra.offline.yab" logger. For recording Yahoo Address Book sync events */
    public static final com.zimbra.common.util.Log yab = LogFactory.getLog("zimbra.offline.yab");

    /** The "zimbra.offline.gab" logger. For recording Google Address Book sync events */
    public static final com.zimbra.common.util.Log gab = LogFactory.getLog("zimbra.offline.gab");

    /** For logging YMail cascade events*/
    public static final com.zimbra.common.util.Log ymail = LogFactory.getLog("zimbra.offline.ymail");
}
