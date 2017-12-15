/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
 * All portions of the code are Copyright (C) 2009, 2010, 2013, 2014, 2016 Synacor, Inc. All Rights Reserved.
 * ***** END LICENSE BLOCK *****
 */

/**
* Creates a CSFE result object.
* @constructor
* @class
* This class represents the result of a CSFE request. The data is either the 
* response that was received, or an exception. If the request resulted in a 
* SOAP fault from the server, there will also be a SOAP header present.
*
* @author Conrad Damon
* @param data			[Object]	response data
* @param isException	[boolean]	true if the data is an exception object
* @param header			[object]	the SOAP header
*/
function ZMTBCsfeResult(data, isException, header) {
	this.set(data, isException, header);
};

/**
* Sets the content of the result.
*
* @param data			[Object]	response data
* @param isException	[boolean]	true if the data is an exception object
* @param header			[object]	the SOAP header
*/
ZMTBCsfeResult.prototype.set =
function(data, isException, header) {
	this._data = data;
	this._isException = (isException === true);
	this._header = header;
};

/**
* Returns the response data. If there was an exception, throws the exception.
*/
ZMTBCsfeResult.prototype.getResponse =
function() {
	// if (this._isException)
	// 	throw this._data;
	// else
		return this._data;
};

/**
* Returns the exception object, if any.
*/
ZMTBCsfeResult.prototype.getException =
function() {
	return this._isException ? this._data : null;
};

ZMTBCsfeResult.prototype.isException = 
function() {
	return this._isException;
};

/**
* Returns the SOAP header that came with a SOAP fault.
*/
ZMTBCsfeResult.prototype.getHeader =
function() {
	return this._header;
};
