/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.taglib.tag.i18n;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class ParamTag extends BodyTagSupport {

	//
	// Data
	//

	protected Object value;

	//
	// Public methods
	//

	public void setValue(Object value) {
		this.value = value;
	}

	//
	// TagSupport methods
	//

	public int doEndTag() throws JspException {
		MessageTag messageTag = (MessageTag)findAncestorWithClass(this, MessageTag.class);
		if (messageTag != null) {
			Object value = this.value;
			if (value == null) {
				BodyContent bodyContent = getBodyContent();
				value = I18nUtil.evaluate(pageContext, bodyContent.getString(), Object.class);
			}
			messageTag.addParam(value);
		}
		// clear state
		this.value= null;
		// process page
		return EVAL_PAGE;
	}

} // class ParamTag