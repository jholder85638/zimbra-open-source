/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2009, 2014, 2016 Synacor, Inc.
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
 * All portions of the code are Copyright (C) 2009, 2014, 2016 Synacor, Inc. All Rights Reserved.
 * ***** END LICENSE BLOCK *****
 */
var ZMTB_ZimbraActions = function(zmtb)
{
	this.super(zmtb)
	var This = this;
	document.getElementById("ZimTB-OpenAdvanced").addEventListener("click",function(event){
		This._rqManager.goToPath("");
	},false);
	document.getElementById("ZimTB-OpenStandard").addEventListener("click",function(event){
		This._rqManager.goToPath("zimbra/h/");
	},false);
	document.getElementById("ZimTB-OpenPreferences").addEventListener("click",function(event){
		This.openPrefsCommand();
	},false);
	document.getElementById("ZimTB-Refresh-Button").addEventListener("command", function(){This._zmtb.update()}, false);
}

ZMTB_ZimbraActions.prototype = new ZMTB_Actions();
ZMTB_ZimbraActions.prototype.constructor = ZMTB_ZimbraActions;
ZMTB_ZimbraActions.prototype.super = ZMTB_Actions;

ZMTB_ZimbraActions.prototype.enable = function()
{
	document.getElementById("ZimTB-OpenAdvanced").disabled = false;
	document.getElementById("ZimTB-OpenStandard").disabled = false;
}

ZMTB_ZimbraActions.prototype.disable = function()
{
	document.getElementById("ZimTB-OpenAdvanced").disabled = true;
	document.getElementById("ZimTB-OpenStandard").disabled = true;
}

ZMTB_ZimbraActions.prototype.openPrefsCommand = function(name, parentId, query)
{
	var wm = Components.classes["@mozilla.org/appshell/window-mediator;1"].getService(Components.interfaces.nsIWindowMediator);
	var enumerator = wm.getEnumerator("");
	while(enumerator.hasMoreElements())
	{
		var win = enumerator.getNext();
		if(win.location == "chrome://zimbratb/content/preferences/preferences.xul")
		{
			win.focus();
			return;
		}
	}
	window.openDialog("chrome://zimbratb/content/preferences/preferences.xul", "preferences", "centerscreen, chrome, titlebar");
}