package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

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
 * Tests for {@link SearchModeSearchDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@ContextConfiguration(locations = {"classpath:/jpaTestApplicationContext.xml"})
public class SearchModeSearchDatabaseAuthenticationHandlerTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private SearchModeSearchDatabaseAuthenticationHandler handler;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Before
    public void setUp() throws Exception {
        this.handler = new SearchModeSearchDatabaseAuthenticationHandler("", null, null, null, this.dataSource, "username", "password", "cassearchusers");

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
        return String.format("insert into cassearchusers (username, password) values('%s', '%s');", "user" + i, "psw" + i);
    }

    @Entity(name = "cassearchusers")
    public static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String username;
        private String password;
    }

    @Test
    public void verifyNotFoundUser() throws Exception {
        final UsernamePasswordCredential c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("hello", "world");

        this.thrown.expect(FailedLoginException.class);
        this.thrown.expectMessage("hello not found with SQL query.");

        this.handler.authenticate(c);
    }

    @Test
    public void verifyFoundUser() throws Exception {
        final UsernamePasswordCredential c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "psw3");
        assertNotNull(this.handler.authenticate(c));
    }

    @Test
    public void verifyMultipleUsersFound() throws Exception {
        final UsernamePasswordCredential c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0");
        assertNotNull(this.handler.authenticate(c));
    }
}
