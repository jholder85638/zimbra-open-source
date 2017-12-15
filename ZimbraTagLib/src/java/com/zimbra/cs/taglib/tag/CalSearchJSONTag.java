/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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
 */
package com.zimbra.cs.taglib.tag;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.soap.SoapTransport;
import com.zimbra.common.soap.ZimbraNamespace;
import com.zimbra.common.zclient.ZClientException;
import com.zimbra.cs.taglib.ZJspSession;
import com.zimbra.cs.taglib.tag.TagUtil.JsonDebugListener;
import com.zimbra.client.ZFolder;
import com.zimbra.client.ZMailbox;

public class CalSearchJSONTag extends ZimbraSimpleTag {

    private static final Pattern sSCRIPT = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);

    private String mVar;
    private ZAuthToken mAuthToken;
    private String mCsrfToken;
    private String mItemsPerPage;
    private String mTypes;
    private TimeZone mTimeZone;

    public void setVar(String var) { this.mVar = var; }
    public void setAuthtoken(ZAuthToken authToken) { this.mAuthToken = authToken; }
    public void setCsrftoken(String csrfToken) { this.mCsrfToken = csrfToken; }
    public void setItemsperpage(String itemsPerPage) { mItemsPerPage = itemsPerPage; }
    public void setTypes(String types) { mTypes = types; }
    public void setTimezone(TimeZone timezone) { mTimeZone = timezone; }

    public void doTag() throws JspException {
        try {
            JspContext ctxt = getJspContext();
            PageContext pageContext = (PageContext) ctxt;
            String url = ZJspSession.getSoapURL(pageContext);
            String remoteAddr = ZJspSession.getRemoteAddr(pageContext);
            Element e = getBootstrapCalSearchJSON(url, remoteAddr, mAuthToken, mCsrfToken, mItemsPerPage, mTypes);

			// Replace "</script>" with "</scr" + "ipt>" because html parsers recognize the close script tag.
			String json = e.toString();
			String json2 = sSCRIPT.matcher(json).replaceAll("</scr\"+\"ipt>");

			ctxt.setAttribute(mVar, json2,  PageContext.REQUEST_SCOPE);

        } catch (ServiceException e) {
            throw new JspTagException(e.getMessage(), e);
        }
    }

    /**
     * used when bootstrapping AJAX client.
     *
     * @param url url to connect to
     * @param authToken auth token to use
     * @param itemsPerPage number of search items to return
     * @param searchTypes what to search for
     * @return top-level JSON respsonse
     * @throws ServiceException on error
     */
    public static Element getBootstrapCalSearchJSON(String url, String remoteAddr, ZAuthToken authToken, String itemsPerPage, String searchTypes) throws ServiceException {
        return getBootstrapCalSearchJSON(url, remoteAddr, authToken, null, itemsPerPage, searchTypes);
    }

    /**
     * used when bootstrapping AJAX client.
     *
     * @param url url to connect to
     * @param authToken auth token to use
     * @param csrfToken csrf token
     * @param itemsPerPage number of search items to return
     * @param searchTypes what to search for
     * @return top-level JSON respsonse
     * @throws ServiceException on error
     */
    public static Element getBootstrapCalSearchJSON(String url, String remoteAddr, ZAuthToken authToken, String csrfToken, String itemsPerPage, String searchTypes) throws ServiceException {
        ZMailbox.Options options = new ZMailbox.Options(authToken, url);
        options.setNoSession(false);
        options.setAuthAuthToken(false);
        options.setClientIp(remoteAddr);

        ZMailbox mbox = ZMailbox.getMailbox(options);
        if (csrfToken != null) {
            mbox.initCsrfToken(csrfToken);
        }
        try {
            TimeZone tz = mbox.getPrefs().getTimeZone();

            Calendar currentDay = tz == null ? Calendar.getInstance() : Calendar.getInstance(tz);
            currentDay.setTimeInMillis(System.currentTimeMillis());
            currentDay.set(Calendar.HOUR_OF_DAY, 0);
            currentDay.set(Calendar.MINUTE, 0);
            currentDay.set(Calendar.SECOND, 0);
            currentDay.set(Calendar.MILLISECOND, 0);
            currentDay.set(Calendar.DAY_OF_MONTH, 1);
            
            StringBuilder sb = new StringBuilder();
            getCheckedCalendarFoldersRecursive(mbox.getUserRoot(), sb);
            String checkedCalendars = sb.toString();

            Calendar other = Calendar.getInstance(currentDay.getTimeZone());
            other.setTimeInMillis(currentDay.getTimeInMillis());
            //7 days for reminder search and 1 day for timezone difference
            other.add(Calendar.DAY_OF_MONTH, -8);

            long calStart = other.getTimeInMillis();

            other.setTimeInMillis(currentDay.getTimeInMillis());
            //no of days shown for minical
            other.add(Calendar.DAY_OF_MONTH, 42);

            long calEnd = other.getTimeInMillis();

            //BatchRequest
            Element batch = new Element.JSONElement(ZimbraNamespace.E_BATCH_REQUEST);

            //GetMiniCalRequest
            Element miniCalRequest = batch.addElement(MailConstants.GET_MINI_CAL_REQUEST);
            miniCalRequest.addAttribute(MailConstants.A_CAL_START_TIME, calStart);
            miniCalRequest.addAttribute(MailConstants.A_CAL_END_TIME, calEnd);

            String [] sArray = null;
            StringBuilder searchQuery = new StringBuilder();

            if (checkedCalendars!=null) {
                if(checkedCalendars.indexOf(",") == -1){
                    sArray = new String[]{checkedCalendars};
                }else{
                    sArray = checkedCalendars.split(",");
                }
                for(int i=0; i<sArray.length; i++) {
                    Element folder = miniCalRequest.addElement(MailConstants.E_FOLDER);
                    folder.addAttribute(MailConstants.A_ID, sArray[i]);
                    if (searchQuery.length() > 1) searchQuery.append(" or ");
                    searchQuery.append("inid:").append("\""+sArray[i]+"\"");
                }
            }

            //SearchRequest
            Element search = batch.addElement(MailConstants.SEARCH_REQUEST);
            if (itemsPerPage != null && itemsPerPage.length() > 0)
                search.addAttribute(MailConstants.A_QUERY_LIMIT, itemsPerPage);
            if (searchTypes != null && searchTypes.length() > 0)
                search.addAttribute(MailConstants.A_SEARCH_TYPES, searchTypes);
            search.addAttribute(MailConstants.A_CAL_EXPAND_INST_START, calStart);
            search.addAttribute(MailConstants.A_CAL_EXPAND_INST_END, calEnd);
            search.addAttribute(MailConstants.A_QUERY_OFFSET, 0);

            Element queryEl = search.addElement(MailConstants.E_QUERY);
            queryEl.setText(searchQuery.toString());

            JsonDebugListener debug = new JsonDebugListener();
            SoapTransport transport = TagUtil.newJsonTransport(url, remoteAddr, authToken, csrfToken, debug);
            transport.invoke(batch);

            Element e = debug.getEnvelope();

            //search params included in response
            Element responseParams = e.addElement(MailConstants.E_SEARCH);
            responseParams.addAttribute(MailConstants.A_CAL_START_TIME, calStart);
            responseParams.addAttribute(MailConstants.A_CAL_END_TIME, calEnd);
            Element queryElement = responseParams.addElement(MailConstants.A_QUERY);
            queryElement.setText(searchQuery.toString());
            responseParams.addAttribute(MailConstants.A_FOLDER, checkedCalendars);
            
            return e;

        } catch (IOException e) {
            throw ZClientException.IO_ERROR("invoke "+e.getMessage(), e);
        }
    }

    private static void getCheckedCalendarFoldersRecursive(ZFolder f, StringBuilder sb) {
        if (f.getDefaultView() == ZFolder.View.appointment && f.isCheckedInUI()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(f.getId());
        }
        for (ZFolder child : f.getSubFolders()) {
            getCheckedCalendarFoldersRecursive(child, sb);
        }
    }

}
