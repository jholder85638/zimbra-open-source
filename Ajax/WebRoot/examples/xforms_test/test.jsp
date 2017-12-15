<!-- 
-->
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Zimbra XForms Test Environment</title>
    <style type="text/css">
      <!--
        @import url(../common/img/loRes/dwtimgs.css);
        @import url(config/style/dv.css);
       -->
    </style>
	<SCRIPT>var ot0 = new Date().getTime()</SCRIPT>
    <jsp:include page="../Messages.jsp"/>
    <jsp:include page="../Ajax.jsp"/>

	<SCRIPT language=JavaScript src="test_scripts.js"></SCRIPT>
	<SCRIPT>var ot1 = new Date().getTime();DBG.println("loading all libraries took " + (ot1-ot0) + " msec");</SCRIPT>
</head>
<body onload='loadTestForm();'>

<table width=100% height=100% cellspacing=0 cellpadding=0>
<tr><td height=1>
	<div class=testHeader>
		<table class=tabtable><tr>		
			<td class=borderbottom>&nbsp;&nbsp;</td>
			<td id=show_display class=tab onclick='showCard("display")'>Display</td>
			<td id=show_HTMLOutput class=tab onclick='showCard("HTMLOutput")'>HTML</td>

			<td id=show_debug class=tab onclick='showCard("debug")'>Debug</td>
			<td id=show_formItems class=tab onclick='showCard("formItems")'>Items</td>
			<td id=show_instanceValue class=tab onclick='showCard("instanceValue")'>Instance</td>
			<td id=show_updateScript class=tab onclick='showCard("updateScript")'>UpdateScr</td>

			<td width=100% class=borderbottom>&nbsp;&nbsp;&nbsp;</td>

			<td class=borderbottom><div class=label>Form:</div></td>
			<td class=borderbottom><select id=formList class=xform_select1 onchange='setCurrentForm(this.options[this.selectedIndex].value)'></select></td>
			<td class=borderbottom><div class=label>Instance:</div></td>
			<td class=borderbottom><select id=instanceList class=xform_select1 onchange='setCurrentInstance(this.selectedIndex)'></select></td>
			<td class=borderbottom>&nbsp;&nbsp;</td>

		</tr>
		</table>
	</div>
</td></tr>
<tr><td height=100%>
<div style='width:100%;height:100%;position:relative;overflow:auto;'>	
		<div ID=display class=displayCard width=100% height=100%>
		</div ID=output>
	
		<div ID=debug class=debugCard>
			<button onclick='DBG.clear()'>Clear</button><pre>
		</div ID=debug>
	
		<div ID=output class=displayCard>
		</div ID=output>
	</div>
</td>
</tr></table>

</body>

<!-- * ADD SCRIPT INCLUDES HERE FOR ALL THE FORMS YOU WANT TO REGISTER 	
	 * CREATE A HELPER FILE LIKE  LmAppoinmentView_helper.js  IF NEEDED 
		TO REGISTER YOUR FORM AND PROVIDE INSTANCES 					
-->
<SCRIPT language=JavaScript src="xform_model_test.js"></SCRIPT>


</html>
