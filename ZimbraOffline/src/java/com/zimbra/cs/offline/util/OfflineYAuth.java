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
package com.zimbra.cs.offline.util;

import com.zimbra.cs.util.yauth.RawAuthManager;
import com.zimbra.cs.util.yauth.MetadataTokenStore;
import com.zimbra.cs.util.yauth.Authenticator;
import com.zimbra.cs.util.yauth.TokenStore;
import com.zimbra.cs.util.yauth.RawAuth;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.datasource.DataSourceManager;
import com.zimbra.cs.offline.OfflineLC;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.common.service.ServiceException;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

public final class OfflineYAuth {
    private static final String APPID = OfflineLC.zdesktop_yauth_appid.value();
    
    private static final Map<Integer, RawAuthManager> rams =
        new HashMap<Integer, RawAuthManager>();

    public static Authenticator newAuthenticator(DataSource ds)
        throws ServiceException {
        RawAuthManager ram = getRawAuthManager(DataSourceManager.getInstance().getMailbox(ds));
        return ram.newAuthenticator(
            APPID, ds.getUsername(), ds.getDecryptedPassword());
    }

    public static RawAuth authenticate(DataSource ds)
        throws ServiceException {
        try {
            return newAuthenticator(ds).authenticate();
        } catch (IOException e) {
            throw ServiceException.FAILURE("Authentication failed", e);
        }
    }

    public static void removeToken(DataSource ds) throws ServiceException {
        TokenStore store = getRawAuthManager(DataSourceManager.getInstance().getMailbox(ds)).getTokenStore();
        store.removeToken(APPID, ds.getUsername());
    }
    
    public static void newToken(DataSource ds, String password)
        throws ServiceException {
        TokenStore store = getRawAuthManager(DataSourceManager.getInstance().getMailbox(ds)).getTokenStore();
        try {
            store.newToken(APPID, ds.getUsername(), password);
        } catch (IOException e) {
            throw ServiceException.FAILURE("Token request failed", e);
        }
    }
    
    public static RawAuthManager getRawAuthManager(Mailbox mbox)
        throws ServiceException {
        synchronized (rams) {
            int id = mbox.getId();
            RawAuthManager ram = rams.get(id);
            if (ram == null) {
                ram = new RawAuthManager(new MetadataTokenStore(mbox));
                rams.put(id, ram);
            }
            return ram;
        }
    }

    public static void deleteRawAuthManager(Mailbox mbox) {
        synchronized (rams) {
            rams.remove(mbox.getId());
        }
    }
}
