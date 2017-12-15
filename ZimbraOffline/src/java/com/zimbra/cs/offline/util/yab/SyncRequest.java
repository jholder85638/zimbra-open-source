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
package com.zimbra.cs.offline.util.yab;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import com.zimbra.cs.offline.util.Xml;
import com.zimbra.cs.util.yauth.AuthenticationException;

/**
 * Yahoo address book synchronization request.
 */
public class SyncRequest extends Request {
    private List<SyncRequestEvent> events;

    private static final String SYNCHRONIZE = "synchronize";
    private static final String MYREV = "myrev";
    private static final String SYNC_REQUEST = "sync-request";
    
    public SyncRequest(Session session, int revision) {
        super(session);
        events = new ArrayList<SyncRequestEvent>();
        addParam(MYREV, String.valueOf(revision));
    }

    public void addEvent(SyncRequestEvent event) {
        events.add(event);
    }

    public List<SyncRequestEvent> getEvents() {
        return new ArrayList<SyncRequestEvent>(events);
    }

    @Override
    protected boolean isPOST() {
        return !events.isEmpty();
    }
    
    @Override
    protected String getAction() {
        return SYNCHRONIZE;
    }

    @Override
    public Element toXml(Document doc) {
        Element e = doc.createElement(SYNC_REQUEST);
        for (SyncRequestEvent event : events) {
            e.appendChild(event.toXml(doc));
        }
        return e;
    }

    @Override
    protected Response parseResponse(Document doc) {
        return SyncResponse.fromXml(doc.getDocumentElement());
    }

    @Override
    public Response send() throws AuthenticationException, YabException, IOException {
        SyncResponse res = (SyncResponse) super.send();
        List<Result> results = res.getResults();
        if (events.size() != results.size()) {
            throw new IOException(String.format(
                "Invalid number of results (expected %d but got %d)",
                events.size(), results.size()));
        }
        for (int i = 0; i < results.size(); i++) {
            events.get(i).setResult(results.get(i));
        }
        return res;
    }

    @Override
    public String toString() {
        return Xml.toString(toXml(Xml.newDocument()));
    }
}
