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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:useBean id="now" class="java.util.Date" />
<%
	TimeZone est = TimeZone.getTimeZone("US/Eastern");
	pageContext.setAttribute("est", est);
	TimeZone pst = TimeZone.getTimeZone("US/Pacific");
	pageContext.setAttribute("pst", pst);
%>

<h3>fmt:setTimeZone</h3>
<h4>As String: "US/Eastern"</h4>
<fmt:setTimeZone value='US/Eastern' />
<h4>As TimeZone: ${est}</h4>
<fmt:setTimeZone value="${est}" />

<h3>fmt:timeZone</h3>
<h4>As String: "US/Pacific"</h4>
<fmt:timeZone value='US/Pacific'>
	<strong>Note:</strong>
	NPE if there is no content.
</fmt:timeZone>
<h4>As TimeZone: ${pst}</h4>
<fmt:timeZone value="${pst}">
	<strong>Note:</strong>
	NPE if there is no content.
</fmt:timeZone>

<h3>Format Time</h3>
<h4>US/Eastern</h4>
<fmt:timeZone value="${est}">
<li>fmt:timeZone: <fmt:formatDate value="${now}" type="time" /></li>
</fmt:timeZone>
<li>String attr: <fmt:formatDate value="${now}" type="time" timeZone="US/Eastern" /></li>
<li>TimeZone attr: <fmt:formatDate value="${now}" type="time" timeZone="${est}" /></li>

<h4>US/Pacific</h4>
<fmt:timeZone value="${pst}">
<li>fmt:timeZone: <fmt:formatDate value="${now}" type="time" /></li>
</fmt:timeZone>
<li>String attr: <fmt:formatDate value="${now}" type="time" timeZone="US/Pacific" /></li>
<li>TimeZone attr: <fmt:formatDate value="${now}" type="time" timeZone="${pst}" /></li>