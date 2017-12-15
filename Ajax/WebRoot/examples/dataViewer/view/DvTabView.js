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


function DvTabView(parent, attrs, user, app) {

    DwtTabViewPage.call(this, parent);

	this._user = user;
	this._app = app;
	this._listView = new DvListView(this, attrs, app);
}

DvTabView.prototype = new DwtTabViewPage;
DvTabView.prototype.constructor = DvTabView;

DvTabView.prototype.toString = 
function() {
	return "DvTabView";
}

DvTabView.prototype.showMe =
function() {
	DwtTabViewPage.prototype.showMe.call(this);
	this._app.setCurrentUser(this._user);
}


DvTabView.prototype.resetSize = 
function(newWidth, newHeight)  {
	DwtTabViewPage.prototype.resetSize.call(this, newWidth, newHeight);
	
	this._listView.resetHeight(newHeight);
}
