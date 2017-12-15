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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import com.zimbra.cs.offline.util.Xml;

public class SyncResult extends Entity implements YContactSyncResult {

    private int clientRev;
    private int yahooRev;
    private Map<String, Contact> diff = new HashMap<String, Contact>();
    private Map<String, Result> results = new HashMap<String, Result>();

    @Override
    public void extractFromXml(Element e) {
        this.yahooRev = Xml.getIntAttribute(e, "yahoo:rev");
        Element result = Xml.getChildren(e).get(0);
        Element diff = Xml.getChildren(e).get(1);
        this.clientRev = Xml.getIntAttribute(diff, "yahoo:clientrev");
        
        for (Element child : Xml.getChildren(result)) {
            if ("contacts".equals(child.getNodeName())) {
                Result res = new Result();
                res.extractFromXml(child);
                this.results.put(res.id, res);
            }
        }
        
        for (Element child : Xml.getChildren(diff)) {
            if (!"contacts".equals(child.getNodeName())) {
                continue;
            }
            Contact contact = new Contact();
            contact.extractFromXml(child);
            this.diff.put(contact.getId(), contact);
        }
    }
    
    public String getYahooRev() {
        return String.valueOf(this.yahooRev);
    }
    
    public int getClientRev() {
        return this.clientRev;
    }
    
    public Map<String, Contact> getDiff() {
        return diff;
    }

    public Map<String, Result> getResults() {
        return results;
    }
    
    public boolean isPutSuccess(String id) {
        return this.results.get(id).success;
    }
    
    public boolean containsContactId(String id) {
        return this.results.containsKey(id);
    }
    
    public String getRefIdByContactId(String id) {
        Result result = this.results.get(id);
        if (result != null) {
            return result.refid;
        }
        return null;
    }

    static class Result extends Entity {
        String id;
        String refid;
        Action op;
        boolean success;
        String error;
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Result)) {
                return false;
            }
            return id.equals(((Result) obj).id);
        }

        @Override
        public void extractFromXml(Element e) {
            for (Element child : Xml.getChildren(e)) {
                if ("id".equals(child.getNodeName())) {
                    this.id = child.getTextContent();
                } else if ("refid".equals(child.getNodeName())) {
                    this.refid = child.getTextContent();
                } else if ("op".equals(child.getNodeName())) {
                    try {
                        this.op = Action.getOp(child.getTextContent());   //TODO could be "merge"
                    } catch (IllegalArgumentException ile) {
                        this.op = Action.UPDATE;    //TODO support merge
                    }
                } else if ("response".equals(child.getNodeName())) {
                    this.success = "success".equals(child.getTextContent()) ? true : false;
                } else if ("error".equals(child.getNodeName())) {
                    this.error = child.getTextContent();
                }
            }
        }
    }

    @Override
    public Collection<Contact> getContacts() {
        return this.diff.values();
    }

    @Override
    public boolean isPushResult() {
        return true;
    }
}
