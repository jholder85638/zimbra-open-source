/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Zimlets
 * Copyright (C) 2006, 2007, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
 * All portions of the code are Copyright (C) 2006, 2007, 2009, 2010, 2013, 2014, 2016 Synacor, Inc. All Rights Reserved.
 * ***** END LICENSE BLOCK *****
 */

function _initYahoo() {
	var yhoo = new Object();
	yhoo.label = "Yahoo";
	yhoo.id = "yahoosearch";
	yhoo.icon = "Yahoo-panelIcon";
	yhoo.xsl = "yahoo/yahoo.xsl";
	yhoo.getRequest = 
		function (ctxt, q) {
			var args = {};
			args.appid = ctxt.getConfig("ywsAppId");
			args.results = ctxt.getConfig("numResults");
			args.query = AjxStringUtil.urlEncode(q);

			var q_url;
			q_url = ctxt.getConfig("yhooSearchUrl");
			var sep = "?";
			for (var arg in args) {
				q_url = q_url + sep + arg + "=" + args[arg];
				sep = "&";
			}
			return {"url":q_url, "req":null}
		};
		
	Com_Zimbra_Xslt.registerService(yhoo);
};

_initYahoo();
