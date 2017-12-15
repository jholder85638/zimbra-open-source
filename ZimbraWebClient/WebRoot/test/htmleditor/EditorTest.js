/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2006, 2007, 2009, 2010, 2013, 2014 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Common Public Attribution License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at: http://www.zimbra.com/license
 * The License is based on the Mozilla Public License Version 1.1 but Sections 14 and 15 
 * have been added to cover use of software over a computer network and provide for limited attribution 
 * for the Original Developer. In addition, Exhibit A has been modified to be consistent with Exhibit B. 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * See the License for the specific language governing rights and limitations under the License. 
 * The Original Code is Zimbra Open Source Web Client. 
 * The Initial Developer of the Original Code is Zimbra, Inc. 
 * All portions of the code are Copyright (C) 2006, 2007, 2009, 2010, 2013, 2014 Zimbra, Inc. All Rights Reserved. 
 * ***** END LICENSE BLOCK *****
 */
function EditorTest() {};

EditorTest.content = [ "<h1>Test</h1><p>a paragraph here</p>",
		       "<table width='100%' style='border: 1px solid #aaf; border-collapse: collapse;'>",
		       "<tr> <td style='border: 1px solid #aaf'><br /></td> <td style='border: 1px solid #aaf'><br /></td> <td style='border: 1px solid #aaf'><br /></td> </tr>",
		       "<tr> <td style='border: 1px solid #aaf'><br /></td> <td style='border: 1px solid #aaf'><br /></td> <td style='border: 1px solid #aaf'><br /></td> </tr>",
		       "<tr> <td style='border: 1px solid #aaf'><br /></td> <td style='border: 1px solid #aaf'><br /></td> <td style='border: 1px solid #aaf'><br /></td> </tr>",
		       "<tr> <td style='border: 1px solid #aaf'><br /></td> <td style='border: 1px solid #aaf'><br /></td> <td style='border: 1px solid #aaf'><br /></td> </tr>",
		       "<tr> <td style='border: 1px solid #aaf'><br /></td> <td style='border: 1px solid #aaf'><br /></td> <td style='border: 1px solid #aaf'><br /></td> </tr>",
		       "<tr> <td style='border: 1px solid #aaf'><br /></td> <td style='border: 1px solid #aaf'><br /></td> <td style='border: 1px solid #aaf'><br /></td> </tr>",
		       "</table>",
		       "<p>another paragraph here</p>" ].join("");

EditorTest.run = function() {
	DBG = new AjxDebug(AjxDebug.NONE, null, false);

	var appCtxt = new ZmAppCtxt();
	var shell = new DwtShell();
	appCtxt.setShell(shell);

	// dirty hacks to be able to run without this
	var kbMgr = shell.getKeyboardMgr();
	kbMgr.registerKeyMap(new ZmKeyMap());
	kbMgr.registerGlobalKeyActionHandler({
		    handleKeyAction : function() {}
		});
	kbMgr.__currTabGroup = {
	    setFocusMember : function() { return false; }
	};

	var cont = new DwtComposite(shell, null, DwtControl.ABSOLUTE_STYLE);
	cont.zShow(true);
	cont.setLocation(10, 10);
	cont.setSize(800, 550);

	var editor = new ZmHtmlEditor(cont, null, EditorTest.content, DwtHtmlEditor.HTML, appCtxt);
	editor.setSize(800, 500);
};
