package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.PreventedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
            s.execute("delete from casusers;");
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

    @Test
    public void verifyAuthenticationFailsToFindUser() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);
        q.setSql(SQL);

        this.thrown.expect(AccountNotFoundException.class);
        this.thrown.expectMessage("usernotfound not found with SQL query");

        q.authenticateUsernamePasswordInternal(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("usernotfound", "psw1"), "psw1");
    }

    @Test
    public void verifyPasswordInvalid() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);
        q.setSql(SQL);

        this.thrown.expect(FailedLoginException.class);
        this.thrown.expectMessage("Password does not match value on record.");

        q.authenticateUsernamePasswordInternal(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user1", "psw11"), "psw11");
    }

    @Test
    public void verifyMultipleRecords() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);
        q.setSql(SQL);

        this.thrown.expect(FailedLoginException.class);
        this.thrown.expectMessage("Multiple records found for user0");

        q.authenticateUsernamePasswordInternal(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"), "psw0");
    }

    @Test
    public void verifyBadQuery() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);
        q.setSql(SQL.replace("password", "*"));

        this.thrown.expect(PreventedException.class);
        this.thrown.expectMessage("SQL exception while executing query for user0");

        q.authenticateUsernamePasswordInternal(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"), "psw0");
    }

    public void verifySuccess() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);
        q.setSql(SQL);
        assertNotNull(q.authenticateUsernamePasswordInternal(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "psw3")));
    }

    /**
     *  This test proves that in case BCRYPT is used authentication using encoded password always fail
     * with FailedLoginException
     * @throws Exception in case encoding fails
     */
    @Test
    public void verifyBCryptFail() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);

        final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(8, new SecureRandom("secret".getBytes(StandardCharsets.UTF_8)));

        q.setSql(SQL.replace("password", "'" + encoder.encode("pswbc1") +"' password"));

        q.setPasswordEncoder(encoder);

        this.thrown.expect(FailedLoginException.class);
        this.thrown.expectMessage("Multiple records found for user0");

        q.authenticateUsernamePasswordInternal(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "pswbc1"));
    }

    /**
     *  This test proves that in case BCRYPT and using raw password test can authenticate
     */
    @Test
    public void verifyBCryptSuccess() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler();
        q.setDataSource(this.dataSource);

        final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(6, new SecureRandom("secret2".getBytes(StandardCharsets.UTF_8)));

        q.setSql(SQL.replace("password", "'" + encoder.encode("pswbc2") +"' password"));

        q.setPasswordEncoder(encoder);
        assertNotNull(q.authenticateUsernamePasswordInternal(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "pswbc2"), "pswbc2"));
    }
}
