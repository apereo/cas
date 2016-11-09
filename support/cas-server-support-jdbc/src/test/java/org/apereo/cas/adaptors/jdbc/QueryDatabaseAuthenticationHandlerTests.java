package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * This is tests for {@link QueryDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@ContextConfiguration(locations = {"classpath:/jpaTestApplicationContext.xml"})
public class QueryDatabaseAuthenticationHandlerTests {

    private static final String SQL = "SELECT password FROM casusers where username=?";

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Before
    public void setUp() throws Exception {
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

    private static String getSqlInsertStatementToCreateUserAccount(final int i) {
        return String.format("insert into casusers (username, password) values('%s', '%s');", "user" + i, "psw" + i);
    }

    @Entity(name = "casusers")
    public static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String username;
        private String password;
    }

    @Test(expected = AccountNotFoundException.class)
    public void verifyAuthenticationFailsToFindUser() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);
        q.setSql(SQL);
        q.authenticateUsernamePasswordInternal(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("usernotfound", "psw1"), "psw1");

    }

    @Test(expected = FailedLoginException.class)
    public void verifyPasswordInvalid() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);
        q.setSql(SQL);
        q.authenticateUsernamePasswordInternal(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("user1", "psw11"), "psw11");

    }

    @Test(expected = FailedLoginException.class)
    public void verifyMultipleRecords() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);
        q.setSql(SQL);
        q.authenticateUsernamePasswordInternal(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"), "psw0");

    }

    @Test(expected = PreventedException.class)
    public void verifyBadQuery() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);
        q.setSql(SQL.replace("password", "*"));
        q.authenticateUsernamePasswordInternal(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"), "psw0");

    }

    public void verifySuccess() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);
        q.setSql(SQL);
        assertNotNull(q.authenticateUsernamePasswordInternal(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "psw3")));

    }

    /**
     *  This test proves that in case BCRYPT is used authentication using encoded password always fail
     * with FailedLoginException
     * @throws Exception
     */
    @Test(expected = FailedLoginException.class)
    public void verifyBCryptFail() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(8,
                new SecureRandom("secret".getBytes(StandardCharsets.UTF_8)));

        q.setSql(SQL.replace("password", "'" + encoder.encode("psw0") +"' password"));

        q.setPasswordEncoder(encoder);
        q.authenticateUsernamePasswordInternal(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"));
    }

    /**
     *  This test proves that in case BCRYPT and using raw password test can authenticate
     */
    @Test
    public void verifyBCryptSuccess() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(6,
                new SecureRandom("secret".getBytes(StandardCharsets.UTF_8)));

        q.setSql(SQL.replace("password", "'" + encoder.encode("psw0") +"' password"));

        q.setPasswordEncoder(encoder);
        q.authenticateUsernamePasswordInternal(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "psw0"),"psw0");
    }
}
