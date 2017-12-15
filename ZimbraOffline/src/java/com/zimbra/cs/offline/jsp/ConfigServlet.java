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
package com.zimbra.cs.offline.jsp;

import javax.servlet.http.HttpServlet;

import com.zimbra.common.localconfig.LC;

public class ConfigServlet extends HttpServlet {

    private static final long serialVersionUID = 8124246834674440988L;

    private static final String LOCALHOST_URL_PREFIX = "http://127.0.0.1:";

    public static String LOCALHOST_SOAP_URL;
    public static String LOCALHOST_ADMIN_URL;

    @Override
    public void init() {
        String port = LC.zimbra_admin_service_port.value();

        //setting static variables
        LOCALHOST_SOAP_URL = LOCALHOST_URL_PREFIX + port + "/service/soap/";
        LOCALHOST_ADMIN_URL = LOCALHOST_URL_PREFIX + port + "/service/admin/soap/";
    }
}
