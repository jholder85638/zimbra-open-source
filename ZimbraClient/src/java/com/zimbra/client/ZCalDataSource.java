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
package com.zimbra.client;

import org.json.JSONException;

import com.zimbra.soap.admin.type.DataSourceType;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.SystemUtil;
import com.zimbra.soap.type.CalDataSource;
import com.zimbra.soap.type.DataSources;

public class ZCalDataSource implements ZDataSource, ToZJSONObject {

    private CalDataSource data;
    
    public ZCalDataSource(String name, String folderId, boolean enabled) {
        data = DataSources.newCalDataSource();
        data.setName(name);
        data.setFolderId(folderId);
        data.setEnabled(enabled);
    }
    
    public ZCalDataSource(CalDataSource data) {
        this.data = DataSources.newCalDataSource(data);
    }
    
    public String getId() {
        return data.getId();
    }

    public String getName() {
        return data.getName();
    }

    public DataSourceType getType() {
        return DataSourceType.cal;
    }
    
    public String getFolderId() {
        return data.getFolderId();
    }
    
    public boolean isEnabled() {
        return SystemUtil.coalesce(data.isEnabled(), Boolean.FALSE);
    }

    public Element toElement(Element parent) {
        Element src = parent.addElement(MailConstants.E_DS_CAL);
        src.addAttribute(MailConstants.A_ID, data.getId());
        src.addAttribute(MailConstants.A_NAME, data.getName());
        src.addAttribute(MailConstants.A_DS_IS_ENABLED, data.isEnabled());
        src.addAttribute(MailConstants.A_FOLDER, data.getFolderId());
        return src;
    }

    public Element toIdElement(Element parent) {
        Element src = parent.addElement(MailConstants.E_DS_CAL);
        src.addAttribute(MailConstants.A_ID, getId());
        return src;
    }

    public ZJSONObject toZJSONObject() throws JSONException {
        ZJSONObject zjo = new ZJSONObject();
        zjo.put("id", data.getId());
        zjo.put("name", data.getName());
        zjo.put("enabled", data.isEnabled());
        zjo.put("folderId", data.getFolderId());
        return zjo;
    }
}
