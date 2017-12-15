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


		{ type: _GROUP_, numCols: 3, colSpan:3, 
			items: [
				{ ref: "workAddr", rowSpan:6, label: "Work", verticalAlignment:_TOP_, labelCssClass:"contactLabelLeft"//,	relevantIfEmpty:false
				},
				{ type: _GROUP_, colSpan: 1, numCols:2, 
					items: [
						{ ref: "workPhone", label: "Phone: " },
						{ ref: "workPhone2", label: "Phone 2: " },
						{ ref: "workFax", label: "Fax: " },
						{ ref: "assistantPhone", label: "Assistant: " },
						{ ref: "companyPhone", label: "Company: " },
						{ ref: "callbackPhone", label: "Callback: " }
					]
				}
			]
		},
		{ ref: "workUrl", labelCssClass:"contactLabelLeft", colSpan:"*", label:"" },

		{ type: _SPACER_, height: 10, colSpan:"*",
			relevant: "get('workAddr') || get('workPhone') || get('workPhone2') || get('workFax') || get('assistantPhone') || get('companyPhone') || get('callbackPhone')" 
		},
		
		{ ref: "homeAddr", rowSpan:5, label: "Home", labelCssClass:"contactLabelLeft", relevant: "get('homeStreet') != null || get('homeCity') != null || get('homePostalCode') != null" },
		{ ref: "homePhone", label: "Phone:" },
		{ ref: "homePhone2", label: "Phone 2:" },
		{ ref: "homeFax", label: "Fax:" },
		{ ref: "mobilePhone", label: "Mobile:" },
		{ ref: "pager", label: "Pager:" },

		{ ref: "homeUrl", labelCssClass:"contactLabelLeft", label:"", colSpan:"*"},
		{ type: _SPACER_, colSpan:"*", height: 15, relevant: "get('homeStreet') != null || get('homeCity') != null || get('homePostalCode') != null || get('homeUrl') != null" },
