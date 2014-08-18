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

package org.jasig.cas.adaptors.jdbc;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.PreventedException;
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
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * @author Misagh Moayyed
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/jpaTestApplicationContext.xml")
public class QueryAndEncodeDatabaseAuthenticationHandlerTests {

    private static final String SQL = "SELECT * FROM users where %s";

    @Autowired
    private DataSource dataSource;

    @Before
    public void setup() throws Exception {
        final Connection c = this.dataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);

        s.execute(getSqlInsertStatementToCreateUserAccount(0));
        for (int i = 0; i < 10; i++) {
            s.execute(getSqlInsertStatementToCreateUserAccount(i));
        }

        c.close();
    }

    private String getSqlInsertStatementToCreateUserAccount(final int i) {
        final String sql = String.format(
                "insert into users (username, password, salt, numIterations) values('%s', '%s', '%s', %s);",
                "user" + i, "password" + i, "salt" + i, i);
        return sql;
    }

    @After
    public void tearDown() throws Exception {
        final Connection c = this.dataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);

        for (int i = 0; i < 5; i++) {
            final String sql = String.format("delete from users;");
            s.execute(sql);
        }
        c.close();
    }

    @Test(expected = AccountNotFoundException.class)
    public void testAuthenticationFailsToFindUser() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q =
                new QueryAndEncodeDatabaseAuthenticationHandler(this.dataSource, buildSql(),
                        MessageDigestAlgorithms.SHA_512);
        q.authenticateUsernamePasswordInternal(TestUtils.getCredentialsWithSameUsernameAndPassword());

    }

    @Test(expected = PreventedException.class)
    public void testAuthenticationInvalidSql() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q =
                new QueryAndEncodeDatabaseAuthenticationHandler(this.dataSource, buildSql("makesNoSenseInSql"),
                        MessageDigestAlgorithms.SHA_512);
        q.authenticateUsernamePasswordInternal(TestUtils.getCredentialsWithSameUsernameAndPassword());

    }

    @Test(expected = FailedLoginException.class)
    public void testAuthenticationMultipleAccounts() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q =
                new QueryAndEncodeDatabaseAuthenticationHandler(this.dataSource, buildSql(),
                        MessageDigestAlgorithms.SHA_512);
        q.authenticateUsernamePasswordInternal(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "someotherpass"));

    }

    private String buildSql(final String where) {
        return String.format(SQL, where);
    }

    private String buildSql() {
        return String.format(SQL, "username=?;");
    }

    @Entity(name="users")
    public class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String username;
        private String password;
        private String salt;
        private long numIterations;
    }
}
