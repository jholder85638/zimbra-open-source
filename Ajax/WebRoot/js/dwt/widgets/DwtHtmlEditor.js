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
 * Html Editor
 *
 * @author Ross Dargahi
 */
function DwtHtmlEditor(parent, className, posStyle, content, mode, blankIframeSrc) {
	if (arguments.length == 0) return;
	this.setBlankIframeSrc(blankIframeSrc);
	className = className || "DwtHtmlEditor";
	DwtComposite.call(this, parent, className, posStyle);

	this._mode = mode == DwtHtmlEditor.HTML && this.isHtmlEditingSupported()
		? mode : DwtHtmlEditor.TEXT;

	this.__eventClosure = AjxCallback.simpleClosure(this.__eventClosure, this);

	// init content
	if (!content)
		content = this._mode == DwtHtmlEditor.HTML ? "<html><head></head><body></body></html>" : "";

	this._pendingContent = content;
	this._htmlModeInited = false;

	this._initialize();
}

DwtHtmlEditor.prototype = new DwtComposite();
DwtHtmlEditor.prototype.constructor = DwtHtmlEditor;

// Modes
DwtHtmlEditor.HTML = 1;
DwtHtmlEditor.TEXT = 2;

// Styles
DwtHtmlEditor.H1 = 1;
DwtHtmlEditor.H2 = 2;
DwtHtmlEditor.H3 = 3;
DwtHtmlEditor.H4 = 4;
DwtHtmlEditor.H5 = 5;
DwtHtmlEditor.H6 = 6;
DwtHtmlEditor.PARAGRAPH = 7;
DwtHtmlEditor.ADDRESS = 8;
DwtHtmlEditor.PREFORMATTED = 9;

DwtHtmlEditor._STYLES = ["", "<h1>", "<h2>", "<h3>", "<h4>", "<h5>", "<h6>", "<p>", "<address>", "<pre>"];

// Font Styles
DwtHtmlEditor.BOLD_STYLE = "bold";
DwtHtmlEditor.ITALIC_STYLE = "italic";
DwtHtmlEditor.UNDERLINE_STYLE = "underline";
DwtHtmlEditor.STRIKETHRU_STYLE = "strikethrough";
DwtHtmlEditor.SUBSCRIPT_STYLE = "subscript";
DwtHtmlEditor.SUPERSCRIPT_STYLE = "superscript";

// Justification
DwtHtmlEditor.JUSTIFY_LEFT = "justifyleft";
DwtHtmlEditor.JUSTIFY_CENTER = "justifycenter";
DwtHtmlEditor.JUSTIFY_RIGHT = "justifyright";
DwtHtmlEditor.JUSTIFY_FULL = "justifyfull";

// Indent
DwtHtmlEditor.OUTDENT = "outdent";
DwtHtmlEditor.INDENT = "indent";

// Elements
DwtHtmlEditor.HORIZ_RULE = "inserthorizontalrule";
DwtHtmlEditor.ORDERED_LIST = "insertorderedlist";
DwtHtmlEditor.UNORDERED_LIST = "insertunorderedlist";
DwtHtmlEditor.IMAGE = "insertimage";

// Direction
DwtHtmlEditor.DIRECTION_R2L;
DwtHtmlEditor.DIRECTION_L2R;

// Borders (for table/cell props)
DwtHtmlEditor.BORDER_TOP    = 0;
DwtHtmlEditor.BORDER_MIDDLE = 1;
DwtHtmlEditor.BORDER_BOTTOM = 2;
DwtHtmlEditor.BORDER_LEFT   = 3;
DwtHtmlEditor.BORDER_CENTER = 4;
DwtHtmlEditor.BORDER_RIGHT  = 5;

// PRIVATE Class Attributes

DwtHtmlEditor._ARIAL_RE = /arial|helvetica|sans-serif/;
DwtHtmlEditor._TIMES_RE = /times|serif/;
DwtHtmlEditor._VERDANA_RE = /verdana/;
DwtHtmlEditor._COURIER_RE = /courier|mono/;

DwtHtmlEditor._H1_RE = /Heading 1|h1/;
DwtHtmlEditor._H2_RE = /Heading 2|h2/;
DwtHtmlEditor._H3_RE = /Heading 2|h3/;
DwtHtmlEditor._H4_RE = /Heading 2|h4/;
DwtHtmlEditor._H5_RE = /Heading 2|h5/;
DwtHtmlEditor._H6_RE = /Heading 2|h6/;
DwtHtmlEditor._PARAGRAPH_RE = /Normal|p/;
DwtHtmlEditor._ADDRESS_RE = /Address|address/;
DwtHtmlEditor._PREFORMATTED_RE = /Formatted|pre/;

DwtHtmlEditor._FONT_NAME = "fontname";
DwtHtmlEditor._FONT_SIZE = "fontsize";
DwtHtmlEditor._FONT_COLOR = "forecolor";
DwtHtmlEditor._FONT_HILITE = "hilitecolor";
DwtHtmlEditor._FONT_HILITE_IE = "backcolor";
DwtHtmlEditor._FORMAT_BLOCK = "formatblock";

/*cut
copy
paste
undo
redo
*/

DwtHtmlEditor._INITDELAY = 50;


DwtHtmlEditor._BLOCK_ELEMENTS = {
	address:1, body:1, div:1, dl:1, fieldset:1, form:1, h1:1, h2:1, h3:1, h4:1, h5:1, h6:1,
	iframe:1, li:1, ol:1, p:1, pre:1, quote:1, table:1, tbody:1, td:1, textarea:1, tfoot: 1,
	thead:1, tr:1, ul:1
};

DwtHtmlEditor._KEY2CMDS = {
	"b":DwtHtmlEditor.BOLD_STYLE, "i":DwtHtmlEditor.ITALIC_STYLE, "u":DwtHtmlEditor.UNDERLINE_STYLE,
	"s":DwtHtmlEditor.STRIKETHRU_STYLE, "l":DwtHtmlEditor.JUSTIFY_LEFT, "e":DwtHtmlEditor.JUSTIFY_CENTER,
	"r":DwtHtmlEditor.JUSTIFY_RIGHT, "j":DwtHtmlEditor.JUSTIFY_FULL, "1":DwtHtmlEditor._STYLES[1],
	"2":DwtHtmlEditor._STYLES[1], "3":DwtHtmlEditor._STYLES[3], "4":DwtHtmlEditor._STYLES[4],
	"5":DwtHtmlEditor._STYLES[5], "6":DwtHtmlEditor._STYLES[6], "0":"DUMP"
};

DwtHtmlEditor.prototype.focus =
function() {
	DBG.println(AjxDebug.DBG1, "DwtHtmlEditor.prototype.focus");
	if (!AjxEnv.isIE && this._mode == DwtHtmlEditor.TEXT) {
		document.getElementById(this._textAreaId).focus();
	} else {
		try {
			this._getIframeWin().focus();
			// Hack to fix IE focusing bug
			if (AjxEnv.isIE) {
				if (this._currInsPt) {
					if (this._currInsPt.text.length <= 1)
						this._currInsPt.collapse(false);
					this._currInsPt.select();
				}
			}
		} catch (ex) {
			// whateva
		}
	}
}

// Moves the caret to the top of the editor.
DwtHtmlEditor.prototype.moveCaretToTop =
function() {
	if (this._mode == DwtHtmlEditor.TEXT) {
		var control = document.getElementById(this._textAreaId);
		if (control.createTextRange) { // IE
			var range = control.createTextRange();
			range.collapse(true);
			range.select();
		} else if (control.setSelectionRange) { // FF
			control.setSelectionRange(0, 0);
		}
	} else {
		this._moveCaretToTopHtml(true);
	}
};

DwtHtmlEditor.prototype._moveCaretToTopHtml =
function(tryOnTimer) {
	var body = this._getIframeDoc().body;
	var success = false;
	if (AjxEnv.isIE) {
		if (body) {
			body.createTextRange().collapse(true);
			success = true;
		}
	} else {
		var selection = this._getSelection();
		if (selection) {
			selection.collapse(body,0);
			success = true;
		}
	}
	if (!success && tryOnTimer) {
		var action = new AjxTimedAction(this, this._moveCaretToTopHtml);
		AjxTimedAction.scheduleAction(action, DwtHtmlEditor._INITDELAY + 1);
	}
};

DwtHtmlEditor.prototype.addStateChangeListener =
function(listener) {
	this.addListener(DwtEvent.STATE_CHANGE, listener);
}

DwtHtmlEditor.prototype.removeStateChangeListener =
function(listener) {
	this.removeListener(DwtEvent.STATE_CHANGE, listener);
}

DwtHtmlEditor.prototype.clear =
function() {
	this.setContent("");
}

DwtHtmlEditor.prototype.enable =
function(enable) {
	if (this._textAreaId != null)
		document.getElementById(this._textAreaId).disabled = !enable;
	if (this._iFrameId != null)
		document.getElementById(this._iFrameId).disabled = !enable;
}

DwtHtmlEditor.prototype.setBlankIframeSrc =
function(src) {
	this._blankIframeSrc = src;
};

DwtHtmlEditor.prototype.isHtmlEditingSupported =
function() {
	return (!AjxEnv.isGeckoBased && !AjxEnv.isIE) ? false : true;
}

/**
 * Get the content
*/
DwtHtmlEditor.prototype.getContent =
function() {
	if (this._mode == DwtHtmlEditor.HTML) {
		var iframeDoc = this._getIframeDoc();
		var html = iframeDoc && iframeDoc.body ? (this._getIframeDoc().body.innerHTML) : "";
		return this._embedHtmlContent(html);
	} else {
		return document.getElementById(this._textAreaId).value;
	}
};

DwtHtmlEditor.prototype._embedHtmlContent =
function(html) {
	return [ "<html><body>",
		 html,
		 "</body></html>" ].join("");
};

/**
 * Set the content to be displayed. This can be HTML
*/
DwtHtmlEditor.prototype.setContent =
function(content) {
	if (AjxEnv.isIE)
		this._currInsPt = null; // reset insertion pointer, bug 11623
	if (this._mode == DwtHtmlEditor.HTML) {
		// If the html is initialed then go ahead and set the content, else let the
		// _finishHtmlModeInit run before we try setting the content
		if (this._htmlModeInited) {
			this._pendingContent = content ? ((content instanceof AjxVector) ? content[0] : content) : "";
			this._setContentOnTimer();
		} else {
			var ta = new AjxTimedAction(this, this.setContent, [content]);
			AjxTimedAction.scheduleAction(ta, DwtHtmlEditor._INITDELAY + 1);
		}
	} else {
		document.getElementById(this._textAreaId).value = (content || "");
	}
}

DwtHtmlEditor.prototype.insertElement =
function(element) {
	this._execCommand(element);
};

DwtHtmlEditor.prototype.insertText =
function(text) {
	var node = this._getIframeDoc().createTextNode(text);
	this._insertNodeAtSelection(node);
};

DwtHtmlEditor.prototype.insertImage =
function(src) {
	this._execCommand(DwtHtmlEditor.IMAGE, src);
};

/** Inserts a table at the current cursor position
 *
 * @param rows [Int] - The number of rows in the table
 * @param cols [Int] - The number of columns in the table
 * @param width [String] - The width of the table. The units should be part of this value e.g. "100%" or "10px"
 * @param cellSpacing [Int] - Cell spacing in the table
 * @param cellPadding [Int] - Cell padding in the table
 * @param alignment [String] - The alignment of the table. This is one of valid alignment values for the HTML
 *    <table> element
 */
DwtHtmlEditor.prototype.insertTable =
function(rows, cols, width, cellSpacing, cellPadding, alignment) {
	if (this._mode != DwtHtmlEditor.HTML)
		return;

	var doc = this._getIframeDoc();
	var table = doc.createElement("table");
	table.className = "DwtHtmlEditor-Table";

	if (width != null) table.style.width = width;
	else table.style.width = "100%";

	table.style.textAlign = "left";
	table.style.verticalAlign = "middle";

	if (alignment != null) table.align = alignment.toLowerCase();

	if (cellSpacing != null) table.cellSpacing = cellSpacing;
	else table.cellSpacing = 0;

	if (cellPadding != null) table.cellPadding = cellPadding;
	else table.cellPadding = 0;

	if (!AjxEnv.isIE)
		table.style.border = "1px solid #000";
	table.style.borderCollapse = "collapse";

	var tdWidth;
	tdWidth = Math.floor(100 / cols) + "%";
	// tdWidth = null;

	var tbody = doc.createElement("tbody");
	table.appendChild(tbody);
	// table.style.tableLayout = "fixed";

	for (var i = 0; i < rows; i++) {
		var tr = doc.createElement("tr");
		tbody.appendChild(tr);
		for (var j = 0; j < cols; j++) {
			var td = doc.createElement("td");
			if (i == 0 && tdWidth)
				td.style.width = tdWidth;
			if (AjxEnv.isGeckoBased) {
				td.appendChild(doc.createElement("br"));
			} else if (AjxEnv.isIE) {
				td.innerHTML = "&nbsp;";
			}
			if (AjxEnv.isIE)
				// since in IE we can't select
				// multiple cells anyway, and since
				// it's buggy if we only assign half
				// of the borders.... :-(
				td.style.border = "1px solid #000";
			else
				td.style.borderTop = td.style.borderLeft = "1px solid #000";
			tr.appendChild(td);
		}
	}

	var p = doc.createElement("br");
	var df = doc.createDocumentFragment();
	df.appendChild(p);
	df.appendChild(table);
	df.appendChild(p.cloneNode(true));

	this._insertNodeAtSelection(df);
	this.selectNodeContents(table.rows[0].cells[0], true);
	return table;
};

DwtHtmlEditor.prototype.applyTableProperties =
function(table, props) {
	var doc = this._getIframeDoc();
	var all_cells_props = [];
	for (var i in props) {
		var val = AjxStringUtil.trim(props[i].toString());
		var is_set = val != "";
		switch (i) {
		    case "caption":
			var caption = table.getElementsByTagName("caption");
			caption = caption.length > 0 ? caption[0] : null;
			if (is_set && !caption) {
				caption = doc.createElement("caption");
				table.insertBefore(caption, table.firstChild);
			}
			if (!is_set && caption)
				caption.parentNode.removeChild(caption);
			if (caption)
				caption.innerHTML = val;
			break;

		    case "summary":
		    case "align":
		    case "cellSpacing":
		    case "cellPadding":
			if (!is_set)
				table.removeAttribute(i, 0);
			else
				table[i] = val;
			break;

		    case "borderWidth":
		    case "borderStyle":
		    case "borderColor":
			// save these attributes to apply them to each cell
			table.style[i] = val;
			all_cells_props.push([ i, val ]);
			break;

		    default :
			// TODO: remove unnecessary style (when is_set is false)
			table.style[i] = val;
			break;
		}
	}
	if (all_cells_props.length > 0) {
		var tds = table.getElementsByTagName("td");
		for (var i = tds.length; --i >= 0;) {
			var td = tds[i];
			for (var j = all_cells_props.length; --j >= 0;)
				td.style[all_cells_props[j][0]] = all_cells_props[j][1];
		}
	}
	if (AjxEnv.isGeckoBased)
		this._forceRedraw();
};

DwtHtmlEditor.prototype.applyCellProperties = function(table, cells, props) {
	var last_row = true;
	for (var row_i = cells.length; --row_i >= 0;) {
		var row = cells[row_i];
		var first_row = (row_i == 0);
		var last_col = true;
		for (var cell_i = row.length; --cell_i >= 0;) {
			var first_col = (cell_i == 0);
			var td = row[cell_i];

			if (props.backgroundColor != null)
				td.style.backgroundColor = props.backgroundColor;

			if (props.color != null)
				td.style.color = props.color;

			if (props.textAlign != null)
				td.style.textAlign = props.textAlign;

			if (props.verticalAlign != null)
				td.style.verticalAlign = props.verticalAlign;

			if (props.width != null) {
				if (props.width)
					td.style.width = props.width + "px";
				else
					td.style.width = "";
			}

			if (props.height != null) {
				if (props.height)
					td.style.height = props.height + "px";
				else
					td.style.height = "";
			}

			if (props.vertPadding != null) {
				if (props.vertPadding)
					td.style.paddingTop = td.style.paddingBottom = props.vertPadding + "px";
				else
					td.style.paddingTop = td.style.paddingBottom = "";
			}

			if (props.horizPadding != null) {
				if (props.horizPadding)
					td.style.paddingLeft = td.style.paddingRight = props.horizPadding + "px";
				else
					td.style.paddingLeft = td.style.paddingRight = "";
			}

			var borders = props.borders, b;

			// horiz. borders

			b = borders[DwtHtmlEditor.BORDER_TOP];
			if (b != null && first_row) {
				td.style.borderTop = b.width + " " + b.style + " " + b.color;
			}

 			b = borders[DwtHtmlEditor.BORDER_MIDDLE];
 			if (b != null) {
				b = b.width + " " + b.style + " " + b.color;
				if (!last_row)
					td.style.borderBottom = b;
				if (!first_row)
					td.style.borderTop = b;
 			}

			b = borders[DwtHtmlEditor.BORDER_BOTTOM];
			if (b != null && last_row) {
				td.style.borderBottom = b.width + " " + b.style + " " + b.color;
			}

			// vertical borders

			b = borders[DwtHtmlEditor.BORDER_LEFT];
			if (b != null && first_col) {
				td.style.borderLeft = b.width + " " + b.style + " " + b.color;
			}

 			b = borders[DwtHtmlEditor.BORDER_CENTER];
 			if (b != null) {
				b = b.width + " " + b.style + " " + b.color;
				if (!last_col)
					td.style.borderRight = b;
				if (!first_col)
					td.style.borderLeft = b;
 			}

			b = borders[DwtHtmlEditor.BORDER_RIGHT];
			if (b != null && last_col) {
				td.style.borderRight = b.width + " " + b.style + " " + b.color;
			}

			last_col = false;
		}
		last_row = false;
	}
	if (AjxEnv.isGeckoBased)
		this._forceRedraw();
};

DwtHtmlEditor.prototype._insertNodeAtSelection =
function(node) {
	this.focus();
	if (!AjxEnv.isIE) {
		var range = this._getRange();
		this._getIframeWin().getSelection().removeAllRanges();
		try {
			range.deleteContents();
			range.insertNode(node);
			range.selectNodeContents(node);
		}
		catch (e) {
			DBG.println("DwtHtmlEditor#_insertNodeAtSelect");
			DBG.println("&nbsp;&nbsp;"+e);
		}
	} else {
		var sel = this._getRange();
		var range = sel.createRange();
		var id = "FOO-" + Dwt.getNextId();
		try {
			range.pasteHTML("<span id='" + id + "'></span>");
		} catch(ex) {
			// bug 12158: IE throws error when the range
			// spans over a table and following text, so
			// let's collapse it.
			range.collapse(false);
			range.pasteHTML("<span id='" + id + "'></span>");
		}
 		var el = this._getIframeDoc().getElementById(id);
 		el.parentNode.insertBefore(node, el);
 		el.parentNode.removeChild(el);
	}
}


/**
* Changes the editor mode.
*
* @param mode	The new mode
* @param convert	If new mode -> HTML and convert, then the content of the widget is escaped. If
*		mode -> Text and convert, then text is stripped out of content
*/
DwtHtmlEditor.prototype.setMode =
function(mode, convert) {
	if (mode == this._mode ||
		(mode != DwtHtmlEditor.HTML && mode != DwtHtmlEditor.TEXT))
	{
		return;
	}

	this._mode = mode;
	if (mode == DwtHtmlEditor.HTML) {
		var textArea = document.getElementById(this._textAreaId);
		var iFrame;
		var idoc = this._getIframeDoc();

		// bug fix #6788 - Safari seems to lose its document so recreate
		if (this._iFrameId != null && idoc) {
			idoc.body.innerHTML = (convert)
				? AjxStringUtil.convertToHtml(textArea.value)
				: textArea.value;
			iFrame = document.getElementById(this._iFrameId);
		} else {
			var content = (convert)
				? AjxStringUtil.convertToHtml(textArea.value)
				: textArea.value;
			content = [ "<html><head></head><body>",content,"</body></html>" ].join("");
			iFrame = this._initHtmlMode(content);
		}
		Dwt.setVisible(textArea, false);
		Dwt.setVisible(iFrame, true);

		// XXX: mozilla hack
		if (AjxEnv.isGeckoBased)
			this._enableDesignMode(this._getIframeDoc());
	} else {
		var textArea = this._textAreaId != null
			? document.getElementById(this._textAreaId)
			: this._initTextMode(true);

		// If we have pending content, then an iFrame is being created. This can happen
		// if the widget is instantiated and immediate setMode is called w/o getting out
		// to the event loop where _finishHtmlMode is triggered
		var content = (!this._pendingContent)
			? this._getIframeDoc().innerHTML
			: (this._pendingContent || "");

		textArea.value = (convert)
			? this._convertHtml2Text()
			: this._getIframeDoc().innerHTML;;

		Dwt.setVisible(document.getElementById(this._iFrameId), false);
		Dwt.setVisible(textArea, true);
	}
}

DwtHtmlEditor.prototype.setTextDirection =
function(direction) {
	if (this._mode != DwtHtmlEditor.HTML)
		return;

	var dir = (direction == DwtHtmlEditor.DIRECTION_R2L) ? "rtl" : "ltr";
	var el = this._getParentElement();

	DBG.println("EL: " + el.nodeName.toLowerCase() + " - " + DwtHtmlEditor._BLOCK_ELEMENTS[el.nodeName.toLowerCase()]);

	while (el && !DwtHtmlEditor._BLOCK_ELEMENTS[el.nodeName.toLowerCase()])
		el = el.parentNode;

	if (el)
		el.style.direction = el.style.direction == dir ? "" : dir;
}

// Font sizes should be 1-7
DwtHtmlEditor.prototype.setFont =
function(family, style, size, color, hiliteColor) {
	if (family)
		this._execCommand(DwtHtmlEditor._FONT_NAME, family);

	if (style)
		this._execCommand(style);

	if (size)
		this._execCommand(DwtHtmlEditor._FONT_SIZE, size);

	if (color)
		this._execCommand(DwtHtmlEditor._FONT_COLOR, color);

	if (hiliteColor)
		this._execCommand((AjxEnv.isIE) ? DwtHtmlEditor._FONT_HILITE_IE : DwtHtmlEditor._FONT_HILITE, hiliteColor);
}

DwtHtmlEditor.prototype.setJustification =
function(justification) {
	this._execCommand(justification);
}

DwtHtmlEditor.prototype.setIndent =
function(indent) {
	this._execCommand(indent);
}

DwtHtmlEditor.prototype.setStyle =
function(style) {
	this._execCommand(DwtHtmlEditor._FORMAT_BLOCK, DwtHtmlEditor._STYLES[style]);
}

DwtHtmlEditor.prototype.setSize =
function(width, height) {
	DwtComposite.prototype.setSize.call(this, width, height);
	var htmlEl = this.getHtmlElement();

	if (this._iFrameId != null) {
		var iFrame = document.getElementById(this._iFrameId);
		iFrame.width = htmlEl.style.width;
		iFrame.height = htmlEl.style.height;
	} else {
		var textArea = document.getElementById(this._textAreaId);
		textArea.style.width = htmlEl.style.width;
		textArea.style.height = htmlEl.style.height;
	}
}

DwtHtmlEditor.prototype.getIframe =
function() {
	return document.getElementById(this._iFrameId);
}

DwtHtmlEditor.prototype.getInputElement =
function() {
	var id = (this._mode == DwtHtmlEditor.HTML) ? this._iFrameId : this._textAreaId;
	return document.getElementById(id)
}

DwtHtmlEditor.prototype._initialize =
function() {
	if (this._mode == DwtHtmlEditor.HTML)
		this._initHtmlMode(this._pendingContent);
	else
		this._initTextMode();
}

DwtHtmlEditor.prototype._initTextMode =
function(ignorePendingContent) {
	var htmlEl = this.getHtmlElement();
	this._textAreaId = "textarea_" + Dwt.getNextId();
	var textArea = document.createElement("textarea");
	textArea.className = "DwtHtmlEditorTextArea";
	textArea.id = this._textAreaId;
	htmlEl.appendChild(textArea);

	// We will ignore pending content if we are called from setMode. See setMode for
	// documentation
	if (!ignorePendingContent) {
		textArea.value = this._pendingContent;
		this._pendingContent = null;
	}
	return textArea;
}

DwtHtmlEditor.prototype._initHtmlMode =
function(content) {
	var iFrame = this._createIFrameEl();

	this._keyEvent = new DwtKeyEvent();
	this._stateEvent = new DwtHtmlEditorStateEvent();
	this._stateEvent.dwtObj = this;

	this._updateStateAction = new AjxTimedAction(this, this._updateState);

	this._pendingContent = content || "";

	// IE can sometimes race ahead and execute script before the underlying component is created
	var timedAction = new AjxTimedAction(this, this._finishHtmlModeInit);
	AjxTimedAction.scheduleAction(timedAction, DwtHtmlEditor._INITDELAY);

	return iFrame;
}

DwtHtmlEditor.prototype._createIFrameEl =
function() {
	var htmlEl = this.getHtmlElement();
	this._iFrameId = "iframe_" + Dwt.getNextId();
	var iFrame = document.createElement("iframe");
	iFrame.id = this._iFrameId;
	iFrame.className = "DwtHtmlEditorIFrame";
	iFrame.setAttribute("border", "0", false);
	iFrame.setAttribute("frameborder", "0", false);
	iFrame.setAttribute("vspace", "0", false);
	iFrame.setAttribute("autocomplete", "off", false);
// 	iFrame.setAttribute("marginwidth", "0", false);
// 	iFrame.setAttribute("marginheight", "0", false);
	if (AjxEnv.isIE && location.protocol == "https:")
		iFrame.src = this._blankIframeSrc || "";
	htmlEl.appendChild(iFrame);

	return iFrame;
}

DwtHtmlEditor.prototype._initializeContent =
function(content) {
	var initHtml = "<html><head></head><body>" + (content || "") + "</body></html>";
	var doc = this._getIframeDoc();
	try {
		doc.write(initHtml);
	} finally {
		doc.close();
	}
};

DwtHtmlEditor.prototype._finishHtmlModeInit =
function(params) {
	var doc = this._getIframeDoc();
	try {
		// XXX: DO NOT REMOVE THIS LINE EVER. IT CAUSES NASTY BUGS (8808, 8895)
		doc.body.innerHTML = this._pendingContent || "";
	} catch (ex) {
		DBG.println("XXX: Error initializing HTML mode :XXX");
		return;
	}

	this._enableDesignMode(doc);
//	this.focus();
	this._updateState();
	this._htmlModeInited = true;

	// bug fix #4722 - setting design mode for the first time seems to null
	// out iframe doc's body in IE - so create a new body...
	if (AjxEnv.isIE) {
		doc.open();
		doc.write(this._pendingContent || "");
		doc.close();
		// doc.body.innerHTML = this._pendingContent || "";
		// these 2 seem to be no-ops.
		// this._execCommand("2D-Position", false);
		// this._execCommand("MultipleSelection", true);
	}

	this._registerEditorEventHandlers(document.getElementById(this._iFrameId), doc);
}

DwtHtmlEditor.prototype._focus =
function() {
	this.focus();
};

DwtHtmlEditor.prototype._getIframeDoc =
function() {
	return this._iFrameId ? Dwt.getIframeDoc(document.getElementById(this._iFrameId)) : null;
}

DwtHtmlEditor.prototype._getIframeWin =
function() {
	return Dwt.getIframeWindow(document.getElementById(this._iFrameId));
}

DwtHtmlEditor.prototype._getParentElement =
function() {
	if (AjxEnv.isIE) {
		var iFrameDoc = this._getIframeDoc();
		var selection = iFrameDoc.selection;
		var range = selection.createRange();
		if (selection.type == "None" || selection.type == "Text")
			// If selection is None still have a parent element
			return selection.createRange().parentElement();
		else if (selection.type == "Control")
			return selection.createRange().item(0);
		else
			return iFrameDoc.body;
	} else {
		try {
			var range = this._getRange();
			var p = range.commonAncestorContainer;
			if (!range.collapsed && range.startContainer == range.endContainer
				&& range.startOffset - range.endOffset <= 1 && range.startContainer.hasChildNodes())
				p = range.startContainer.childNodes[range.startOffset];
			while (p.nodeType == 3)
				p = p.parentNode;
			return p;
		} catch (e) {
			return null;
		}
	}
}

DwtHtmlEditor.prototype.getNearestElement =
function(tagName) {
	try {
		var p = this._getParentElement();
		tagName = tagName.toLowerCase();
		while (p && p.nodeName.toLowerCase() != tagName)
			p = p.parentNode;
		return p;
	} catch(ex) {
		return null;
	}
};

DwtHtmlEditor.prototype.selectNodeContents =
function(node, pos, inclusive) {
	var range;
	var collapsed = (typeof pos == "boolean");
	if (AjxEnv.isIE) {
		range = this._getIframeDoc().body.createTextRange();
		range.moveToElementText(node);
		(collapsed) && range.collapse(pos);
		range.select();
	} else {
		var sel = this._getSelection();
		range = this._getIframeDoc().createRange();
		if (inclusive)
			range.selectNode(node);
		else
			range.selectNodeContents(node);
		(collapsed) && range.collapse(pos);
		sel.removeAllRanges();
		sel.addRange(range);
	}
};

DwtHtmlEditor.prototype._forceRedraw = function() {
// this doesn't work :(
// 	var save_collapse = table.style.borderCollapse;
// 	table.style.borderCollapse = "collapse";
// 	table.style.borderCollapse = "separate";
// 	table.style.borderCollapse = save_collapse;

// this works but wrecks the caret position
//	var body = this._getIframeDoc().body;
//	body.innerHTML = body.innerHTML;

	var body = this._getIframeDoc().body;
	body.style.display = "none";
	var self = this;
	setTimeout(function() {
		body.style.display = "";
		self.focus();
		self = null;
	}, 10);
};

DwtHtmlEditor.prototype.getSelectedCells = function() {
	var cells = null;
	var sel = this._getSelection();
	var range, i = 0;
	var rows = [];
	var row = null;
	if (!AjxEnv.isIE) {
		try {
			while (range = sel.getRangeAt(i++)) {
				var td = range.startContainer.childNodes[range.startOffset];
				if (td.parentNode != row) {
					row = td.parentNode;
					cells && rows.push(cells);
					cells = [];
				}
				if (td.tagName && /^td$/i.test(td.tagName))
					cells.push(td);
			}
		} catch(ex) {}
		rows.push(cells);
	} else {
		// it's kind of creepy to do this in IE
		range = sel.createRange();
		var orig_range = range.duplicate(); // save selection
		range.collapse(true); // move to start
		while (orig_range.compareEndPoints("EndToStart", range) >= 0) {
			// search for a TD
			var td = range.parentElement();
			while (td && td.nodeName.toLowerCase() != "td")
				td = td.parentNode;
			if (td) {
				if (td.parentNode != row) {
					row = td.parentNode;
					cells && rows.push(cells);
					cells = [];
				}
				cells.push(td);
				range.moveToElementText(td); // select full td
				range.collapse(false);	     // move to end
			}
			if (range.move("character", 1) == 0) {
				// EOF
				break;
			}
		}
		orig_range.select();
		rows.push(cells);
	}
	if (rows.length == 0 || !rows[0] || rows[0].length == 0) {
		cells = this.getNearestElement("td");
		if (cells)
			rows = [[cells]];
	}
	return rows;
};

DwtHtmlEditor.prototype._splitCells = function(td) {
	var table = td;
	while (table && table.nodeName.toLowerCase() != "table")
		table = table.parentNode;
	var mozbr = AjxEnv.isGeckoBased ? "<br />" : "";
	function splitRow(td) {
		var n = td.rowSpan;
		var nc = td.colSpan;
		td.rowSpan = 1;
		tr = td.parentNode;
		var itr = tr.rowIndex;
		var trs = tr.parentNode.rows;
		var index = td.cellIndex;
		while (--n > 0) {
			tr = trs[++itr];
			var otd = td.cloneNode(false);
			otd.removeAttribute("rowspan");
			otd.colSpan = td.colSpan;
			otd.innerHTML = mozbr;
			tr.insertBefore(otd, tr.cells[index]);
		}
	};

	function splitCol(td) {
		var nc = td.colSpan;
		td.colSpan = 1;
		tr = td.parentNode;
		var ref = td.nextSibling;
		while (--nc > 0) {
			var otd = td.cloneNode(false);
			otd.removeAttribute("colspan");
			otd.rowSpan = td.rowSpan;
			otd.innerHTML = mozbr;
			tr.insertBefore(otd, ref);
		}
	};

	function splitCell(td) {
		var nc = td.colSpan;
		splitCol(td);
		var items = td.parentNode.cells;
		var index = td.cellIndex;
		while (nc-- > 0) {
			splitRow(items[index++]);
		}
	};
	splitCell(td);
};

DwtHtmlEditor.prototype.doTableOperation =
function(cmd, params) {
	var table = params.table || this.getNearestElement("table");
	var td = params.td || this.getNearestElement("td");
	var cellIndex, rowIndex, tr;
	if (td) {
		cellIndex = td.cellIndex;
		tr = td.parentNode;
		rowIndex = tr.rowIndex;
	} else {
		// Bug 8974: as usual, IE is weird to say the least.
		cellIndex = 0;
		tr = this.getNearestElement("tr");
		if (tr)
			rowIndex = tr.rowIndex;
	}
	var cells = params.cells;
	while (true) {
		switch (cmd) {
		    case "insertRowAbove":
			DwtHtmlEditor.table_fixCells(
				DwtHtmlEditor.table_insertRow(td));
			break;

		    case "insertRowUnder":
			DwtHtmlEditor.table_fixCells(
				DwtHtmlEditor.table_insertRow(td, true));
			break;

		    case "insertColumnBefore":
			DwtHtmlEditor.table_fixCells(
				DwtHtmlEditor.table_insertCol(td));
			break;

		    case "insertColumnAfter":
			DwtHtmlEditor.table_fixCells(
				DwtHtmlEditor.table_insertCol(td, true));
			break;

		    case "deleteRow":
			for (var i = 0; i < cells.length; ++i) {
				var td = DwtHtmlEditor.table_deleteRow(cells[i][0]);
				if (td) {
					this.selectNodeContents(td, true);
					this.focus();
				}
			}
			break;

		    case "deleteColumn":
			for (var i = 0; i < cells[0].length; ++i) {
				var td = DwtHtmlEditor.table_deleteCol(cells[0][i]);
				if (td) {
					this.selectNodeContents(td, true);
					this.focus();
				}
			}
			break;

		    case "mergeCells":
			td = cells[0][0];
			var html = [ td.innerHTML.replace(/<br>$/i, "") ];
			for (var i = 0; i < cells.length; ++i) {
				var row = cells[i];
				for (var j = 0; j < row.length; ++j) {
					if (i || j) {
						html.push(row[j].innerHTML.replace(/<br>$/i, ""));
						row[j].parentNode.removeChild(row[j]);
					}
				}
			}
			td.colSpan = cells[0].length;
			td.rowSpan = cells.length;
			html = html.join(" ");
			if (AjxEnv.isGeckoBased)
				html += "<br/>";
			td.innerHTML = html;
			this.selectNodeContents(td, true);
			break;

		    case "splitCells":
			this._splitCells(td);
			break;

		    case "deleteTable":
			if (!AjxEnv.isIE) {
				// this is better for Gecko because it places
				// the caret where it has to be.
				this.selectNodeContents(table, null, true);
				this.deleteSelectedNodes();
			} else
				table.parentNode.removeChild(table);
			break;
		}
		break;
	}
	if (AjxEnv.isGeckoBased)
		this._forceRedraw();
	this._updateState();
};

DwtHtmlEditor.prototype._getRange =
function() {
	var iFrameDoc = this._getIframeDoc();
	if (AjxEnv.isIE) {
		return iFrameDoc.selection;
	} else {
		this.focus();
		var selection = this._getIframeWin().getSelection();
		if (selection != null) {
			try {
				return selection.getRangeAt(0);
			} catch(e) {
				return iFrameDoc.createRange();
			}
		} else {
			return iFrameDoc.createRange();
		}
	}
};

DwtHtmlEditor.prototype.deleteSelectedNodes =
function() {
	var sel = this._getSelection();
	if (AjxEnv.isGeckoBased)
		sel.deleteFromDocument();
	else
		sel.clear();
};

DwtHtmlEditor.prototype._getSelection =
function() {
	if (AjxEnv.isIE) {
		return this._getIframeDoc().selection;
	} else {
		return this._getIframeWin().getSelection();
	}
};

// This is transformed into a "simple closure" in the constructor.  Simply
// dispatch the call to _handleEditorEvent passing the right event depending on
// the browser.
DwtHtmlEditor.prototype.__eventClosure =
function(ev) {
	return this._handleEditorEvent(AjxEnv.isIE ? this._getIframeWin().event : ev);
};

DwtHtmlEditor.prototype._registerEditorEventHandlers =
function(iFrame, iFrameDoc) {
	var events = ["mouseup", "keydown", "keypress", "drag", "mousedown", "contextmenu"];
	// Note that since we're not creating the closure here anymore, it's
	// safe to call this function any number of times (we do this for
	// Gecko/Linux to work around bugs).  The browser won't add the same
	// event if it already exists (DOM2 requirement)
	for (var i = 0; i < events.length; ++i) {
		if (AjxEnv.isIE) {
			iFrameDoc.attachEvent("on" + events[i], this.__eventClosure);
		} else {
			iFrameDoc.addEventListener(events[i], this.__eventClosure, true);
		}
	}
};

DwtHtmlEditor.prototype._handleEditorEvent =
function(ev) {
	var retVal = true;

	// If we have a mousedown event, then let DwtMenu know. This is a nasty hack that we have to do since
	// the iFrame is in a different document etc
	if (ev.type == "mousedown") {
		DwtMenu._outsideMouseDownListener(ev);
	}

	if (ev.type == "contextmenu") {
		// context menu event; we want to translate the event
		// coordinates from iframe to parent document coords,
		// before notifying listeners.
		var mouseEv = DwtShell.mouseEvent;
		mouseEv.setFromDhtmlEvent(ev);
		var pos = Dwt.getLocation(document.getElementById(this._iFrameId));
		if (!AjxEnv.isIE) {
			var doc = this._getIframeDoc();
			var sl = doc.documentElement.scrollLeft || doc.body.scrollLeft;
			var st = doc.documentElement.scrollTop || doc.body.scrollTop;
			pos.x -= sl;
			pos.y -= st;
		}
		mouseEv.docX += pos.x;
		mouseEv.docY += pos.y;
		DwtControl.__mouseEvent(ev, DwtEvent.ONCONTEXTMENU, this, mouseEv);
		retVal = mouseEv._returnValue;
	}

	var cmd = null;
	if (DwtKeyEvent.isKeyPressEvent(ev)) {
		var ke = this._keyEvent;
		ke.setFromDhtmlEvent(ev);
		if (ke.ctrlKey) {
			var key = String.fromCharCode(ke.charCode).toLowerCase();
			var value = null;

			switch (key) {
			    case '1':
			    case '2':
			    case '3':
			    case '4':
			    case '5':
			    case '6':
					cmd = DwtHtmlEditor._FORMAT_BLOCK;
					value = DwtHtmlEditor._KEY2CMDS[key];
					break;

				case '0':
					try {
						this.setMode((this._mode == DwtHtmlEditor.HTML) ? DwtHtmlEditor.TEXT : DwtHtmlEditor.HTML, true);
					} catch (e) {
						DBG.println(AjxDebug.DBG1, "EXCEPTION!: " + e);
					}
					ke._stopPropagation = true;
					ke._returnValue = false;
					ke.setToDhtmlEvent(ev);
					retVal = false;
					break;

				default:
					// IE Has full on keyboard shortcuts
					//if (!AjxEnv.isIE)
						cmd = DwtHtmlEditor._KEY2CMDS[key];
					break;
			}
		}
	}
	if (cmd) {
		DBG.println(AjxDebug.DBG1, "CMD: " + cmd);
		this._execCommand(cmd, value);
		DBG.println(AjxDebug.DBG1, "AFTER EXEC");
		ke._stopPropagation = true;
		ke._returnValue = false;
		ke.setToDhtmlEvent(ev);
		retVal = false;
	} else if (ev.type == "keydown") {
		// pass to keyboard mgr for kb nav
		retVal = DwtKeyboardMgr.__keyDownHdlr(ev);
	}

	// TODO notification for any updates etc
	// Set's up the a range for the current ins point or selection. This is IE only because the iFrame can
	// easily lose focus (e.g. by clicking on a button in the toolbar) and we need to be able to get back
	// to the correct insertion point/selection.
	if (AjxEnv.isIE) {
		var iFrameDoc = this._getIframeDoc();
		this._currInsPt = iFrameDoc.selection.createRange();
		// If just at the insertion point, then collapse so that we don't get
		// a range selection on a call to DwtHtmlEditor.focus()
		if (iFrameDoc.selection.type == "None") {
			this._currInsPt.collapse(false);
		}
	}

	if (this._stateUpdateActionId != null)
		AjxTimedAction.cancelAction(this._stateUpdateActionId);

	this._stateUpdateActionId = AjxTimedAction.scheduleAction(this._updateStateAction, 100);

	return retVal;
}

DwtHtmlEditor.prototype._updateState =
function() {
	if (this._mode != DwtHtmlEditor.HTML)
		return;

	this._stateUpdateActionId = null;
	var ev = this._stateEvent;
	ev.reset();

	var iFrameDoc = this._getIframeDoc();
	try {
		ev.isBold = iFrameDoc.queryCommandState(DwtHtmlEditor.BOLD_STYLE);
		ev.isItalic = iFrameDoc.queryCommandState(DwtHtmlEditor.ITALIC_STYLE);
		ev.isUnderline = iFrameDoc.queryCommandState(DwtHtmlEditor.UNDERLINE_STYLE);
		ev.isStrikeThru = iFrameDoc.queryCommandState(DwtHtmlEditor.STRIKETHRU_STYLE);
		ev.isSuperscript = iFrameDoc.queryCommandState(DwtHtmlEditor.SUPERSCRIPT_STYLE);
		ev.isSubscript = iFrameDoc.queryCommandState(DwtHtmlEditor.SUBSCRIPT_STYLE);
		ev.isOrderedList = iFrameDoc.queryCommandState(DwtHtmlEditor.ORDERED_LIST);
		ev.isUnorderedList = iFrameDoc.queryCommandState(DwtHtmlEditor.UNORDERED_LIST);

		// Don't futz with the order of the if statements below. They are important due to the
		// nature of the RegExs
		var family = iFrameDoc.queryCommandValue(DwtHtmlEditor._FONT_NAME);
		if (family) {
			family = family.toLowerCase();
			if (family.search(DwtHtmlEditor._VERDANA_RE) != -1)
				ev.fontFamily = 3;
			else if (family.search(DwtHtmlEditor._ARIAL_RE) != -1)
				ev.fontFamily = 0;
			else if (family.search(DwtHtmlEditor._TIMES_RE) != -1)
				ev.fontFamily = 1;
			else if (family.search(DwtHtmlEditor._COURIER_RE) != -1)
				ev.fontFamily = 2;
			else
				ev.fontFamily = null;
		} else {
			ev.fontFamily = null;
		}

		ev.fontSize = iFrameDoc.queryCommandValue(DwtHtmlEditor._FONT_SIZE);
		ev.backgroundColor = iFrameDoc.queryCommandValue((AjxEnv.isIE) ? "backcolor" : "hilitecolor");
		ev.color = iFrameDoc.queryCommandValue("forecolor");
		if (AjxEnv.isIE) {
			// For some reason, IE returns a number, so we transform it into
			// a color specifier that we can actually use
			ev.backgroundColor = "#" + DwtButtonColorPicker.toHex(ev.backgroundColor, 6).replace(/(..)(..)(..)/, "$3$2$1");
			ev.color = "#" + DwtButtonColorPicker.toHex(ev.color, 6).replace(/(..)(..)(..)/, "$3$2$1");
		}
		ev.justification = null;
		ev.direction = null;

		var style = iFrameDoc.queryCommandValue(DwtHtmlEditor._FORMAT_BLOCK);
		if (style) {
			if (style.search(DwtHtmlEditor._H1_RE) != -1)
				ev.style = DwtHtmlEditor.H1;
			else if (style.search(DwtHtmlEditor._H2_RE) != -1)
				ev.style = DwtHtmlEditor.H2;
			else if (style.search(DwtHtmlEditor._H3_RE) != -1)
				ev.style = DwtHtmlEditor.H3;
			else if (style.search(DwtHtmlEditor._H4_RE) != -1)
				ev.style = DwtHtmlEditor.H4;
			else if (style.search(DwtHtmlEditor._H5_RE) != -1)
				ev.style = DwtHtmlEditor.H5;
			else if (style.search(DwtHtmlEditor._H6_RE) != -1)
				ev.style = DwtHtmlEditor.H6;
			else if (style.search(DwtHtmlEditor._PARAGRAPH_RE) != -1)
				ev.style = DwtHtmlEditor.PARAGRAPH;
			else if (style.search(DwtHtmlEditor._ADDRESS_RE) != -1)
				ev.style = DwtHtmlEditor.ADDRESS;
			else if (style.search(DwtHtmlEditor._PREFORMATTED_RE) != -1)
				ev.style = DwtHtmlEditor.PREFORMATTED;
		}

		if (iFrameDoc.queryCommandState(DwtHtmlEditor.JUSTIFY_LEFT))
			ev.justification = DwtHtmlEditor.JUSTIFY_LEFT;
		else if (iFrameDoc.queryCommandState(DwtHtmlEditor.JUSTIFY_CENTER))
			ev.justification = DwtHtmlEditor.JUSTIFY_CENTER;
		else if (iFrameDoc.queryCommandState(DwtHtmlEditor.JUSTIFY_RIGHT))
			ev.justification = DwtHtmlEditor.JUSTIFY_RIGHT;
		else if (iFrameDoc.queryCommandState(DwtHtmlEditor.JUSTIFY_FULL))
			ev.justification = DwtHtmlEditor.JUSTIFY_FULL;


		// Notify any listeners
		if (this.isListenerRegistered(DwtEvent.STATE_CHANGE))
			this.notifyListeners(DwtEvent.STATE_CHANGE, ev);
	} catch (ex) {
		if (AjxEnv.isGeckoBased) {
			this._enableDesignMode(iFrameDoc);
		}
	}
}

DwtHtmlEditor.prototype._enableDesignMode =
function(iFrameDoc) {
	if (!iFrameDoc) return;

	try {
		iFrameDoc.designMode = "on";
		// Probably a regression of FF 1.5.0.1/Linux requires us to
		// reset event handlers here (Zimbra bug: 6545).
 		if (AjxEnv.isGeckoBased && (AjxEnv.isLinux || AjxEnv.isMac))
 			this._registerEditorEventHandlers(document.getElementById(this._iFrameId), iFrameDoc);
	} catch (ex) {
		//Gecko may take some time to enable design mode..
		if (AjxEnv.isGeckoBased) {
			var ta = new AjxTimedAction(this, this._enableDesignMode, [iFrameDoc]);
			AjxTimedAction.scheduleAction(ta, 10);
			return true;
		} else {
			// TODO Should perhaps throw an exception?
			return false;
		}
	}
}

DwtHtmlEditor.prototype._onContentInitialized =
function() {};

DwtHtmlEditor.prototype._setContentOnTimer =
function() {
	var iframeDoc = this._getIframeDoc();
	try {
		iframeDoc.body.innerHTML = this._pendingContent;
		// XXX: mozilla hack
		if (AjxEnv.isGeckoBased)
			this._enableDesignMode(iframeDoc);
		this._onContentInitialized();
	} catch (ex) {
		var ta = new AjxTimedAction(this, this._setContentOnTimer);
		AjxTimedAction.scheduleAction(ta, 10);
		return true;
	}
}

DwtHtmlEditor.prototype._execCommand =
function(command, option) {
	if (this._mode != DwtHtmlEditor.HTML)
		return;

	try {
		this.focus();
		this._getIframeDoc().execCommand(command, false, option);
	} catch (e) {
		// perhaps retry the command?
		this._enableDesignMode(this._getIframeDoc());
	}
	this._updateState();
}

DwtHtmlEditor.prototype._convertHtml2Text =
function() {
	var iFrameDoc = this._getIframeDoc();
	return iFrameDoc && iFrameDoc.body ?
		AjxStringUtil.convertHtml2Text(iFrameDoc.body) : "";
}

/** Table Helper Functions **/
// XXX: Hairy code!  It's advisable not to mess with it. ;-)

DwtHtmlEditor.table_analyzeCells = function(currentCell) {
	var table = currentCell.parentNode.parentNode;
	while (table && !/table/i.test(table.tagName))
		table = table.parentNode;
	var spans = {};
	var rows = table.rows;
	for (var i = 0; i < rows.length; ++i) {
		var cells = rows[i].cells;
		var index = 0;
		for (var j = 0; j < cells.length;) {
			var td = cells[j];
			var cs = td.colSpan || 1;
			var rs = (td.rowSpan || 1) - 1;
			var tmp = spans[index];
			if (tmp) {
				if (--tmp.rs == 0)
					spans[index] = null;
				index += tmp.cs;
			}
			td.ZmIndex = index;
			if (++j < cells.length) {
				if (rs)
					spans[index] = { cs: cs, rs: rs };
				index += cs;
			}
		}
	}
	return table;
};

DwtHtmlEditor.table_getCellAt = function(tr, index) {
	var cells = tr.cells;
	var last = null, next = null;
	for (var i = 0; i < cells.length; ++i) {
		var td = cells[i];
		var cs = (td.colSpan || 1) - 1;
		if (td.ZmIndex <= index
		    && td.ZmIndex + cs >= index) {
			var rs = (td.rowSpan || 1) - 1;
			return { td: td, cs: cs, rs: rs };
		} else if (td.ZmIndex < index) {
			last = td;
		} else if (td.ZmIndex + cs > index && !next) {
			next = td;
		}
	}
	return { last: last, next: next };
};

DwtHtmlEditor.table_getPrevCellAt = function(tr, index) {
	var table = tr.parentNode;
	while (table && !/table/i.test(table.tagName))
		table = table.parentNode;
	var rows = table.rows;
	for (var i = tr.rowIndex; --i >= 0;) {
		var info = DwtHtmlEditor.table_getCellAt(rows[i], index);
		if (info.td) {
			info.dist = tr.rowIndex - i;
			return info;
		}
	}
	return null;
};

DwtHtmlEditor.table_fixCells = function(cells) {
	for (var i = 0; i < cells.length; ++i) {
		var td = cells[i];
		if (AjxEnv.isIE) {
			td.innerHTML = "&nbsp;";
		} else if (AjxEnv.isGeckoBased) {
			td.innerHTML = "<br/>";
		}
		// who knows what other browsers would like..
	}
};

DwtHtmlEditor.table_insertCol = function(td, after) {
	var table = DwtHtmlEditor.table_analyzeCells(td);
	var rows = table.rows;
	var index = td.ZmIndex;
 	if (after)
 		index += td.colSpan - 1;
	var newcells = [];
	var rs = 0;
	for (var i = 0; i < rows.length; ++i) {
		var tr = rows[i];
		var info = DwtHtmlEditor.table_getCellAt(tr, index);
		var cc = info.td;
		var newcell = null;
		if (cc) {
			if (cc.ZmIndex == index && !after) {
				newcell = tr.insertCell(cc.cellIndex);
			} else if (cc.ZmIndex + info.cs == index && after) {
				newcell = tr.insertCell(cc.cellIndex + 1);
			}
			if (newcell) {
				rs = info.rs;
			} else {
				cc.colSpan = info.cs + 2;
			}
		} else if (rs > 0) {
			// FIXME: no notion of "after" in this case?  I suppose so...
			if (info.last) {
				newcell = tr.insertCell(info.last.cellIndex + 1);
			} else if (info.next) {
				newcell = tr.insertCell(info.next.cellIndex);
			}
			--rs;
		}
		if (newcell)
			newcells.push(newcell);
	}
	return newcells;
};

DwtHtmlEditor.table_insertRow = function(td, after) {
	var tr = td.parentNode;
	var table = DwtHtmlEditor.table_analyzeCells(td);
	var index = tr.rowIndex;
	if (after) {
		index += td.rowSpan;
		tr = table.rows[index - 1];
	}
	td = table.rows[0].cells[table.rows[0].cells.length-1];
	var max = td.ZmIndex + td.colSpan;
	var newrow = table.insertRow(index);
	var newcells = [];
	var cells = tr.cells;
	var newcell;
	for (var i = 0; i < max; ++i) {
		var info = DwtHtmlEditor.table_getCellAt(tr, i);
		if (info.td) {
			if (!after || !info.rs) {
				for (var j = 0; j <= info.cs; ++j)
					newcells.push(newrow.insertCell(-1));
			} else if (info.rs) {
				info.td.rowSpan = info.rs + 2;
			}
		} else {
			info = DwtHtmlEditor.table_getPrevCellAt(tr, i);
			if (after && info.rs == info.dist) {
				for (var j = 0; j <= info.cs; ++j)
					newcells.push(newrow.insertCell(-1));
			} else {
				info.td.rowSpan = info.rs + 2;
			}
		}
		i += info.cs;
	}
	return newcells;
};

DwtHtmlEditor.table_deleteCol = function(td) {
	var table = DwtHtmlEditor.table_analyzeCells(td);
	var rows = table.rows;
	var index = td.ZmIndex;
	var tr = td.parentNode;
	var nextcell;
	try {
		nextcell = tr.cells[td.cellIndex + 1];
		if (!nextcell)
			nextcell = tr.cells[td.cellIndex - 1];
	} catch(ex) {
		nextcell = null;
	}
	for (var i = 0; i < rows.length; ++i) {
		var tr = rows[i];
		var info = DwtHtmlEditor.table_getCellAt(tr, index);
		if (info.td) {
			if (info.cs)
				info.td.colSpan = info.cs;
			else {
				tr.removeChild(info.td);
				if (tr.cells.length == 0)
					tr.parentNode.removeChild(tr);
			}
			i += info.rs;
		}
	}
	if (table.rows.length == 0)
		table.parentNode.removeChild(table);
	return nextcell;
};

DwtHtmlEditor.table_deleteRow = function(td) {
	var tr = td.parentNode;
	var table = DwtHtmlEditor.table_analyzeCells(td);
	td = table.rows[0].cells[table.rows[0].cells.length-1];
	var max = td.ZmIndex + td.colSpan;
	var nextrow;
	for (var i = max; --i >= 0;) {
		var info = DwtHtmlEditor.table_getCellAt(tr, i);
		if (info.td) {
			if (info.rs) {
				nextrow = table.rows[tr.rowIndex+1];
				var tmp = DwtHtmlEditor.table_getCellAt(nextrow, i);
				td = null;
				if (tmp.last) {
					td = nextrow.insertCell(tmp.last.cellIndex + 1);
				} else if (tmp.next) {
					td = nextrow.insertCell(tmp.next.cellIndex);
				}
				if (td) {
					// we should absolutely always get here, but we check anyway...
					if (info.cs > 0)
						td.colSpan = info.cs + 1;
					if (info.rs > 1)
						td.rowSpan = info.rs;
				}
			}
			tr.removeChild(info.td);
		} else {
			info = DwtHtmlEditor.table_getPrevCellAt(tr, i);
			if (info) {
				if (info.rs) {
					info.td.rowSpan = info.rs;
					i -= info.cs;
				}
			}
		}
	}
	try {
		nextrow = table.rows[tr.rowIndex + 1];
		if (!nextrow)
			nextrow = table.rows[tr.rowIndex - 1];
	} catch(ex) {
		nextrow = null;
	}
	tr.parentNode.removeChild(tr);
	if (table.rows.length == 0)
		table.parentNode.removeChild(table);
	if (nextrow)
		return nextrow.cells[0];
};
