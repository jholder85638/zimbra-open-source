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

/**
 * @constructor
 * @class
 * DwtCssStyle is a static class that defines a number of contants and helper methods that
 * support the working with CSS
 * 
 * @author Ross Dargahi
 */
function DwtCssStyle() {
}

// Common class name constants used in Dwt

/** UI component has been selected e.g a left-click in a list or tree
 * @type string*/
DwtCssStyle.SELECTED = "selected";

/** UI component has been actioned e.g. a right-click in a list or tree
 * @type string*/
DwtCssStyle.ACTIONED = "actioned";

/** TODO: Should this be here matched item in a list
 * @type string*/
DwtCssStyle.MATCHED	 = "matched";

/** DnD icon version of an item
 * @type string*/
DwtCssStyle.DND = "dnd";

/** actioned item (right-click) in a list or tree
 * @type string*/
DwtCssStyle.ACTIVE = "active";		// a button that is the default for some action

/** actioned item (right-click) in a list or tree
 * @type string*/
DwtCssStyle.ACTIVATED = "activated";	// a button that has the focus

/** UI component has been triggered e.g. a button being pressed (but not yet released)
 * @type string*/
DwtCssStyle.TRIGGERED = "triggered";

/** actioned item (right-click) in a list or tree
 * @type string*/
DwtCssStyle.TOGGLED = "toggled";	// a button that has been toggled on

/** actioned item (right-click) in a list or tree
 * @type string*/
DwtCssStyle.INACTIVE = "inactive";	// a button that is inactive (closed tab button)

/** actioned item (right-click) in a list or tree
 * @type string*/
DwtCssStyle.DISABLED = "disabled";	// a disabled item

/** item has keyboard focus
 * @type string */
 DwtCssStyle.FOCUSED = "focused";


DwtCssStyle.getProperty = 
function(htmlElement, cssPropName) {
	var result;
	if (htmlElement.ownerDocument == null) {
		// IE5.5 does not suppoert ownerDocument
		for(var parent = htmlElement.parentNode; parent.parentNode != null; parent = parent.parentNode);
		var doc = parent;
	} else {
		var doc = htmlElement.ownerDocument;
	}
	
	if (doc.defaultView && !AjxEnv.isSafari) {
		var cssDecl = doc.defaultView.getComputedStyle(htmlElement, "");
		result = cssDecl.getPropertyValue(cssPropName);
	} else {
		  // Convert CSS -> DOM name for IE etc
			var tokens = cssPropName.split("-");
			var propName = "";
			var i;
			var len = tokens.length;
			for (i = 0; i < len; i++) {
				if (i != 0) 
					propName += tokens[i].substring(0, 1).toUpperCase();
				else 
					propName += tokens[i].substring(0, 1);
				propName += tokens[i].substring(1);
			}
			if (htmlElement.currentStyle)
				result = htmlElement.currentStyle[propName];
			else if (htmlElement.style)
				result = htmlElement.style[propName];
	}
	return result;
};

DwtCssStyle.getComputedStyleObject = 
function(htmlElement) {
	if (htmlElement.ownerDocument == null) {
		// IE5.5 does not suppoert ownerDocument
		for(var parent = htmlElement.parentNode; parent.parentNode != null; parent = parent.parentNode);
		var doc = parent;
	} else {
		var doc = htmlElement.ownerDocument;
	}
	
	if (doc.defaultView && !AjxEnv.isSafari)
		return doc.defaultView.getComputedStyle(htmlElement, "");
	else if (htmlElement.currentStyle)
		return htmlElement.currentStyle;
	else if (htmlElement.style)
		return htmlElement.style;
};

DwtCssStyle.removeProperty = function(el, prop) {
	if (prop instanceof Array) {
		for (var i = prop.length; --i >= 0;)
			DwtCssStyle.removeProperty(el, prop[i]);
	} else {
		if (AjxEnv.isIE) {
			el.style.removeAttribute(prop, true);
		} else {
			prop = prop.replace(/([A-Z])/g, "-$1");
			el.style.removeProperty(prop);
		}
	}
};
