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


if (window.Cos_String_XModelItem) {
	var model = new XModel({
		items:[
			{ id:"name", type:_COS_STRING_},
			{ id:"length", type:_COS_NUMBER_}
		]
	});
	
	var formAttr ={
		items:[
			{ ref:"name", type:_COS_TEXTFIELD_, label:"Name", valueLabel:""},
			{ ref:"length", type:_COS_TEXTFIELD_, label:"Length", valueLabel:"millimeters"}
		]
	};


	var instances = {
		account1:{
			cos:{ 
				attr: {
					name:"COS Name", 
					length:10
				}
			},
			account:{
				attr: {
					name:"Account 1 name",
					length:null
				}
			}
		},
		account2:{
			cos:{ 
				attr: {
					name:"COS Name", 
					length:10
				}
			},
			account:{
				attr: {
					name:"Account 2 name",
					length:20
				}
			}
		},
		empty:{
			cos:{
				attr: {
				}				
			},
			account:{
				attr: {
				}
			}
		}
	
	}

	registerForm("COS Fields", new XForm(formAttr, model), instances);
}
