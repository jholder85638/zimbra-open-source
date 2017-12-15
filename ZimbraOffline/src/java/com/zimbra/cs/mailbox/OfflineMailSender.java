/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Zimbra, Inc.
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
package com.zimbra.cs.mailbox;

import java.io.IOException;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.offline.OfflineLog;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.util.AccountUtil;

public class OfflineMailSender extends MailSender {

    public OfflineMailSender() {
        setTrackBadHosts(false);
    }

    @Override
    public ItemId sendMimeMessage(OperationContext octxt, Mailbox mbox,
            MimeMessage mm) throws ServiceException {
        try {
            // for messages that aren't actually *sent*, just go down the standard save-to-sent path
            if (mm.getAllRecipients() == null)
                return super.sendMimeMessage(octxt, mbox, mm);
        } catch (MessagingException me) {
            throw ServiceException.FAILURE("could not determine message recipients; aborting mail send", me);
        }

        Account acct = mbox.getAccount();
        Account authuser = octxt == null ? null : octxt.getAuthenticatedUser();
        if (authuser == null)
            authuser = acct;
        // bug 49820: Hide the "local@host.local" fake account address from From/Sender header checks.
        if (AccountUtil.isZDesktopLocalAccount(authuser.getId()))
            authuser = acct;

        try {
            // set the From, Sender, Date, Reply-To, etc. headers
            updateHeaders(mm, acct, authuser, octxt, null /* don't set originating IP in offline client */, isReplyToSender());

            // save as a draft to be sent during sync interval
            ParsedMessage pm = new ParsedMessage(mm, mm.getSentDate().getTime(),
                mbox.attachmentsIndexingEnabled());
            if (getIdentity() == null)
                setIdentity(Provisioning.getInstance().getDefaultIdentity(authuser));
            String identityId = getIdentity() == null ? null :
                getIdentity().getAttr(Provisioning.A_zimbraPrefIdentityId);
            int draftId = mbox.saveDraft(octxt, pm, Mailbox.ID_AUTO_INCREMENT,
                (getOriginalMessageId() != null ? getOriginalMessageId().toString(acct) : null), getReplyType(),
                identityId, acct.getId(), 0).getId();
            mbox.move(octxt, draftId, MailItem.Type.MESSAGE, DesktopMailbox.ID_FOLDER_OUTBOX);
            // we can now purge the uploaded attachments
            if (getUploads() != null)
                FileUploadServlet.deleteUploads(getUploads());

            // update contact rankings
            Address[] rcpts = getRecipients(mm);
            if (rcpts != null && rcpts.length > 0) {
                ContactRankings.increment(acct.getId(), rcpts);
            }

            return new ItemId(mbox, draftId);
        } catch (MessagingException me) {
            OfflineLog.offline.warn("exception occurred during SendMsg", me);
            throw ServiceException.FAILURE("MessagingException", me);
        } catch (IOException ioe) {
            OfflineLog.offline.warn("exception occured during send msg", ioe);
            throw ServiceException.FAILURE("IOException", ioe);
        }
    }

    @Override
    public void checkMTAConnection() throws ServiceException {
        //do nothing; in ZD we don't have a direct connection to MTA
    }
}
