/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Zimlets
 * Copyright (C) 2005, 2006, 2007, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.taglib;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.zimlet.ZimletUserProperties;
import com.zimbra.soap.account.type.Prop;

public class Property extends ZimbraTag {

    private String mZimlet;
    private String mVar;
    private String mName;
    private String mAction;
    private String mValue;

    public void setZimlet(String zimlet) {
        mZimlet = zimlet;
    }

    public String getZimlet() {
        return mZimlet;
    }

    public void setVar(String val) {
        mVar = val;
    }

    public String getVar() {
        return mVar;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setAction(String action) {
        mAction = action;
    }

    public String getAction() {
        return mAction;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    private String doListProperty(Account acct, OperationContext octxt) throws ZimbraTagException, ServiceException {
        if (mVar == null) {
            throw ZimbraTagException.MISSING_ATTR("var");
        }
        ZimletUserProperties props = ZimletUserProperties.getProperties(acct);

        Map<String,String> m = new HashMap<String,String>();
        for (Prop zp: props.getAllProperties()) {
            if (zp.getZimlet().equals(mZimlet)) {
                m.put(zp.getName(), zp.getValue());
            }
        }
        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        req.setAttribute(mVar, m);
        return "";
    }

    private String doGetProperty(Account acct, OperationContext octxt) throws ZimbraTagException, ServiceException {
        if (mName == null) {
            throw ZimbraTagException.MISSING_ATTR("name");
        }
        ZimletUserProperties props = ZimletUserProperties.getProperties(acct);

        StringBuffer ret = new StringBuffer("undefined");
        for (Prop zp: props.getAllProperties()) {
            if (zp.getZimlet().equals(mZimlet) &&
                zp.getName().equals(mName)) {
                return zp.getValue();
            }
        }
        return ret.toString();
    }

    private String doSetProperty(Account acct, OperationContext octxt) throws ZimbraTagException, ServiceException {
        if (mName == null) {
            throw ZimbraTagException.MISSING_ATTR("name");
        }
        if (mValue == null) {
            throw ZimbraTagException.MISSING_ATTR("value");
        }
        ZimletUserProperties props = ZimletUserProperties.getProperties(acct);
        props.setProperty(mZimlet, mName, mValue);
        props.saveProperties(acct);
        return "";
    }

    public String getContentStart(Account acct, OperationContext octxt) throws ZimbraTagException, ServiceException {
        if (mZimlet == null) {
            throw ZimbraTagException.MISSING_ATTR("zimlet");
        }
        if (mAction != null && mAction.equals("set")) {
            return doSetProperty(acct, octxt);
        } else if (mAction != null && mAction.equals("list")) {
            return doListProperty(acct, octxt);
        }
        return doGetProperty(acct, octxt);
    }
}
