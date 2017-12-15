/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2005, 2006, 2007, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
 *
 * The contents of this file are subject to the Common Public Attribution License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at: https://www.zimbra.com/license
 * The License is based on the Mozilla Public License Version 1.1 but Sections 14 and 15
 * have been added to cover use of software over a computer network and provide for limited attribution
 * for the Original Developer. In addition, Exhibit A has been modified to be consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 * The Original Code is Zimbra Open Source Web Client.
 * The Initial Developer of the Original Code is Zimbra, Inc.  All rights to the Original Code were
 * transferred by Zimbra, Inc. to Synacor, Inc. on September 14, 2015.
 *
 * All portions of the code are Copyright (C) 2005, 2006, 2007, 2009, 2010, 2013, 2014, 2016 Synacor, Inc. All Rights Reserved.
 * ***** END LICENSE BLOCK *****
 */

function MemLeakTests(appCtxt, domain) {
/*
	ZmController.call(this, appCtxt);

	ZmCsfeCommand.setServerUri(location.protocol + "//" + domain + appCtxt.get(ZmSetting.CSFE_SERVER_URI));
	appCtxt.setAppController(this);
	appCtxt.setClientCmdHdlr(new ZmClientCmdHandler(appCtxt));
*/
	this._shell = appCtxt.getShell();

	this.startup();
};

//MemLeakTests.prototype = new ZmController;
//MemLeakTests.prototype.constructor = MemLeakTests;


MemLeakTests.run =
function(domain) {
	// Create the global app context
	var appCtxt = new ZmAppCtxt();

	//appCtxt.setIsPublicComputer(false);

	// Create the shell
	//var settings = appCtxt.getSettings();
	var shell = new DwtShell();

	appCtxt.setShell(shell);
	//appCtxt.setItemCache(new AjxCache());
	//appCtxt.setUploadManager(new AjxPost(appCtxt.getUploadFrameId()));

	// Go!
	new MemLeakTests(appCtxt, domain);
};


// Public methods

MemLeakTests.prototype.toString = 
function() {
	return "MemLeakTests";
};

MemLeakTests.prototype.startup =
function() {
	this._shell.getHtmlElement().innerHTML = "hello world.";
};
