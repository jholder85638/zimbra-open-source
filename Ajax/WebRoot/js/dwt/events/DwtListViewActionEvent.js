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


function DwtListViewActionEvent() {
	DwtMouseEvent.call(this);
	this.reset(true);
}

DwtListViewActionEvent.prototype = new DwtMouseEvent;
DwtListViewActionEvent.prototype.constructor = DwtListViewActionEvent;

DwtListViewActionEvent.prototype.toString = 
function() {
	return "DwtListViewActionEvent";
}

DwtListViewActionEvent.prototype.reset =
function(dontCallParent) {
	if (!dontCallParent)
		DwtMouseEvent.prototype.reset.call(this);
	this.field = null;
	this.item = null;
	this.detail = null;
}