/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2012, 2013, 2014, 2015, 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.admin.tests.backuprestore;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.util.HarnessException;
import com.zimbra.qa.selenium.framework.util.SleepUtil;
import com.zimbra.qa.selenium.framework.util.ZAssert;
import com.zimbra.qa.selenium.projects.admin.core.AdminCommonTest;
import com.zimbra.qa.selenium.projects.admin.ui.PageManageBackups;
import com.zimbra.qa.selenium.projects.admin.ui.PageManageBackups.Locators;

public class NavigateBackupRestore extends AdminCommonTest {
	
	public NavigateBackupRestore() {
		logger.info("New "+ NavigateBackupRestore.class.getCanonicalName());

		// All tests start at the "Accounts" page
		super.startingPage = app.zPageManageBackups;
	}
	
	/**
	 * Testcase : Navigate to Backups page
	 * Steps :
	 * 1. Verify navigation path -- "Home --> Tools and Migraton --> Backups"
	 * @throws HarnessException
	 */
	@Test(	description = "Navigate to Backups",
			groups = { "sanity","network" })
			public void NavigateMigration_01() throws HarnessException {
		
		/*
		 * Verify navigation path -- "Home --> Tools and Migraton --> Backups"
		 */
		app.zPageManageBackups.zClick(Locators.TOOLS_AND_MIGRATION_ICON);
		SleepUtil.sleep(5000);
		app.zPageManageBackups.zClickAt(Locators.BACKUP,"");
		ZAssert.assertTrue(app.zPageManageBackups.zVerifyHeader(PageManageBackups.Locators.HOME), "Verfiy the \"Home\" text exists in navigation path");
		ZAssert.assertTrue(app.zPageManageBackups.zVerifyHeader(PageManageBackups.Locators.TOOLS_AND_MIGRATION), "Verfiy the \"Tools and Migration\" text exists in navigation path");
		ZAssert.assertTrue(app.zPageManageBackups.zVerifyHeader(PageManageBackups.Locators.BACKUPS), "Verfiy the \"Backups\" text exists in navigation path");
		
	}

}
