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
<%@ taglib prefix="jfmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="zfmt" uri="com.zimbra.i18n" %>

<% int times = 1000; %>

<h2>Loading ZhMsg Comparison</h2>
<% long before, after; %>

<h3>&lt;fmt:setBundle> (JSTL, <%=times%> times)</h3>
<% before = System.currentTimeMillis(); %>
<% for (int i = 0; i < times; i++) { %>
	<jfmt:setBundle basename="/messages/ZhMsg" />
<% } %>
<% after = System.currentTimeMillis(); %>
<li>Time: <%=after-before%> ms</li>

<h3>&lt;fmt:setBundle> (Zimbra, <%=times%> times)</h3>
<% before = System.currentTimeMillis(); %>
<% for (int i = 0; i < times; i++) { %>
	<zfmt:setBundle basename="/messages/ZhMsg" />
<% } %>
<% after = System.currentTimeMillis(); %>
<li>Time: <%=after-before%> ms</li>

<h2>Loading ZmMsg Comparison</h2>

<h3>&lt;fmt:setBundle> (JSTL, <%=times%> times)</h3>
<% before = System.currentTimeMillis(); %>
<% for (int i = 0; i < times; i++) { %>
	<jfmt:setBundle basename="/messages/ZmMsg" />
<% } %>
<% after = System.currentTimeMillis(); %>
<li>Time: <%=after-before%> ms</li>

<h3>&lt;fmt:setBundle> (Zimbra, <%=times%> times)</h3>
<% before = System.currentTimeMillis(); %>
<% for (int i = 0; i < times; i++) { %>
	<zfmt:setBundle basename="/messages/ZmMsg" />
<% } %>
<% after = System.currentTimeMillis(); %>
<li>Time: <%=after-before%> ms</li>
