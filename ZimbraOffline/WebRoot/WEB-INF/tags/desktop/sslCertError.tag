<%--
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
<%@ taglib prefix="fmt" uri="com.zimbra.i18n" %>

(<a href="javascript:zd.toggle('certInfo')"><fmt:message key='CertDetails'/></a>)

<div id="certInfo" style="display:none">
	<p class="ZFieldSubLabelLeft">
		<c:choose>
			<c:when test="${bean.sslCertInfo.mismatch}">
				<fmt:message key='CertWarningMismatch'>
					<fmt:param><b>${bean.sslCertInfo.hostname}</b></fmt:param>
					<fmt:param><b>${bean.sslCertInfo.commonName}</b></fmt:param>
				</fmt:message>
			</c:when>
			<c:otherwise>
				<fmt:message key='CertWarningUntrusted'>
					<fmt:param><b>${bean.sslCertInfo.hostname}</b></fmt:param>
				</fmt:message>
			</c:otherwise>
		</c:choose>
	</p>
	<p>
		<table cellpadding=2 cellspacing=0 border=0>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><b><fmt:message key='CertIssuedTo'/></b></td>
				<td></td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><fmt:message key='CertCommonName'/></td>
				<td class="ZFieldCertLabel">${bean.sslCertInfo.commonName}</td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><fmt:message key='CertOrganizationUnit'/></td>
				<td class="ZFieldCertLabel">${bean.sslCertInfo.organizationUnit}</td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><fmt:message key='CertOrganization'/></td>
				<td class="ZFieldCertLabel">${bean.sslCertInfo.organization}</td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><fmt:message key='CertSerialNumber'/></td>
				<td class="ZFieldCertLabel">${bean.sslCertInfo.serialNumber}</td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><b><fmt:message key='CertIssuedBy'/></b></td>
				<td></td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><fmt:message key='CertIssuerCommonName'/></td>
				<td class="ZFieldCertLabel">${bean.sslCertInfo.issuerCommonName}</td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><fmt:message key='CertIssuerOrganizationUnit'/></td>
				<td class="ZFieldCertLabel">${bean.sslCertInfo.issuerOrganizationUnit}</td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><fmt:message key='CertIssuerOrganization'/></td>
				<td class="ZFieldCertLabel">${bean.sslCertInfo.issuerOrganization}</td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><b><fmt:message key='CertValidity'/></b></td>
				<td></td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><fmt:message key='CertIssuedOn'/></td>
				<td class="ZFieldCertLabel">${bean.sslCertInfo.issuedOn}</td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><fmt:message key='CertExpiresOn'/></td>
				<td class="ZFieldCertLabel">${bean.sslCertInfo.expiresOn}</td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><b><fmt:message key='CertFingerPrints'/></b></td>
				<td></td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><fmt:message key='CertSHA1'/></td>
				<td class="ZFieldCertLabel">${bean.sslCertInfo.sha1}</td>
			</tr>
			<tr>
				<td class="ZFieldSubLabel ZFieldCertLabel"><fmt:message key='CertMD5'/></td>
				<td class="ZFieldCertLabel">${bean.sslCertInfo.md5}</td>
			</tr>
		</table>
	</p>
</div>
