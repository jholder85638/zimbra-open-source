/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.ajax.tests.main.login;

import org.testng.annotations.Test;
import com.zimbra.qa.selenium.framework.util.HarnessException;
import com.zimbra.qa.selenium.framework.util.SleepUtil;
import com.zimbra.qa.selenium.framework.util.ZAssert;
import com.zimbra.qa.selenium.framework.util.ZimbraAccount;
import com.zimbra.qa.selenium.framework.util.ZimbraAdminAccount;
import com.zimbra.qa.selenium.framework.util.ZimbraSeleniumProperties;
import com.zimbra.qa.selenium.framework.util.staf.StafServicePROCESS;
import com.zimbra.qa.selenium.projects.ajax.core.AjaxCommonTest;

public class LoginWithCsrfTokenCheckDisabled extends AjaxCommonTest {

	public LoginWithCsrfTokenCheckDisabled() {
		logger.info("New "+ LoginWithCsrfTokenCheckDisabled.class.getCanonicalName());

		// All tests start at the login page
		super.startingPage = app.zPageLogin;
		super.startingAccountPreferences = null;

	}

	@Test(	description = "Login to the webclient after disabling csrf check",
			groups = { "smoke" })
	public void LoginWithCsrfTokenCheckDisabled_01() throws HarnessException {
		try {

			String zimbraCsrfTokenCheckEnabledValue = "FALSE";

			// Change zimbraCsrfTokenCheckEnabled value to false
			ZimbraAdminAccount.GlobalAdmin().soapSend(
					"<ModifyConfigRequest xmlns='urn:zimbraAdmin'>"
							+		"<a n='zimbraCsrfTokenCheckEnabled'>"+ zimbraCsrfTokenCheckEnabledValue + "</a>"
							+	"</ModifyConfigRequest>");

			// Restart zimbra services
			StafServicePROCESS staf = new StafServicePROCESS();
			staf.execute("zmmailboxdctl restart");

			// Wait for the service to come up
			SleepUtil.sleep(60000);

			staf.execute("zmcontrol status");

			SleepUtil.sleepMedium();

			// Login
			app.zPageLogin.zLogin(ZimbraAccount.AccountZWC());

			// Verify main page becomes active
			ZAssert.assertTrue(app.zPageMain.zIsActive(), "Verify that the account is logged in");
		}

		finally {
		
			String zimbraCsrfTokenCheckEnabledValue = "TRUE";
			
			// Change zimbraCsrfTokenCheckEnabled value to false
						ZimbraAdminAccount.GlobalAdmin().soapSend(
								"<ModifyConfigRequest xmlns='urn:zimbraAdmin'>"
										+		"<a n='zimbraCsrfTokenCheckEnabled'>"+ zimbraCsrfTokenCheckEnabledValue + "</a>"
										+	"</ModifyConfigRequest>");

						StafServicePROCESS staf = new StafServicePROCESS();
						staf.execute("zmmailboxdctl restart");
						
			// Open the base URL
			app.zPageLogin.sOpen(ZimbraSeleniumProperties.getBaseURL());
		}

	}
}