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
package com.zimbra.cs.offline.ab;

import com.zimbra.cs.offline.ab.Address;
import com.zimbra.cs.offline.ab.Name;

public class AddressTest {
    private static final String ADDRESSES[] = {
        "1747A Stockton St., San Francisco, CA 94133, USA",
        "235 Montgomery St.\r\n\r\n15th Floor\r\nSan Francisco\r\nCalifornia 94133-4544\r\nUnited States\r\n",
        "235 Montgomery St., 15th Floor, San Francisco",
        "San Francisco, CA",
        "1234 Main Street, Scarsdale, NY"
    };

    private static final String NAMES[] = {
        "David Connelly",
        "Connelly",
        "Mr. Connelly",
        "David W. Connelly",
        "D. Connelly",
        "Mr. D. W. Connelly"
    };

    public static void main(String... args) {
        for (String addr : ADDRESSES) {
            System.out.println(Address.parse(addr).toString());
        }
        for (String name : NAMES) {
            System.out.println(Name.parse(name).toString());
        }
    }
}
