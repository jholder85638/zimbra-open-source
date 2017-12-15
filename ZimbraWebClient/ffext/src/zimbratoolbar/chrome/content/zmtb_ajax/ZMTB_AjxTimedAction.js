/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
 * All portions of the code are Copyright (C) 2009, 2010, 2013, 2014, 2016 Synacor, Inc. All Rights Reserved.
 * ***** END LICENSE BLOCK *****
 */


function ZMTB_AjxTimedAction(obj, func, args) {
	ZMTB_AjxCallback.call(this, obj, func, args);
	this._tid = -1;
	this._id = -1;
}
ZMTB_AjxTimedAction.prototype = new ZMTB_AjxCallback();
ZMTB_AjxTimedAction.prototype.constructor = ZMTB_AjxTimedAction;

ZMTB_AjxTimedAction.prototype.toString = 
function() {
	return "ZMTB_AjxTimedAction";
};

ZMTB_AjxTimedAction._pendingActions = {};
ZMTB_AjxTimedAction._nextActionId = 0;

ZMTB_AjxTimedAction.scheduleAction =
function(action, timeout){
	// if tid already exists, cancel previous timeout before setting a new one
	if (action._tid) {
		ZMTB_AjxTimedAction.cancelAction(action._id);
	}

	var id = action._id = ZMTB_AjxTimedAction._nextActionId++;
	ZMTB_AjxTimedAction._pendingActions[id] = action;
	var actionStr = "ZMTB_AjxTimedAction._exec(" + id + ")";
	action._tid = window.setTimeout(actionStr, timeout ? timeout : 0); // mac no like null/void
	return action._id;
};

ZMTB_AjxTimedAction.cancelAction =
function(actionId) {
	var action = ZMTB_AjxTimedAction._pendingActions[actionId];
	if (action) {
		window.clearTimeout(action._tid);
		delete ZMTB_AjxTimedAction._pendingActions[actionId];
		delete action._tid;
	}
};

ZMTB_AjxTimedAction._exec =
function(actionId) {
	var action = ZMTB_AjxTimedAction._pendingActions[actionId];
	delete ZMTB_AjxTimedAction._pendingActions[actionId];
	delete action._tid;
	action.run();
};