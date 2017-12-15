/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Zimlets
 * Copyright (C) 2005, 2006, 2007, 2009, 2010, 2013, 2014 Zimbra, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */

/*
 * Created on May 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.zimbra.cs.zimlet.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zimbra.cs.object.ObjectHandlerException;
import com.zimbra.cs.zimlet.ZimletConf;
import com.zimbra.cs.zimlet.ZimletConfig;
import com.zimbra.cs.zimlet.ZimletException;
import com.zimbra.cs.zimlet.ZimletHandler;

/**
 * @author schemers
 *
 * TODO: currently just recognizing UPS 1Z* version
 * 
 *  UPS Tracking Numbers appear in the following formats:
 *
 *   1Z 999 999 99 9999 999 9
 *   9999 9999 9999
 *   T999 9999 999
 *
 * we are only looking for the first format right now...
 * 
 *  
 */
public class TrackingHandler implements ZimletHandler {

    private static final String UPS = "1[zZ]\\s?\\w{3}\\s?\\w{3}\\s?\\w{2}\\s?\\w{4}\\s?\\w{3}\\s?\\w{1}";
    private static final String FEDEX = "\\d{12}";
    
    private static final Pattern TRACKING = Pattern.compile("(\\b((" + UPS + ")|(" + FEDEX + "))\\b)");        

	public String[] match(String text, ZimletConf config) {
        Matcher t = TRACKING.matcher(text);
        List l = new ArrayList();
        while (t.find()) {
            l.add(text.substring(t.start(), t.end()));
        }
        return (String[]) l.toArray(new String[0]);
	}
    
    public static void test(TrackingHandler h, String text, boolean expected) throws ObjectHandlerException, ZimletException {
        String[] array = h.match(text, new ZimletConfig((String)null));
        boolean actual  = array.length > 0;
        if (expected != actual)
            System.out.println("["+text+"] ************** expected="+expected+" actual="+actual);
        else
            System.out.println("["+text+ "] expected="+expected);
    }

    public static void main(String args[]) throws ObjectHandlerException, ZimletException {
        TrackingHandler th = new TrackingHandler();
        test(th, "792806493666", true);
        test(th, "'792806493666'", true);        
        test(th, "\"792806493666\"", true);        
        test(th, "7928064936669", false);        
        test(th, "ups: 1Z11X1400217079799", true);
        test(th, "ups: 1Z11X14002170797999", false);        
        test(th, "ups: z1Z11X1400217079799", false);
        test(th, "ups(1Z11X1400217079799)", true);
        test(th, "The tracking number is: 792806493888.", true); 
    }
}
