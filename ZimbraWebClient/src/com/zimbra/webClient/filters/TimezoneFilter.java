/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
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

package com.zimbra.webClient.filters;

import java.io.*;
import javax.servlet.*;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.kabuki.tools.tz.GenerateData;

public class TimezoneFilter implements Filter {

	//
	// Constants
	//

	static final String TIMEZONE_DATA_FILENAME = "/js/ajax/util/AjxTimezoneData.js";
	static final String TIMEZONE_ICS_FILENAME = "/opt/zimbra/conf/timezones.ics";

	static final String EXT_BACKUP = ".backup";
	static final String PRE_TEMP = TimezoneFilter.class.getName();
	static final String SUF_TEMP = ".js";

	//
	// Data
	//

	protected ServletContext context;

	//
	// Filter methods
	//

	public void doFilter(ServletRequest request, ServletResponse response,
						 FilterChain chain) throws ServletException, IOException {
		ZimbraLog.webclient.debug("%%% TimezoneFilter#doFilter");
		updateTimezoneData();
		chain.doFilter(request, response);
	}

	public void init(FilterConfig config) {
		ZimbraLog.webclient.debug("%%% TimezoneFilter#init");
		this.context = config.getServletContext();
		updateTimezoneData();
	}

	public void destroy() {}

	//
	// Protected methods
	//

	protected synchronized void updateTimezoneData() {
		ZimbraLog.webclient.debug("%%% TimezoneFilter#updateTimezoneData");
		File ftemp = null;
		try {
			File fin = new File(TIMEZONE_ICS_FILENAME);
			File fout = new File(this.context.getRealPath(TIMEZONE_DATA_FILENAME));
			ZimbraLog.webclient.debug("%%% timezone data in:  "+fin);
			ZimbraLog.webclient.debug("%%% timezone data out: "+fout);

			// is there anything to do?
			if (fin.lastModified() - fout.lastModified() <= 0) {
				return;
			}

			// generate new data
			ZimbraLog.webclient.debug("%%% timezone data out of sync, need to regenerate");
			ftemp = File.createTempFile(PRE_TEMP, SUF_TEMP);
			GenerateData.print(fin, ftemp);

			// save backup and move generated file
			fout.renameTo(new File(this.context.getRealPath(TIMEZONE_DATA_FILENAME+EXT_BACKUP)));
			ftemp.renameTo(fout);
			ZimbraLog.webclient.debug("%%% done");
		}
		catch (Exception e) {
			ZimbraLog.webclient.debug("%%% timezone data error", e);
			if (ftemp != null) {
				ftemp.delete();
			}
		}
	}

} // class TimezoneFilter
