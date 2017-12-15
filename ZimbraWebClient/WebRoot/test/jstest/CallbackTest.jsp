<!-- 
***** BEGIN LICENSE BLOCK *****
Zimbra Collaboration Suite Web Client
Copyright (C) 2006, 2007, 2010, 2013, 2014, 2015, 2016 Synacor, Inc.

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software Foundation,
version 2 of the License.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <https://www.gnu.org/licenses/>.
***** END LICENSE BLOCK *****
-->

<%@ page language="java" import="java.lang.*, java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>

		<title>Callback Test</title>

		<script src="/zimbra/js/ajax/boot/AjxCallback.js"></script>
		<script src="/zimbra/js/ajax/util/AjxBuffer.js"></script>

		<script>
			function myFunc() {
				var x = "hello";
			}
			
			var buffer = new AjxBuffer("hello", "there");
			var num = 100000;
			function testAjxCallback() {
				var s = (new Date()).getTime();
				for (var i = 0; i < num; i++) {
					var callback = new AjxCallback(buffer, buffer.join);
					callback.run(" ");
				}
				var e = (new Date()).getTime();
				var t = e - s;
				var el = document.getElementById("resultsDivA");
				el.innerHTML = num + " iterations took " + t + "ms";
			}
			
			function testClosure() {
				var s = (new Date()).getTime();
				for (var i = 0; i < num; i++) {
					var callback = AjxCallback.simpleClosure(buffer.join, buffer);
					callback(" ");
				}
				var e = (new Date()).getTime();
				var t = e - s;
				var el = document.getElementById("resultsDivB");
				el.innerHTML = num + " iterations took " + t + "ms";
			}
		</script>

	</head>

	<body>
    <button onclick="testAjxCallback();">AjxCallback</button>
    <div id="resultsDivA"></div>
    <button onclick="testClosure();">closure</button>
    <div id="resultsDivB"></div>
	</body>

</html>
