package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryJdbcAuthenticationProperties;
import org.apereo.cas.jpa.JpaPersistenceProviderContext;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is tests for {@link QueryDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Tag("JDBCAuthentication")
@Import(NamedQueryDatabaseAuthenticationHandlerTests.DatabaseTestConfiguration.class)
public class NamedQueryDatabaseAuthenticationHandlerTests extends BaseDatabaseAuthenticationHandlerTests {

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private static String getSqlInsertStatementToCreateUserAccount(final int i, final String expired, final String disabled) {
        return String.format("insert into cas_named_users (username, password, expired, disabled, phone) values('%s', '%s', '%s', '%s', '%s');",
            "user" + i, "psw" + i, expired, disabled, "123456789");
    }

    @BeforeEach
    public void initialize() throws Exception {
        try (val c = this.dataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute(getSqlInsertStatementToCreateUserAccount(0, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
            }
        }
    }

    @AfterEach
    public void afterEachTest() throws Exception {
        try (val c = this.dataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute("delete from cas_named_users;");
            }
        }
    }

    @Test
    public void verifySuccess() throws Exception {
        val sql = "SELECT * FROM CAS_NAMED_USERS where username=:username";
        val map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(List.of("phone:phoneNumber"));

        val properties = new QueryJdbcAuthenticationProperties().setSql(sql).setFieldPassword("password");
        properties.setName("namedHandler");
        val q = new QueryDatabaseAuthenticationHandler(properties,
            null, PrincipalFactoryUtils.newPrincipalFactory(),
            this.dataSource, CollectionUtils.wrap(map));
        val result = q.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"));
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertTrue(result.getPrincipal().getAttributes().containsKey("phoneNumber"));
    }

    @Test
    public void verifySuccessWithCount() throws Exception {
        val sql = "SELECT count(*) as total FROM CAS_NAMED_USERS where username=:username AND password=:password";
        val map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(List.of("phone:phoneNumber"));
        val properties = new QueryJdbcAuthenticationProperties().setSql(sql);
        properties.setName("namedHandler");
        val q = new QueryDatabaseAuthenticationHandler(properties,
            null, PrincipalFactoryUtils.newPrincipalFactory(),
            this.dataSource, CollectionUtils.wrap(map));
        val result = q.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"));
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertFalse(result.getPrincipal().getAttributes().containsKey("phoneNumber"));
    }

    @Test
    public void verifyFailsWithMissingTotalField() {
        val sql = "SELECT count(*) FROM CAS_NAMED_USERS where username=:username AND password=:password";
        val properties = new QueryJdbcAuthenticationProperties().setSql(sql).setFieldPassword("password");
        properties.setName("namedHandler");
        val q = new QueryDatabaseAuthenticationHandler(properties,
            null, PrincipalFactoryUtils.newPrincipalFactory(),
            this.dataSource, new HashMap<>());
        assertThrows(FailedLoginException.class,
            () -> q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("whatever", "psw0")));
    }

    @TestConfiguration("TestConfiguration")
    public static class DatabaseTestConfiguration {
        @Bean
        public JpaPersistenceProviderContext persistenceProviderContext() {
            return new JpaPersistenceProviderContext().setIncludeEntityClasses(Set.of(UsersTable.class.getName()));
        }
    }

    @Entity(name = "CAS_NAMED_USERS")
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
