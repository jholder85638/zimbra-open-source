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
package com.zimbra.cs.offline.util.yc;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.zimbra.cs.offline.util.Xml;

/**
 * 
 * for syncresult as a response for client's add/update/remove requests
 *
 */
public class PutResponse extends Response {

    private static DocumentBuilder docBuilder = Xml.newDocumentBuilder();
    
    public PutResponse(int retCode, String resp) {
        super(retCode, resp);
    }
    
    public void extract(SyncResult result) throws YContactException {
        Document doc;
        Element root;

        try {
            doc = docBuilder.parse(new InputSource(new StringReader(getResp())));
            root = doc.getDocumentElement();
            result.extractFromXml(root);
        } catch (Exception e) {
            throw new YContactException("parsing response error", "", false, e, null);
        }
    }
}
