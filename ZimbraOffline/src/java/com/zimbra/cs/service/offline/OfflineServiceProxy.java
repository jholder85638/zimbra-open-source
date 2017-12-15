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

import org.dom4j.QName;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OfflineServiceException;
import com.zimbra.cs.mailbox.ZcsMailbox;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;

public class OfflineServiceProxy extends DocumentHandler {

    private String mOp;
    private boolean mQuiet;
    private boolean mHandleLocal;
    
    public OfflineServiceProxy(String op, boolean quiet, boolean handleLocal) {
        mOp = op;
        mQuiet = quiet;
        mHandleLocal = handleLocal;
    }

    public OfflineServiceProxy(String op, boolean quiet, boolean handleLocal, QName responseQname) {
        this(op, quiet, handleLocal);
        setResponseQName(responseQname);
    }
    
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext ctxt = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(ctxt);
        if (!(mbox instanceof ZcsMailbox)) {
            if (mHandleLocal)
                return getResponseElement(ctxt);
            else
                throw OfflineServiceException.MISCONFIGURED("incorrect mailbox class: " + mbox.getClass().getSimpleName());
        }
                
        Element response = ((ZcsMailbox)mbox).proxyRequest(request, ctxt.getResponseProtocol(), mQuiet, mOp);
        if (response != null)
            response.detach();
        
        if (mQuiet && response == null)
            return getResponseElement(ctxt);
        
        return response;
    }
    
    public static OfflineServiceProxy SearchCalendarResources() {
        return new OfflineServiceProxy("search cal resources", false, true);
    }
    
    public static OfflineServiceProxy GetPermission() {
        return new OfflineServiceProxy("get permission", true, true);
    }
    
    public static OfflineServiceProxy GrantPermission() {
        return new OfflineServiceProxy("grant permission", false, false);
    }
    
    public static OfflineServiceProxy RevokePermission() {
        return new OfflineServiceProxy("revoke permission", false, false);
    }
    
    public static OfflineServiceProxy CheckPermission() {
        return new OfflineServiceProxy("check permission", false, false);
    }
    
    public static OfflineServiceProxy GetShareInfoRequest() {
        return new OfflineServiceProxy("get share info", false, false);
    }
    
    public static OfflineServiceProxy AutoCompleteGalRequest() {
        return new OfflineServiceProxy("auto-complete gal", true, true);
    }
    
    public static OfflineServiceProxy CheckRecurConflictsRequest() {
        return new OfflineServiceProxy("check recur conflicts", false, false);
    }
    
    public static OfflineServiceProxy GetDLMembersRequest() {
        return new OfflineServiceProxy("get dl members", false, false);
    }
}



