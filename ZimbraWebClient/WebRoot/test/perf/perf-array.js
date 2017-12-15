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
function arrayConcat(iters, resultsEl) {
	var before = new Date().getTime();
	var a = [];
	for (var i = 0; i < iters; i++) {
		a.concat(1, 2, [3, 4]);
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}

function arrayJoin(iters, resultsEl) {
	var a = [];
	for (var i = 0; i < 1000; i++) {
		a.push(i);
	}
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		var s = a.join(" ");
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}

function arrayPush(iters, resultsEl) {
	var a = [];
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		a.push(i);
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}
function arrayPop(iters, resultsEl) {
	var a = [];
	for (var i = 0; i < iters; i++) {
		a.push(i);
	}
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		var o = a.pop();
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}
function arrayPop2(iters, resultsEl) {
	var a = [];
	for (var i = 0; i < iters; i++) {
		a.push(i);
	}
	var before = new Date().getTime();
	for (var i = iters-1; i >= 0; i--) {
		var o = a[i];
	}
	a.length = 0;
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}

function arrayReverse(iters, resultsEl) {
	var a = [];
	for (var i = 0; i < iters; i++) {
		a.push(i);
	}
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		a.reverse();
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}

function arrayUnshift(iters, resultsEl) {
	var a = [];
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		a.unshift(i);
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}
function arrayShift(iters, resultsEl) {
	var a = [];
	for (var i = 0; i < iters; i++) {
		a.push(i);
	}
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		var o = a.shift();
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}

function arraySlice(iters, resultsEl) {
	var a = [];
	for (var i = 0; i < iters; i++) {
		a.push(i);
	}
	var before = new Date().getTime();
	for (var i = 0; i < iters - 1; i++) {
		var array = a.slice(i, i+1);
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}

function arraySplice(iters, resultsEl) {
	var a = [];
	for (var i = 0; i < iters; i++) {
		a.push(i);
	}
	var before = new Date().getTime();
	for (var i = 0; i < iters - 1; i++) {
		a.splice(i, 1, 42);
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}

function arraySort(iters, resultsEl, length) {
	var arrays = [];
	for (var i = 0; i < iters; i++) {
		var a = [];
		for (var j = 0; j < length; j++) {
			a.push(j);
		}
		a.reverse();
		arrays.push(a);
	}
	var before = new Date().getTime();
	for (var i = 0; i < iters; i++) {
		arrays[i].sort();
	}
	var after = new Date().getTime();
	
	resultsEl.innerHTML = after - before;
}

var ARRAY_TESTS = {
	name: "Array Tests", tests: [
		{ name: "concat", iters: 100000, func: arrayConcat },
		{ name: "join", iters: 1000, func: arrayJoin },
		{ name: "push", iters: 10000, func: arrayPush },
		{ name: "pop", iters: 10000, func: arrayPop },
		{ name: "pop (set length after accesses)", iters: 10000, func: arrayPop2 },
		{ name: "reverse", iters: 1000, func: arrayReverse },
		{ name: "shift", iters: 1000, func: arrayShift },
		{ name: "unshift", iters: 1000, func: arrayUnshift },
		{ name: "slice", iters: 5000, func: arraySlice },
		{ name: "splice", iters: 5000, func: arraySplice },
		{ name: "sort (len = 100)", iters: 100, func: arraySort, args: [100] },
		{ name: "sort (len = 1000)", iters: 100, func: arraySort, args: [1000] }
	]
};
