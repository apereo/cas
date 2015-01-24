/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

package org.jasig.cas.adaptors.jdbc;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * Tests for {@link org.jasig.cas.adaptors.jdbc.SearchModeSearchDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/jpaTestApplicationContext.xml")
public class SearchModeSearchDatabaseAuthenticationHandlerTests {

    private SearchModeSearchDatabaseAuthenticationHandler handler;

    @Autowired
    private DataSource dataSource;

    @Before
    public void setup() throws Exception {
        this.handler = new SearchModeSearchDatabaseAuthenticationHandler();
        handler.setDataSource(this.dataSource);
        handler.setTableUsers("cassearchusers");
        handler.setFieldUser("username");
        handler.setFieldPassword("password");
        handler.afterPropertiesSet();

        final Connection c = this.dataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);

        s.execute(getSqlInsertStatementToCreateUserAccount(0));
        for (int i = 0; i < 10; i++) {
            s.execute(getSqlInsertStatementToCreateUserAccount(i));
        }

        c.close();
    }

    @After
    public void tearDown() throws Exception {
        final Connection c = this.dataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);

        for (int i = 0; i < 5; i++) {
            final String sql = String.format("delete from casusers;");
            s.execute(sql);
        }
        c.close();
    }

    private String getSqlInsertStatementToCreateUserAccount(final int i) {
        return String.format("insert into cassearchusers (username, password) values('%s', '%s');", "user" + i, "psw" + i);
    }

    @Entity(name="cassearchusers")
    public static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String username;
        private String password;
    }

    @Test(expected = FailedLoginException.class)
    public void verifyNotFoundUser() throws Exception {
        final UsernamePasswordCredential c = TestUtils.getCredentialsWithDifferentUsernameAndPassword("hello", "world");
        this.handler.authenticateUsernamePasswordInternal(c);
    }

    @Test
    public void verifyFoundUser() throws Exception {
        final UsernamePasswordCredential c = TestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "psw3");
        assertNotNull(this.handler.authenticateUsernamePasswordInternal(c));
    }

    @Test
    public void verifyMultipleUsersFound() throws Exception {
        final UsernamePasswordCredential c = TestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0");
        assertNotNull(this.handler.authenticateUsernamePasswordInternal(c));
    }

}

