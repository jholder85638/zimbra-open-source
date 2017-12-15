/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.offline.jsp;

import com.zimbra.soap.type.DataSource.ConnectionType;

public class GmailBean extends ImapBean {
    public static final String Domain = "gmail.com";

    public GmailBean() {}
	
    @Override
    protected void doRequest() {
	domain = Domain;
        if (verb != null && (verb.isAdd() || verb.isModify())) {
            if (!isEmpty(email) && email.indexOf('@') < 0)
                email += '@' + domain;
	    username = email;
        }
	host = "imap.gmail.com";
        connectionType = ConnectionType.ssl;
	port = "993";

	smtpHost = "smtp.gmail.com";
	smtpPort = "465";
	isSmtpSsl = true;
	isSmtpAuth = true;
	smtpUsername = email;
	smtpPassword = password;
	super.doRequest();
    }

    public boolean isCalendarSyncSupported() {
	return true;
    }

    public boolean isContactSyncSupported() {
	return true;
    }

    public boolean isServerConfigSupported() {
	return false;
    }

    public boolean isSmtpConfigSupported() {
	return false;
    }

    public boolean isUsernameRequired() {
	return false;
    }
}
