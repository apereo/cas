/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket.registry.support;

import java.sql.Timestamp;
import java.util.Calendar;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlRowSetResultSetExtractor;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

/**
 * Locking strategy that uses database storage for lock state that has the
 * following properties:
 * <ul>
 * <li><strong>Exclusivity</strong> - Only only client at a time may acquire the lock.</li>
 * <li><strong>Non-reentrant</strong> - An attempt to re-acquire the lock by the current
 * holder will fail.</li>
 * <li><strong>Lock expiration</strong> - Locks are acquired with an expiration such that
 * a request to acquire the lock after the expiration date will succeed even
 * if it is currently held by another client.</li>
 * </ul>
 * <p>
 * This class requires a backing database table to exist based on the
 * following template:
 * <pre>
 * CREATE TABLE LOCKS (
 *   APPLICATION_ID VARCHAR(50) NOT NULL,
 *   UNIQUE_ID VARCHAR(50) NULL,
 *   EXPIRATION_DATE TIMESTAMP NULL
 * );
 * ALTER TABLE LOCKS ADD CONSTRAINT LOCKS_PK
 * PRIMARY KEY (APPLICATION_ID);
 * </pre>
 * </p>
 * <p>
 * Note that table and column names can be controlled through instance
 * properties, but the create table script above is consistent with defaults.
 * </p>
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.3.6
 * @deprecated Replaced by {@link JpaLockingStrategy} as of 3.4.11.
 * @see JpaLockingStrategy
 *
 */
@Deprecated
public class JdbcLockingStrategy
    implements LockingStrategy, InitializingBean {

    /** Default lock timeout is 1 hour */
    public static final int DEFAULT_LOCK_TIMEOUT = 3600;

    /** Default database platform is SQL-92 */
    private static final DatabasePlatform DEFAULT_PLATFORM =
        DatabasePlatform.SQL92;

    /** Default locking table name is LOCKS */
    private static final String DEFAULT_TABLE_NAME = "LOCKS";

    /** Default unique identifier column name is UNIQUE_ID */
    private static final String UNIQUE_ID_COLUMN_NAME = "UNIQUE_ID";

    /** Default application identifier column name is APPLICATION_ID */
    private static final String APPLICATION_ID_COLUMN_NAME = "APPLICATION_ID";

    /** Default expiration date column name is EXPIRATION_DATE */
    private static final String EXPIRATION_DATE_COLUMN_NAME = "EXPIRATION_DATE";

    /** Database table name that stores locks */
    @NotNull
    private String tableName = DEFAULT_TABLE_NAME;

    /** Database column name that holds unique identifier */
    @NotNull
    private String uniqueIdColumnName = UNIQUE_ID_COLUMN_NAME;

    /** Database column name that holds application identifier */
    @NotNull
    private String applicationIdColumnName = APPLICATION_ID_COLUMN_NAME;

    /** Database column name that holds expiration date */
    @NotNull
    private String expirationDateColumnName = EXPIRATION_DATE_COLUMN_NAME;

    /** Unique identifier that identifies the client using this lock instance */
    @NotNull
    private String uniqueId;

    /**
     * Application identifier that identifies rows in the locking table,
     * each one of which may be for a different application or usage within
     * a single application.
     */
    @NotNull
    private String applicationId;

    /** Amount of time in seconds lock may be held */
    private int lockTimeout = DEFAULT_LOCK_TIMEOUT;

    /** JDBC data source */
    @NotNull
    private DataSource dataSource;

    /** Database platform */
    @NotNull
    private DatabasePlatform platform = DEFAULT_PLATFORM;

    /** Spring JDBC template used to execute SQL statements */
    private JdbcTemplate jdbcTemplate;

    /** SQL statement for selecting a lock */
    private String selectSql;

    /** SQL statement for creating a lock for a given application ID */
    private String createSql;

    /** SQL statement for updating a lock to acquired state */
    private String updateAcquireSql;

    /** SQL statement for updating a lock to released state */
    private String updateReleaseSql;


    /**
     * Supported database platforms provides support for platform-specific
     * behavior such as locking semantics.
     */
    public enum DatabasePlatform {
        /**
         * Any platform that supports the SQL-92 FOR UPDATE updatability clause
         * for SELECT queries and the related exclusive row locking
         * semantics it suggests.
         */
        SQL92,

        /** HSQLDB platform */
        HSQL,

        /** Microsoft SQL Server platform */
        SqlServer;
    }


    /**
     * @param  id  Identifier used to identify this instance in a row of the
     *             lock table.  Must be unique across all clients vying for
     *             locks for a given application ID.
     */
    public void setUniqueId(final String id) {
        this.uniqueId = id;
    }


    /**
     * @param  id  Application identifier that identifies a row in the lock
     *             table for which multiple clients vie to hold the lock.
     *             This must be the same for all clients contending for a
     *             particular lock.
     */
    public void setApplicationId(final String id) {
        this.applicationId = id;
    }


    /**
     * @param  seconds  Maximum amount of time in seconds lock may be held.
     */
    public void setLockTimeout(final int seconds) {
        this.lockTimeout = seconds;
    }


    /**
     * @param  name  Name of database table holding locks.
     */
    public void setTableName(final String name) {
        this.tableName = name;
    }


    /**
     * @param  name  Name of database column that stores application ID.
     */
    public void setApplicationIdColumnName(final String name) {
        this.applicationIdColumnName = name;
    }


    /**
     * @param  name  Name of database column that stores unique ID.
     */
    public void setUniqueIdColumnName(final String name) {
        this.uniqueIdColumnName = name;
    }


    /**
     * @param  name  Name of database column that stores lock expiration date.
     */
    public void setExpirationDateColumnName(final String name) {
        this.expirationDateColumnName = name;
    }


    /**
     * @param  dataSource  JDBC data source.
     */
    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }


    /**
     * @param  platform  Database platform that indicates when special syntax
     *                   is needed for database operations.
     */
    public void setPlatform(final DatabasePlatform platform) {
        this.platform = platform;
    }


    /** {@inheritDoc} */
    public void afterPropertiesSet() {
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.jdbcTemplate.afterPropertiesSet();
        this.createSql = String.format(
                "INSERT INTO %s (%s, %s, %s) VALUES(?, ?, ?)",
                this.tableName,
                this.applicationIdColumnName,
                this.uniqueIdColumnName,
                this.expirationDateColumnName);
        this.updateAcquireSql = String.format(
                "UPDATE %s SET %s=?, %s=? WHERE %s=?",
                this.tableName,
                this.uniqueIdColumnName,
                this.expirationDateColumnName,
                this.applicationIdColumnName);
        this.updateReleaseSql = String.format(
                "UPDATE %s SET %s=NULL, %s=NULL WHERE %s=? AND %s=?",
                this.tableName,
                this.uniqueIdColumnName,
                this.expirationDateColumnName,
                this.applicationIdColumnName,
                this.uniqueIdColumnName);

        // Support platform-specific syntax for select query
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT %s, %s FROM %s WHERE %s=?",
            this.uniqueIdColumnName,
            this.expirationDateColumnName,
            this.tableName,
            this.applicationIdColumnName));
        switch (this.platform) {
        case HSQL:
        case SqlServer:
            // Neither HSQL nor SQL Server support FOR UPDATE
            break;
        default:
            // SQL-92 compliant platforms support FOR UPDATE updatability clause
            sb.append(" FOR UPDATE");
            break;
        }
        this.selectSql = sb.toString();
    }


    /**
     * @see org.jasig.cas.ticket.registry.support.LockingStrategy#acquire()
     */
    @Transactional
    public boolean acquire() {
        boolean lockAcquired = false;
        if (this.platform == DatabasePlatform.SqlServer) {
           this.jdbcTemplate.execute("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE");
        }
        try {
	        final SqlRowSet rowSet = (SqlRowSet) this.jdbcTemplate.query(
	            this.selectSql,
	            new Object[] {this.applicationId},
	            new SqlRowSetResultSetExtractor());
	        final Timestamp expDate = getExpirationDate();
	        if (!rowSet.next()) {
	            // No row exists for this applicationId so create it.
	            // Row is created with uniqueId of this instance
	            // which indicates the lock is initially held by this instance.
	            this.jdbcTemplate.update(this.createSql, new Object[] {this.applicationId, this.uniqueId, expDate});
	            return true;
	        }
	        lockAcquired = canAcquire(rowSet);
	        if (lockAcquired) {
	            // Update unique ID of row to indicate this instance holds lock
	            this.jdbcTemplate.update(this.updateAcquireSql, new Object[] {this.uniqueId, expDate, this.applicationId});
	        }
        } finally {
            // Always attempt to revert current connection to default isolation
            // level on SQL Server
	        if (this.platform == DatabasePlatform.SqlServer) {
	           this.jdbcTemplate.execute("SET TRANSACTION ISOLATION LEVEL READ COMMITTED");
	        }
        }
        return lockAcquired;
    }


    /**
     * @see org.jasig.cas.ticket.registry.support.LockingStrategy#release()
     */
    @Transactional
    public void release() {
        // Update unique ID of row to indicate this instance holds lock
        this.jdbcTemplate.update(this.updateReleaseSql, new Object[] {this.applicationId, this.uniqueId});
    }


    /**
     * Determines whether this instance can acquire the lock.
     *
     * @param  lockRow  Row of lock data for this application ID.
     *
     * @return  True if lock can be acquired, false otherwise.
     */
    private boolean canAcquire(final SqlRowSet lockRow) {
        if (lockRow.getString(this.uniqueIdColumnName) != null) {
            final Calendar expCal = Calendar.getInstance();
            expCal.setTime(lockRow.getTimestamp(this.expirationDateColumnName));
            return Calendar.getInstance().after(expCal);
        }
        return true;
    }


    /**
     * @return  The expiration date for a lock acquired at the current system
     * time
     */
    private Timestamp getExpirationDate() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, this.lockTimeout);
        return new Timestamp(cal.getTimeInMillis());
    }
}
