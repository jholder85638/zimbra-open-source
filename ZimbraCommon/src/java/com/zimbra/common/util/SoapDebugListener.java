/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2012, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.common.util;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport.HttpDebugListener;

public class SoapDebugListener implements HttpDebugListener {
    
    public interface Printer {
        // print an empty line
        public void println();
        
        // print str on a line
        public void println(String str);
    }
    
    private static class StdoutPrinter implements Printer {
        @Override
        public void println() {
            System.out.println();
        }
        
        @Override
        public void println(String str) {
            System.out.println(str);
        }
    }
    
    public enum Level {
        OFF,
        HEADER,
        BODY,
        ALL;
        
        private static boolean needsHeader(Level level) {
            return level == Level.ALL || level == Level.HEADER;
        }
        
        private static boolean needsBody(Level level) {
            return level == Level.ALL || level == Level.BODY;
        }
    }
    
    private final Level level;
    private final Printer printer;
    
    public SoapDebugListener() {
        this(null, null);
    }
    
    public SoapDebugListener(Printer printer, Level level) {
        
        if (printer == null) {
            printer = new StdoutPrinter();
        }
        
        if (level == null) {
            level = Level.BODY;
        }
        
        this.printer = printer;
        this.level = level;
    }
    
    @Override
    public void receiveSoapMessage(PostMethod postMethod, Element envelope) {
        if (level == Level.OFF) {
            return;
        }
        
        printer.println();
        printer.println("=== Response ===");
        
        if (Level.needsHeader(level)) {
            Header[] headers = postMethod.getResponseHeaders();
            for (Header header : headers) {
                printer.println(header.toString().trim()); // trim the ending crlf
            }
            printer.println();
        }
        
        if (Level.needsBody(level)) {
            printer.println(envelope.prettyPrint());
        }
    }

    @Override
    public void sendSoapMessage(PostMethod postMethod, Element envelope, HttpState httpState) {
        if (level == Level.OFF) {
            return;
        }
        
        printer.println();
        printer.println("=== Request ===");
        
        if (Level.needsHeader(level)) {
            try {
                URI uri = postMethod.getURI();
                printer.println(uri.toString());
            } catch (URIException e) {
                e.printStackTrace();
            }
            
            // headers
            Header[] headers = postMethod.getRequestHeaders();
            for (Header header : headers) {
                printer.println(header.toString().trim()); // trim the ending crlf
            }
            printer.println();
            
            //cookies
            if (httpState != null) {
                Cookie[] cookies = httpState.getCookies();
                for (Cookie cookie : cookies) {
                    printer.println("Cookie: " + cookie.toString());
                }
            }
            printer.println();
        }
        
        if (Level.needsBody(level)) {
            printer.println(envelope.prettyPrint());
        }
    }
}
