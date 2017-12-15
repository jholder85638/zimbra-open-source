/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.offline;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

public class OfflineConfigTool {

	public static void setPort(String dataRoot, int port) {
		setPortInLocalConfig(dataRoot, port);
		setPortInWebAppIni(dataRoot, port);
    }
    
	private static void setPortInLocalConfig(String dataRoot, int port) {
		String path = dataRoot + "/conf/localconfig.xml";
		try {
			String text = "";
			String line;
			
			BufferedReader in = new BufferedReader(new FileReader(path));
			boolean done = false;
			while ((line = in.readLine()) != null) {
				text = text + line + "\n";
				if (!done && line.indexOf("zimbra_admin_service_port") > 0) {
					text = text + "    <value>" + Integer.toString(port) + "</value>\n";
					in.readLine();
					done = true;
				}
			}
			in.close();
			
			writeToFile(path, text);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}		
	}
	
	private static void setPortInWebAppIni(String dataRoot, int port) {
		String path = dataRoot + "/zdesktop.webapp/webapp.ini";
		try {
			String text = "";
			String line;
			
			BufferedReader in = new BufferedReader(new FileReader(path));
			boolean done = false;
			while ((line = in.readLine()) != null) {
				if (!done && line.startsWith("uri=http://")) {
			        int pos = line.indexOf("http://");
			        if (pos > 0) {
			            pos += 7;
			            int sc, sl;
			            sc = line.indexOf(':', pos);
			            sl = line.indexOf('/', pos);
			            if (sc > 0 && sl > 0 && sl > sc)
			                line = line.substring(0, sc + 1) + Integer.toString(port) + line.substring(sl);
			        }
					done = true;
				}
				text = text + line + "\n";
			}
			in.close();
			
			writeToFile(path, text);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}		
	}
	
    private static void writeToFile(String path, String text) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            out.write(text);
            out.close();
        } catch (IOException e) {
			System.out.println(e.getMessage());
        }    	
    }
}
