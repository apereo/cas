package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is tests for {@link QueryDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Tag("JDBC")
public class NamedQueryDatabaseAuthenticationHandlerTests extends BaseDatabaseAuthenticationHandlerTests {

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private static String getSqlInsertStatementToCreateUserAccount(final int i, final String expired, final String disabled) {
        return String.format("insert into cas_named_users (username, password, expired, disabled, phone) values('%s', '%s', '%s', '%s', '%s');",
            "user" + i, "psw" + i, expired, disabled, "123456789");
    }

    @BeforeEach
    @SneakyThrows
    public void initialize() {
        try (val c = this.dataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute(getSqlInsertStatementToCreateUserAccount(0, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
            }
        }
    }

    @AfterEach
    @SneakyThrows
    public void afterEachTest() {
        try (val c = this.dataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute("delete from cas_named_users;");
            }
        }
    }

    @Test
    @SneakyThrows
    public void verifySuccess() {
        val sql = "SELECT * FROM cas_named_users where username=:username";
        val map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(List.of("phone:phoneNumber"));
        val q = new QueryDatabaseAuthenticationHandler("namedHandler",
            null, PrincipalFactoryUtils.newPrincipalFactory(), 0,
            this.dataSource, sql, "password",
            null, null,
            CollectionUtils.wrap(map));
        val result = q.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"));
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertTrue(result.getPrincipal().getAttributes().containsKey("phoneNumber"));
    }

    @Test
    @SneakyThrows
    public void verifySuccessWithCount() {
        val sql = "SELECT count(*) as total FROM cas_named_users where username=:username AND password=:password";
        val map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(List.of("phone:phoneNumber"));
        val q = new QueryDatabaseAuthenticationHandler("namedHandler",
            null, PrincipalFactoryUtils.newPrincipalFactory(), 0,
            this.dataSource, sql, null,
            null, null,
            CollectionUtils.wrap(map));
        val result = q.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"));
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertFalse(result.getPrincipal().getAttributes().containsKey("phoneNumber"));
    }

    @Test
    public void verifyFailsWithMissingTotalField() {
        val sql = "SELECT count(*) FROM cas_named_users where username=:username AND password=:password";
        val q = new QueryDatabaseAuthenticationHandler("namedHandler",
            null, PrincipalFactoryUtils.newPrincipalFactory(), 0,
            this.dataSource, sql, null,
            null, null,
            new LinkedHashMap<>());
        assertThrows(FailedLoginException.class,
            () -> q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("whatever", "psw0")));
    }

    @Entity(name = "cas_named_users")
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
