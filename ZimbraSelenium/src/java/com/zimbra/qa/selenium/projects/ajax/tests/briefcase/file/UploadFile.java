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
package com.zimbra.qa.selenium.projects.ajax.tests.briefcase.file;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.items.FileItem;
import com.zimbra.qa.selenium.framework.items.FolderItem;
import com.zimbra.qa.selenium.framework.items.FolderItem.SystemFolder;
import com.zimbra.qa.selenium.framework.ui.Action;
import com.zimbra.qa.selenium.framework.ui.Button;
import com.zimbra.qa.selenium.framework.util.HarnessException;
import com.zimbra.qa.selenium.framework.util.ZAssert;
import com.zimbra.qa.selenium.framework.util.ZimbraAccount;
import com.zimbra.qa.selenium.framework.util.ZimbraSeleniumProperties;
import com.zimbra.qa.selenium.projects.ajax.core.FeatureBriefcaseTest;
import com.zimbra.qa.selenium.projects.ajax.ui.briefcase.DialogUploadFile;

public class UploadFile extends FeatureBriefcaseTest {

	public UploadFile() throws HarnessException {
		logger.info("New " + UploadFile.class.getCanonicalName());
		super.startingPage = app.zPageBriefcase;
		super.startingAccountPreferences.put("zimbraPrefShowSelectionCheckbox","TRUE");
	}

	@Test(description = "Upload file through RestUtil - verify through GUI", groups = { "sanity" })
	public void UploadFile_01() throws HarnessException {
		ZimbraAccount account = app.zGetActiveAccount();

		FolderItem briefcaseFolder = FolderItem.importFromSOAP(account,	SystemFolder.Briefcase);

		// Create file item
		String filePath = ZimbraSeleniumProperties.getBaseDirectory() + "/data/public/other/testtextfile.txt";
		
		FileItem fileItem = new FileItem(filePath);

		String fileName = fileItem.getName();

		// Upload file to server through RestUtil
		String attachmentId = account.uploadFile(filePath);

		// Save uploaded file to briefcase through SOAP
		account.soapSend("<SaveDocumentRequest xmlns='urn:zimbraMail'>"
				+ "<doc l='" + briefcaseFolder.getId() + "'><upload id='"
				+ attachmentId + "'/></doc></SaveDocumentRequest>");

		// refresh briefcase page
		app.zTreeBriefcase.zTreeItem(Action.A_LEFTCLICK, briefcaseFolder, true);

		// Verify document is created
		String name = app.zPageBriefcase.getItemNameFromListView(fileName);
		ZAssert.assertStringContains(name, fileName, "Verify file name through GUI");
		
		// delete file upon test completion
		app.zPageBriefcase.deleteFileByName(fileItem.getName());
	}
	
	@Test(description = "Upload file through GUI - verify through GUI", groups = { "sanity","windows" })
	public void UploadFile_02() throws HarnessException {
		
		try {
			
			ZimbraAccount account = app.zGetActiveAccount();
			
			FolderItem briefcaseFolder = FolderItem.importFromSOAP(account, SystemFolder.Briefcase);
			
			// Create file item
			final String fileName = "testtextfile.txt";
			final String filePath = ZimbraSeleniumProperties.getBaseDirectory() + "\\data\\public\\other\\" + fileName;
			
			FileItem fileItem = new FileItem(filePath);
	
			// refresh briefcase page
			app.zTreeBriefcase.zTreeItem(Action.A_LEFTCLICK, briefcaseFolder, false);
			
			// Click on Upload File button in the Toolbar
			DialogUploadFile dlg = (DialogUploadFile) app.zPageBriefcase.zToolbarPressButton(Button.B_UPLOAD_FILE, fileItem);
			
			app.zPageBriefcase.sClickAt("css=div[class='ZmUploadDialog'] input[name='uploadFile']", "");
			
			zUpload(filePath);
			
			dlg.zClickButton(Button.B_OK);
			
			// Click on created File
			app.zPageBriefcase.zListItem(Action.A_BRIEFCASE_CHECKBOX, fileItem);
			
			// Verify file is uploaded
			String name = app.zPageBriefcase.getItemNameFromListView(fileName);
			ZAssert.assertStringContains(name, fileName, "Verify file name through GUI");
			
		} finally {
			
			Robot robot;
			
			try {
				robot = new Robot();
				robot.delay(250);
				robot.keyPress(KeyEvent.VK_ESCAPE);
				robot.keyRelease(KeyEvent.VK_ESCAPE);
				robot.delay(50);
				
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	@Test(description = "Upload file through RestUtil - verify through SOAP", groups = { "smoke" })
	public void UploadFile_03() throws HarnessException {
		ZimbraAccount account = app.zGetActiveAccount();

		FolderItem briefcaseFolder = FolderItem.importFromSOAP(account, SystemFolder.Briefcase);

		// Create file item
		String filePath = ZimbraSeleniumProperties.getBaseDirectory() + "/data/public/other/testtextfile.txt";
		
		FileItem file = new FileItem(filePath);

		String fileName = file.getName();
		
		// Upload file to server through RestUtil
		String attachmentId = account.uploadFile(filePath);
	
		// Save uploaded file to briefcase through SOAP
		account.soapSend(

		"<SaveDocumentRequest xmlns='urn:zimbraMail'>" +

		"<doc l='" + briefcaseFolder.getId() + "'>" +

		"<upload id='" + attachmentId + "'/>" +

		"</doc>" +

		"</SaveDocumentRequest>");

		account.soapSelectNode("//mail:SaveDocumentResponse", 1);

		// search the uploaded file
		app.zGetActiveAccount().soapSend(
				"<SearchRequest xmlns='urn:zimbraMail' types='document'>"
						+ "<query>" + fileName + "</query>"
						+ "</SearchRequest>");

		// Verify file name through SOAP
		String name = account.soapSelectValue("//mail:doc", "name");
		ZAssert.assertEquals(name, fileName, "Verify file name through SOAP");
		
		//delete file upon test completion
		String id = account.soapSelectValue("//mail:doc", "id");
		app.zPageBriefcase.deleteFileById(id);
	}
	
}