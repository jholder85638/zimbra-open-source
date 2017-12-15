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
 * @class DwtIframe
 * @author Mihai Bazon
 *
 * Simple event proxy.  Creates an IFRAME, inserts the given html into it and
 * forwards any events to the parent widget, translating mouse coordinates in
 * between.
 *
 * @param parent - The parent DwtComposite
 * @param html - the HTML code to be inserted in the IFRAME.  There will be
 *   slight modifications to it (i.e. the margins and paddings of the HTML
 *   element will be set to 0, also any margins for BODY).  @param
 * @param noscroll - (optional, default false) hide the scroll bars?
 * @param posStyle - (optional, default "static") passed over to DwtControl
 * @param processHtmlCallback - (optional) AjxCallback that will be called
 *   immediately after the HTML code was inserted.  A ref. to the document object
 *   will be passed.
 * @param useKbMgmt	[boolean]*		if true, participate in keyboard mgmt
 */
function DwtIframe(params) {
	var posStyle = params.posStyle ? params.posStyle : DwtControl.STATIC_STYLE;
	DwtControl.call(this, params.parent, params.className || "DwtIframe", posStyle, false);
	this._styles = params.styles;
	this._noscroll = params.noscroll;
	this._iframeID = Dwt.getNextId();
	this._processHtmlCallback = params.processHtmlCallback;
	this._hidden = params.hidden;
	this._createFrame(params.html);
	
	if (params.useKbMgmt) {
		var iframe = this.getIframe();
		var idoc = Dwt.getIframeDoc(iframe);
		var doc = AjxEnv.isIE ? idoc : iframe.contentWindow;
		Dwt.setHandler(doc, DwtEvent.ONKEYDOWN, DwtKeyboardMgr.__keyDownHdlr);
		Dwt.setHandler(doc, DwtEvent.ONKEYUP, DwtKeyboardMgr.__keyUpHdlr);
		Dwt.setHandler(doc, DwtEvent.ONKEYPRESS, DwtKeyboardMgr.__keyPressHdlr);
	}
};

DwtIframe.prototype = new DwtControl;
DwtIframe.prototype.constructor = DwtIframe;

DwtIframe.prototype.getIframe = function() {
	return document.getElementById(this._iframeID);
};

DwtIframe.prototype.getDocument = function() {
	return this.getIframe().contentWindow.document;
};

/// Forwards events to the parent widget
DwtIframe.prototype._rawEventHandler = function(ev) {
	var iframe = this.getIframe();
	var win = iframe.contentWindow;
	if (AjxEnv.isIE)
		ev = win.event;

	var dw;
	// This probably sucks.
	if (/mouse|context|click|select/i.test(ev.type))
		dw = new DwtMouseEvent(true);
	else
		dw = new DwtUiEvent(true);
	dw.setFromDhtmlEvent(ev);

	// HACK! who would have know.. :-(
	// perhaps we need a proper mapping
	var type = dw.type.toLowerCase();
	if (!/^on/.test(type))
		type = "on" + type;
	// translate event coordinates
	var pos = this.getLocation();

	// What I can tell for sure is that we don't want the code below for IE
	// and we want it for Gecko, but I can't be sure of other browsers..
	// Let's assume they follow Gecko.  Seems mostly a trial and error
	// process :(
	if (!AjxEnv.isIE) {
		var doc = win.document;
		var sl = doc.documentElement.scrollLeft || doc.body.scrollLeft;
		var st = doc.documentElement.scrollTop || doc.body.scrollTop;
		pos.x -= sl;
		pos.y -= st;
	}

	dw.docX += pos.x;
	dw.docY += pos.y;
	dw.elementX += pos.x;
	dw.elementY += pos.y;

//   	window.status = dw.type + " doc(" + dw.docX + ", " + dw.docY + ") " +
//   		" element(" + dw.elementX + ", " + dw.elementY + ") " +
//  		" stopPropagation: " + dw._stopPropagation + ", " +
//  		" returnValue: " + dw._returnValue;

	var capture = DwtMouseEventCapture.getCaptureObj();
	if (AjxEnv.isIE || AjxEnv.isSafari || !capture) {
		// go for Dwt events
		DwtEventManager.notifyListeners(type, dw);
		this.parent.notifyListeners(type, dw);
	} else {
		// Satisfy object that holds the mouse capture.

		// the following is DOM2, not supported by IE
		var fake = document.createEvent("MouseEvents");
		fake.initMouseEvent(ev.type,
				    true, // can bubble
				    true, // cancellable
				    document.defaultView, // the view
				    0, // event detail ("click count")
				    ev.screenX, // screen X
				    ev.screenY, // screen Y
				    dw.docX, // clientX, but translated to page
				    dw.docY, // clientY, translated
				    ev.ctrlKey, // key status...
				    ev.altKey,
				    ev.shiftKey,
				    ev.metaKey,
				    ev.button,
				    ev.relatedTarget);
		document.body.dispatchEvent(fake);
		// capture[DwtIframe._captureEvents[dw.type]](fake);
	}

	dw.setToDhtmlEvent(ev);
	return dw._returnValue;
};

// map event names to the handler name in a DwtMouseEventCapture object
// DwtIframe._captureEvents = { mousedown : "_mouseDownHdlr",
// 			     mousemove : "_mouseMoveHdlr",
// 			     mouseout  : "_mouseOutHdlr",
// 			     mouseover : "_mouseOverHdlr",
// 			     mouseup   : "_mouseUpHdlr" };

DwtIframe._forwardEvents = [ DwtEvent.ONCHANGE,
			     DwtEvent.ONCLICK,
			     DwtEvent.ONDBLCLICK,
			     DwtEvent.ONFOCUS,
			     DwtEvent.ONKEYDOWN,
			     DwtEvent.ONKEYPRESS,
			     DwtEvent.ONKEYUP,
			     DwtEvent.ONMOUSEDOWN,
			     DwtEvent.ONMOUSEENTER,
			     DwtEvent.ONMOUSELEAVE,
			     DwtEvent.ONMOUSEMOVE,
			     DwtEvent.ONMOUSEOUT,
			     DwtEvent.ONMOUSEOVER,
			     DwtEvent.ONMOUSEUP,
			     DwtEvent.ONSELECTSTART ];

DwtIframe.prototype._createFrame = function(html) {
	var self = this;

	// this is an inner function so that we can access the object (self).
	// it shouldn't create a memory leak since it doesn't directly "see"
	// the iframe variable (it's protected below)
	function rawHandlerProxy(ev) { return self._rawEventHandler(ev); };

	// closure: protect the reference to the iframe node here.
	(function() {
		var iframe, tmp = [], i = 0, idoc;

		tmp[i++] = "<iframe";
		if (self._noscroll)
			tmp[i++] = " scrolling='no'";
		if (self._hidden)
			tmp[i++] = " style='visibility:hidden'";
		tmp[i++] = " frameborder='0' width='100%' id='";
		tmp[i++] = self._iframeID;
		tmp[i++] = "' src='javascript:\"\";'></iframe>";
		self.setContent(tmp.join(''));

		// Bug 7523: @import url() lines will make Gecko report
		// document.body is undefined until "onload" (unacceptable) so
		// we drop these lines now.
		html = html.replace(/(<style[^>]*>)[\s\t\u00A0]*(.*?)[\s\t\u00A0]*<\x2fstyle>/mgi,
				    function(s, p1, p2) {
					    return p1 + p2.replace(/@import.*?(;|[\s\t\u00A0]*$)/gi, "") + "</style>";
				    });

		iframe = self.getIframe();
		idoc = Dwt.getIframeDoc(iframe);
		idoc.open();
		// make sure to explicitly add head tag for safari otherwise it cannot
		// be implicitly referenced (i.e. getElementsByTagName('head'))
		if (AjxEnv.isSafari)
			idoc.write("<html><head></head>");
		if (self._styles)
			idoc.write([ "<style type='text/css'>", self._styles, "</style>" ].join(""));
		idoc.write(html);
		idoc.close();
		// if we're not giving a break, we can safely do any postprocessing
		// here.  I.e. if we want to drop backgroundImage-s, it's safe to do it
		// here because the browser won't have a chance to load them.
		if (self._processHtmlCallback)
			self._processHtmlCallback.run(idoc);

		// if we have margins, the translated coordinates won't be OK.
		// it's best to remove them.  THE way to have some spacing is
		// to set padding on the body element.
		tmp = idoc.documentElement.style;
		tmp.margin = tmp.padding = idoc.body.style.margin = "0";

		// not sure this is needed, but it seems technically OK.
		Dwt.associateElementWithObject(idoc, self);

		// assign event handlers
		tmp = DwtIframe._forwardEvents;
		if (!AjxEnv.isIE)
			idoc = iframe.contentWindow;
		for (i = tmp.length; --i >= 0;)
			idoc[tmp[i]] = rawHandlerProxy;

		// catch browser context menus
		// idoc[DwtEvent.ONCONTEXTMENU] = DwtShell._preventDefaultPrt;
	})();
};
