/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.offline.jsp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import com.zimbra.common.net.SSLCertInfo;
import com.zimbra.common.service.RemoteServiceException;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.offline.jsp.JspConstants.JspVerb;

public abstract class FormBean extends PageBean {

    protected JspVerb verb;
    
    private String error;
    
    private String stackTrace;

    private Set<String> invalids = new HashSet<String>();
    
    private SSLCertInfo sslCertInfo;
    
    protected String sslCertAlias;
    
    public FormBean() {}
    
    public void setVerb(String strVerb) {
        verb = strVerb != null ? JspVerb.fromString(strVerb) : null;
    }
    
    public boolean isNoVerb() {
        return verb == null;
    }
    
    public boolean isAdd() {
        return verb != null && verb.isAdd();
    }
    
    public boolean isDelete() {
        return verb != null && verb.isDelete();
    }       
    
    public boolean isExport() {
        return verb != null && verb.isExport();
    }       
    
    public boolean isImport() {
        return verb != null && verb.isImport();
    }
    
    public boolean isModify() {
        return verb != null && verb.isModify();
    }
    
    public boolean isReset() {
        return verb != null && verb.isReset();
    }    
    
    protected void setError(String error) {
        String failprefix = "system failure:";
        this.error = this.error == null ? (error == null ? getMessage("UnknownError") : (error.startsWith(failprefix) ? error.substring(failprefix.length()) : error)) : this.error;
    }
    
    protected void setExceptionError(ServiceException ex) {
        String exMsg, exCode;        
        if (this.error != null || (exMsg = ex.getMessage()) == null || (exCode = ex.getCode()) == null)
            return;        
        String msg = getMessage("exception." + exCode, false);
        if (msg == null)
            error = exCode + ": " + exMsg;
        else if(msg.indexOf("{0}") >= 0)
            error = MessageFormat.format(msg, exMsg);
        else
            error = msg;
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw, true));
        stackTrace = sw.toString();
        if (exCode.equals(RemoteServiceException.SSLCERT_ERROR) || exCode.equals(RemoteServiceException.SSLCERT_MISMATCH)) {
            if (exMsg.length() > 0)
                sslCertInfo = SSLCertInfo.deserialize(exMsg);
        }
    }

    public SSLCertInfo getSslCertInfo() {
        return sslCertInfo;
    }
    
    public void setSslCertAlias(String sslCertAlias) {
        this.sslCertAlias = sslCertAlias;
    }
    
    public String getError() {
        return error;
    }

    public String getStackTrace() {
        return stackTrace;
    }
        
    public String getVerb() {
        return verb == null ? "" : verb.toString();
    }
        
    protected void addInvalid(String name) {
        invalids.add(name);
    }
    
    public boolean isAllValid() {
        return invalids.isEmpty();
    }
    
    public boolean isAllOK() {
        return isAllValid() && getError() == null;
    }
    
    protected boolean isValid(String name) {
        return !invalids.contains(name);
    }
    
    protected abstract void reload();

    protected abstract void doRequest();
    
    public static boolean isValid(FormBean formBean, String name) {
        return formBean.isValid(name);
    }
    
    public static void reload(FormBean formBean) {
        formBean.reload();
    }
    
    public static void doRequest(FormBean formBean) {
        formBean.doRequest();
    }
    
    protected String require(String input) {
        input = input == null ? null : input.trim();
        if (isEmpty(input))
            error = getMessage("MissingRequired");
        return input;
    }
    
    protected String optional(String input) {
        return input == null ? "" : input.trim();
    }
    
    protected boolean isEmpty(String input) {
        return input == null || input.trim().length() == 0;
    }
    
    protected boolean isValidNumber(String input) {
        if (isEmpty(input))
            return false;
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException x) {
            return false;
        }
    }

    protected boolean isValidSyncFixedDate(String input) {
        if (isEmpty(input))
            return false;

        if (input.indexOf('/') > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date testDate = null;
            try {
              testDate = sdf.parse(input);
            } catch (ParseException e) {
              return false;
            }
            if (!sdf.format(testDate).equals(input)) {
              return false;
            }
        } else {
                return false;
            }
        return true;
    }

    protected boolean isValidSyncRelativeDate(String input) {
        if (isEmpty(input))
            return false;

        try {
            int relativedate = Integer.parseInt(input);
            if (relativedate < 0)
                return false;
        } catch (NumberFormatException x) {
            return false;
        }
        return true;
    }

    protected boolean isValidSyncEmailDate(String input) {
        if (isEmpty(input))
            return false;

        try {
            int relativedate = Integer.parseInt(input);
            if (relativedate < 0 || relativedate > 2)
                return false;
        } catch (NumberFormatException x) {
            return false;
        }
        return true;
    }

    protected boolean isValidPort(String input) {
        if (isEmpty(input))
            return false;
        try {
            int port = Integer.parseInt(input);
            if (port <= 0 || port > 65535)
                return false;
            return true;
        } catch (NumberFormatException x) {
            return false;
        }
    }

    protected boolean isValidHost(String input) {
        return !isEmpty(input) && input.indexOf(':') < 0 && input.indexOf('/') < 0;
    }
    
    protected boolean isValidEmail(String input) {
        if (isEmpty(input))
            return false;
        int at = input.indexOf('@');
        if (at > 0 && at < input.length() - 1)
            return true;
        return false;
    }
}