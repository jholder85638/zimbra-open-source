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
function jsEval(iters, resultsEl) {
	var a = ['var object = {'];
	for (var i = 0; i < iters; i++) {
		if (i > 0) a.push(', ');
		a.push("i"+i, ':', i);
	}
	a.push('}');
	var js = a.join(" ");
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		eval(js);
	}
	var after = new Date().getTime();

	resultsEl.innerHTML = after - before;
}

function jsParseInt(iters, resultsEl) {
	var num = "42";
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		var val = parseInt(num);
	}
	var after = new Date().getTime();

	resultsEl.innerHTML = after - before;
}
function jsCastInt(iters, resultsEl) {
	var num = "42";
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		var val = Number(num);
	}
	var after = new Date().getTime();

	resultsEl.innerHTML = after - before;
}

function jsParseFloat(iters, resultsEl) {
	var num = "3.14";
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		var val = parseFloat(num);
	}
	var after = new Date().getTime();

	resultsEl.innerHTML = after - before;
}
function jsCastFloat(iters, resultsEl) {
	var num = "3.14";
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		var val = Number(num);
	}
	var after = new Date().getTime();

	resultsEl.innerHTML = after - before;
}

var JS_TESTS = {
	name: "JavaScript Tests", tests: [
		{ name: "eval JSON", iters: 1000, func: jsEval },
		{ name: "parse int", iters: 100000, func: jsParseInt },
		{ name: "cast int", iters: 100000, func: jsCastInt },
		{ name: "parse float", iters: 100000, func: jsParseFloat },
		{ name: "cast float", iters: 100000, func: jsCastFloat }
	]
};