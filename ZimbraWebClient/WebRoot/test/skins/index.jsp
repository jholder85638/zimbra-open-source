<!--
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2007, 2008, 2009, 2010, 2013, 2014, 2015, 2016 Synacor, Inc.
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
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.io.*" %>
<%@page import="java.util.*" %>
<%
String skin = request.getParameter("skin");
if (skin == null) skin = application.getInitParameter("zimbraDefaultSkin");

String contextPath = request.getContextPath();
%>
<html>
<head>
<title>Skin Tester</title>
<link rel="stylesheet" type="text/css" href="style.css">
<!-- messages and keys -->
<script src='../../messages/I18nMsg,AjxMsg,ZMsg,ZmMsg.js'></script>
<script src="../../keys/AjxKeys,ZmKeys.js"></script>
<!-- source code -->
<jsp:include page="../../public/Boot.jsp" />
<jsp:include page="../../public/jsp/Ajax.jsp" />
<jsp:include page="../../public/jsp/Zimbra.jsp" />
<script>
var appContextPath = "<%=request.getContextPath()%>";
</script>
<jsp:include page="../../public/jsp/ZimbraCore.jsp" />
<!-- skin changer source -->
<script src='util.js'></script>
<script src='ui.js'></script>
<script src='skin.js'></script> 
<script>
function launch() {
  // setup dwt
  window.DBG = new AjxDebug(AjxDebug.NONE, null, false);
  var shell = new DwtShell(null, false, null, $("main"), null, false);
  var appCtxt = new ZmAppCtxt();
  appCtxt.setShell(shell);

  // load skin
  loadSkin("<%=skin%>");
}
</script>
<!-- skin resources -->
<link id='skin-styles' rel='stylesheet' type='text/css'>
<script id="skin-source"></script>
</head>
<body onload='launch()'>
<div id='main'>
<div style='display:none'>
  <select id='skin-selector' onchange='skinSelected(this)'>
    <%
    String dirname = getServletContext().getRealPath("/skins");
    File dir = new File(dirname);

      List<String> filelist = new LinkedList();
    String[] filenames = dir.list();
    for (String filename : filenames) {
      File file = new File(dir, filename);
      if (file.isDirectory() && !filename.startsWith("_")) {
        filelist.add(filename);
      }
    }

    Collections.sort(filelist);
    for (String filename : filelist) {
      String selected = filename.equals(skin) ? "selected='selected'" : ""; %>
      <option id='skin-item-<%=filename%>' value='<%=filename%>' <%=selected%>><%=filename%></option>
      <%
    }
    %>
  </select>
</div>
<div id='skin-body'></div>
</div>
</body>
</html>