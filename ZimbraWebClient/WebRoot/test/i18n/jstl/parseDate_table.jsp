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
<table border="1" cellspacing=0 cellpadding=3>
	<tr><th>Length</th><th>Pattern</th><th>Example</th><th>Parsed</th></tr>
	<jsp:include page="parseDate_row.jsp">
		<jsp:param name="timeZone" value="${param.timeZone}" />
		<jsp:param name="locale" value="${param.locale}" />
		<jsp:param name="type" value="${param.type}" />
		<jsp:param name="length" value="short" />
	</jsp:include>
	<jsp:include page="parseDate_row.jsp">
		<jsp:param name="timeZone" value="${param.timeZone}" />
		<jsp:param name="locale" value="${param.locale}" />
		<jsp:param name="type" value="${param.type}" />
		<jsp:param name="length" value="medium" />
	</jsp:include>
	<jsp:include page="parseDate_row.jsp">
		<jsp:param name="timeZone" value="${param.timeZone}" />
		<jsp:param name="locale" value="${param.locale}" />
		<jsp:param name="type" value="${param.type}" />
		<jsp:param name="length" value="long" />
	</jsp:include>
	<jsp:include page="parseDate_row.jsp">
		<jsp:param name="timeZone" value="${param.timeZone}" />
		<jsp:param name="locale" value="${param.locale}" />
		<jsp:param name="type" value="${param.type}" />
		<jsp:param name="length" value="full" />
	</jsp:include>
</table>