<!-- 
-->
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>DwtHtmlEditor Example</title>
    <style type="text/css">
      <!--
        @import url(../common/img/loRes/dwtimgs.css);
        @import url(DwtHtmlEditorExample.css);
      -->
    </style>
    <jsp:include page="../Messages.jsp"/>
    <jsp:include page="../Ajax.jsp"/>
    <script type="text/javascript" src="ExMsg.js"></script>
    <script type="text/javascript" src="DwtHtmlEditorExample.js"></script>
  </head>
    <body>
    <noscript><p><b>Javascript must be enabled to use this.</b></p></noscript>
    <script language="JavaScript">   	
   		function launch() {
   			DBG = new AjxDebug(AjxDebug.NONE, null, false);
	    	DwtHtmlEditorExample.run();
	    }
        AjxCore.addOnloadListener(launch);
    </script>
    </body>
</html>

