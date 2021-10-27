package org.jasig.cas.adaptors.jdbc;

import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

public class SearchModeSearchDatabaseAuthenticationHandlerTests {

    private SearchModeSearchDatabaseAuthenticationHandler handler;


    private DataSource dataSource;

    @Before
    public void setup() throws Exception {


        final ClassPathXmlApplicationContext ctx = new
            ClassPathXmlApplicationContext("classpath:/jpaTestApplicationContext.xml");
        this.dataSource = ctx.getBean("dataSource", DataSource.class);

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

    private static String getSqlInsertStatementToCreateUserAccount(final int i) {
        return String.format("insert into cassearchusers (username, password) values('%s', '%s');", "user" + i, "psw" + i);
    }

    @Entity(name="cassearchusers")
    public static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
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

