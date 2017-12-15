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
package com.zimbra.cs.filter;

import java.util.Iterator;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;

class OfflineRuleRewriter extends RuleRewriter {

	/**
	 * We override this to avoid folder/tag creation during filter creation time
	 * @param sb
	 * @param actionName
	 * @param element
	 * @param ruleName
	 * @throws ServiceException
	 */
	@Override
	void action(StringBuffer sb, String actionName, Element element, String ruleName) throws ServiceException {
        for (Iterator<Element> it = element.elementIterator("arg"); it.hasNext(); ) {
            Element subnode = it.next();
            String argVal = subnode.getText();
            sb.append(" \"").append(argVal).append("\"");
        }
        sb.append(";\n");
	}
}
