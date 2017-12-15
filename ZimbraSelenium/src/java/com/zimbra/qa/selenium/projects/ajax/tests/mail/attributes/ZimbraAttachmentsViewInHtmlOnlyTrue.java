/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2013, 2014, 2015, 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.ajax.tests.mail.attributes;

import java.io.*;
import java.util.*;

import org.testng.annotations.*;

import com.zimbra.qa.selenium.framework.items.*;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.*;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.*;

public class ZimbraAttachmentsViewInHtmlOnlyTrue extends PrefGroupMailByMessageTest {

	
	
	public ZimbraAttachmentsViewInHtmlOnlyTrue() {
		
		super.startingAccountPreferences.put("zimbraAttachmentsViewInHtmlOnly", "TRUE");
		
	}

	@Test(
			description = "Verify 'download' link does not appear when zimbraAttachmentsViewInHtmlOnly = TRUE", 
			groups = { "functional" })
	public void ZimbraAttachmentsViewInHtmlOnlyTrue_01() throws HarnessException {

		//-- DATA
		
		final String mimeFile = ZimbraSeleniumProperties.getBaseDirectory() + "/data/public/mime/email05/mime01.txt";
		final String subject = "subject151615738";
		final String attachmentname = "file.txt";
		
		LmtpInject.injectFile(app.zGetActiveAccount().EmailAddress, new File(mimeFile));
		
		
		//-- GUI
		
		
		// Refresh current view
		app.zPageMail.zVerifyMailExists(subject);

		// Select the message so that it shows in the reading pane
		DisplayMail display = (DisplayMail) app.zPageMail.zListItem(Action.A_LEFTCLICK, subject);

		
		//-- VERIFICATION
		
		List<AttachmentItem> items = display.zListGetAttachments();
		ZAssert.assertEquals(items.size(), 1, "Verify one attachment is in the message");
		
		AttachmentItem found = null;
		for ( AttachmentItem item : items ) {
			if ( item.getAttachmentName().equals(attachmentname)) {
				found = item;
				break;
			}
		}
		ZAssert.assertNotNull(found, "Verify the attachment appears in the list (by file name)");
		
		String locator = found.getLocator() + " a[id$='_download']";
		ZAssert.assertFalse(app.zPageMail.sIsElementPresent(locator), "Verify the download link is not present");


	}

	
}