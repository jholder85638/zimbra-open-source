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
 * @constructor
 * @class
 * A drop target is registered with a control to indicate that the control is 
 * a drop target. Thedrop target is the mechanism by which the DnD framework provides 
 * the binding between the UI components and the application.
 * 
 * Application developers instantiate <i>DwtDropTarget</i> and register it with the control
 * which is to be a drop taget (via <code>DwtControl.prototype.setDropTarget</code>). The
 * application should then register a listener with the <i>DwtDropTarget</i>. This way
 * when drop events occur the application will be notified and may act on them 
 * accordingly
 * 
 * @author Ross Dargahi
 * 
 * @param {function...} transferType A list of supported object types that may be dropped onto
 * 		this drop target. Typically the items repreent classes (i.e. functions) whose 
 * 		instances may be dropped on this drop target e.g. 
 * 		<code>new DwtDropTarget(MailItem, AppointmentItme)</code>
 * 
 * @see DwtDropEvent
 * @see DwtControl
 * @see DwtControl#setDropTarget
 */
function DwtDropTarget(transferType) {
	/** @private */
	this._evtMgr = new AjxEventMgr();

	/** @private */
	this.__hasMultiple = false;
	
	/**@private*/
	this.__transferTypes = new Array();
	if (transferType) {
		if (transferType instanceof Array) {
			this.__transferTypes = transferType;
		} else {
			var len = arguments.length;
			for (var i = 0; i < len; i++)
				this.__transferTypes[i] = arguments[i];
				
			this.__transferTypes.length = i;
		}
	}
}

/** @private */
DwtDropTarget.__DROP_LISTENER = "DwtDropTarget.__DROP_LISTENER";

/** @private */
DwtDropTarget.__dropEvent = new DwtDropEvent();

/**
 * @return The name of this class
 * @type String
 */
DwtDropTarget.prototype.toString = 
function() {
	return "DwtDropTarget";
}

/**
 * Registers a listener for <i>DwtDragEvent</i> events.
 *
 * @param {AjxListener} dropTargetListener Listener to be registered 
 * 
 * @see DwtDropEvent
 * @see AjxListener
 * @see #removeDropListener
 */
DwtDropTarget.prototype.addDropListener =
function(dropTargetListener) {
	this._evtMgr.addListener(DwtDropTarget.__DROP_LISTENER, dropTargetListener);
}

/**
 * Removes a registered event listener.
 * 
 * @param {AjxListener} dropTargetListener Listener to be removed
 * 
 * @see AjxListener
 * @see #addDropListener
 */
DwtDropTarget.prototype.removeDropListener =
function(dropTargetListener) {
	this._evtMgr.removeListener(DwtDropTarget.__DROP_LISTENER, dropTargetListener);
}

/**
 *  Check to see if the types in <code>items</code> can be dropped on this drop target
 *
 * @param {object|array} items an array of objects or single object whose types are
 * 		to be checked against the set of transfer types supported by this drop target
 * 
 * @return true if all of the objects in <code>items</code> may legally be dropped on 
 * 		this drop target
 * @type boolean
 */
DwtDropTarget.prototype.isValidTarget =
function(items) {
	if (items instanceof Array) {
		var len = items.length;
		for (var i = 0; i < len; i++) {
			if (!this.__checkTarget(items[i]))
				return false;
		}
		return true;
	} else {
		return this.__checkTarget(items);
	}
}

/**
 * Calling this method indicates that the UI component backing this drop target has multiple 
 * sub-components
 */
DwtDropTarget.prototype.markAsMultiple = 
function() {
	this.__hasMultiple = true;
};

/**
 * @return true if the UI component backing this drop target has multiple sub-components
 * @type boolean
 */
DwtDropTarget.prototype.hasMultipleTargets = 
function () {
	return this.__hasMultiple;
};

/**
 * @return the list of transfer types supported by this drop target
 * @type array
 * 
 * @see #setTransferTypes
 */
DwtDropTarget.prototype.getTransferTypes =
function() {
	return this.__transferTypes;
}

/** 
 * Sets the transfer types supported by this drop target
 * 
 * @param {function..} transferType An list of supported object types that may be dropped onto
 * 		this drop target. Typically the items repreent classes (i.e. functions) whose 
 * 		instances may be dropped on this drop target e.g. 
 * 		<code>dropTarget.setTransferTypes(MailItem, AppointmentItme)</code>
 * 
 * @see #getTransferTypes
 */
DwtDropTarget.prototype.setTransferTypes =
function(transferType) {
	var len = arguments.length;
	for (var i = 0; i < len; i++) {
		this.__transferTypes[i] = arguments[i];
	}
	this.__transferTypes.length = i;
}

// The following methods are called by DwtControl during the Drag lifecycle 

/** @private */
DwtDropTarget.prototype._dragEnter =
function(operation, targetControl, srcData, ev, dndIcon) {
	DwtDropTarget.__dropEvent.operation = operation;
	DwtDropTarget.__dropEvent.targetControl = targetControl;
	DwtDropTarget.__dropEvent.action = DwtDropEvent.DRAG_ENTER;
	DwtDropTarget.__dropEvent.srcData = srcData;
	DwtDropTarget.__dropEvent.uiEvent = ev;
	DwtDropTarget.__dropEvent.doIt = true;
	DwtDropTarget.__dropEvent.dndIcon = dndIcon;
	this._evtMgr.notifyListeners(DwtDropTarget.__DROP_LISTENER, DwtDropTarget.__dropEvent);
	return DwtDropTarget.__dropEvent.doIt;
}

/** @private */
DwtDropTarget.prototype._dragLeave =
function() {
	DwtDropTarget.__dropEvent.action = DwtDropEvent.DRAG_LEAVE;
	this._evtMgr.notifyListeners(DwtDropTarget.__DROP_LISTENER, DwtDropTarget.__dropEvent);
}

/** @private */
DwtDropTarget.prototype._dragOpChanged =
function(newOperation) {
	DwtDropTarget.__dropEvent.operation = newOperation;
	DwtDropTarget.__dropEvent.action = DwtDropEvent.DRAG_OP_CHANGED;
	this._evtMgr.notifyListeners(DwtDropTarget.__DROP_LISTENER, DwtDropTarget.__dropEvent);
	return DwtDropTarget.__dropEvent.doIt;
};

/** @private */
DwtDropTarget.prototype._drop =
function(srcData, ev) {
	DwtDropTarget.__dropEvent.action = DwtDropEvent.DRAG_DROP;
	DwtDropTarget.__dropEvent.srcData = srcData;
	DwtDropTarget.__dropEvent.uiEvent = ev;
	this._evtMgr.notifyListeners(DwtDropTarget.__DROP_LISTENER, DwtDropTarget.__dropEvent);
	return DwtDropTarget.__dropEvent.doIt;
};


// Private methods

/**@private*/
DwtDropTarget.prototype.__checkTarget =
function(item) {
	var len = this.__transferTypes.length;
	for (var i = 0; i < len; i++) {
		if (item instanceof this.__transferTypes[i])
			return true;
	}	
	if (i == this.__transferTypes.length)
		return false;
};