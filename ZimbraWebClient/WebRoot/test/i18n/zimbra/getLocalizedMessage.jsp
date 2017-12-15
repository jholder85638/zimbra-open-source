<!--
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
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
-->
<%@ page import="java.io.*,com.zimbra.cs.taglib.tag.i18n.*" %>
<%@ taglib prefix="fmt" uri="com.zimbra.i18n" %>
<%!
	static final String BASENAME = "/messages/test/i18n/getLocalizedMessage";
	static final String BASENAME2 = BASENAME+"2";
	static final String BASENAME3 = BASENAME+"3";
	static final String BASENAME4 = BASENAME+"4";

	static final String MESSAGE =
		"foo = [Test:foo]\n"+
		"bar = [Test:bar] Hello, {0}!\n"
	;

	static final String[] BASENAMES = {
		BASENAME,
		BASENAME2,
		BASENAME3,
		BASENAME4
	};
	static final String[] MESSAGES = {
		MESSAGE,
		MESSAGE.replaceAll("Test", "Test2"),
		MESSAGE.replaceAll("Test", "Test3"),
		MESSAGE.replaceAll("Test", "Test4"),
	};
%>
<%
	for (int i = 0; i < BASENAMES.length; i++) {
		String basename = BASENAMES[i];
		File file = new File(getServletContext().getRealPath("/WEB-INF/classes"+basename+".properties"));
		if (file.exists()) break;

		File dir = file.getParentFile();
		dir.mkdirs();
		OutputStream fout = new FileOutputStream(file);
		fout.write(MESSAGES[i].getBytes());
		fout.close();
	}
%>

<h3>fmt:bundle (Basename: "<%=BASENAME%>")</h3>
<fmt:bundle basename="<%=BASENAME%>">
<li>foo = <%=I18nUtil.getLocalizedMessage(pageContext, "foo")%></li>
<li>bar = <%=I18nUtil.getLocalizedMessage(pageContext, "bar", new Object[]{ "Andy" })%></li>
</fmt:bundle>

<h3>fmt:setBundle (Bundle: "<%=BASENAME2%>")</h3>
<fmt:setBundle basename="<%=BASENAME2%>" />
<li>foo = <%=I18nUtil.getLocalizedMessage(pageContext, "foo")%></li>
<li>bar = <%=I18nUtil.getLocalizedMessage(pageContext, "bar", new Object[]{ "Andy" })%></li>

<h3>Basename: "<%=BASENAME3%>"</h3>
<li>foo = <%=I18nUtil.getLocalizedMessage(pageContext, "foo", BASENAME3)%></li>
<li>bar = <%=I18nUtil.getLocalizedMessage(pageContext, "bar", new Object[]{ "Andy" }, BASENAME3)%></li>

<h3>Basename: "<%=BASENAME4%>"</h3>
<li>foo = <%=I18nUtil.getLocalizedMessage(pageContext, "foo", BASENAME4)%></li>
<li>bar = <%=I18nUtil.getLocalizedMessage(pageContext, "bar", new Object[]{ "Andy" }, BASENAME4)%></li>
