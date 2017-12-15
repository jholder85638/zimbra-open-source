<%--
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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
--%>
<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="com.zimbra.i18n" %>
<%@ taglib prefix="zdf" uri="com.zimbra.cs.offline.jsp" %>
<%@ attribute name="userAgent" required="true" %>

<table class="ZPanelBottom" align="center" cellpadding="4" cellspacing="0">
	<tr>
		<td class="ZTip"><a href="http://www.zimbra.com/products/desktop2.html" target="_blank"><fmt:message key='TipsHome'/></a></td>
		<td class="ZDot">&#8226;</td>
		<td class="ZTip"><a href="http://www.zimbra.com/desktop7/help/en_US/Zimbra_Mail_Help.htm" target="_blank"><fmt:message key='TipsHelp'/></a></td>
		<td class="ZDot">&#8226;</td>
		<td class="ZTip"><a href="http://wiki.zimbra.com/index.php?title=Zimbra_Desktop_7" target="_blank"><fmt:message key='TipsNotes'/></a></td>
		<td class="ZDot">&#8226;</td>
		<td class="ZTip"><a href="http://wiki.zimbra.com/index.php?title=Zimbra_Desktop_7_FAQ" target="_blank"><fmt:message key='TipsFaq'/></a></td>
		<td class="ZDot">&#8226;</td>
		<td class="ZTip"><a href="http://www.zimbra.com/forums/zimbra-desktop/" target="_blank"><fmt:message key='TipsForums'/></a></td>
		<c:if test="${zdf:isPrism(userAgent)}">
			<td class="ZDot">&#8226;</td>
			<td class="ZTip"><a href="javascript:window.platform.openURI('${zdf:addAuthToken(zdf:getBaseUri(), pageContext.request)}');"><fmt:message key='TipsOpenInBrowser'/></a></td>
		</c:if>
	</tr>
</table>
