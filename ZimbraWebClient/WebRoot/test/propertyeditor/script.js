/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2005, 2006, 2007, 2009, 2010, 2013, 2014 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Common Public Attribution License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at: http://www.zimbra.com/license
 * The License is based on the Mozilla Public License Version 1.1 but Sections 14 and 15 
 * have been added to cover use of software over a computer network and provide for limited attribution 
 * for the Original Developer. In addition, Exhibit A has been modified to be consistent with Exhibit B. 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * See the License for the specific language governing rights and limitations under the License. 
 * The Original Code is Zimbra Open Source Web Client. 
 * The Initial Developer of the Original Code is Zimbra, Inc. 
 * All portions of the code are Copyright (C) 2005, 2006, 2007, 2009, 2010, 2013, 2014 Zimbra, Inc. All Rights Reserved. 
 * ***** END LICENSE BLOCK *****
 */
function App() {
	this.shell = new DwtShell("MainShell", false, null, null, false);
	this.shell._setMouseEventHdlrs();
	this.shell.addListener(DwtEvent.ONMOUSEMOVE, new AjxListener(this, this.func));

	var pi = this._pi = new DwtPropertyEditor(this.shell, true, null, "absolute");

	pi.setBounds(100, 100, 500, 500);
	pi.setZIndex(Dwt.Z_VIEW);

	var prop = [

		{ label          : "User ID",
		  name           : "userid",
		  type           : "string",
		  value          : "rootshell",
		  minLength      : 4,
		  maxLength      : 16,
		  mustMatch      : /^[a-z][a-z0-9-]+$/i,
		  mustNotMatch   : /^(root|guest|admin)$/i
		},

		{ label      : "Password",
		  name       : "passwd",
		  type       : "password",
		  minLength  : 4,
		  maxLength  : 8,
		  value      : "default"
		},

		{ label      : "Account type",
		  name       : "accttype",
		  type       : "select",
		  value      : "wheel",
		  item       : [
			  { label : "Administrator",
			    value : "root" },
			  { label : "Power users",
			    value : "wheel" },
			  { label : "Normal users",
			    value : "luser" }
			  ]
		},

		{ label      : "Read-only field",
		  name       : "readonly",
		  readonly   : true,
		  value      : "U can't touch this"
		},

		{ label      : "Address",
		  name       : "address",
		  type       : "struct",
		  children   : [

			  { label  : "Street",
			    name   : "street",
			    type   : "string",
			    value  : "Al. T. Neculai, nr. 19"
			  },

			  { label  : "City",
			    name   : "city",
			    type   : "string",
			    value  : "Iasi"
			  },

			  { label     : "Postal code",
			    name      : "postCode",
			    type      : "string",
			    mustMatch : /^[0-9]+$/,
			    value     : "700713",
			    required  : true
			  },

			  { label    : "Dates",
			    name     : "dates",
			    type     : "struct",
			    children : [

				    { label  : "Before",
				      name   : "dateBefore",
				      type   : "date",
				      format : "EEE, MMM dd, yyyy",
				      value  : new Date("March 8, 1979").getTime()
				    },

				    { label  : "After",
				      name   : "dateAfter",
				      type   : "date",
				      format : "yyyy MMMMMM dd" },

				    { label : "Age",
				      type  : "integer",
				      name  : "age"
				    }

				    ]
			  },

			  { label    : "State",
			    name     : "state",
			    type     : "string",
			    value    : "Iasi"
			  },

			  { label  : "Country",
			    name   : "country",
			    type   : "string",
			    value  : "Romania"
			  }

			  ]
		},

		{ label      : "Integer",
		  name       : "integer",
		  type       : "integer",
		  value      : "10.00",
		  minValue   : 10,
		  maxValue   : 30
		},

		{ label      : "Float",
		  name       : "float",
		  type       : "number",
		  value      : "15",
		  minValue   : 15,
		  maxValue   : 25
		},

		{ label      : "Decimal",
		  name       : "decimal",
		  type       : "number",
		  value      : "10",
		  decimals   : 2
		}

		];

	pi.initProperties(prop);
	pi.setFixedLabelWidth();
	pi.setFixedFieldWidth();
};

App.run = function() {
	new App();
};

App.prototype.func = function(ev) {
	window.status = "(" + ev.docX + ", " + ev.docY + ") on a " + ev.target.tagName;
};
