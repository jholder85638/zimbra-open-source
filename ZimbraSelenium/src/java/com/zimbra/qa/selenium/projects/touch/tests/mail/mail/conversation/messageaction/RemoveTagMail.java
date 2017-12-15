/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2015, 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.touch.tests.mail.mail.conversation.messageaction;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.items.MailItem;
import com.zimbra.qa.selenium.framework.items.TagItem;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.touch.core.PrefGroupMailByConversationTest;

public class RemoveTagMail extends PrefGroupMailByConversationTest {

	public RemoveTagMail() {
		logger.info("New "+ DeleteMail.class.getCanonicalName());
	}
	
	@Test( description = "Cancel tag operation in message view",
			groups = { "functional" })
			
	public void RemoveTagMail_01() throws HarnessException {
		
		String subject = "subject" + ZimbraSeleniumProperties.getUniqueString();
		String body = "text <strong>bold"+ ZimbraSeleniumProperties.getUniqueString() +"</strong> text";
		String htmlBody = XmlStringUtil.escapeXml(
				"<html>" +
					"<head></head>" +
					"<body>"+ body +"</body>" +
				"</html>");

		// Send a message to the account
		ZimbraAccount.AccountA().soapSend(
				"<SendMsgRequest xmlns='urn:zimbraMail'>" +
					"<m>" +
						"<e t='t' a='"+ app.zGetActiveAccount().EmailAddress +"'/>" +
						"<e t='c' a='"+ ZimbraAccount.AccountB().EmailAddress +"'/>" +
						"<su>"+ subject +"</su>" +
						"<mp ct='multipart/alternative'>" +
						"<mp ct='text/plain'>" +
							"<content>"+ body +"</content>" +
						"</mp>" +
						"<mp ct='text/html'>" +
							"<content>"+ htmlBody +"</content>" +
						"</mp>" +
					"</mp>" +
					"</m>" +
				"</SendMsgRequest>");
		app.zPageMail.zRefresh();
		
		// Create a tag
		TagItem tagItem = TagItem.CreateUsingSoap(app.zGetActiveAccount());
		String tagName = tagItem.getName();
		
		// Tag a mail
		app.zPageMail.zConversationListItem(Button.B_CONVERSATION_ACTION_DROPDOWN, subject);
		app.zPageMail.zListItem(Button.B_TAG_CONVERSATION);
		app.zTreeMail.zSelectTag(tagName);
		app.zTreeMail.zSelectMailTag(tagName);
		app.zPageMail.zCancelMailAction(Button.B_REMOVE_TAG_MAIL);
		
		// SOAP verification
		MailItem actual = MailItem.importFromSOAP(app.zGetActiveAccount(),"tag:"+tagItem.getName()+" AND subject:(" + subject + ")");
        ZAssert.assertNull(actual, "Verify the mail is not with the tag");
		
	}
}