/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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

import junit.framework.TestCase;

public class TestVersion extends TestCase {
	
	public void testVersion() {
		
		OfflineAccount.Version v = new OfflineAccount.Version("4.5.6_GA_763.FC5_20070323134056");
		
		System.out.println("major=" + v.getMajor());
		System.out.println("minor=" + v.getMinor());
		System.out.println("maint=" + v.getMaintenance());
		
		v = new OfflineAccount.Version("5.a");
		
		System.out.println("major=" + v.getMajor());
		System.out.println("minor=" + v.getMinor());
		System.out.println("maint=" + v.getMaintenance());
	}

}
