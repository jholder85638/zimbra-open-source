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
package com.zimbra.qa.selenium.projects.admin.tests.Bugs;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.core.Bugs;
import com.zimbra.qa.selenium.framework.ui.Action;
import com.zimbra.qa.selenium.framework.ui.Button;
import com.zimbra.qa.selenium.framework.util.HarnessException;
import com.zimbra.qa.selenium.framework.util.SleepUtil;
import com.zimbra.qa.selenium.framework.util.ZAssert;
import com.zimbra.qa.selenium.framework.util.ZimbraAdminAccount;
import com.zimbra.qa.selenium.framework.util.ZimbraSeleniumProperties;
import com.zimbra.qa.selenium.projects.admin.core.AdminCommonTest;
import com.zimbra.qa.selenium.projects.admin.items.AccountItem;
import com.zimbra.qa.selenium.projects.admin.items.CosItem;
import com.zimbra.qa.selenium.projects.admin.items.DistributionListItem;
import com.zimbra.qa.selenium.projects.admin.items.DomainItem;
import com.zimbra.qa.selenium.projects.admin.ui.PageEditAccount;
import com.zimbra.qa.selenium.projects.admin.ui.PageEditCOS;
import com.zimbra.qa.selenium.projects.admin.ui.PageEditDistributionList;
import com.zimbra.qa.selenium.projects.admin.ui.PageMain;
import com.zimbra.qa.selenium.projects.admin.ui.PageManageDomains;
import com.zimbra.qa.selenium.projects.admin.ui.PageManageGlobalSettings;
import com.zimbra.qa.selenium.projects.admin.ui.PageSearchResults;

public class Bug_102850 extends AdminCommonTest {
	public Bug_102850() {
		logger.info("New "+ Bug_102850.class.getCanonicalName());		
	}

	/**
	 * Testcase : Bug 102850 - Grantee name is disabled while adding ACL in account
	 * Steps :
	 *1. Login to admin console
	 *2. Edit any account
	 *3. Click on ACL >> click on add
	 *4. Verify Grantee name field
	 * @throws HarnessException
	 */
	@Bugs( ids = "102850")
	@Test(	description = "Verify grantee name is enabled while adding ACL in account",
			groups = { "smoke" })
	public void Bug_102850_Grantee_Name_At_Account() throws HarnessException {

		// Create a new account in the Admin Console using SOAP
		AccountItem account = new AccountItem("email" + ZimbraSeleniumProperties.getUniqueString(),ZimbraSeleniumProperties.getStringProperty("testdomain"));
		ZimbraAdminAccount.AdminConsoleAdmin().soapSend(
				"<CreateAccountRequest xmlns='urn:zimbraAdmin'>"
						+			"<name>" + account.getEmailAddress() + "</name>"
						+			"<password>test123</password>"
						+		"</CreateAccountRequest>");
		
		//Navigate to accounts page
		app.zPageManageAccounts.zNavigateTo();

		// Refresh the account list
		app.zPageManageAccounts.sClickAt(PageMain.Locators.REFRESH_BUTTON, "");

		// Enter the search string to find the account
		app.zPageSearchResults.zAddSearchQuery(account.getEmailAddress());

		// Click search
		app.zPageSearchResults.zToolbarPressButton(Button.B_SEARCH);
		SleepUtil.sleepMedium();


		// Click on account to be edited
		app.zPageSearchResults.zListItem(Action.A_LEFTCLICK, account.getEmailAddress());

		// Click on Edit button
		app.zPageSearchResults.setType(PageSearchResults.TypeOfObject.ACCOUNT);
		app.zPageSearchResults.zToolbarPressPulldown(Button.B_GEAR_BOX, Button.O_EDIT);
		SleepUtil.sleepMedium();

		//Click on ACL tab
		app.zPageEditAccount.zClickAt(PageEditAccount.ztab_ACCOUNT_EDIT_ACL,"");

		//Click on Add button
		app.zPageEditAccount.zClickAt(PageEditAccount.ACCOUNT_EDIT_ACL_ADD,"");

		//Verify Grantee name is enabled
		ZAssert.assertEquals(app.zPageEditAccount.zIsElementDisabled(PageEditAccount.ACCOUNT_EDIT_ACL_GRANTEE_NAME), false, "Verify Grantee name is enabled");

		app.zPageMain.logout();
	}

	/**
	 * Testcase : Bug 102850 - Grantee name is disabled while adding ACL in cos
	 * Steps :
	 *1. Login to admin console
	 *2. Edit any COS
	 *3. Click on ACL >> click on add
	 *4. Verify Grantee name field
	 * @throws HarnessException
	 */
	@Test(	description = "Verify grantee name is enabled while adding ACL in COS",
			groups = { "functional" })
	public void Bug_102850_Grantee_Name_At_COS() throws HarnessException {

		// Create a new cos in the Admin Console using SOAP
		CosItem cos = new CosItem();
		String cosName=cos.getName();
		ZimbraAdminAccount.AdminConsoleAdmin().soapSend(
				"<CreateCosRequest xmlns='urn:zimbraAdmin'>"
						+			"<name>" + cosName + "</name>"
						+		"</CreateCosRequest>");
		
		//Navigate to COS page
		app.zPageManageCOS.zNavigateTo();
		
		// Search COS
		app.zPageSearchResults.setType(PageSearchResults.TypeOfObject.COS);
		
		// Enter the search string to find the account
		app.zPageSearchResults.zAddSearchQuery(cosName);
		
		// Click search
		app.zPageSearchResults.zToolbarPressPulldown(Button.B_SEARCH_TYPE, Button.O_CLASS_OF_SERVICE);
		app.zPageSearchResults.zToolbarPressButton(Button.B_SEARCH);

		// Click on cos
		app.zPageSearchResults.zListItem(Action.A_RIGHTCLICK, cos.getName());
		app.zPageSearchResults.setType(PageSearchResults.TypeOfObject.COS);

		// Click on Edit -> Advanced button
		app.zPageSearchResults.zToolbarPressButton(Button.B_TREE_EDIT);
		SleepUtil.sleepMedium();

		// Click on ACL
		app.zPageEditCOS.zClickAt(PageEditCOS.Locators.COS_EDIT_ACL,"");

		//Click on Add button
		app.zPageEditAccount.zClickAt(PageEditAccount.ACCOUNT_EDIT_ACL_ADD,"");

		//Verify Grantee name is enabled
		ZAssert.assertEquals(app.zPageEditAccount.zIsElementDisabled(PageEditAccount.ACCOUNT_EDIT_ACL_GRANTEE_NAME), false, "Verify Grantee name is enabled");
		app.zPageMain.logout();

	}

	/**
	 * Testcase : Bug 102850 - Grantee name is disabled while adding ACL in DL
	 * Steps :
	 *1. Login to admin console
	 *2. Edit any DL
	 *3. Click on ACL >> click on add
	 *4. Verify Grantee name field
	 * @throws HarnessException
	 */
	@Test(	description = "Verify grantee name is enabled while adding ACL in DL",
			groups = { "functional" })
	public void Bug_102850_Grantee_Name_At_DL() throws HarnessException {

		// Create a new dl in the Admin Console using SOAP
		DistributionListItem dl = new DistributionListItem();
		String dlEmailAddress=dl.getEmailAddress();

		ZimbraAdminAccount.AdminConsoleAdmin().soapSend(
				"<CreateDistributionListRequest xmlns='urn:zimbraAdmin'>"
						+			"<name>" + dlEmailAddress + "</name>"
						+		"</CreateDistributionListRequest>");

		// Refresh the list
		app.zPageManageDistributionList.sClickAt(PageMain.Locators.REFRESH_BUTTON, "");

		app.zPageManageDistributionList.zNavigateTo();

		// Click on distribution list 
		app.zPageManageDistributionList.zListItem(Action.A_LEFTCLICK, dl.getEmailAddress());

		// Click on Edit button
		app.zPageManageDistributionList.zToolbarPressPulldown(Button.B_GEAR_BOX,Button.O_EDIT);
		SleepUtil.sleepMedium();

		app.zPageEditDistributionList.zClickAt(PageEditDistributionList.Locators.DL_EDIT_ACL,"");

		//Click on Add button
		app.zPageEditAccount.zClickAt(PageEditAccount.ACCOUNT_EDIT_ACL_ADD,"");

		//Verify Grantee name is enabled
		ZAssert.assertEquals(app.zPageEditAccount.zIsElementDisabled(PageEditAccount.ACCOUNT_EDIT_ACL_GRANTEE_NAME), false, "Verify Grantee name is enabled");

		app.zPageMain.logout();

	}

	/**
	 * Testcase : Bug 102850 - Grantee name is disabled while adding ACL in domain
	 * Steps :
	 *1. Login to admin console
	 *2. Edit any domain
	 *3. Click on ACL >> click on add
	 *4. Verify Grantee name field
	 * @throws HarnessException
	 */
	@Test(	description = " Bug 102850 - Grantee name is disabled while adding ACL in domain",
			groups = { "functional" })
	public void Bug_102850_Grantee_Name_At_Domain() throws HarnessException {


		// Create a new domain in the Admin Console using SOAP
		DomainItem domain = new DomainItem();
		String domainName=domain.getName();

		ZimbraAdminAccount.AdminConsoleAdmin().soapSend(
				"<CreateDomainRequest xmlns='urn:zimbraAdmin'>"
						+			"<name>" + domainName + "</name>"
						+		"</CreateDomainRequest>");


		// Refresh the domain list
		app.zPageManageDomains.sClickAt(PageMain.Locators.REFRESH_BUTTON, "");

		app.zPageManageDomains.zNavigateTo();


		// Click on account to be deleted.
		app.zPageManageDomains.zListItem(Action.A_LEFTCLICK, domain.getName());

		app.zPageManageDomains.zToolbarPressPulldown(Button.B_GEAR_BOX, Button.O_EDIT);
		SleepUtil.sleepMedium();

		app.zPageManageDomains.zClickAt(PageManageDomains.Locators.DOMAIN_EDIT_ACL,"");

		//Click on Add button
		app.zPageEditAccount.zClickAt(PageEditAccount.ACCOUNT_EDIT_ACL_ADD,"");

		//Verify Grantee name is enabled
		ZAssert.assertEquals(app.zPageEditAccount.zIsElementDisabled(PageEditAccount.ACCOUNT_EDIT_ACL_GRANTEE_NAME), false, "Verify Grantee name is enabled");

		app.zPageMain.logout();

	}

	/**
	 * Testcase : Bug 102850 - Verify Grantee name is enabled while adding ACL in global settings
	 * Steps :
	 *1. Login to admin console
	 *2. Edit any global settings
	 *3. Click on ACL >> click on add
	 *4. Verify Grantee name field
	 * @throws HarnessException
	 */
	@Test(	description = " Bug 102850 - Verify Grantee name is enabled while adding ACL in global settings",
			groups = { "functional" })
	public void Bug_102850_Grantee_Name_At_Global_Settings() throws HarnessException {

		//Navigate to global settings page
		app.zPageManageGlobalSettings.zNavigateTo();

		//Click on ACL
		app.zPageManageGlobalSettings.zClickAt(PageManageGlobalSettings.Locators.GLOBAL_SETTINGS_ACL,"");

		//Click on Add button
		app.zPageEditAccount.zClickAt(PageEditAccount.ACCOUNT_EDIT_ACL_ADD,"");

		//Verify Grantee name is enabled
		ZAssert.assertEquals(app.zPageEditAccount.zIsElementDisabled(PageEditAccount.ACCOUNT_EDIT_ACL_GRANTEE_NAME), false, "Verify Grantee name is enabled");

		app.zPageMain.logout();

	}
}