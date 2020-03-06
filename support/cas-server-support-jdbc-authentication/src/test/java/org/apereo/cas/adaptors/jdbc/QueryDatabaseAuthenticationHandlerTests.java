package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is tests for {@link QueryDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@SuppressWarnings("JDBCExecuteWithNonConstantString")
@Tag("JDBC")
public class QueryDatabaseAuthenticationHandlerTests extends BaseDatabaseAuthenticationHandlerTests {
    private static final String SQL = "SELECT * FROM casusers where username=?";

    private static final String PASSWORD_FIELD = "password";

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private static String getSqlInsertStatementToCreateUserAccount(final int i, final String expired, final String disabled) {
        return String.format("insert into casusers (username, password, expired, disabled, phone) values('%s', '%s', '%s', '%s', '%s');",
            "user" + i, "psw" + i, expired, disabled, "123456789");
    }

    @BeforeEach
    @SneakyThrows
    public void initialize() {
        try (val c = this.dataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);

                s.execute(getSqlInsertStatementToCreateUserAccount(0, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
                for (var i = 0; i < 10; i++) {
                    s.execute(getSqlInsertStatementToCreateUserAccount(i, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
                }
                s.execute(getSqlInsertStatementToCreateUserAccount(20, Boolean.TRUE.toString(), Boolean.FALSE.toString()));
                s.execute(getSqlInsertStatementToCreateUserAccount(21, Boolean.FALSE.toString(), Boolean.TRUE.toString()));
            }
        }
    }

    @AfterEach
    @SneakyThrows
    public void afterEachTest() {
        try (val c = this.dataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);

                for (var i = 0; i < 5; i++) {
                    s.execute("delete from casusers;");
                }
            }
        }
    }

    @Test
    public void verifyAuthenticationFailsToFindUser() {
        val q = new QueryDatabaseAuthenticationHandler(StringUtils.EMPTY, null, null, null, this.dataSource, SQL, PASSWORD_FIELD, null,
            null, new HashMap<>(0));
        assertThrows(AccountNotFoundException.class,
            () -> q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("usernotfound", "psw1")));
    }

    @Test
    public void verifyPasswordInvalid() {
        val q = new QueryDatabaseAuthenticationHandler(StringUtils.EMPTY, null, null, null, this.dataSource, SQL, PASSWORD_FIELD,
            null, null, new HashMap<>(0));
        assertThrows(FailedLoginException.class,
            () -> q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user1", "psw11")));
    }

    @Test
    public void verifyMultipleRecords() {
        val q = new QueryDatabaseAuthenticationHandler(StringUtils.EMPTY, null, null, null, this.dataSource, SQL, PASSWORD_FIELD,
            null, null, new HashMap<>(0));
        assertThrows(FailedLoginException.class,
            () -> q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0")));
    }

    @Test
    public void verifyBadQuery() {
        val q = new QueryDatabaseAuthenticationHandler(StringUtils.EMPTY, null, null, null, this.dataSource, SQL.replace("*", "error"),
            PASSWORD_FIELD, null, null, new HashMap<>(0));
        assertThrows(PreventedException.class,
            () -> q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0")));
    }

    @Test
    @SneakyThrows
    public void verifySuccess() {
        val map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(List.of("phone:phoneNumber"));
        val q = new QueryDatabaseAuthenticationHandler(StringUtils.EMPTY, null, null, null,
            this.dataSource, SQL, PASSWORD_FIELD,
            null, null,
            CollectionUtils.wrap(map));
        val result = q.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "psw3"));
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertTrue(result.getPrincipal().getAttributes().containsKey("phoneNumber"));
    }

    @Test
    public void verifyFindUserAndExpired() {
        val q = new QueryDatabaseAuthenticationHandler(StringUtils.EMPTY, null, null, null, this.dataSource, SQL, PASSWORD_FIELD,
            "expired", null, new HashMap<>(0));
        assertThrows(AccountPasswordMustChangeException.class,
            () -> q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user20", "psw20")));
    }

    @Test
    public void verifyFindUserAndDisabled() {
        val q = new QueryDatabaseAuthenticationHandler(StringUtils.EMPTY, null, null, null, this.dataSource, SQL, PASSWORD_FIELD,
            null, "disabled", new HashMap<>(0));
        assertThrows(AccountDisabledException.class,
            () -> q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user21", "psw21")));
    }

    /**
     * This test proves that in case BCRYPT is used authentication using encoded password always fail
     * with FailedLoginException
     */
    @Test
    public void verifyBCryptFail() {
        val encoder = new BCryptPasswordEncoder(8, RandomUtils.getNativeInstance());
        val sql = SQL.replace("*", '\'' + encoder.encode("pswbc1") + "' password");
        val q = new QueryDatabaseAuthenticationHandler(StringUtils.EMPTY, null, null, null, this.dataSource, sql, PASSWORD_FIELD,
            null, null, new HashMap<>(0));
        q.setPasswordEncoder(encoder);
        assertThrows(FailedLoginException.class,
            () -> q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "pswbc1")));
    }

    /**
     * This test proves that in case BCRYPT and
     * using raw password test can authenticate
     */
    @Test
    @SneakyThrows
    public void verifyBCryptSuccess() {
        val encoder = new BCryptPasswordEncoder(6, RandomUtils.getNativeInstance());
        val sql = SQL.replace("*", '\'' + encoder.encode("pswbc2") + "' password");
        val q = new QueryDatabaseAuthenticationHandler(StringUtils.EMPTY, null, null, null, this.dataSource, sql, PASSWORD_FIELD,
            null, null, new HashMap<>(0));

        q.setPasswordEncoder(encoder);
        assertNotNull(q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "pswbc2")));
    }

    @SuppressWarnings("unused")
    @Entity(name = "casusers")
    public static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column
        private String username;

        @Column
        private String password;

        @Column
        private String expired;

        @Column
        private String disabled;

        @Column
        private String phone;
    }
}
