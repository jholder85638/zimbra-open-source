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

<td class='toolbar'>
	<input class="inputSubmit" type="submit" name="actspam" value="<fmt:message key="reportSpam"/>">
</td>
<td class='toolbar'>
	<input class="inputSubmit" type="submit" name="acttrash" value="<fmt:message key="delete"/>"/>
</td>
<td class='toolbar'>
	<select name="actionfid">
		<option selected disabled><fmt:message key="moveTo"/></option>
		<zm:forEachFolder var="folder">
			<c:if test="${folder.isConversationMoveTarget}">
				<option value="m:${folder.id}" />${zm:repeatString('&nbsp;&nbsp;', folder.depth)}${fn:escapeXml(folder.name)}
			</c:if>
		</zm:forEachFolder>
	</select>
	<input class="inputSubmit" type="submit" name="actmove" value="<fmt:message key="move"/>">
</td>
<td class='toolbar'>
	<select name="action">
		<option selected disabled><fmt:message key="moreActions"/></option>
		<option value='read' class='actionOption'><fmt:message key="markAsRead"/></option>
		<option value='unread' class='actionOption'><fmt:message key="markAsUnread"/></option>
		<option value='flag' class='actionOption'><fmt:message key="addStar"/></option>
		<option value='unflag' class='actionOption'><fmt:message key="removeStar"/></option>
		<option disabled>--------</option>
		<option disabled><fmt:message key="applyLabel"/></option>
		<zm:forEachTag var="tag">
			<option class='actionOption' value="t:${tag.id}">${fn:escapeXml(tag.name)}</option>
		</zm:forEachTag>
		<option class='actionOption' value='new'><fmt:message key="newLabel"/></option>
		<option disabled><fmt:message key="removeLabel"/></option>
		<zm:forEachTag var="tag">
			<option class='actionOption' value="u:${tag.id}">${fn:escapeXml(tag.name)}</option>
		</zm:forEachTag>
	</select>
	<input class="inputSubmit" type="submit" name="actgo" value='<fmt:message key="go"/>'/>
</td>
