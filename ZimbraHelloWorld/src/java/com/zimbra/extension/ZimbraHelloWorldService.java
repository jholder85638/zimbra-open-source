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
package com.zimbra.extension;

import org.dom4j.Namespace;
import org.dom4j.QName;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.DocumentDispatcher;
import com.zimbra.soap.DocumentService;

public class ZimbraHelloWorldService implements DocumentService {
	public static final String E_HELLO_WORLD_REQUEST = "HelloWorldRequest";
	public static final String E_HELLO_WORLD_RESPONSE = "HelloWorldResponse";
	public static final QName HELLO_WORLD_REQUEST = QName.get(E_HELLO_WORLD_REQUEST, AccountConstants.NAMESPACE);
	public static final QName HELLO_WORLD_RESPONSE = QName.get(E_HELLO_WORLD_RESPONSE, AccountConstants.NAMESPACE);
	
	/**
	 * register new SOAP handlers here
	 */
	@Override
	public void registerHandlers(DocumentDispatcher dispatcher) {
		dispatcher.registerHandler(HELLO_WORLD_REQUEST, new HelloWorld());
	}

}
