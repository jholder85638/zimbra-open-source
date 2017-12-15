/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.account.offline;

import org.junit.Assert;
import org.junit.Test;

public class OfflineAccountTest {

    private static class MockOfflineAccount extends OfflineAccount {

        private String mail = "";
        private boolean isGal = false;
        private String id = "";

        public MockOfflineAccount(boolean isGal, String mail, String id) {
            super(mail, "", null, null, null, null);
            this.isGal = isGal;
            this.mail = mail;
            this.id = id;
        }

        @Override
        public String getMail() {
            return this.mail;
        }

        @Override
        public boolean isGalAccount() {
            return this.isGal;
        }

        @Override
        public String getId() {
            return this.id;
        }
    }

    private String zimbraId = "3ed11feb-4d65-47c3-9fb3-45da9b80fcfa";
    private OfflineAccount galAccount = new MockOfflineAccount(true, "offline_gal@zimbra.com__OFFLINE_GAL__", zimbraId);
    private OfflineAccount nonGalAccount = new MockOfflineAccount(false, "dogfood-test@zimbra.com", zimbraId);

    @Test
    public void testGetDomain() {
        Assert.assertEquals("zimbra.com", galAccount.getDomain());
        Assert.assertEquals("zimbra.com", nonGalAccount.getDomain());
        Assert.assertEquals(galAccount, nonGalAccount);
    }
}