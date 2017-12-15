/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2006 Zimbra, Inc.
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
* XFormItem class: "dynselect"
* A select box with asynchronous autocomplete capability
* @class DynSelect_XFormItem
* @constructor DynSelect_XFormItem
* @author Greg Solovyev
**/
function DynSelect_XFormItem() {}
XFormItemFactory.createItemType("_DYNSELECT_", "dynselect", DynSelect_XFormItem, OSelect1_XFormItem);
DynSelect_XFormItem.prototype.dataFetcherClass = null;
DynSelect_XFormItem.prototype.dataFetcherMethod = null;
DynSelect_XFormItem.prototype.dataFetcherObject = null;
DynSelect_XFormItem.prototype.initFormItem = function () {
	// if we're dealing with an XFormChoices object...
	var choices  = this.getInheritedProperty("choices");
	this.choices = choices ? choices : new XFormChoices([], XFormChoices.OBJECT_LIST, "name", "name");
//	this.choices = new XFormChoices([], XFormChoices.OBJECT_LIST, "name", "name");
	//	...set up to receive notification when its choices change
	var listener = new AjxListener(this, this.choicesChangeLsnr);
	this.choices.addListener(DwtEvent.XFORMS_CHOICES_CHANGED, listener);
	this.dataFetcherClass  = this.getInheritedProperty("dataFetcherClass");
	this.dataFetcherMethod  = this.getInheritedProperty("dataFetcherMethod");	
	this.dataFetcherObject = null;
}
DynSelect_XFormItem.prototype.changeChoicesCallback = 
function (data) {
	var choices = this.getChoices();
	if(!choices)
		return;
	choices.setChoices(data);
	choices.dirtyChoices();
}

DynSelect_XFormItem.prototype.onKeyUp = function(value, event) {
	var method = this.getKeyUpMethod();
	if(method) {
		method.call(this, value, event);
	} else {
		if(!this.dataFetcherObject && this.dataFetcherClass !=null && this.dataFetcherMethod !=null) {
			this.dataFetcherObject = new this.dataFetcherClass(this.getForm().getController());
		}
		if(!this.dataFetcherObject)
			return;
			
		var callback = new AjxCallback(this, this.changeChoicesCallback);
		this.dataFetcherMethod.call(this.dataFetcherObject, value, event, callback);
		this.getForm().itemChanged(this, value, event);
	}
}

DynSelect_XFormItem.prototype.resetChoices = function () {
	if(!this.dataFetcherObject && this.dataFetcherClass !=null && this.dataFetcherMethod !=null) {
			this.dataFetcherObject = new this.dataFetcherClass(this.getForm().getController());
	}
	if(!this.dataFetcherObject)
		return;
		
	var callback = new AjxCallback(this, this.changeChoicesCallback);
	this.dataFetcherMethod.call(this.dataFetcherObject, "", null, callback);
}

DynSelect_XFormItem.prototype.updateElement = function (newValue) {
	OSelect1_XFormItem.prototype.updateElement.call(this, newValue);
	if(!newValue || newValue=="") {
		if(!this.dataFetcherObject && this.dataFetcherClass !=null && this.dataFetcherMethod !=null) {
			this.dataFetcherObject = new this.dataFetcherClass(this.getForm().getController());
		}
		if(!this.dataFetcherObject)
			return;
		
		var callback = new AjxCallback(this, this.changeChoicesCallback);
		this.dataFetcherMethod.call(this.dataFetcherObject, "", null, callback);
	}
}