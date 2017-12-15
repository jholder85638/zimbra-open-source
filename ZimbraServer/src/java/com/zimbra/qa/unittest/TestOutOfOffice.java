/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2005, 2006, 2007, 2009, 2010, 2011, 2013, 2014 Zimbra, Inc.
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

package com.zimbra.qa.unittest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.zimbra.client.ZEmailAddress;
import com.zimbra.client.ZMailbox;
import com.zimbra.client.ZMessage;
import com.zimbra.common.account.ProvisioningConstants;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbOutOfOffice;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.util.ProvisioningUtil;

/**
 * Tests out-of-office notification.  All tests must be run inside the server, because
 * the cleanup code needs to delete notification rows from the database table.
 */
public class TestOutOfOffice {

    private DbConnection mConn;
    private Mailbox mMbox;

    private static String NAME_PREFIX = TestOutOfOffice.class.getSimpleName();
    private static String RECIPIENT_NAME = "testoutofoffice-recipient";
    private static String SENDER_NAME = "testoutofoffice-sender";
    private static String RECIPIENT1_ADDRESS = "testoutofoffice1@example.zimbra.com";
    private static String RECIPIENT2_ADDRESS = "testoutofoffice2@example.zimbra.com";
    private boolean originalLCSetting = false;

    @Before
    public void setUp() throws Exception {
        cleanupAccounts();
        originalLCSetting = ProvisioningUtil.getServerAttribute(Provisioning.A_zimbraIndexManualCommit, true);
        Provisioning.getInstance().getLocalServer().setIndexManualCommit(true);
        TestUtil.createAccount(RECIPIENT_NAME);
        TestUtil.createAccount(SENDER_NAME);
        TestUtil.sendMessage(TestUtil.getZMailbox(SENDER_NAME), RECIPIENT_NAME, "message to init recipient's mailbox");
        TestUtil.sendMessage(TestUtil.getZMailbox(RECIPIENT_NAME), SENDER_NAME, "message to init sender's mailbox");
        mMbox = TestUtil.getMailbox(RECIPIENT_NAME);
        mConn = DbPool.getConnection();
    }

    @After
    public void tearDown()
    throws Exception {
        DbPool.quietClose(mConn);
        cleanupAccounts();
        Provisioning.getInstance().getLocalServer().setIndexManualCommit(originalLCSetting);
    }

    private void cleanupAccounts() throws Exception {
        TestUtil.deleteAccount(SENDER_NAME);
        TestUtil.deleteAccount(RECIPIENT_NAME);
    }

    @Test
    public void testRowExists() throws Exception {
        long fiveDaysAgo = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 5) - 100000;

        DbOutOfOffice.setSentTime(mConn, mMbox, RECIPIENT1_ADDRESS, fiveDaysAgo);
        mConn.commit();
        assertFalse("1 day", DbOutOfOffice.alreadySent(mConn, mMbox, RECIPIENT1_ADDRESS, 1 * Constants.MILLIS_PER_DAY));
        assertFalse("4 days", DbOutOfOffice.alreadySent(mConn, mMbox, RECIPIENT1_ADDRESS, 4 * Constants.MILLIS_PER_DAY));
        assertFalse("5 days", DbOutOfOffice.alreadySent(mConn, mMbox, RECIPIENT1_ADDRESS, 5 * Constants.MILLIS_PER_DAY));
        assertTrue("6 days", DbOutOfOffice.alreadySent(mConn, mMbox, RECIPIENT1_ADDRESS, 6 * Constants.MILLIS_PER_DAY));
        assertTrue("100 days", DbOutOfOffice.alreadySent(mConn, mMbox, RECIPIENT1_ADDRESS, 100 * Constants.MILLIS_PER_DAY));
    }

    @Test
    public void testRowDoesntExist() throws Exception {
        assertFalse("1 day", DbOutOfOffice.alreadySent(mConn, mMbox, RECIPIENT1_ADDRESS, 1 * Constants.MILLIS_PER_DAY));
        assertFalse("5 days", DbOutOfOffice.alreadySent(mConn, mMbox, RECIPIENT1_ADDRESS, 5 * Constants.MILLIS_PER_DAY));
        assertFalse("100 days", DbOutOfOffice.alreadySent(mConn, mMbox, RECIPIENT1_ADDRESS, 100 * Constants.MILLIS_PER_DAY));
    }

    @Test
    public void testPrune() throws Exception {
        long fiveDaysAgo = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 5) - 100000;
        long sixDaysAgo = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 6) - 100000;

        DbOutOfOffice.setSentTime(mConn, mMbox, RECIPIENT1_ADDRESS, fiveDaysAgo);
        DbOutOfOffice.setSentTime(mConn, mMbox, RECIPIENT2_ADDRESS, sixDaysAgo);
        mConn.commit();

        // Prune the entry for 6 days ago
        DbOutOfOffice.prune(mConn, 6 * Constants.MILLIS_PER_DAY);
        mConn.commit();

        // Make sure that the later entry is still there and the earlier one is gone
        assertTrue("recipient1", DbOutOfOffice.alreadySent(mConn, mMbox, RECIPIENT1_ADDRESS, 6 * Constants.MILLIS_PER_DAY));
        assertFalse("recipient2", DbOutOfOffice.alreadySent(mConn, mMbox, RECIPIENT2_ADDRESS, 7 * Constants.MILLIS_PER_DAY));
    }

    /**
     * Confirms that out-of-office notifications use the user's address preference
     * (bug 40869).
     */
    @Test
    public void testPrefFromAddress()
    throws Exception {
        String newFromAddress = TestUtil.getAddress("testPrefFromAddress");
        String newFromDisplay = NAME_PREFIX + " testPrefFromAddress";
        String newReplyToAddress = TestUtil.getAddress("testReplyToAddress");
        String newReplyToDisplay = NAME_PREFIX + " testReplyToAddress";

        // Turn on out-of-office.
        Account recipient = TestUtil.getAccount(RECIPIENT_NAME);
        long now = System.currentTimeMillis();
        recipient.setPrefOutOfOfficeFromDate(new Date(now));
        recipient.setPrefOutOfOfficeUntilDate(new Date(now + Constants.MILLIS_PER_DAY));
        recipient.setPrefOutOfOfficeReplyEnabled(true);

        // Don't allow any From address, set display name pref only.
        recipient.setAllowAnyFromAddress(false);
        recipient.setPrefFromDisplay(newFromDisplay);
        recipient.setPrefReplyToAddress(newReplyToAddress);
        recipient.setPrefReplyToDisplay(newReplyToDisplay);
        recipient.setPrefReplyToEnabled(false);
        String subject = NAME_PREFIX + " testPrefFromAddress 1";
        ZMailbox senderMbox = TestUtil.getZMailbox(SENDER_NAME);
        TestUtil.sendMessage(senderMbox, RECIPIENT_NAME, subject);
        ZMailbox recipientMbox = TestUtil.getZMailbox(RECIPIENT_NAME);
        TestUtil.waitForMessage(recipientMbox, "in:inbox subject:\"" + subject + "\"");
        ZMessage reply = TestUtil.waitForMessage(senderMbox, "in:inbox subject:\"" + subject + "\"");

        // Validate addresses.
        ZEmailAddress fromAddress = getAddress(reply, ZEmailAddress.EMAIL_TYPE_FROM);
        assertEquals(recipient.getName(), fromAddress.getAddress());
        assertEquals(newFromDisplay, fromAddress.getPersonal());
        assertNull(getAddress(reply, ZEmailAddress.EMAIL_TYPE_REPLY_TO));

        DbOutOfOffice.clear(mConn, mMbox);
        mConn.commit();

        // Don't allow any From address, set display name, from address, and reply-to prefs.
        recipient.setPrefFromAddress(newFromAddress);
        recipient.setAllowAnyFromAddress(false);
        recipient.setPrefReplyToEnabled(true);
        subject = NAME_PREFIX + " testPrefFromAddress 2";
        TestUtil.sendMessage(senderMbox, RECIPIENT_NAME, subject);
        TestUtil.waitForMessage(recipientMbox, "in:inbox subject:\"" + subject + "\"");
        reply = TestUtil.waitForMessage(senderMbox, "in:inbox subject:\"" + subject + "\"");

        // Validate addresses.
        fromAddress = getAddress(reply, ZEmailAddress.EMAIL_TYPE_FROM);
        assertEquals(recipient.getName(), fromAddress.getAddress());
        assertEquals(recipient.getDisplayName(), fromAddress.getPersonal());

        ZEmailAddress replyToAddress = getAddress(reply, ZEmailAddress.EMAIL_TYPE_REPLY_TO);
        assertEquals(newReplyToAddress, replyToAddress.getAddress());
        assertEquals(newReplyToDisplay, replyToAddress.getPersonal());

        DbOutOfOffice.clear(mConn, mMbox);
        mConn.commit();

        // Allow any From address, set display name and address prefs.
        recipient.setAllowAnyFromAddress(true);
        subject = NAME_PREFIX + " testPrefFromAddress 3";
        TestUtil.sendMessage(senderMbox, RECIPIENT_NAME, subject);
        TestUtil.waitForMessage(recipientMbox, "in:inbox subject:\"" + subject + "\"");
        reply = TestUtil.waitForMessage(senderMbox, "in:inbox subject:\"" + subject + "\"");
        ZimbraLog.test.info("Second reply:\n" + TestUtil.getContent(senderMbox, reply.getId()));

        // Validate addresses.
        fromAddress = getAddress(reply, ZEmailAddress.EMAIL_TYPE_FROM);
        assertEquals(newFromAddress, fromAddress.getAddress());
        assertEquals(newFromDisplay, fromAddress.getPersonal());

        replyToAddress = getAddress(reply, ZEmailAddress.EMAIL_TYPE_REPLY_TO);
        assertEquals(newReplyToAddress, replyToAddress.getAddress());
        assertEquals(newReplyToDisplay, replyToAddress.getPersonal());
    }

    /**
     * Tests fix for bug 26818 (OOO message does not work when
     * "Don't keep a local copy of messages" is checked)
     *
     * @throws Exception
     */
    @Test
    public void testOOOWhenForwardNoDelivery() throws Exception {

        Account recipientAcct = TestUtil.getAccount(RECIPIENT_NAME);
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(Provisioning.A_zimbraPrefOutOfOfficeReplyEnabled, ProvisioningConstants.TRUE);
        attrs.put(Provisioning.A_zimbraPrefOutOfOfficeReply, "I am OOO");
        attrs.put(Provisioning.A_zimbraPrefMailForwardingAddress, "abc@xyz.com");
        attrs.put(Provisioning.A_zimbraPrefMailLocalDeliveryDisabled, ProvisioningConstants.TRUE);
        Provisioning.getInstance().modifyAttrs(recipientAcct, attrs);

        ZMailbox senderMbox = TestUtil.getZMailbox(SENDER_NAME);
        ZMailbox recipMbox = TestUtil.getZMailbox(RECIPIENT_NAME);

        String subject = NAME_PREFIX + " testOOOWhenForwardNoDelivery";

        // Send the message
        TestUtil.sendMessage(senderMbox, RECIPIENT_NAME, subject, "testing");

        // Make sure message was not delivered since local delivery is disabled
        assertEquals(0, TestUtil.search(recipMbox, "in:inbox subject:'" + subject + "'").size());

        // But check for out-of-office reply
        TestUtil.waitForMessage(senderMbox, "in:inbox subject:'" + subject + "'");
    }

    private ZEmailAddress getAddress(ZMessage msg, String type) {
        for (ZEmailAddress address : msg.getEmailAddresses()) {
            if (address.getType().equals(type)) {
                return address;
            }
        }
        return null;
    }
}