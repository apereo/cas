package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryJdbcAuthenticationProperties;
import org.apereo.cas.jpa.JpaPersistenceProviderContext;
import org.apereo.cas.util.RandomUtils;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is tests for {@link QueryDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@SuppressWarnings("JDBCExecuteWithNonConstantString")
@Tag("JDBCAuthentication")
@Import(QueryDatabaseAuthenticationHandlerTests.DatabaseTestConfiguration.class)
class QueryDatabaseAuthenticationHandlerTests extends BaseDatabaseAuthenticationHandlerTests {
    private static final String SQL = "SELECT * FROM casusers where username=?";

    private static final String PASSWORD_FIELD = "password";

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private static String getSqlInsertStatementToCreateUserAccount(final int i, final String expired, final String disabled) {
        return String.format("insert into casusers (username, password, expired, disabled, phone) values('%s', '%s', '%s', '%s', '%s');",
            "user%d".formatted(i), "psw%d".formatted(i), expired, disabled, "123456789");
    }

    @BeforeEach
    void initialize() throws Exception {
        try (val connection = dataSource.getConnection()) {
            try (val statement = connection.createStatement()) {
                connection.setAutoCommit(true);

                statement.execute(getSqlInsertStatementToCreateUserAccount(0, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
                for (var i = 0; i < 10; i++) {
                    statement.execute(getSqlInsertStatementToCreateUserAccount(i, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
                }
                statement.execute(getSqlInsertStatementToCreateUserAccount(20, Boolean.TRUE.toString(), Boolean.FALSE.toString()));
                statement.execute(getSqlInsertStatementToCreateUserAccount(21, Boolean.FALSE.toString(), Boolean.TRUE.toString()));
            }
        }
    }

    @AfterEach
    public void afterEachTest() throws Exception {
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
    void verifyAuthenticationFailsToFindUser() {
        val properties = new QueryJdbcAuthenticationProperties().setSql(SQL).setFieldPassword(PASSWORD_FIELD);
        val handler = new QueryDatabaseAuthenticationHandler(properties, PrincipalFactoryUtils.newPrincipalFactory(),
            this.dataSource);
        assertThrows(AccountNotFoundException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("usernotfound", "psw1"), mock(Service.class)));
    }

    @Test
    void verifyPasswordInvalid() {
        val properties = new QueryJdbcAuthenticationProperties().setSql(SQL).setFieldPassword(PASSWORD_FIELD);
        val handler = new QueryDatabaseAuthenticationHandler(properties, PrincipalFactoryUtils.newPrincipalFactory(),
            this.dataSource);
        assertThrows(FailedLoginException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user1", "psw11"), mock(Service.class)));
    }

    @Test
    void verifyMultipleRecords() {
        val properties = new QueryJdbcAuthenticationProperties().setSql(SQL).setFieldPassword(PASSWORD_FIELD);
        val handler = new QueryDatabaseAuthenticationHandler(properties, PrincipalFactoryUtils.newPrincipalFactory(),
            this.dataSource);
        assertThrows(FailedLoginException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"), mock(Service.class)));
    }

    @Test
    void verifyBadQuery() {
        val properties = new QueryJdbcAuthenticationProperties().setSql(SQL.replace("*", "error")).setFieldPassword(PASSWORD_FIELD);
        val handler = new QueryDatabaseAuthenticationHandler(properties, PrincipalFactoryUtils.newPrincipalFactory(),
            this.dataSource);
        assertThrows(PreventedException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"), mock(Service.class)));
    }

    @Test
    void verifySuccess() throws Throwable {
        val properties = new QueryJdbcAuthenticationProperties().setSql(SQL).setFieldPassword(PASSWORD_FIELD);
        properties.setPrincipalAttributeList(List.of("phone:phoneNumber"));
        val handler = new QueryDatabaseAuthenticationHandler(properties, PrincipalFactoryUtils.newPrincipalFactory(), dataSource);
        val result = handler.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "psw3"), mock(Service.class));
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertTrue(result.getPrincipal().getAttributes().containsKey("phoneNumber"));
    }

    @Test
    void verifyFindUserAndExpired() {
        val properties = new QueryJdbcAuthenticationProperties().setSql(SQL).setFieldPassword(PASSWORD_FIELD).setFieldExpired("expired");
        val handler = new QueryDatabaseAuthenticationHandler(properties, PrincipalFactoryUtils.newPrincipalFactory(),
            this.dataSource);
        assertThrows(AccountPasswordMustChangeException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user20", "psw20"), mock(Service.class)));
    }

    @Test
    void verifyFindUserAndDisabled() {
        val properties = new QueryJdbcAuthenticationProperties().setSql(SQL).setFieldPassword(PASSWORD_FIELD).setFieldDisabled("disabled");
        val handler = new QueryDatabaseAuthenticationHandler(properties, PrincipalFactoryUtils.newPrincipalFactory(), dataSource);
        assertThrows(AccountDisabledException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user21", "psw21"), mock(Service.class)));
    }

    /**
     * This test proves that in case BCRYPT is used authentication using encoded password always fail
     * with FailedLoginException
     */
    @Test
    void verifyBCryptFail() {
        val encoder = new BCryptPasswordEncoder(8, RandomUtils.getNativeInstance());
        val sql = SQL.replace("*", '\'' + encoder.encode("pswbc1") + "' password");
        val properties = new QueryJdbcAuthenticationProperties().setSql(sql).setFieldPassword(PASSWORD_FIELD);
        val handler = new QueryDatabaseAuthenticationHandler(properties, PrincipalFactoryUtils.newPrincipalFactory(), dataSource);
        handler.setPasswordEncoder(encoder);
        assertThrows(FailedLoginException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "pswbc1"), mock(Service.class)));
    }

    /**
     * This test proves that in case BCRYPT and
     * using raw password test can authenticate
     */
    @Test
    void verifyBCryptSuccess() throws Throwable {
        val encoder = new BCryptPasswordEncoder(6, RandomUtils.getNativeInstance());
        val sql = SQL.replace("*", '\'' + encoder.encode("pswbc2") + "' password");
        val properties = new QueryJdbcAuthenticationProperties().setSql(sql).setFieldPassword(PASSWORD_FIELD);
        val handler = new QueryDatabaseAuthenticationHandler(properties, PrincipalFactoryUtils.newPrincipalFactory(), dataSource);

        handler.setPasswordEncoder(encoder);
        assertNotNull(handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "pswbc2"), mock(Service.class)));
    }

    @TestConfiguration(value = "TestConfiguration", proxyBeanMethods = false)
    static class DatabaseTestConfiguration {
        @Bean
        public JpaPersistenceProviderContext persistenceProviderContext() {
            return new JpaPersistenceProviderContext().setIncludeEntityClasses(Set.of(QueryDatabaseAuthenticationHandlerTests.UsersTable.class.getName()));
        }
    }

    @SuppressWarnings("unused")
    @Entity(name = "casusers")
    static class UsersTable {
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
