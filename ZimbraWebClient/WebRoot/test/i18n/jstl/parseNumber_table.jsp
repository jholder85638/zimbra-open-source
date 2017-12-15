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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<h3>Locale: "${requestScope.locale}"</h3>
<fmt:setLocale value="${requestScope.locale}" /> 
<table border="1" cellspacing=0 cellpadding=3>
	<tr><th>Type</th><th>Pattern</th><th>Example</th><th>Parsed</th></tr>
	<jsp:include page="parseNumber_row.jsp">
		<jsp:param name="type" value="number" />
	</jsp:include>
	<jsp:include page="parseNumber_row.jsp">
		<jsp:param name="type" value="currency" />
	</jsp:include>
	<jsp:include page="parseNumber_row.jsp">
		<jsp:param name="type" value="percent" />
	</jsp:include>
</table>
