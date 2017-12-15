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
* DvAttr represents a single class of attribute for an item. It may be represented
* as a column in the list view, and/or as part of the filter in the filter panel.
* @author Conrad Damon
*
* @param id			a unique numeric ID
* @param name		text name
* @param type		data type
* @param width		width of the corresponding column in the list view
* @param options	choices (for select or checkbox type)
*/
function DvAttr(id, name, type, width, options) {

	DvModel.call(this);
	
	this.id = id;
	this.name = name;
	this.type = type;
	this.width = width;
	this.options = options;
}

// attribute types
var i = 1;
DvAttr.T_STRING_EXACT			= "StringExact";
DvAttr.T_STRING_CONTAINS		= "StringContains";
DvAttr.T_SELECT					= "SingleSelect";
DvAttr.T_MULTI_SELECT			= "MultipleSelect";
DvAttr.T_NUMBER					= "Number";
DvAttr.T_NUMBER_RANGE			= "NumberRange";
DvAttr.T_NUMBER_RANGE_BOUNDED	= "NumberRangeBounded";
DvAttr.T_DATE_RANGE				= "DateRange";
DvAttr.T_TIME_RANGE				= "TimeRange";
DvAttr.T_BOOLEAN				= "Boolean";

DvAttr.prototype = new DvModel;
DvAttr.prototype.constructor = DvAttr;
