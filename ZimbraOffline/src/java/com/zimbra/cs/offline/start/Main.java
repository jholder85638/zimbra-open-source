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
package com.zimbra.cs.offline.start;

import java.lang.reflect.Method;

public class Main {
	public static void main(String[] args) {
		
		//The first argument must be the class name of the jetty starter
		if (args.length == 0) {
			System.err.println("Main launcher class name expected as first argument.");
			System.exit(1);
		}
		
		try {
			Class<?> starter = Class.forName(args[0]);
			Method main = starter.getMethod("main", new Class[] {String[].class});
			String[] newArgs;
			if (args.length > 1) {
				newArgs = new String[args.length - 1];
				System.arraycopy(args, 1, newArgs, 0, args.length - 1);
			} else {
				newArgs = new String[] {};
			}
			main.invoke(null, (Object)(newArgs));
		} catch (Exception x) {
			x.printStackTrace(System.err);
			System.exit(2);
		}
	}
}
