/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2005, 2006 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * 
 * ***** END LICENSE BLOCK *****
 */


//
//	DWT SHIM -- makes simple objects for all of the DWT things needed in the XForms test
//




//
//	DwtComposite -- base class of the form
//	 (should we just go ahead and include this ???)
//

function DwtComposite() {}
var CP = DwtComposite.prototype;

CP.getHtmlElement = function(){}
CP.notifyListeners = function(){}
CP.addListener = function() {}
CP.removeListener = function () {}




//
//	debugging -- particular to our environment (Eg: leave)
//

var DBG = {
	messages : []
};
//if (window.Components) {
//	DBG.console = Components.classes['@mozilla.org/consoleservice;1'].getService(Components.interfaces.nsIConsoleService);
//}

DBG.println = function () {
	arguments.join = this.messages.join;
	var message = arguments.join("");
	this.messages.push(message);
//	if (DBG.console) DBG.console.logStringMessage(message);
}

DBG.clear = function() {
	DBG.messages = [];
	XFG.getEl("debug").innerHTML = "<button onclick='DBG.clear()'>Clear<\/button><BR><pre>";
}

DBG.getMessages = function () {
	return this.messages.join("<BR>");
}





var AjxMsg = {
	xformRepeatAdd : '+',
	xformRepeatRemove : '-',
	xformDateTimeFormat : '{0,date} at {0,time}'
};


function DwtKeyEvent(){}
DwtKeyEvent.getCharCode = function(){ return -1}

DwtKeyEvent.prototype.setFromDhtmlEvent = function(){}
var DwtUiEvent = {	setBehavior : function () {} }


function AjxTimedAction() {
	this.params = [];
	this.params.add = function (item) {
		this[this.length] = item;
	}
}
var ______id = 0;

AjxTimedAction.scheduleAction = function (action, delay){
	var id = "id_" + window.______id++;
	window[id] = action;
	setTimeout("AjxTimedAction.performCallback(window."+id+"); window."+id+"=null;", delay);
}

AjxTimedAction.performCallback = function (action){
	var obj = action.obj;
	var method = action.method;
	method.call(obj, action.params);
}



var DwtListView = {}