/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.db.DbDataSource.DataSourceItem;
import com.zimbra.cs.mailbox.Mailbox;

public class MockDataSourceDbMapping extends DataSourceDbMapping {

    private Map<DataSource, Map<Integer, String>> items = new HashMap<DataSource, Map<Integer, String>>();
    private static final int FOLDER_ID = Mailbox.ID_FOLDER_TAGS; //expose this later if we want to work with other data sources

    private Map<Integer, String> getItemsForDataSource(DataSource ds) {
        Map<Integer, String> dsItems = items.get(ds); 
        if (dsItems == null) {
            dsItems = new HashMap<Integer, String>();
            items.put(ds, dsItems);
        }
        return dsItems;
    }

    @Override
    public void addMapping(DataSource ds, DataSourceItem item)
                    throws ServiceException {
        Map<Integer, String> dsItems = getItemsForDataSource(ds);
        assert(!dsItems.containsKey(item.itemId));
        dsItems.put(item.itemId, item.remoteId);
    }

    @Override
    public void deleteMapping(DataSource ds, int itemId)
                    throws ServiceException {
        Map<Integer, String> dsItems = getItemsForDataSource(ds);
        dsItems.remove(itemId);
    }

    @Override
    public DataSourceItem getMapping(DataSource ds, int itemId)
                    throws ServiceException {
        Map<Integer, String> dsItems = getItemsForDataSource(ds);
        return new DataSourceItem(FOLDER_ID, itemId,  dsItems.get(itemId), null);
    }

    @Override
    public DataSourceItem getReverseMapping(DataSource ds, String remoteId)
                    throws ServiceException {
        Map<Integer, String> dsItems = getItemsForDataSource(ds);
        for (Entry<Integer, String> e : dsItems.entrySet()) {
            if (e.getValue().equals(remoteId)) {
                return new DataSourceItem(FOLDER_ID, e.getKey(), e.getValue(), null);
            }
        }
        return new DataSourceItem(0, 0, null, null);
    }

    @Override
    public void updateMapping(DataSource ds, DataSourceItem item)
                    throws ServiceException {
        throw new UnsupportedOperationException();
    }

    public void clearData() {
        items = new HashMap<DataSource, Map<Integer, String>>();
    }

    @Override
    public Collection<DataSourceItem> getAllMappingsInFolder(DataSource ds, int folderId) throws ServiceException {
        Map<Integer, String> dsItems = getItemsForDataSource(ds);
        Collection<DataSourceItem> items = new ArrayList<DataSourceItem>();
        for (Entry<Integer, String> e : dsItems.entrySet()) {
            items.add(new DataSourceItem(FOLDER_ID, e.getKey(), e.getValue(), null));
        }
        return items;
    }

}
