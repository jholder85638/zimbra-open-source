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
 * Represents a entry field for entering numeric values.  Has 2 arrow buttons
 * that can be used to increment or decrement the current value with a step
 * that can be specified.
 *
 * @extends DwtControl
 *
 * <h4>CSS</h4>
 *
 * - DwtSpinner              -- a table that contains the spinner elements
 * - DwtSpinner-inputCell    -- the TD that holds the input field
 * - DwtSpinner-btnCell      -- a DIV holding the 2 arrow buttons
 * - DwtSpinner-upBtn        -- the DIV button for increment operation
 * - DwtSpinner-downBtn      -- the DIV button for decrement operation
 * - DwtSpinner-up-pressed   -- upBtn while pressed
 * - DwtSpinner-down-pressed -- downBtn while pressed
 * - DwtSpinner-disabled     -- the table gets this class added when the widget is disabled
 *
 * @param {DwtComposite} parent Parent widget, passed to DwtControl
 * @param {String} className the class name for the containing DIV, passed to DwtControl
 * @param {String} posStyle positioning style, passed to DwtControl
 * @param {Number} max The maximum value
 * @param {Number} min The minimum value
 * @param {Number} size Size of the input field, as in <input size="X">
 * @param {Number} value The original value of the input field
 * @param {Number} maxLen The maximum length of the text in the input field
 * @param {Number} step Amount to add or substract when the arrow buttons are pressed
 * @param {Number} decimals Number of decimal digits.  Specify 0 to allow only
 *                 integers (default).  Pass 'null' to allow float numbers but
 *                 not enforce decimals.
 * @param {String} align The align of the input field text; defaults to "right" in dwt.css
 * @param {boolean} skipCaretHack true to NOT do hack to make the cursor show up in Firefox.
 * 								  The hack uses a block display div, so for an input that needs
 * 								  to be displayed inline, set this parameter to true.
 *
 * @author Mihai Bazon
 */
function DwtSpinner(params) {
	if (arguments.length == 0) return;
	DwtControl.call(this, params.parent, params.className, params.posStyle);

	// setup arguments
	this._maxValue      = params.max  != null ? params.max  : null;
	this._minValue      = params.min  != null ? params.min  : null;
	this._fieldSize     = params.size != null ? params.size : 3;
	this._origValue     = params.value     || 0;
	this._maxLen        = params.maxLen    || null;
	this._step          = params.step      || 1;
	this._decimals      = 'decimals' in params ? params.decimals : 0;
	this._align         = params.align     || null;
	this._skipCaretHack = params.skipCaretHack;

	// timerFunc is a closure that gets called upon timeout when the user
	// presses and holds the mouse button
	this._timerFunc = AjxCallback.simpleClosure(this._timerFunc, this);

	// upon click and hold we capture mouse events
	this._btnPressCapture = new DwtMouseEventCapture(
		this, "DwtSpinner",
		null, // no mouseover
		null, // no mousedown
		null, // no mousemove
		AjxCallback.simpleClosure(this._stopCapture, this), // mouseup
		null, // no mouseout
		true);  // hard capture

	this._createElements();
};

DwtSpinner.prototype = new DwtControl;
DwtSpinner.prototype.constructor = DwtSpinner;

DwtSpinner.INIT_TIMER = 250;
DwtSpinner.SLOW_TIMER = 125;
DwtSpinner.FAST_TIMER = 33;

DwtSpinner.prototype._createElements = function() {
	var div = this.getHtmlElement();
	var id = Dwt.getNextId();
	this._idField = id;
	this._idUpButton = id + "-up";
	this._idDownButton = id + "-down";
	var hackBegin = this._skipCaretHack ? "" : Dwt.CARET_HACK_BEGIN;
	var hackEnd = this._skipCaretHack ? "" : Dwt.CARET_HACK_END;
	var html = [ "<table class='DwtSpinner' cellspacing='0' cellpadding='0'>",
		     "<tr><td rowspan='2' class='DwtSpinner-inputCell'>", hackBegin, "<input id='", id, "' autocomplete='off' />", hackEnd, "</td>",
		     "<td unselectable id='", this._idUpButton, "' class='DwtSpinner-upBtn'><div class='ImgUpArrowSmall'>&nbsp;</div></td>",
		     "</tr><tr>",
		     "<td unselectable id='", this._idDownButton, "' class='DwtSpinner-downBtn'><div class='ImgDownArrowSmall'>&nbsp;</div></td>",
		     "</tr></table>" ];


// 		     "<td><div class='DwtSpinner-btnCell'>",
// 		     "<div unselectable class='DwtSpinner-upBtn' id='", this._idUpButton, "'><div class='ImgUpArrowSmall'>&nbsp;</div></div>",
// 		     "<div unselectable class='DwtSpinner-downBtn' id='", this._idDownButton, "'><div class='ImgDownArrowSmall'>&nbsp;</div></div>",
// 		     "</div></td></tr></table>" ];
	div.innerHTML = html.join("");

	var b1 = this._getUpButton();
	b1.onmousedown = AjxCallback.simpleClosure(this._btnPressed, this, "Up");
	var b2 = this._getDownButton();
	b2.onmousedown = AjxCallback.simpleClosure(this._btnPressed, this, "Down");
// 	if (AjxEnv.isIE) {
// 		b1.ondblclick = b1.onmousedown;
// 		b2.ondblclick = b2.onmousedown;
//	}
// 	if (AjxEnv.isIE && b1.offsetHeight == 1) {
// 		// we must correct button heights for IE
// 		div = b1.parentNode;
// 		var td = div.parentNode;
// 		div.style.height = td.offsetHeight + "px";
// // 		b1.style.height = b2.style.height = td.offsetHeight / 2 + "px";
// // 		b2.style.top = "";
// // 		b2.style.bottom = "0px";
// 	}
	var input = this.getInputElement();
	if (this._maxLen)
		input.maxLength = this._maxLen;
	if (this._fieldSize)
		input.size = this._fieldSize;
	if (this._align)
		input.style.textAlign = this._align;
	if (this._origValue != null)
		this.setValue(this._origValue);

	input.onblur = AjxCallback.simpleClosure(this.setValue, this, null);
	input[(AjxEnv.isIE || AjxEnv.isOpera) ? "onkeydown" : "onkeypress"]
		= AjxCallback.simpleClosure(this.__onKeyPress, this);
};

DwtSpinner.prototype._getValidValue = function(val) {
	var n = parseFloat(val);
	if (isNaN(n) || n == null)
		n = this._lastValidValue; // note that this may be string
	if (n == null)
		n = this._minValue || 0;
	if (this._minValue != null && n < this._minValue)
		n = this._minValue;
	if (this._maxValue != null && n > this._maxValue)
		n = this._maxValue;
	// make sure it's a number
	n = parseFloat(n);
	if (this._decimals != null)
		n = n.toFixed(this._decimals);
	this._lastValidValue = n;
	return n;
};

DwtSpinner.prototype.getInputElement = function() {
	return document.getElementById(this._idField);
};

DwtSpinner.prototype._getUpButton = function() {
	return document.getElementById(this._idUpButton);
};

DwtSpinner.prototype._getDownButton = function() {
	return document.getElementById(this._idDownButton);
};

DwtSpinner.prototype._getButton = function(direction) {
	switch (direction) {
	    case "Up"   : return this._getUpButton();
	    case "Down" : return this._getDownButton();
	}
};

DwtSpinner.prototype._setBtnState = function(dir, disabled) {
	var btn = this._getButton(dir);
	if (disabled) {
		Dwt.addClass(btn, "DwtSpinner-" + dir + "-disabled");
		btn.firstChild.className = "Img" + dir + "ArrowSmallDis";
	} else {
		Dwt.delClass(btn, "DwtSpinner-" + dir + "-disabled");
		btn.firstChild.className = "Img" + dir + "ArrowSmall";
	}
};

DwtSpinner.prototype.getValue = function() {
	return parseFloat(this._getValidValue(this.getInputElement().value));
};

DwtSpinner.prototype.setValue = function(val) {
	if (val == null)
		val = this.getInputElement().value;
	val = this._getValidValue(val);
	this.getInputElement().value = val;
	val = parseFloat(val);
	this._setBtnState("Down", this._minValue != null && this._minValue == val);
	this._setBtnState("Up", this._maxValue != null && this._maxValue == val);
};

DwtSpinner.prototype.setEnabled = function(enabled) {
	DwtControl.prototype.setEnabled.call(this, enabled);
	this.getInputElement().disabled = !enabled;
	var table = this.getHtmlElement().firstChild;
	if (!enabled)
		Dwt.addClass(table, "DwtSpinner-disabled");
	else
		Dwt.delClass(table, "DwtSpinner-disabled");
};

DwtSpinner.prototype._rotateVal = function(direction) {
	var val = this.getValue();
	switch (direction) {
	    case "Up"   : val += this._step; break;
	    case "Down" : val -= this._step; break;
	}
	this.setValue(val);
};

DwtSpinner.prototype._btnPressed = function(direction) {
	if (!this.getEnabled())
		return;
	Dwt.addClass(this._getButton(direction), "DwtSpinner-" + direction + "-pressed");
	this._direction = direction;
	this._rotateVal(direction);
	this._btnPressCapture.capture();
	this._timerSteps = 0;
	this._timer = setTimeout(this._timerFunc, DwtSpinner.INIT_TIMER);
};

DwtSpinner.prototype._timerFunc = function() {
	var v1 = this.getValue();
	this._rotateVal(this._direction);
	var v2 = this.getValue();
	this._timerSteps++;
	var timeout = this._timerSteps > 4 ? DwtSpinner.FAST_TIMER : DwtSpinner.SLOW_TIMER;
	if (v1 != v2)
		this._timer = setTimeout(this._timerFunc, timeout);
	else
		this._stopCapture();
};

DwtSpinner.prototype._stopCapture = function() {
	if (this._timer)
		clearTimeout(this._timer);
	this._timer = null;
	this._timerSteps = null;
	var direction = this._direction;
	Dwt.delClass(this._getButton(direction), "DwtSpinner-" + direction + "-pressed");
	this._direction = null;
	this._btnPressCapture.release();
	var input = this.getInputElement();
	input.focus();
	Dwt.setSelectionRange(input, 0, input.value.length);
};

DwtSpinner.prototype.__onKeyPress = function(ev) {
	if (AjxEnv.isIE)
		ev = window.event;
	var dir = null;
	switch (ev.keyCode) {
	    case 38:
		dir = "Up";
		break;
	    case 40:
		dir = "Down";
		break;
	}
	if (dir) {
		this._rotateVal(dir);
		var input = this.getInputElement();
		Dwt.setSelectionRange(input, 0, input.value.length);
	}
};

DwtSpinner.prototype.focus = function() {
	this.getInputElement().focus();
};

DwtSpinner.prototype.select = function() {
	var input = this.getInputElement();
	input.focus();
	Dwt.setSelectionRange(input, 0, input.value.length);
};
