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

<%@ taglib prefix="zm" uri="com.zimbra.zm" %>
<%@ taglib prefix="app" uri="com.zimbra.htmlextras" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<zm:getMailbox var="mailbox"/>

<app:overviewFolder folder="${mailbox.inbox}" key="i" label="inbox"/>
<app:overviewFolder folder="${mailbox.sent}" key="s" label="sent"/>
<app:overviewFolder folder="${mailbox.drafts}" key="d" label="drafts"/>
<app:overviewFolder folder="${mailbox.spam}" key="u" label="spam"/>
<app:overviewFolder folder="${mailbox.trash}" key="t" label="trash"/>

<p/>

<zm:forEachFolder var="folder">
	<c:if test="${!folder.isSystemFolder and (folder.isNullView or folder.isMessageView or folder.isConversationView)}">
		<c:if test="${!folder.isSearchFolder}">
			<app:overviewFolder folder="${folder}"/>
		</c:if>
		<c:if test="${folder.isSearchFolder and folder.depth gt 0}">
			<app:overviewSearchFolder folder="${folder}"/>
		</c:if>
	</c:if>
</zm:forEachFolder>
