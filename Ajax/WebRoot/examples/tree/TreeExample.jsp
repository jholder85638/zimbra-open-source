<!-- 
-->
<html>
  <head>
    <title>Tree Example</title>
    <style type="text/css">
      <!--
        @import url(../common/img/loRes/dwtimgs.css);
        @import url(TreeExample.css);
      -->
    </style>
    <jsp:include page="../Messages.jsp"/>
    <jsp:include page="../Ajax.jsp"/>
    <script type="text/javascript" src="ExMsg.js"></script>
    <script type="text/javascript" src="TreeExample.js"></script>
  </head>
    <body>
    <noscript><p><b>Javascript must be enabled to use this.</b></p></noscript>
    <script language="JavaScript">   	
   		function launch() {
   			DBG = new AjxDebug(AjxDebug.NONE, null, false);
 	    	TreeExample.run();
	    }
        AjxCore.addOnloadListener(launch);
    </script>
    </body>
</html>

