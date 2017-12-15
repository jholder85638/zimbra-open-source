/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2012, 2013, 2014, 2016 Synacor, Inc.
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

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.NamedElement;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Fix Calendar priority
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_FIX_CALENDAR_PRIORITY_REQUEST)
public class FixCalendarPriorityRequest {

    /**
     * @zm-api-field-tag sync
     * @zm-api-field-description Sync flag
     * <table>
     * <tr> <td> <b>1 (true)</b> </td> <td> command blocks until processing finishes </td> </tr>
     * <tr> <td> <b>0 (false) [default]</b> </td> <td> command returns right away </td> </tr>
     * </table>
     */
    @XmlAttribute(name=AdminConstants.A_TZFIXUP_SYNC, required=false)
    private final ZmBoolean sync;

    /**
     * @zm-api-field-description Accounts
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT, required=false)
    private List<NamedElement> accounts = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private FixCalendarPriorityRequest() {
        this((Boolean) null);
    }

    public FixCalendarPriorityRequest(Boolean sync) {
        this.sync = ZmBoolean.fromBool(sync);
    }

    public void setAccounts(Iterable <NamedElement> accounts) {
        this.accounts.clear();
        if (accounts != null) {
            Iterables.addAll(this.accounts,accounts);
        }
    }

    public FixCalendarPriorityRequest addAccount(NamedElement account) {
        this.accounts.add(account);
        return this;
    }

    public Boolean getSync() { return ZmBoolean.toBool(sync); }
    public List<NamedElement> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }
}