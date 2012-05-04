/*
 * Copyright 2009 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry.support;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.jasig.cas.ticket.registry.support.JdbcLockingStrategy.DatabasePlatform;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * Unit test for {@link JdbcLockingStrategy} class.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.3.6
 *
 */
public class JdbcLockingStrategyTests extends TestCase {
    /** DDL statement to create lock table */
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE LOCKS ("
        + "APPLICATION_ID VARCHAR(50) NOT NULL,"
        + "UNIQUE_ID VARCHAR(50) NULL,"
        + "EXPIRATION_DATE TIMESTAMP NULL)";

    /** DDL statement to create primary key on table */
    private static final String CREATE_PRI_KEY_SQL =
        "ALTER TABLE LOCKS ADD CONSTRAINT LOCKS_PK "
        + "PRIMARY KEY (APPLICATION_ID)";

    /** HSQL in-memory data source */
    private DataSource testDataSource;


    /**
     * @throws  Exception on test setup.
     */
    public void setUp() throws Exception {
        super.setUp();
        this.testDataSource = new SimpleDriverDataSource(
                new org.hsqldb.jdbcDriver(),
                "jdbc:hsqldb:mem:locktest",
                "sa",
                "");
        final JdbcTemplate tmpl = new JdbcTemplate(this.testDataSource);
        tmpl.execute(CREATE_TABLE_SQL);
        tmpl.execute(CREATE_PRI_KEY_SQL);
    }


    /**
     * Test method for {@link JdbcLockingStrategy#acquire()}.
     * @throws  Exception on test errors.
     */
    public void testAcquireAndRelease() throws Exception {
        final JdbcLockingStrategy lock1 = new JdbcLockingStrategy();
        final JdbcLockingStrategy lock2 = new JdbcLockingStrategy();
        lock1.setDataSource(this.testDataSource);
        lock2.setDataSource(this.testDataSource);
        lock1.setPlatform(DatabasePlatform.HSQL);
        lock2.setPlatform(DatabasePlatform.HSQL);
        lock1.setApplicationId("ticketregistry");
        lock2.setApplicationId("ticketregistry");
        lock1.setUniqueId("lock1");
        lock2.setUniqueId("lock2");
        lock1.setLockTimeout(2);
        lock2.setLockTimeout(2);
        lock1.afterPropertiesSet();
        lock2.afterPropertiesSet();

        // Ensure initial acquisition works
        assertTrue(lock1.acquire());
        assertFalse(lock2.acquire());

        // Ensure locks are not re-entrant
        assertFalse(lock1.acquire());

        // Ensure someone else can acquire the lock
        lock1.release();
        assertTrue(lock2.acquire());
        assertFalse(lock1.acquire());

        // Another re-entrant check
        assertFalse(lock2.acquire());

        // Lock expiration check
        // #2 has the lock currently
        // Allow it to time out, then #1 should succeed acquiring lock
        Thread.sleep(3000);
        assertTrue(lock1.acquire());
        assertFalse(lock2.acquire());

        lock1.release();
        lock2.release();
    }

}
