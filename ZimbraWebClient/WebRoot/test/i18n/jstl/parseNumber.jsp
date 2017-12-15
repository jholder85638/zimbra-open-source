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
<%@ page import="java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<style type="text/css">
	TR { vertical-align: top; }
	TH { text-align: left; }
	TD { text-align: right; }
</style>

<c:set var="number" value="${1234.5678}" scope="request" />

<h2>${number}</h2>
<li>parse to output: <fmt:parseNumber value='${number}' pattern="###0.####" /></li>

<c:set var="locale" value="${pageContext.request.locale}" scope="request" />
<jsp:include page="parseNumber_table.jsp" />

<% pageContext.setAttribute("locale", new Locale("ja"), PageContext.REQUEST_SCOPE); %>
<jsp:include page="parseNumber_table.jsp" />
