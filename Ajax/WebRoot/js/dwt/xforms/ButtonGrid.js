/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2005, 2006, 2007 Zimbra, Inc.
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
* @class ButtonGrid  example of putting a custom widget in a XForm
* @constructor
**/

function ButtonGrid(attributes) {
	XFG.assignUniqueId(this, "__BUTTON_GRID__");

	// copy any props passed in into the object
	for (var prop in attributes) {
		this[prop] = attributes[prop];
	}
	// handle any props that need special care
	if (this.onChange) this.setOnChange(this.onChange);

	// initialize to an empty array for value
	this.value = [];
	
	
}
var BGP = ButtonGrid.prototype;
BGP.choices = null;
BGP.numCols = 4;
BGP.cssClass = "xform_button_grid_medium";
BGP.onChange = null;
BGP.multiple = true;
BGP.addBracketingCells = false;
BGP.toggleMode = true;

BGP.setOnChange = function (onChange) {
	if (onChange && typeof onChange == "string") {
		onChange = new Function("value, event", onChange);	
	}
	this.onChange = onChange;
}


BGP.getValue = function() {
	return this.value;
}

BGP.setValue = function(value) {
	if (value == null) value = [];
	if (typeof value == "string") value = value.split(",");
	this.value = value;
	
	this.showValue();
}

BGP.showValue = function() {
	var value = this.value;
	if (value == null) value = "";

	// assumes value is a comma-delimited string or an array
	if (typeof value == "string") value = value.split(",");
	
	// hack up value to make searching for a particular option value easier
	var uniqueStartStr = "{|[", uniqueEndStr = "]|}";
	value = uniqueStartStr + value.join(uniqueEndStr + uniqueStartStr) + uniqueEndStr;
	var choices = this.choices;
	for (var i = 0; i < choices.length; i++) {
		var element = XFG.getEl(this.getButtonId(i));
		var isPresent = (value.indexOf(uniqueStartStr + choices[i].value + uniqueEndStr) > -1);
		if (isPresent) {
			XFG.showSelected(element);
		} else {
			XFG.hideSelected(element);
		}
	}	
}

BGP.toggleValue = function(value, element) {
	if (this.multiple) {
		if (this.valueIsSelected(value)) {
			this.deselectValue(value, element);
		} else {
			this.selectValue(value, element);
		}
	} else {
		this.value = value;
	}
	return this.value;
}

BGP.valueIsSelected = function(value) {
	for (var i = 0; i < this.value.length; i++) {
		if (this.value[i] == value) return true;
	}
}

BGP.selectValue = function(value, element) {
	if (!this.valueIsSelected(value)) {
		this.value.push(value);
	}
	if (element) XFG.showSelected(element);
}

BGP.deselectValue = function(value, element) {
	if (this.valueIsSelected(value)) {
		for (var i = 0; i < this.value.length; i++) {
			if (this.value[i] == value) {
				this.value = this.value.slice(0, i).concat(this.value.slice(i+1, this.value.length));
			}
		}
	}
	if (element) XFG.hideSelected(element);
}

BGP.getButtonId = function (btnNum) {
	return this.__id + "_button_" + btnNum;
}


BGP.onButtonClick = function(choiceValue, element, event) {
	if(this.toggleMode)  {
		var newValue = this.toggleValue(choiceValue, element);
		if (this.onChange) {
			this.onChange(newValue, event);
		}
	} 
}

BGP.onMouseDown = function(choiceValue, element, event) {
	if(!this.toggleMode)  {
		if (element) 
			XFG.showSelected(element);
	}
	
}

BGP.onMouseUp = function(choiceValue, element, event) {
	if(!this.toggleMode)  {
		if (this.onChange) 
			this.onChange(choiceValue, event,element);
			
		if (element) 
			XFG.hideSelected(element);
	}
}

BGP.getHTML = function (indent) {
	if (this.choices == null) return (this.__HTMLOutput = null);

	var buffer = new AjxBuffer();
	if (indent == null) indent = "";
	
	// write HTML for this element
	var buttonCssClass = this.cssClass + "_button";
	buffer.append(indent, "<table class=\"", this.cssClass, "_table\">");
	var i = 0;
	var numRows = Math.ceil(this.choices.length / this.numCols);
	for (var r = 0; r < numRows; r++) {
		buffer.append("<tr>\r");
		if (this.addBracketingCells) {
			buffer.append("\t<td width=50%><div class=", this.cssClass + "_start></div></td>");
		}
		for (var c = 0; c < this.numCols; c++) {
			var choice = this.choices[i];
			if (typeof choice == "string") {
				choice = this.choices[i] = {value:choice, label:choice};
			}
			buffer.append(indent, "\t<td class=", this.cssClass + "_td ><div id=", this.getButtonId(i), " class=", buttonCssClass);
			if(this.toggleMode) {
				buffer.append(" onclick=\"XFG.cacheGet('", this.__id, "').onButtonClick('",choice.value,"',this,event);\"");
			} else {
				buffer.append(" onmousedown=\"XFG.cacheGet('", this.__id, "').onMouseDown('",choice.value,"',this,event);\"");	
				buffer.append(" onmouseup=\"XFG.cacheGet('", this.__id, "').onMouseUp('",choice.value,"',this,event);\"");					
			}
			buffer.append(">",choice.label,"</div></td>\r");
			i++;
			if (i >= this.choices.length) break;
		}
		if (this.addBracketingCells) {
			buffer.append("\t<td width=50%><div class=", this.cssClass + "_end></div></td>");
		}
		buffer.append(indent, "</tr>");
	}
	buffer.append("</table>");
	this.__HTMLOutput = buffer.toString();
	return this.__HTMLOutput;
}


BGP.insertIntoXForm = function (form, item, element) {
	element.innerHTML = this.getHTML();
}

BGP.updateChoicesHTML = function(labels) {
	var i = 0;
	for (var r = 0; r < numRows; r++) {
		for (var c = 0; c < this.numCols; c++) {
			var btn = document.getElementById(this.getButtonId(i));
			if(btn) {
				btn.innerHTML =labels[i].label;
			}
			i++;
			if (i >= labels.length) break;
		}
	}
}

BGP.updateInXForm = function (form, item, value, element) {
	var valueStr = (value instanceof Array ? value.join(",") : value);
	if (!form.forceUpdate && this.__lastDisplayValue == valueStr) return;
	
	this.setValue(value);
	this.__lastDisplayValue = valueStr;
}

