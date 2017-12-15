<!--
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
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
-->
<%@ page import="java.text.*,java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="com.zimbra.i18n" %>
<%
	String type = request.getParameter("type");
	Locale locale = (Locale)pageContext.getAttribute("locale", PageContext.REQUEST_SCOPE);

	String pattern = null;
	if ("number".equals(type)) {
		pattern = ((DecimalFormat)NumberFormat.getNumberInstance(locale)).toPattern();
	}
	else if ("currency".equals(type)) {
		pattern = ((DecimalFormat)NumberFormat.getCurrencyInstance(locale)).toPattern();
	}
	else if ("percent".equals(type)) {
		pattern = ((DecimalFormat)NumberFormat.getPercentInstance(locale)).toPattern();
	}

	pageContext.setAttribute("pattern", pattern);
%>

<tr><td>${param.type}</td>
	<td>${pattern}</td>
	<td><fmt:formatNumber var='value' value='${requestScope.number}' pattern="${pattern}" />
		${value}
	</td>
	<td><% try { %>
			<fmt:parseNumber var='value' value="${value}" pattern='${pattern}' />
			<fmt:formatNumber value="${value}" pattern="#,##0.####" />
		<% } catch (Exception e) { %>
			<%=e.getMessage()%>
		<% } %>
	</td>
</tr>
