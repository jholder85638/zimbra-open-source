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
package com.zimbra.cs.offline.util.yc.oauth;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.zimbra.cs.offline.OfflineLog;
import com.zimbra.cs.offline.util.Xml;
import com.zimbra.cs.offline.util.yc.YContactException;

public class OAuthGetContactsResponse extends OAuthResponse {

    private static DocumentBuilder docBuilder = Xml.newDocumentBuilder();

    public OAuthGetContactsResponse(String resp) throws YContactException {
        super(resp);
    }

    @Override
    protected void handleResponse() throws YContactException {
        try {
            Document doc = docBuilder.parse(new InputSource(new StringReader(getRawResponse())));
            Element root = doc.getDocumentElement();

            OfflineLog.yab.info("client rev: %s, server rev: %s", root.getAttribute("yahoo:clientrev"),
                    root.getAttribute("yahoo:rev"));

        } catch (Exception e) {
            throw new YContactException("error while creating xml document", "", false, e, null);
        }
    }

}
