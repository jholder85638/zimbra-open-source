<!--
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2008, 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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
-->
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="zd" tagdir="/WEB-INF/tags/desktop" %>
<%@ taglib prefix="zdf" uri="com.zimbra.cs.offline.jsp" %>
<%@ taglib prefix="zm" uri="com.zimbra.zm" %>

<jsp:useBean id="bean" class="com.zimbra.cs.offline.jsp.PageBean"/>
<jsp:setProperty name="bean" property="locale" value="${pageContext.request.locale}"/>
<jsp:useBean id="eventBean" class="com.zimbra.cs.offline.jsp.ClientEventBean"/>

<zd:auth/>

${zdf:onLogin(eventBean)}

<c:if test="${empty bean.loginUsername}">
    <c:redirect url="${zdf:addAuthToken('/desktop/console.jsp', pageContext.request)}"/>
</c:if>

<c:set var="attrsToFetch" value="zimbraFeatureMailEnabled,zimbraFeatureCalendarEnabled,zimbraFeatureContactsEnabled,zimbraFeatureIMEnabled,zimbraFeatureNotebookEnabled,zimbraFeatureOptionsEnabled,zimbraFeatureTasksEnabled,zimbraFeatureBriefcasesEnabled"/>
<c:set var="prefsToFetch" value="zimbraPrefSkin,zimbraPrefClientType,zimbraPrefLocale"/>

<c:catch var="loginException">
    <zm:login username="${empty param.username ? bean.loginUsername : param.username}" password="${zdf:getLocalConfig('zdesktop_installation_key')}"
        varRedirectUrl="postLoginUrl" varAuthResult="authResult" rememberme="true"
        prefs="${prefsToFetch}" attrs="${attrsToFetch}" requestedSkin="${param.skin}"/>
</c:catch>

<c:if test="${not empty loginException}">
    <%-- try and use existing cookie if possible --%>
    <c:set var="authtoken" value="${not empty param.zauthtoken ? param.zauthtoken : cookie.ZM_AUTH_TOKEN.value}"/>
    <c:if test="${not empty authtoken}">
        <zm:login authtoken="${authtoken}" authtokenInUrl="${not empty param.zauthtoken}"
            varRedirectUrl="postLoginUrl" varAuthResult="authResult"
            rememberme="true" prefs="${prefsToFetch}" attrs="${attrsToFetch}"
            requestedSkin="${param.skin}"/>
    </c:if>
</c:if>

<c:choose>
<c:when test="${not empty authResult}">
    <jsp:forward page="/desktop/launchZD.jsp"/>
</c:when>
<c:otherwise>
    <c:redirect url="${zdf:addAuthToken('/desktop/console.jsp', pageContext.request)}"/>
</c:otherwise>
</c:choose>

