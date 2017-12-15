<!-- 
-->
<% 
   String contextPath = (String)request.getContextPath(); 
%>
<html>
<head>
</head>
<h2> Sample Apps </h2>
<ul>
<li> <a href="<%= contextPath %>/examples/tree/TreeExample.jsp">Tree Example</a></li>
<li> <a href="<%= contextPath %>/examples/htmlEditor/DwtHtmlEditorExample.jsp">DwtEditor Example</a></li>
<li> <a href="<%= contextPath %>/examples/dataViewer/FlightInfo.jsp">Data Viewer Example (FlightInfo)</a></li>
<li> <a href="<%= contextPath %>/examples/mixing/MixingExample.jsp">Mixing DWT & Plain HTML</a></li>
<li> <a href="<%= contextPath %>/examples/mixing2/MixingExample.jsp">Mixing DWT into an HTML Page</a></li>
<li> <a href="<%= contextPath %>/examples/xforms_test/xforms.jsp">Xforms Example</a></li>
</ul>
</html>