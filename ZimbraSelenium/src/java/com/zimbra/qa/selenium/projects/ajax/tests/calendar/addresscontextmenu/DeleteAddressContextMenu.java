/*
 * ***** BEGIN LICENSE BLOCK *****
 *
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
 *
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.qa.selenium.projects.ajax.tests.calendar.addresscontextmenu;

//package com.zimbra.qa.selenium.projects.ajax.tests.mail.compose.contextmenu;



import org.testng.annotations.*;

import com.zimbra.qa.selenium.framework.items.*;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.PrefGroupMailByMessageTest;
import com.zimbra.qa.selenium.projects.ajax.ui.DialogWarning;
import com.zimbra.qa.selenium.projects.ajax.ui.DialogWarning.DialogWarningID;
import com.zimbra.qa.selenium.projects.ajax.ui.calendar.FormApptNew;
import com.zimbra.qa.selenium.projects.ajax.ui.calendar.PageCalendar.Locators;


public class DeleteAddressContextMenu extends PrefGroupMailByMessageTest {

	public DeleteAddressContextMenu() {
		logger.info("New " + DeleteAddressContextMenu.class.getCanonicalName());

		super.startingPage = app.zPageCalendar;

	}

	@Test(description = "Right click To bubble address>>Delete", groups = { "smoke" })
	public void DeleteAttendeeContextMenu() throws HarnessException {

		String apptAttendee1,apptContent;
		AppointmentItem appt = new AppointmentItem();
		apptAttendee1 = ZimbraAccount.AccountA().EmailAddress;
		apptContent = ZimbraSeleniumProperties.getUniqueString();
		appt.setAttendees(apptAttendee1);
		appt.setContent(apptContent);

		FormApptNew apptForm = (FormApptNew) app.zPageCalendar
				.zToolbarPressButton(Button.B_NEW);
		apptForm.zFill(appt);

		app.zPageCalendar.zRightClickAddressBubble();
		app.zPageMail.DeleteAddressContextMenu();

		ZAssert.assertTrue(app.zPageMail.sIsElementPresent("css=td[id='zcs1_person'] div div[class='addrBubbleHolder-empty']"),"Attendee should be empty");
		app.zPageCalendar.zClickAt(Locators.CloseAptTab,"");

		DialogWarning warning = (DialogWarning) app.zPageMain.zGetWarningDialog(DialogWarningID.SaveChanges);
		if ( warning.zIsActive() ) {
			warning.zClickButton(Button.B_NO);
		}



	}

	@Test(description = "Right click To bubble address>>Delete (Keyboard shortcut)", groups = { "smoke" })
	public void DeleteAttendeeContextMenu_DeleteShortcut() throws HarnessException {

		String apptAttendee1,apptContent;
		AppointmentItem appt = new AppointmentItem();
		apptAttendee1 = ZimbraAccount.AccountA().EmailAddress;
		apptContent = ZimbraSeleniumProperties.getUniqueString();
		appt.setAttendees(apptAttendee1);
		appt.setContent(apptContent);

		FormApptNew apptForm = (FormApptNew) app.zPageCalendar
				.zToolbarPressButton(Button.B_NEW);
		apptForm.zFill(appt);

		app.zPageCalendar.sMouseOut(Locators.AttendeeBubbleAddr);
		app.zPageCalendar.sClickAt(Locators.AttendeeBubbleAddr,"");
		app.zPageCalendar.sKeyDown(Locators.AttendeeBubbleAddr, "46");
		ZAssert.assertTrue(app.zPageMail.sIsElementPresent("css=td[id='zcs1_person'] div div[class='addrBubbleHolder-empty']"),"Attendee should be empty");
		app.zPageCalendar.zClickAt(Locators.CloseAptTab,"");

		DialogWarning warning = (DialogWarning) app.zPageMain.zGetWarningDialog(DialogWarningID.SaveChanges);
		if ( warning.zIsActive() ) {
			warning.zClickButton(Button.B_NO);
		}

	}

	@Test(description = "Right click To bubble address>>Delete (BackSpace shortcut)", groups = { "smoke" })
	public void DeleteAttendeeContextMenu_Backspace() throws HarnessException {

		String apptAttendee1,apptContent;
		AppointmentItem appt = new AppointmentItem();
		apptAttendee1 = ZimbraAccount.AccountA().EmailAddress;
		apptContent = ZimbraSeleniumProperties.getUniqueString();
		appt.setAttendees(apptAttendee1);
		appt.setContent(apptContent);

		FormApptNew apptForm = (FormApptNew) app.zPageCalendar
				.zToolbarPressButton(Button.B_NEW);
		apptForm.zFill(appt);

		app.zPageCalendar.sMouseOut(Locators.AttendeeBubbleAddr);
		//	app.zPageCalendar.sMouseOver(Locators.AttendeeBubbleAddr);
		app.zPageCalendar.sClickAt(Locators.AttendeeBubbleAddr,"");

		//Backspace delete keycode=8
		app.zPageCalendar.zKeyDown("8");
		//app.zPageCalendar.sKeyDown(Locators.AttendeeBubbleAddr, "8");
		ZAssert.assertTrue(app.zPageMail.sIsElementPresent("css=td[id='zcs1_person'] div div[class='addrBubbleHolder-empty']"),"Attendee should be empty");
		app.zPageCalendar.zClickAt(Locators.CloseAptTab,"");

		DialogWarning warning = (DialogWarning) app.zPageMain.zGetWarningDialog(DialogWarningID.SaveChanges);
		if ( warning.zIsActive() ) {
			warning.zClickButton(Button.B_NO);
		}



	}


}