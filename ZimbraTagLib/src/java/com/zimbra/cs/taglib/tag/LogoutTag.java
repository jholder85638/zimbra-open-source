/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2013, 2014 Zimbra, Inc.
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
package com.zimbra.cs.taglib.tag;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import com.zimbra.client.ZMailbox;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.ZimbraAuthToken;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.cs.taglib.ZJspSession;

public class LogoutTag extends ZimbraSimpleTag {

    @Override
    public void doTag() throws JspException, IOException {
        JspContext jctxt = getJspContext();
        PageContext pageContext = (PageContext) jctxt;
        ZAuthToken authToken = ZJspSession.getAuthToken(pageContext);
        try {
        	if(authToken != null) {
        		AuthToken at = ZimbraAuthToken.getAuthToken(authToken.getValue());
        		Account acc = at.getAccount();
        		if(at != null && acc != null) {
                    //Force a authRequest with csrfSupported=1 to get the csrfToken. EndSessionRequest would need it.
    		        ZMailbox mbox = ZMailbox.getByAuthToken(authToken, URLUtil.getSoapURL(acc.getServer(), false), true, true);
    		        mbox.logout();//this invalidates the session in mailbox app
        		}
        	}
        } catch (ServiceException e) {
            if (!ServiceException.AUTH_EXPIRED.equals(e.getCode())) { //don't throw an exception if this token has already been invalidated
                throw new JspTagException(e.getMessage(), e);
            }
		} catch (AuthTokenException e) {
		    throw new JspTagException(e.getMessage(), e);
        } finally {
	        HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
	        ZAuthToken.clearCookies(response);
	        ZJspSession.clearSession(pageContext);
		}
    }
}
