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


var theList = ["One", "Two","Three","Four","Five","Six"];
//var theList = [{value:"1", label:"One"}, {value:"2", label:"Two"}];
//var theList = {"1":"One", "2":"Two","3":"Three"};


var dynChoices = new XFormChoices(theList);



var form = {
	id:"form",
	numCols:3,
	items:[
		{type:_CELL_SPACER_},
		{type:_OUTPUT_, value:"HTML Select"},
		{type:_OUTPUT_, value:"OSelect"},
		
		{ref:"value", id:"html_select1", type:_SELECT1_, choices:dynChoices, label:"Select 1", width:100},
		{ref:"value", id:"oselect1", type:_OSELECT1_, choices:dynChoices, width:100},

		{ref:"value", id:"html_select", type:_SELECT_, choices:dynChoices, label:"Select", width:100, height:70},
		{ref:"value", id:"oselect", type:_OSELECT_, choices:dynChoices, width:100, height:70},
		{type:_SEPARATOR_},
		{ref:"theList", type:_REPEAT_, number:10, label:"Change choices here", colSpan:"*",
			items:[
				{type:_TEXTFIELD_, ref:".", 
					elementChanged: function (value, instValue, event) {
						dynChoices.dirtyChoices();
						this.getForm().itemChanged(this, value, event);
					}
				}
			]
		}
	]
}

var model = {
	items:[
		{id:"value", type:_STRING_},
		{id:"theList", type:_LIST_, 
			listItem:{type:_UNTYPED_}
		}
	]
}

var instances = {
	"list":{
		value:"Two",
		theList:theList
	},
	empty:{}
}


var model = new XModel(model);
registerForm("Choices test", new XForm(form, model), instances);

