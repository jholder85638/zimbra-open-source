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

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ZimbraExtension;
import com.zimbra.soap.SoapServlet;
/**
 * The main entry point for extensions
 * @author gsolovyev
 *
 */
public class ZimbraHelloWorldExtension implements ZimbraExtension {
	public static String ZAS_EXTENSION_NAME = "com_zimbra_appointment_summary";
	public static final String APPOINTMENT_SUMMARY_TASK_NAME = "SendAppointmentSummary";
	public static final String E_helloWorld = "HelloWorld";
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return ZAS_EXTENSION_NAME;
	}

	@Override
	public void init() throws ExtensionException, ServiceException {
		SoapServlet.addService("SoapServlet", new ZimbraHelloWorldService());
	}

}
