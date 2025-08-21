package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.jdbc.authn.SearchJdbcAuthenticationProperties;
import org.apereo.cas.jpa.JpaPersistenceProviderContext;

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

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link SearchModeSearchDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@SuppressWarnings("JDBCExecuteWithNonConstantString")
@Tag("JDBCAuthentication")
@Import(SearchModeSearchDatabaseAuthenticationHandlerTests.DatabaseTestConfiguration.class)
class SearchModeSearchDatabaseAuthenticationHandlerTests extends BaseDatabaseAuthenticationHandlerTests {
    private SearchModeSearchDatabaseAuthenticationHandler handler;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private static String getSqlInsertStatementToCreateUserAccount(final int i) {
        return String.format("insert into cassearchusers (username, password) values('%s', '%s');", "user%d".formatted(i), "psw%d".formatted(i));
    }

    @BeforeEach
    void initialize() throws Exception {
        val props = new SearchJdbcAuthenticationProperties().setFieldUser("username")
            .setFieldPassword("password").setTableUsers("cassearchusers");
        this.handler = new SearchModeSearchDatabaseAuthenticationHandler(props,
            PrincipalFactoryUtils.newPrincipalFactory(), this.dataSource);

        try (val connection = this.dataSource.getConnection()) {
            try (val statement = connection.createStatement()) {
                connection.setAutoCommit(true);

                statement.execute(getSqlInsertStatementToCreateUserAccount(0));
                for (var i = 0; i < 10; i++) {
                    statement.execute(getSqlInsertStatementToCreateUserAccount(i));
                }
            }
        }
    }

    @AfterEach
    public void afterEachTest() throws Exception {
        try (val connection = this.dataSource.getConnection()) {
            try (val statement = connection.createStatement()) {
                connection.setAutoCommit(true);
                statement.execute("delete from cassearchusers;");
            }
        }
    }

    @Test
    void verifyNotFoundUser() {
        val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("hello", "world");
        assertThrows(FailedLoginException.class, () -> handler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyFoundUser() throws Throwable {
        val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "psw3");
        assertNotNull(handler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyMultipleUsersFound() throws Throwable {
        val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0");
        assertNotNull(this.handler.authenticate(credential, mock(Service.class)));
    }

    @TestConfiguration(value = "TestConfiguration", proxyBeanMethods = false)
    static class DatabaseTestConfiguration {
        @Bean
        public JpaPersistenceProviderContext persistenceProviderContext() {
            return new JpaPersistenceProviderContext().setIncludeEntityClasses(Set.of(SearchModeSearchDatabaseAuthenticationHandlerTests.UsersTable.class.getName()));
        }
    }

    @SuppressWarnings("unused")
    @Entity(name = "cassearchusers")
    static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String username;

        private String password;
    }
}
