/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010, 2013, 2014, 2016 Synacor, Inc.
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

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.offline.common.OfflineConstants;

public class ExtensionBean extends PageBean {
    
    private boolean isXsyncEnabled;
    
    public ExtensionBean() {
        String uri = getBaseUri() + "/service/soap/";
        try {
            SoapHttpTransport transport = new SoapHttpTransport(uri);
            transport.setTimeout(5000);
            transport.setRetryCount(1);
            transport.setRequestProtocol(SoapProtocol.Soap12);
            transport.setResponseProtocol(SoapProtocol.Soap12);

            Element request = new Element.XMLElement(OfflineConstants.GET_EXTENSIONS_REQUEST);
            Element response = transport.invokeWithoutSession(request.detach());
            for (Element e : response.listElements(OfflineConstants.EXTENSION))
                if (OfflineConstants.EXTENSION_XSYNC.equals(e.getAttribute(OfflineConstants.EXTENSION_NAME)))
                    isXsyncEnabled = true;
        } catch (Exception x) {
            System.out.println("failed getting extensions");
            x.printStackTrace(System.out);
        }
    }
    
    public boolean isXsyncEnabled() {
        return isXsyncEnabled;
    }
}
