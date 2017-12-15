<!-- 
***** BEGIN LICENSE BLOCK *****
Zimbra Collaboration Suite Web Client
Copyright (C) 2005, 2006, 2007, 2010, 2013, 2014, 2015, 2016 Synacor, Inc.

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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Property Editor test</title>
<% 
   String contextPath = (String)request.getContextPath(); 
   String vers = (String)request.getAttribute("version");
   String ext = (String)request.getAttribute("fileExtension");
   if (vers == null){
      vers = "";
   }
   if (ext == null){
      ext = "";
   }
%>
    <jsp:include page="../../public/Messages.jsp"/>
    <jsp:include page="../../public/Ajax.jsp"/>
    <jsp:include page="../../public/Dwt.jsp"/>
    <script type="text/javascript" src="<%= contextPath %>/js/ajax/dwt/widgets/DwtPropertyEditor.js<%= ext %>?v=<%= vers %>"></script>
    <script type="text/javascript" src="script.js"></script>

<style type="text/css">
<!--
        @import url(/zimbra/img/imgs.css?v=<%= vers %>);
        @import url(/zimbra/img/skins/steel/skin.css?v=<%= vers %>);
        @import url(/zimbra/js/zimbraMail/config/style/dwt.css?v=<%= vers %>);
        @import url(/zimbra/js/zimbraMail/config/style/common.css?v=<%= vers %>);
        @import url(/zimbra/js/zimbraMail/config/style/zm.css?v=<%= vers %>);
        @import url(/zimbra/js/zimbraMail/config/style/spellcheck.css?v=<%= vers %>);
        @import url(/zimbra/skins/steel/skin.css?v=<%= vers %>);
-->
</style>
    <style type="text/css">
      <!--
        @import url(style.css);
      -->
    </style>

  </head>
    <body>
    <noscript><p><b>Javascript must be enabled to use this.</b></p></noscript>
    <script language="JavaScript">   	
   		function launch() {
   			DBG = new AjxDebug(AjxDebug.NONE, null, false);
 	    	App.run();
	    }
        AjxCore.addOnloadListener(launch);
    </script>
    </body>
</html>

