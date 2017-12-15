<!--
***** BEGIN LICENSE BLOCK *****
Zimbra Collaboration Suite Web Client
Copyright (C) 2006, 2007, 2010, 2013, 2014 Zimbra, Inc.

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
<%
    String contextPath = request.getContextPath();
    String vers = (String)request.getAttribute("version");
    String ext = (String)request.getAttribute("fileExtension");
//  String mode = (String) request.getAttribute("mode");
    if (vers == null){
       vers = "";
    }
    if (ext == null){
       ext = "";
    }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Zimbra HTML Editor Test</title>
	<style type="text/css">
	<!--
    @import url(<%=contextPath %>/img/imgs.css?v=<%=vers%>);
    @import url(<%=contextPath %>/img/skins/steel/steel.css?v=<%=vers%>);
    @import url(<%=contextPath %>/skins/steel/dwt.css?v=<%=vers%>);
    @import url(<%=contextPath %>/skins/steel/common.css?v=<%=vers%>);
    @import url(<%=contextPath %>/skins/steel/msgview.css?v=<%=vers%>);
    @import url(<%=contextPath %>/skins/steel/zm.css?v=<%=vers%>);
    @import url(<%=contextPath %>/skins/steel/spellcheck.css?v=<%=vers%>);
    @import url(<%=contextPath %>/skins/steel/steel.css?v=<%=vers%>);
    @import url(<%=contextPath %>/ALE/spreadsheet/style.css?v=<%=vers%>);
	-->
	</style>
    <script type="text/javascript">
       var appContextPath = "<%=contextPath%>";
    </script>
    <jsp:include page="../../public/Messages.jsp"/>
    <script type="text/javascript" src="EditorTest.js"></script>
    <jsp:include page="../../public/Boot.jsp"/>
    <jsp:include page="../../public/Ajax.jsp"/>
    <jsp:include page="../../public/jsp/Zimbra.jsp"/>
    <jsp:include page="../../public/jsp/ZimbraCore.jsp"/>
    <jsp:include page="../../public/jsp/Mail.jsp"/>
  </head>
    <body>
    <noscript><p><b>Javascript must be enabled to use this.</b></p></noscript>
    <script type="text/javascript" language="JavaScript">
        AjxCore.addOnloadListener(EditorTest.run);
    </script>
    </body>
</html>

