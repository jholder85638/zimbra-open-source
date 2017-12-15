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
<%@ attribute name="showGroups" rtexprvalue="true" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="app" uri="com.zimbra.htmlextras" %>
<%@ taglib prefix="zm" uri="com.zimbra.zm" %>

<td class='toolbar unread'><input class="inputSubmit" type="submit" name="actionCompose" value="<fmt:message key="compose"/>"></td>
<td class='toolbar'><input class="inputSubmit" type="submit" name="actionDelete" value="<fmt:message key="delete"/>"></td>
<c:if test="${showGroups}">
	<td class='toolbar'>
		<select>
			<option selected disabled><fmt:message key="addToGroup"/></option>
			<option class='actionOption' value='newgroup'><fmt:message key="newGroup"/></option>
		</select>
	</td>
</c:if>
<td class='toolbar'>
	<select name="folderId">
		<option selected disabled><fmt:message key="moveTo"/></option>
		<zm:forEachFolder var="folder">
			<c:if test="${folder.isContactMoveTarget}">
				<option value="m:${folder.id}" />${zm:repeatString('&nbsp;&nbsp;', folder.depth)}${fn:escapeXml(folder.name)}
			</c:if>
		</zm:forEachFolder>
	</select>
	<input class="inputSubmit" type="submit" name="actionMove" value="<fmt:message key="move"/>">
</td>
<td class='toolbar'>
	<select name="actionOp">
		<option selected disabled><fmt:message key="applyLabel"/></option>
		<zm:forEachTag var="tag">
			<option class='actionOption' value="t:${tag.id}">${fn:escapeXml(tag.name)}</option>
		</zm:forEachTag>
		<option class='actionOption' value='new'><fmt:message key="newLabel"/></option>
		<option disabled><fmt:message key="removeLabel"/></option>
		<zm:forEachTag var="tag">
			<option class='actionOption' value="u:${tag.id}">${fn:escapeXml(tag.name)}</option>
		</zm:forEachTag>
	</select>
	<input class="inputSubmit" type="submit" name="action" value='<fmt:message key="go"/>'/>
</td>
<td width=100%></td>
<td><nobr><a href="newContact"><fmt:message key="addContact"/></a> - </nobr></td>
<td><nobr><a href="/mail/import"><fmt:message key="import"/></a> - </nobr></td>
<td style="padding-right:3px"><nobr><a href="/mail/export"><fmt:message key="export"/></a></nobr></td>
