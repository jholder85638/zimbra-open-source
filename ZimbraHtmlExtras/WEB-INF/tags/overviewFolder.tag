<%--
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2006, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
--%>
<%@ tag body-content="empty" %>
<%@ attribute name="folder" rtexprvalue="true" required="true" type="com.zimbra.cs.taglib.bean.ZFolderBean" %>
<%@ attribute name="label" rtexprvalue="true" required="false" %>
<%@ attribute name="base" rtexprvalue="true" required="false" %>
<%@ attribute name="key" rtexprvalue="true" required="false" %>
<%@ attribute name="alwaysBold" rtexprvalue="true" required="false" %>
<%@ attribute name="isShared" rtexprvalue="true" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="app" uri="com.zimbra.htmlextras" %>
<%@ taglib prefix="zm" uri="com.zimbra.zm" %>

<c:if test="${!empty label}"><fmt:message key="${label}" var="label"/></c:if>
<div class="folder<c:if test="${folder.hasUnread or alwaysBold}"> unread</c:if><c:if test="${folder.id eq requestScope.context.selectedId}"> folderSelected</c:if><c:if test="${isShared}"> sharedFolder</c:if>" style='padding-left: ${4+folder.depth*8}px'>
	<a href='${empty base ? "clv" : base}?sfi=${folder.id}'>
		${fn:escapeXml(empty label ? folder.name : label)}
		<c:if test="${folder.hasUnread}"> (${folder.unreadCount})</c:if>
	</a>
</div>
