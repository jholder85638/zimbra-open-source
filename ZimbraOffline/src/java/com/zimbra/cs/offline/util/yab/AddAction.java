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
package com.zimbra.cs.offline.util.yab;

import org.w3c.dom.Element;

public enum AddAction {
    ADD("add"), MERGE("merge");

    private static final String TAG = "add-action";
    
    private String attr;

    private AddAction(String addr) { this.attr = addr; }

    public static AddAction fromXml(Element e) {
        String s = e.getAttribute(TAG);
        if (s == null || s.equals("")) return null;
        if (ADD.attr.equals(s)) return ADD;
        if (MERGE.attr.equals(s)) return MERGE;
        throw new IllegalArgumentException("Invalid 'add-action' value: " + s);
    }

    public void setAttribute(Element e) {
        e.setAttribute(TAG, attr);
    }
}
