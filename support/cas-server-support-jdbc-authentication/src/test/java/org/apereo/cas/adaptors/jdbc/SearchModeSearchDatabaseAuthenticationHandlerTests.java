package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SearchModeSearchDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@SuppressWarnings("JDBCExecuteWithNonConstantString")
@Tag("JDBC")
public class SearchModeSearchDatabaseAuthenticationHandlerTests extends BaseDatabaseAuthenticationHandlerTests {
    private SearchModeSearchDatabaseAuthenticationHandler handler;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private static String getSqlInsertStatementToCreateUserAccount(final int i) {
        return String.format("insert into cassearchusers (username, password) values('%s', '%s');", "user" + i, "psw" + i);
    }

    @BeforeEach
    @SneakyThrows
    public void initialize() {
        this.handler = new SearchModeSearchDatabaseAuthenticationHandler(StringUtils.EMPTY, null, null, null, this.dataSource, "username", "password", "cassearchusers");

        try (val c = this.dataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);

                s.execute(getSqlInsertStatementToCreateUserAccount(0));
                for (var i = 0; i < 10; i++) {
                    s.execute(getSqlInsertStatementToCreateUserAccount(i));
                }
            }
        }
    }

    @AfterEach
    @SneakyThrows
    public void afterEachTest() {
        try (val c = this.dataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute("delete from casusers;");
            }
        }
    }

    @Test
    public void verifyNotFoundUser() {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("hello", "world");

        assertThrows(FailedLoginException.class, () -> handler.authenticate(c));
    }

    @Test
    @SneakyThrows
    public void verifyFoundUser() {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "psw3");
        assertNotNull(handler.authenticate(c));
    }

    @Test
    @SneakyThrows
    public void verifyMultipleUsersFound() {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0");
        assertNotNull(this.handler.authenticate(c));
    }

    @SuppressWarnings("unused")
    @Entity(name = "cassearchusers")
    public static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String username;

        private String password;
    }
}
