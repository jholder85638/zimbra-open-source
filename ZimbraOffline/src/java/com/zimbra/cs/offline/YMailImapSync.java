/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010, 2013, 2014, 2016 Synacor, Inc.
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

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.datasource.imap.ImapSync;
import com.zimbra.cs.offline.common.OfflineConstants;
import com.zimbra.cs.offline.util.OfflineYAuth;
import com.zimbra.cs.util.yauth.Authenticator;
import com.zimbra.cs.util.yauth.XYMEAuthenticator;

import javax.security.auth.login.LoginException;
import java.io.IOException;

/*
 * Supports Yahoo XYMEAuthentication mechanism for faster login.
 */
class YMailImapSync extends ImapSync {
    YMailImapSync(DataSource ds) throws ServiceException {
        super(ds);
        setReuseConnections(false);
    }

    @Override
    protected void connect() throws ServiceException {
        Authenticator auth = OfflineYAuth.newAuthenticator(getDataSource());
        setAuthenticator(newXYMEAuthenticator(auth));
        try {
            super.connect();
        } catch (ServiceException e) {
            if (!isAuthError(e)) throw e;
            ZimbraLog.datasource.debug("Invalidating possibly expired cookie and retrying auth");
            auth.invalidate();
            setAuthenticator(newXYMEAuthenticator(auth));
            super.connect();
        }
    }

    private XYMEAuthenticator newXYMEAuthenticator(Authenticator auth)
        throws ServiceException {
        try {
            return new XYMEAuthenticator(auth.authenticate(), OfflineConstants.YMAIL_PARTNER_NAME);
        } catch (IOException e) {
            throw ServiceException.FAILURE("Authentication error", e);
        }
    }

    private static boolean isAuthError(ServiceException e) {
        Throwable cause = e.getCause();
        return cause == null || cause instanceof LoginException;
    }
}
