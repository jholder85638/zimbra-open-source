/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2005, 2006 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * 
 * ***** END LICENSE BLOCK *****
 */


package com.zimbra.kabuki.servlets;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This class looks for the resource bundle for the requested file (e.g.
 * "/path/Messages.js"), resolves it, and generates a JavaScript file with
 * a class that contains all of the properties in the bundle. The servlet
 * takes into account the locale of the user request in order to load the
 * correct resource bundle.
 * <p>
 * For example, if the client requested the URL "/path/Messages.js" and
 * the locale was set to Japanese/Japan, the servlet would try to load the
 * Japanese version of the resource bundle. The base name of the bundle
 * would be just "/path/Messages" but the ResourceBundle class would
 * resolve this with the locale and look for the resource files.
 * <p>
 * Once all of the properties in the resource bundle have been resolved,
 * then the servlet iterates over the resource keys and generates a line
 * of JavaScript for each value. For example, if "/path/Messages.properties"
 * contains the following:
 * <pre>
 * one = One
 * two : Two\
 * Two
 * three = Three\
 * 		Three\
 * 		Three
 * </pre>
 * the generated JavaScript would look like this:
 * <pre>
 * function Messages() {}
 * 
 * Messages.one = "One";
 * Messages.two = "TwoTwo";
 * Messages.three = "ThreeThreeThree";
 * </pre>
 * <p>
 * <strong>Note:</strong>
 * The implementation assumes that the basename of the resource bundle
 * will always be "/msgs/" concatenated with the filename without the
 * extension.
 * 
 * @author Andy Clark
 */
public class Props2JsServlet 
	extends HttpServlet {

    //
    // Constants
    //
    
    private static final String COMPRESSED_EXT = ".zgz";

    //
    // Data
    //
    
    private Map<Locale,Map<String,byte[]>> buffers =
		new HashMap<Locale,Map<String,byte[]>>();
    
    //
    // HttpServlet methods
    //
    
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException, ServletException {
        Locale locale = getLocale(req);
        String uri = req.getRequestURI();
        
        OutputStream out = resp.getOutputStream();
        resp.setContentType("text/javascript");
        byte[] buffer = getBuffer(locale, uri);
        out.write(buffer);
        out.close();
    } // doGet(HttpServletRequest,HttpServletResponse)
    
    //
    // Private methods
    //

    private Locale getLocale(HttpServletRequest req) {
    	String language = req.getParameter("language");
    	if (language != null) {
        	String country = req.getParameter("country");
        	if (country != null) {
            	String variant = req.getParameter("variant");
            	if (variant != null) {
            		return new Locale(language, country, variant);
            	}
            	return new Locale(language, country);
        	}
        	return new Locale(language);
    	}
    	return req.getLocale();
    } // getLocale(HttpServletRequest):Locale
    
    private synchronized byte[] getBuffer(Locale locale, String uri)
	throws IOException {
        // get locale buffers
        Map<String,byte[]> localeBuffers = buffers.get(locale);
        if (localeBuffers == null) {
            localeBuffers = new HashMap<String,byte[]>();
            buffers.put(locale, localeBuffers);
        }
        
        // get byte buffer
        byte[] buffer = localeBuffers.get(uri);
        if (buffer == null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PrintStream out = uri.endsWith(COMPRESSED_EXT) 
            				? new PrintStream(new GZIPOutputStream(bos)) 
            				: new PrintStream(bos); 
            out.println("// Locale: "+locale);

			// This gets the base directory for the resource bundle
			// basename. For example, if the URI is:
			//
			//   .../msgs/I18nMsg.js
			//
			// then the basedir is "/msgs/" and if the URI is:
			//
			//   .../keys/ZmKeys.js
			//
			//       then the basedir is "/keys/".
			//
			// NOTE: The <url-pattern>s in the web.xml file restricts
			//       which URLs map to this servlet so there's no risk
			//       that the basedir will be other than what we expect.
			int lastSlash = uri.lastIndexOf('/');
			int prevSlash = uri.substring(0, lastSlash).lastIndexOf('/');
			String basedir = uri.substring(prevSlash, lastSlash + 1);

			String filenames = uri.substring(uri.lastIndexOf('/')+1);
            String classnames = filenames.substring(0, filenames.indexOf('.'));
            StringTokenizer tokenizer = new StringTokenizer(classnames, ",");
            while (tokenizer.hasMoreTokens()) {
                String classname = tokenizer.nextToken();
                load(out, locale, basedir, classname);
            }
            
            // save buffer
            out.close();
            buffer = bos.toByteArray();
            localeBuffers.put(uri, buffer);
        }

        return buffer;
    } // getBuffer(Locale,String):byte[]

    private void load(PrintStream out, Locale locale,
					  String basedir, String classname) {
        String basename = basedir+classname;

        out.println();
        out.println("// Basename: "+basename);

        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(basename, locale);
			Props2Js.convert(out, bundle, classname);
		}
        catch (MissingResourceException e) {
            out.println("// resource bundle not found");
			System.out.println("unable to load resource bundle: "+basename);
		}
		catch (IOException e) {
			out.println("// error: "+e.getMessage());
		}
	} // load(PrintStream,String)
    
} // class Props2JsServlet
