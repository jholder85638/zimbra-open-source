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


var form = {
	numCols:2,
	items:[
		{ref:"SELECT", id:"oselect", type:_OSELECT_, label:"oselect"},
		{ref:"SELECT", id:"oselect_check", type:_OSELECT_CHECK_, label:"oselect_check"},
		{ref:"SELECT", id:"oselect1", type:_OSELECT1_, label:"oselect1", multiple:true}
	]
}

var model = {
	items:[
		{id:"SUBJECT", type:_STRING_},
		{id:"START_DATE", type:_DATE_},
		{id:"SELECT", type:_STRING_, choices:[
				{value:"A", label:"a value"},
				{value:"B", label:"b value"},
				{value:"C", label:"c value"},
				{value:"D", label:"d value"},
				{value:"E", label:"eeeeeeeeeeeeeeeeeeeeeee value"}
			]
		},
		{id:"ALL_DAY", trueValue:"T", falseValue:"F"},
		{id:"IMG", srcPath:"../images/", cssStyle:"width:32px;height:32px;"},
		{id:"IMG_CHOICES", srcPath:"../images/", cssStyle:"width:32px;height:32px;",
			choices:[
				{value:"doc", label:"sm_icon_document.gif"},
				{value:"help", label:"sm_icon_help.gif"},
				{value:null, label:"spacer.gif"}
			]
		}

	]
}

var instances = {
	instance1:{
		START_DATE:new Date(),
		SUBJECT:"Subject",
		SELECT:"A",
		ALL_DAY:"F",
		IMG:"sm_icon_document.gif",
		IMG_CHOICES:"doc",
		current_page:"A"
	},
	instance2:{
		START_DATE:new Date(),
		SUBJECT:"Second instance subject",
		SELECT:"B",
		ALL_DAY:"F",
		IMG:"sm_icon_help.gif",
		IMG_CHOICES:"help",
		current_page:"B"
	},
	empty:{}
}


var model = new XModel(model);
registerForm("OSELECT test", new XForm(form, model), instances);

