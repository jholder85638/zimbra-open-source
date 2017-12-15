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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="app" uri="com.zimbra.htmlextras" %>
<%@ taglib prefix="zm" uri="com.zimbra.zm" %>

<c:set var="ids" value="${fn:join(paramValues.id, ',')}"/>
<c:choose>
    <c:when test="${empty ids}">
        <app:status style="Warning"><fmt:message key="actionNoConvSelected"/></app:status>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${!empty param.actspam}">
                <zm:markConversationSpam  var="result" id="${ids}" spam="true"/>
                <app:status>
                    <fmt:message key="actionConvMarkedSpam">
                        <fmt:param value="${result.idCount}"/>
                    </fmt:message>
                </app:status>
            </c:when>
            <c:when test="${!empty param.acttrash}">
                <zm:getMailbox var="mailbox"/>
                <zm:moveConversation  var="result" id="${ids}" folderid="${mailbox.trash.id}"/>
                <app:status>
                    <fmt:message key="actionConvMovedTrash">
                        <fmt:param value="${result.idCount}"/>
                    </fmt:message>
                </app:status>
            </c:when>
            <c:when test="${param.action eq 'unread' or param.action eq 'read'}">
                <zm:markConversationRead var="result" id="${ids}" read="${param.action eq 'read'}"/>
                <app:status>
                    <fmt:message key="${param.action eq 'read' ? 'actionConvMarkedRead' : 'actionConvMarkedUnread'}">
                        <fmt:param value="${result.idCount}"/>
                    </fmt:message>
                </app:status>
            </c:when>
            <c:when test="${param.action eq 'flag' or param.action eq 'unflag'}">
                <zm:flagConversation var="result" id="${ids}" flag="${param.action eq 'flag'}"/>
                <app:status>
                    <fmt:message key="${param.action eq 'flag' ? 'actionConvFlag' : 'actionConvUnflag'}">
                        <fmt:param value="${result.idCount}"/>
                    </fmt:message>
                </app:status>
            </c:when>
            <c:when test="${fn:startsWith(param.action, 't:') or fn:startsWith(param.action, 'u:')}">
                <c:set var="tag" value="${fn:startsWith(param.action, 't')}"/>
                <c:set var="tagid" value="${fn:substring(param.action, 2, -1)}"/>
                <zm:tagConversation tagid="${tagid}"var="result" id="${ids}" tag="${tag}"/>
                <app:status>
                    <fmt:message key="${tag ? 'actionConvTag' : 'actionConvUntag'}">
                        <fmt:param value="${result.idCount}"/>
                        <fmt:param value="${zm:getTagName(pageContext, tagid)}"/>
                    </fmt:message>
                </app:status>
            </c:when>
            <c:when test="${fn:startsWith(param.actionfid, 'm:')}">
                <c:set var="folderid" value="${fn:substring(param.actionfid, 2, -1)}"/>
                <zm:getMailbox var="mailbox"/>
                <zm:moveConversation folderid="${folderid}"var="result" id="${ids}"/>
                <app:status>
                    <fmt:message key="actionConvMoved">
                        <fmt:param value="${result.idCount}"/>
                        <fmt:param value="${zm:getFolderName(pageContext, folderid)}"/>
                    </fmt:message>
                </app:status>
            </c:when>
            <c:when test="${!empty param.actmove}">
                <app:status style="Warning"><fmt:message key="actionNoFolderSelected"/></app:status>
            </c:when>
            <c:otherwise>
                <app:status style="Warning"><fmt:message key="actionNoActionSelected"/></app:status>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
