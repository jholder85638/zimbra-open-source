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
<%@ taglib prefix="fmt" uri="com.zimbra.i18n" %>
<style type="text/css">
	TR { vertical-align: top; }
	TH { text-align: left; }
</style>

<jsp:useBean id="now" class='java.util.Date' scope="request" />

<h2>${requestScope.now}</h2>
<li>parse to output: <fmt:parseDate value='200601016' pattern="yyyyMMdd" /></li>

<jsp:include page="parseDate_section.jsp">
	<jsp:param name="timeZone" value="PST" />
	<jsp:param name="locale" value="${pageContext.request.locale}" />
</jsp:include>

<jsp:include page="parseDate_section.jsp">
	<jsp:param name="timeZone" value="EST" />
	<jsp:param name="locale" value="${pageContext.request.locale}" />
</jsp:include>

<fmt:setLocale value="ja" />
<jsp:include page="parseDate_section.jsp">
	<jsp:param name="timeZone" value="EST" />
	<jsp:param name="locale" value="ja" />
</jsp:include>
