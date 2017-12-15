/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2008, 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.service.offline;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.continuation.ContinuationSupport;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.offline.OfflineSyncManager;
import com.zimbra.cs.service.mail.NoOp;
import com.zimbra.soap.SoapServlet;

public class OfflineNoOp extends NoOp {

    private static final String TIME_KEY = "ZDNoOpStartTime";

	public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        HttpServletRequest servletRequest = (HttpServletRequest) context.get(SoapServlet.SERVLET_REQUEST);
        boolean isResumed = !ContinuationSupport.getContinuation(servletRequest).isInitial();
        if (!isResumed) {
			OfflineSyncManager.getInstance().clientPing();
        }
        boolean wait = request.getAttributeBool(MailConstants.A_WAIT, false);
        long start = System.currentTimeMillis();
        if (!isResumed) {
            servletRequest.setAttribute(TIME_KEY, start);
        }
		Element response = super.handle(request, context);
		response.addAttribute(MailConstants.A_WAIT, wait);
		response.addAttribute(MailConstants.A_TIME, System.currentTimeMillis()-(Long) servletRequest.getAttribute(TIME_KEY));
		return response;
	}
}
