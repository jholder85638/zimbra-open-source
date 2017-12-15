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
 * @constructor
 * @class
 * Base class for all exceptions in the toolkit
 * 
 * @author Ross Dargahi
 * 
 * @param {string} msg Human readable message (optional)
 * @param {string|number} code Any error or fault code (optional)
 * @param {string} method Name of the method throwing the exception (optional)
 * @param {string} detail Any additional detail (optional)
 */

function ZMTB_AjxException(msg, code, method, detail) {
	if (arguments.length == 0) return;
	
	/** Human readable message if applicable
	 * @type string*/
	this.msg = msg;
	
	/** error or fault code if applicable
	 * @type string|number*/
	this.code = code;
	
	/** Name of the method throwing the exception if applicable
	 * @type string*/
	this.method = method;
	
	/** Any additional detail
	 * @type string*/
	this.detail = detail;
}

/**
 * This method returns this class' name. Subclasses will
 * override this method to return their own name
 * 
 * @return class name
 * @type String
 */
ZMTB_AjxException.prototype.toString = 
function() {
	return "ZMTB_AjxException";
}

/**
 * @return A string representing the state of the exception
 * @type string
 */
ZMTB_AjxException.prototype.dump = 
function() {
	return "ZMTB_AjxException: msg="+this.msg+" code="+this.code+" method="+this.method+" detail="+this.detail;
}

/** Invalid parent exception code
 * @type string */
ZMTB_AjxException.INVALIDPARENT 			= "ZMTB_AjxException.INVALIDPARENT";

/** Invalid operation exception code
 * @type string */
ZMTB_AjxException.INVALID_OP 			= "ZMTB_AjxException.INVALID_OP";

/** Internal error exception code
 * @type string */
ZMTB_AjxException.INTERNAL_ERROR 		= "ZMTB_AjxException.INTERNAL_ERROR";

/** Invalid parameter to method/operation exception code
 * @type string */
ZMTB_AjxException.INVALID_PARAM 			= "ZMTB_AjxException.INVALID_PARAM";

/** Unimplemented method called exception code
 * @type string */
ZMTB_AjxException.UNIMPLEMENTED_METHOD 	= "ZMTB_AjxException.UNIMPLEMENTED_METHOD";

/** Network error exception code
 * @type string */
ZMTB_AjxException.NETWORK_ERROR 			= "ZMTB_AjxException.NETWORK_ERROR";

/** Out or RPC cache exception code
 * @type string */
ZMTB_AjxException.OUT_OF_RPC_CACHE		= "ZMTB_AjxException.OUT_OF_RPC_CACHE";

/** Unsupported operation code
 * @type string */
ZMTB_AjxException.UNSUPPORTED 			= "ZMTB_AjxException.UNSUPPORTED";

/** Unknown error exception code
 * @type string */
ZMTB_AjxException.UNKNOWN_ERROR 			= "ZMTB_AjxException.UNKNOWN_ERROR";

/** Operation cancelled exception code
 * @type string */
ZMTB_AjxException.CANCELED				= "ZMTB_AjxException.CANCELED";
