/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
 * All portions of the code are Copyright (C) 2007, 2009, 2010, 2013, 2014, 2016 Synacor, Inc. All Rights Reserved.
 * ***** END LICENSE BLOCK *****
 */

/**
* @class ZaSambaDomainXFormView
* @contructor
* @param parent
* @param app
* @author Greg Solovyev
**/
function ZaSambaDomainXFormView (parent) {
	ZaTabView.call(this, parent, "ZaSambaDomainXFormView");	
		
	this.initForm(ZaSambaDomain.myXModel,this.getMyXForm());
	this._localXForm.setController();
}

ZaSambaDomainXFormView.prototype = new ZaTabView();
ZaSambaDomainXFormView.prototype.constructor = ZaSambaDomainXFormView;
ZaTabView.XFormModifiers["ZaSambaDomainXFormView"] = new Array();


ZaSambaDomainXFormView.prototype.setObject = 
function (entry) {
	this._containedObject = new ZaSambaDomain();
	this._containedObject.attrs = new Object();

	if(entry.id)
		this._containedObject.id = entry.id;
		
	if(entry.name)
		this._containedObject.name = entry.name;

	for (var a in entry.attrs) {
		if(entry.attrs[a] instanceof Array) {
			this._containedObject.attrs[a] = new Array();
			var cnt = entry.attrs[a].length;
			for(var ix = 0; ix < cnt; ix++) {
				this._containedObject.attrs[a][ix]=entry.attrs[a][ix];
			}
		} else {
			this._containedObject.attrs[a] = entry.attrs[a];
		}
	}
	
	if(!entry[ZaModel.currentTab])
		this._containedObject[ZaModel.currentTab] = "1";
	else
		this._containedObject[ZaModel.currentTab] = entry[ZaModel.currentTab];
		
	this._localXForm.setInstance(this._containedObject);	
	this.updateTab();		
}

ZaSambaDomainXFormView.myXFormModifier = function(xFormObject, entry) {	
	xFormObject.tableCssStyle="width:100%;overflow:auto;";
	
	xFormObject.items = [
		{type:_GROUP_, cssClass:"ZmSelectedHeaderBg", colSpan: "*", id:"xform_header", 
			items: [
				{type:_GROUP_,	numCols:4,colSizes:["32px","350px","100px","250px"],
					items: [
						{type:_AJX_IMAGE_, src:"Domain_32", label:null},
						{type:_OUTPUT_, ref:ZaSambaDomain.A_sambaDomainName, label:null,cssClass:"AdminTitle", rowSpan:2},				
						{type:_OUTPUT_, ref:ZaSambaDomain.A_sambaSID, label:"sambaSID"}
					]
				}
			],
			cssStyle:"padding-top:5px; padding-left:2px; padding-bottom:5px"
		},	
		{type:_TAB_BAR_,  ref:ZaModel.currentTab,
			choices:[
				{value:1, label:zimbra_samba.SambaDomainTabGeneral}				
			],
			cssClass:"ZaTabBar", id:"xform_tabbar"
		},
		{type:_SWITCH_, 
			items:[
				{type:_ZATABCASE_, caseKey:1, 
					colSizes:["250px","*"],
					items:[
						{ ref: ZaSambaDomain.A_sambaDomainName, type:_TEXTFIELD_, 
						  label:zimbra_samba.Domain_DomainName,onChange:ZaTabView.onFormFieldChanged
						},
						{ ref: ZaSambaDomain.A_sambaSID, type:_TEXTFIELD_, 
						  label:"sambaSID", width:300,
						  onChange:ZaTabView.onFormFieldChanged
					  	},
						{ ref: ZaSambaDomain.A_sambaAlgorithmicRidBase, type:_TEXTFIELD_, 
						  label:"sambaAlgorithmicRidBase", cssClass:"admin_xform_number_input",
						  onChange:ZaTabView.onFormFieldChanged
					  	}						
					]
				}
			]
		}	
	];
}

ZaTabView.XFormModifiers["ZaSambaDomainXFormView"].push(ZaSambaDomainXFormView.myXFormModifier);