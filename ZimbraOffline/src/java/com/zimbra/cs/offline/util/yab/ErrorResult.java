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
import com.zimbra.cs.offline.util.Xml;

public class ErrorResult extends Result {
    private int code;
    private String userMessage;
    private String debugMessage;
    private int retryAfter = -1;

    public static final int CODE_EXPIRED_COOKIES = 17005;
    public static final int CODE_CLIENT_TIMED_OUT = 17010;
    public static final int CODE_SERVER_DOWN = 17011;
    public static final int CODE_USER_LOCKED = 50302;
    public static final int CODE_CONTACT_DOES_NOT_EXIST = -40402;

    public static final String TAG = "error";

    private static final String CODE = "code";
    private static final String USER_MESSAGE = "user-message";
    private static final String DEBUG_MESSAGE = "debug-message";
    private static final String RETRY_AFTER = "retry-after";

    private ErrorResult() {}

    @Override
    public boolean isError() { return true; }
    
    public int getCode() {
        return code;
    }

    public boolean isTemporary() {
        return retryAfter > 0;
    }
    
    public String getUserMessage() {
        return userMessage;
    }

    public int getRetryAfter() {
        return retryAfter;
    }

    public String getDebugMessage() {
        return debugMessage;
    }
 
    public static ErrorResult fromXml(Element e) {
        return new ErrorResult().parseXml(e);
    }

    private ErrorResult parseXml(Element e) {
        assert e.getTagName().equals(TAG);
        for (Element child : Xml.getChildren(e)) {
            String name = child.getTagName();
            if (name.equals(CODE)) {
                code = Xml.getIntValue(child);
            } else if (name.equals(USER_MESSAGE)) {
                userMessage = Xml.getTextValue(child);
            } else if (name.equals(DEBUG_MESSAGE)) {
                debugMessage = Xml.getTextValue(child);
            } else if (name.equals(RETRY_AFTER)) {
                retryAfter = Xml.getIntValue(child);
            } else {
                throw new IllegalArgumentException(
                    "Invalid '" + TAG + "' result element: " + name);
            }
        }
        return this;
    }

    @Override
    public Element toXml(Document doc) {
        Element e = doc.createElement(TAG);
        if (code != -1) {
            Xml.appendElement(e, CODE, code);
        }
        if (userMessage != null) {
            Xml.appendElement(e, USER_MESSAGE, userMessage);
        }
        if (debugMessage != null) {
            Xml.appendElement(e, DEBUG_MESSAGE, debugMessage);
        }
        if (retryAfter != -1) {
            Xml.appendElement(e, RETRY_AFTER, retryAfter);
        }
        return e;
    }                                  

    public String toString() {
        return String.format("%s (code = %d)", userMessage, code);
    }
}
