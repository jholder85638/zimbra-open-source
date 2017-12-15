/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2012, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.ajax.tests.preferences.mail.signatures;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.ui.AbsDialog;
import com.zimbra.qa.selenium.framework.ui.Action;
import com.zimbra.qa.selenium.framework.ui.Button;
import com.zimbra.qa.selenium.framework.util.HarnessException;
import com.zimbra.qa.selenium.framework.util.SleepUtil;
import com.zimbra.qa.selenium.framework.util.ZAssert;
import com.zimbra.qa.selenium.framework.util.ZimbraSeleniumProperties;
import com.zimbra.qa.selenium.projects.ajax.core.AjaxCommonTest;
import com.zimbra.qa.selenium.projects.ajax.ui.preferences.TreePreferences.TreeItem;
import com.zimbra.qa.selenium.projects.ajax.ui.preferences.signature.FormSignatureNew;
import com.zimbra.qa.selenium.projects.ajax.ui.preferences.signature.PageSignature;
import com.zimbra.qa.selenium.projects.ajax.ui.preferences.signature.FormSignatureNew.Field;

public class CancelHtmlSignature extends AjaxCommonTest{
	public CancelHtmlSignature() throws HarnessException {
		super.startingPage = app.zPagePreferences;
		super.startingAccountPreferences = null;
	}

	@Test(description = "Cancel text signature through GUI", groups = { "smoke" })
	public void CancelHtmlSignature_01() throws HarnessException {

		String sigName = "signame" + ZimbraSeleniumProperties.getUniqueString();
		String sigBody = "sigbody" + ZimbraSeleniumProperties.getUniqueString();

		//Click on signature from left pane
		app.zTreePreferences.zTreeItem(Action.A_LEFTCLICK,TreeItem.MailSignatures);

		//Click on New signature button
		FormSignatureNew signew = (FormSignatureNew) app.zPageSignature.zToolbarPressButton(Button.B_NEW);

		//Select html format from drop down
		signew.zSelectFormat("html");
		
		//Reason:With "?dev=1&debug=0", Tinymce editor in HTML mode takes more time to load 
		//commented out incompatible to webdriver reference
		//if(ClientSessionFactory.session().selenium().getEval("window.tinyMCE").equalsIgnoreCase("null")){
			SleepUtil.sleepVeryLong();
		//}

		// Fill Signature Name and body
		signew.zFillField(Field.SignatureName, sigName);
		signew.zFillField(Field.SignatureHtmlBody, sigBody);

		//Verify Warning Dialog gets pop up after click on Cancel button
		AbsDialog warning = (AbsDialog) signew.zToolbarPressButton(Button.B_CANCEL);
		ZAssert.assertNotNull(warning, "Verify the dialog is returned");

		//click on No button
		warning.zClickButton(Button.B_NO);

		// Verify canceled html signature name from SignatureListView
		app.zPagePreferences.zNavigateTo();
		app.zTreePreferences.zTreeItem(Action.A_LEFTCLICK,TreeItem.MailSignatures);

		PageSignature pagesig = new PageSignature(app);
		String SignatureListViewName = pagesig.zGetSignatureNameFromListView();

		// Verify signature name doesn't exist in SignatureListView
		ZAssert.assertStringDoesNotContain(SignatureListViewName, sigName,"Verify after  Cancelled, html signature  does not present in SignatureList view");
	}
}