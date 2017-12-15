<!-- 
***** BEGIN LICENSE BLOCK *****
Zimbra Collaboration Suite Web Client
Copyright (C) 2004, 2005, 2006, 2007, 2010, 2013, 2014 Zimbra, Inc.

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software Foundation,
version 2 of the License.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
***** END LICENSE BLOCK *****
-->

<%@ page language="java" 
         import="java.lang.*, java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="inline" value="outline" scope="session"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Zimbra Browser</title>
    <script type="text/javascript" src="/ZimbraConsole/js/debug/Debug.js"></script>
    <jsp:include page="util.jsp"/>
    <jsp:include page="DWT.jsp"/>
  </head>
  <body>
    <script language="JavaScript">
      <jsp:include page="main.js"/>
    </script>
  </body>
</html>
