/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2013, 2014, 2015, 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.ajax.tests.mail.mail;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.items.*;
import com.zimbra.qa.selenium.framework.items.FolderItem.SystemFolder;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.PrefGroupMailByMessageTest;


public class MarkUnSpamMessage extends PrefGroupMailByMessageTest {

	
	public MarkUnSpamMessage() {
		logger.info("New "+ MarkUnSpamMessage.class.getCanonicalName());
		
		
		

		
	}
	
	@Test(	description = "Mark a message as not spam, using 'Not Spam' toolbar button",
			groups = { "smoke" })
	public void MarkUnSpamMessage_01() throws HarnessException {
		
		String subject = "subject"+ ZimbraSeleniumProperties.getUniqueString();
		
		// Get the junk and inbox folder
		FolderItem junk = FolderItem.importFromSOAP(app.zGetActiveAccount(), SystemFolder.Junk);
		FolderItem inbox = FolderItem.importFromSOAP(app.zGetActiveAccount(), SystemFolder.Inbox);


		// Add a message to the account's junk folder
		app.zGetActiveAccount().soapSend(
				"<AddMsgRequest xmlns='urn:zimbraMail'>" +
            		"<m l='"+ junk.getId() +"'>" +
                		"<content>From: foo@foo.com\n" +
"To: foo@foo.com \n" +
"Subject: "+ subject +"\n" +
"MIME-Version: 1.0 \n" +
"Content-Type: text/plain; charset=utf-8 \n" +
"Content-Transfer-Encoding: 7bit\n" +
"\n" +
"simple text string in the body\n" +
"</content>" +
                	"</m>" +
            	"</AddMsgRequest>");
		
		// Get the mail item for the new message
		// Need 'is:anywhere' to include the spam folder
		MailItem mail = MailItem.importFromSOAP(app.zGetActiveAccount(), "is:anywhere subject:("+ subject +")");
		

		// Refresh current view
		app.zPageMail.zVerifyMailExists(subject);
				
		// Go to the Junk folder
		app.zTreeMail.zTreeItem(Action.A_LEFTCLICK, junk);
		
		// Select the item
		app.zPageMail.zListItem(Action.A_LEFTCLICK, mail.dSubject);
		
		// Click spam
		app.zPageMail.zToolbarPressButton(Button.B_RESPORTNOTSPAM);

		
		
		// Get the mail item for the new message
		// Need 'is:anywhere' to include the spam folder
		mail = MailItem.importFromSOAP(app.zGetActiveAccount(), "is:anywhere subject:("+ subject +")");
		ZAssert.assertNotNull(mail, "Make sure the mail is found");

		ZAssert.assertEquals(mail.dFolderId, inbox.getId(), "Verify the message is in the inbox folder");
				
	}


}
