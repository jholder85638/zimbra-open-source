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
 * A drag source is registered with a control to indicate that the control is 
 * draggable. The drag source is the mechanism by which the DnD framework provides 
 * the binding between the UI components and the application.
 * 
 * Application developers instantiate <i>DwtDragSource</i> and register it with the control
 * which is to be draggable (via <code>DwtControl.prototype.setDragSource</code>). The
 * application should then register a listener with the <i>DwtDragSource</i>. This way
 * when drag events occur the application will be notified and may act on them 
 * accordingly
 * 
 * @author Ross Dargahi
 * 
 * @param {int} supportedOps Supported operations. This is an arithmatic ORing of
 * 		the operations supported by the drag source. Supported values are: <ul>
 * 			<li><i>Dwt.DND_DROP_NONE</i></li>
 * 			<li><i>Dwt.DND_DROP_COPY</i></li>
 * 			<li><i>Dwt.DND_DROP_MOVE</i></li></ul> 
 * 
 * @see DwtDragEvent
 * @see DwtControl
 * @see DwtControl#setDragSource
 */
function DwtDragSource(supportedOps) {
	this.__supportedOps = supportedOps
	this.__evtMgr = new AjxEventMgr();
};

/**@private*/
DwtDragSource.__DRAG_LISTENER = "DwtDragSource.__DRAG_LISTENER";

/**@private*/
DwtDragSource.__dragEvent = new DwtDragEvent();

/**
 * @return The name of this class
 * @type String
 */
DwtDragSource.prototype.toString = 
function() {
	return "DwtDragSource";
};


/**
 * Registers a listener for <i>DwtDragEvent</i> events.
 *
 * @param {AjxListener} dragSourceListener Listener to be registered 
 * 
 * @see DwtDragEvent
 * @see AjxListener
 * @see #removeDragListener
 */
DwtDragSource.prototype.addDragListener =
function(dragSourceListener) {
	this.__evtMgr.addListener(DwtDragSource.__DRAG_LISTENER, dragSourceListener);
};

/**
 * Removes a registered event listener.
 * 
 * @param {AjxListener} dragSourceListener Listener to be removed
 * 
 * @see AjxListener
 * @see #addDragListener
 */
DwtDragSource.prototype.removeDragListener =
function(dragSourceListener) {
	this.__evtMgr.removeListener(DwtDragSource.__DRAG_LISTENER, dragSourceListener);
};

// The following methods are called by DwtControl during the drag lifecycle 

/** @private */
DwtDragSource.prototype._beginDrag =
function(operation, srcControl) {
	if (!(this.__supportedOps & operation))
		return Dwt.DND_DROP_NONE;
		
	DwtDragSource.__dragEvent.operation = operation;
	DwtDragSource.__dragEvent.srcControl = srcControl;
	DwtDragSource.__dragEvent.action = DwtDragEvent.DRAG_START;
	DwtDragSource.__dragEvent.srcData = null;
	DwtDragSource.__dragEvent.doit = true;
	this.__evtMgr.notifyListeners(DwtDragSource.__DRAG_LISTENER, DwtDragSource.__dragEvent);
	return DwtDragSource.__dragEvent.operation;
};

/** @private */
DwtDragSource.prototype._getData =
function() {
	DwtDragSource.__dragEvent.action = DwtDragEvent.SET_DATA;
	this.__evtMgr.notifyListeners(DwtDragSource.__DRAG_LISTENER, DwtDragSource.__dragEvent);
	return DwtDragSource.__dragEvent.srcData;
};

/** @private */
DwtDragSource.prototype._endDrag =
function() {
	DwtDragSource.__dragEvent.action = DwtDragEvent.DRAG_END;
	DwtDragSource.__dragEvent.doit = false;
	this.__evtMgr.notifyListeners(DwtDragSource.__DRAG_LISTENER, DwtDragSource.__dragEvent);
	return DwtDragSource.__dragEvent.doit;
};
