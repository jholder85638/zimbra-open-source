/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2008, 2014, 2016 Synacor, Inc.
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
 * All portions of the code are Copyright (C) 2008, 2014, 2016 Synacor, Inc. All Rights Reserved.
 * ***** END LICENSE BLOCK *****
 */
function stringConcat(iters, resultsEl) {
	var before = new Date().getTime();
	var s = "";
	for (var i = 0; i < iters; i++) {
		s += " ";
	}
	var after = new Date().getTime();

	resultsEl.innerHTML = after - before;
}
function stringPushJoin(iters, resultsEl) {
	var before = new Date().getTime();
	var a = [];
	for (var i = 0; i < iters; i++) {
		a.push(" ");
	}
	var s = a.join("");
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}

var SAMPLE_STRING = 
	"one two     three four   five six    seven eight   nine  ten eleven "+
	"twelve  thirteen  fourteen fifteen sixteen seventeen   eighteen "+
	"nineteen                twenty";
function stringSplit(iters, resultsEl, delim) {
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		var a = SAMPLE_STRING.split(delim);
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}
function stringSubstring(iters, resultsEl) {
	var s = getEmptyString(iters);	
	
	var before = new Date().getTime();
	for (var i = 0; i < iters - 1; i++) {
		var sub = s.substring(i, i+1);
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}
function stringSubstr(iters, resultsEl) {
	var s = getEmptyString(iters);	
	
	var before = new Date().getTime();
	for (var i = 0; i < iters - 1; i++) {
		var sub = s.substr(i, 1);
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}

function stringCharAt(iters, resultsEl) {
	var s = getEmptyString(iters);	

	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		var c = s.charAt(i);
	}
	var after = new Date().getTime();
		
	resultsEl.innerHTML = after - before;
}

function stringReplace(iters, resultsEl, regex, replace) {
	var s = document.body.innerHTML;
	
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		var rs = s.replace(regex, replace);
	}
	var after = new Date().getTime();

	resultsEl.innerHTML = after - before;
}

function stringSlice(iters, resultsEl) {
	var s = getEmptyString(iters);	

	var before = new Date().getTime();
	for (var i = 0; i < iters - 1; i++) {
		var ss = s.slice(i, i+1);
	}
	var after = new Date().getTime();

	resultsEl.innerHTML = after - before;
}

function getEmptyString(length) {
	var s = [];

	var ten = "          ";
	var times = length / 10;
	for (var i = 0; i < times; i++) {
		s.push(ten);
	}

	var one = " ";
	var remainder = length % 10;
	for (var i = 0; i < remainder; i++) {
		s.push(one);
	}

	return s.join("");
}

var STRING_TESTS = {
	name: "String Tests", tests: [
		{ name: "concat", iters: 100000, func: stringConcat },
		{ name: "push + join", iters: 100000, func: stringPushJoin },
		{ name: "split (regex delim)", iters: 10000, func: stringSplit, args: [/\s+/] },
		{ name: "split (string delim)", iters: 10000, func: stringSplit, args: [" "] },
		{ name: "substring", iters: 100000, func: stringSubstring },
		{ name: "substr", iters: 100000, func: stringSubstr },
		{ name: "slice", iters: 100000, func: stringSlice },
		{ name: "charAt", iters: 100000, func: stringCharAt },
		{ name: "replace(/&lt;/g,'&amp;lt;')", iters: 1000, func: stringReplace, args: [/</g, "&lt;"] }
	]
};
