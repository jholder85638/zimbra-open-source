/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2006, 2007 Zimbra, Inc.
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
 * This class implements a group of radio buttons
 *
 * @param {Object} radios A hash whose keys are the ids of the radio button elements
 * 		and whose values are the values associated with those buttons. (Optional)
 * @param {String} selectedId The id of the button to select initially. (Optional)
 */
function DwtRadioButtonGroup(radios, selectedId) {
	this._values = {};
	this._eventMgr = new AjxEventMgr();
	
	for (var id in radios) {
		this.addRadio(id, radios[id], id == selectedId);
	}
	if (selectedId) {
		this.setSelectedId(selectedId);
	}
};

DwtRadioButtonGroup.toString =
function() {
	return "DwtRadioButtonGroup";
};

DwtRadioButtonGroup.prototype.addRadio =
function(id, value, selected) {
	this._values[id] = value;
	var element = document.getElementById(id);
    Dwt.setHandler(element, DwtEvent.ONCLICK, DwtRadioButtonGroup.__handleClick);
    Dwt.associateElementWithObject(element, this);
   	element.checked = selected ? true : false;
    if (selected) {
    	this._selectedId = id;
    }
};

DwtRadioButtonGroup.prototype.setSelectedId =
function(id) {
	if (id != this._selectedId) {
		document.getElementById(id).checked = true;
		this._selectedId = id;
		var selEv = DwtShell.selectionEvent;
		selEv.reset();
		this._notifySelection(selEv);
	}
};

DwtRadioButtonGroup.prototype.setSelectedValue =
function(value) {
	var id = this._valueToId(value);
	this.setSelectedId(id);
};

DwtRadioButtonGroup.prototype.getSelectedId =
function() {
	return this._selectedId;
};

DwtRadioButtonGroup.prototype.getSelectedValue =
function() {
	return this._values[this._selectedId];
};

DwtRadioButtonGroup.prototype.addSelectionListener =
function(listener) {
	return this._eventMgr.addListener(DwtEvent.SELECTION, listener); 	
};

DwtRadioButtonGroup.prototype._valueToId = 
function(value) {
	for (var id in this._values) {
		if (this._values[id] == value) {
			return id;
		}
	}
	return null;
};

DwtRadioButtonGroup.prototype._notifySelection = 
function(selEv) {
    selEv.item = this;
    selEv.detail = { id: this._selectedId, value: this._values[this._selectedId] };
    this._eventMgr.notifyListeners(DwtEvent.SELECTION, selEv);
};

DwtRadioButtonGroup.prototype._handleClick = 
function(event, target) {
	var id = target.id;
	if (id != this._selectedId) {
		this._selectedId = id;
	    var selEv = DwtShell.selectionEvent;
	    DwtUiEvent.copy(selEv, event);
		this._notifySelection(selEv);
	}
};

DwtRadioButtonGroup.__handleClick = 
function(event) {
	event = DwtUiEvent.getEvent(event);
    var target = DwtUiEvent.getTarget(event);
    var group = Dwt.getObjectFromElement(target);
    group._handleClick(event, target);
};
