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
* @constructor
* @class
* The Keymap manager exception class.
* 
* @author Ross Dargahi
*
* @see DwtKeyMapMgr
*/
function DwtKeyMapMgrException(msg, code, method, keySeqStr) {
	DwtException.call(this, msg, code, method, detail);
	this._keySeqStr = keySeqStr;
}

DwtKeyMapMgrException.prototype = new DwtException;
DwtKeyMapMgrException.prototype.constructor = DwtKeyMapMgrException;

DwtKeyMapMgrException.NON_TERM_HAS_ACTION = 1;
DwtKeyMapMgrException.TERM_HAS_SUBMAP = 2;

DwtException.prototype.toString = 
function() {
	return "DwtKeyMapMgrException";
}
