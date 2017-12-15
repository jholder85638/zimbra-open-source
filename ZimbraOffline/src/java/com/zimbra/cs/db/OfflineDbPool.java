/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.db.Db.Capability;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.db.DbPool.PoolConfig;


/**
 * Connection pool for Offline DB operations. Assumes DbPool has been started prior to first access. Application must call shutdown() to exit cleanly.
 *
 */
public class OfflineDbPool {

    private static OfflineDbPool instance;

    public static synchronized OfflineDbPool getInstance() {
        if (instance == null) {
            instance = new OfflineDbPool();
        }
        return instance;
    }

    private PoolingDataSource mDataSource;
    private GenericObjectPool mConnectionPool;

    private OfflineDbPool() {
        initPool();
    }

    public DbConnection getConnection() throws ServiceException {
        Connection dbconn = null;
        DbConnection conn = null;
        try {
            dbconn = mDataSource.getConnection();
            if (dbconn.getAutoCommit() != false) {
                dbconn.setAutoCommit(false);
            }
            conn = new DbConnection(dbconn);
            Db.getInstance().postOpen(conn);
        } catch (SQLException e) {
            try {
                if (dbconn != null && !dbconn.isClosed()) {
                    dbconn.close();
                }
            } catch (SQLException e2) {
                ZimbraLog.sqltrace.warn("DB connection close caught exception", e);
            }
            throw ServiceException.FAILURE("getting database connection", e);
        }
        return conn;
    }

    private void initPool() {
        PoolConfig pconfig = Db.getInstance().getPoolConfig();
        int poolSize = Db.supports(Capability.ROW_LEVEL_LOCKING) ? pconfig.mPoolSize : 1;
        //if no row lock, we need serial access to the underlying db file
        //still need external synchronization for now since other connections to zimbra.db can occur through DbPool
        mConnectionPool = new GenericObjectPool(null, poolSize, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, -1, poolSize);
        ConnectionFactory cfac = ZimbraConnectionFactory.getConnectionFactory(pconfig);
        boolean defAutoCommit = false, defReadOnly = false;
        new PoolableConnectionFactory(cfac, mConnectionPool, null, null, defReadOnly, defAutoCommit);
        try {
            PoolingDataSource pds = new PoolingDataSource(mConnectionPool);
            pds.setAccessToUnderlyingConnectionAllowed(true);
            Db.getInstance().startup(pds, pconfig.mPoolSize);
            mDataSource = pds;
        } catch (SQLException e) {
            ZimbraLog.system.error("failed to initialize offline db pool", e);
        }
    }

    public void closeResults(ResultSet rs) throws ServiceException {
        DbPool.closeResults(rs);
    }

    public void closeStatement(PreparedStatement stmt) throws ServiceException {
        DbPool.closeStatement(stmt);
    }

    public void quietClose(DbConnection conn) {
        DbPool.quietClose(conn);
    }

    public void close() throws Exception {
        if (mConnectionPool != null) {
            mConnectionPool.close();
            mConnectionPool = null;
        }
        mDataSource = null;
    }

    public static synchronized void shutdown() throws Exception {
        getInstance().close();
    }
}
