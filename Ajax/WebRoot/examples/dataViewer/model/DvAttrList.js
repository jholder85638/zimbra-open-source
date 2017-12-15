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


/**
* @class
* This class represents a list of attributes.
* @author Conrad Damon
*/
function DvAttrList() {

	DvList.call(this, true);

	this._vector = new AjxVector();
	this._idHash = new Object();
	this._nameHash = new Object();
	this._evt = new DvEvent();
}

DvAttrList.prototype = new DvList;
DvAttrList.prototype.constructor = DvAttrList;

DvAttrList.prototype.toString = 
function() {
	return "DvAttrList";
}

/**
* Converts a list of attributes (each of which is a list of properties) into a DvAttrList.
*
* @param attrs		list of attributes
*/
DvAttrList.prototype.load =
function(attrs) {
	for (var id in attrs) {
		var props = attrs[id];
		var attr = new DvAttr(id, props[0], props[1], props[2], props[3]);
		this.add(attr);
		this._nameHash[attr.name] = attr;
	}
}

DvAttrList.prototype.getByName =
function(name) {
	return this._nameHash[name];
}
