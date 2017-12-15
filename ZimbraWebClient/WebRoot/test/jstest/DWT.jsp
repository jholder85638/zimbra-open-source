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
<c:if test="${inline != 'inline'}">
<script type="text/javascript" src="/ZimbraConsole/js/dwt/core/DWT.js"></script>
<script type="text/javascript" src="/ZimrbaConsole/js/dwt/events/EventTypes.js"></script>
<script type="text/javascript" src="/ZimbraConsole/js/dwt/events/DWTEvent.js"></script>
<script type="text/javascript" src="/ZimbraConsole/js/dwt/events/KeyEvent.js"></script>
<script type="text/javascript" src="/ZimbraConsole/js/dwt/events/MouseEvent.js"></script>
<script type="text/javascript" src="/ZimbraConsole/js/dwt/widgets/Widget.js"></script>
<script type="text/javascript" src="/ZimbraConsole/js/dwt/widgets/Control.js"></script>
<script type="text/javascript" src="/ZimbraConsole/js/dwt/widgets/Composite.js"></script>
<script type="text/javascript" src="/ZimbraConsole/js/dwt/widgets/Text.js"></script>
<script type="text/javascript" src="/ZimbraConsole/js/dwt/widgets/Label.js"></script>
<script type="text/javascript" src="/ZimbraConsole/js/dwt/widgets/Button.js"></script>
<script type="text/javascript" src="/ZimbraConsole/js/dwt/widgets/Window.js"></script>
</c:if>

<c:if test="${inline == 'inline'}">
<script language="JavaScript>
<jsp:include page="/js/dwt/core/DWT.js"/>
<jsp:include page="/js/dwt/events/EventTypes.js"/>
<jsp:include page="/js/dwt/events/DWTEvent.js"/>
<jsp:include page="/js/dwt/events/KeyEvent.js"/>
<jsp:include page="/js/dwt/events/MouseEvent.js"/>
<jsp:include page="/js/dwt/widgets/Widget.js"/>
<jsp:include page="/js/dwt/widgets/Control.js"/>
<jsp:include page="/js/dwt/widgets/Composite.js"/>
<jsp:include page="/js/dwt/widgets/Text.js"/>
<jsp:include page="/js/dwt/widgets/Label.js"/>
<jsp:include page="/js/dwt/widgets/Button.js"/>
<jsp:include page="/js/dwt/widgets/Window.js"/>
</script>
</c:if>