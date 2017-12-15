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
<%@ page import="java.text.*" %>
<% MessageFormat formatter = new MessageFormat("You last logged in on {3,date,short} at {3,time,short}, {0}."); %>
<h3>Formats By Argument Index</h3>
<% {
	Format[] formats = formatter.getFormatsByArgumentIndex();
	for (int i = 0; i < formats.length; i++) { %>
		<li>formats[<%=i%>] = <%=formats[i]%></li>
<%	}
} %><h3>Formats</h3>
<% {
	Format[] formats = formatter.getFormats();  
	for (int i = 0; i < formats.length; i++) { %>
		<li>formats[<%=i%>] = <%=formats[i]%></li>
<%	}
} %>