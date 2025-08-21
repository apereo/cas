package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.jdbc.authn.BindJdbcAuthenticationProperties;
import org.apereo.cas.jpa.JpaPersistenceProviderContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BindModeSearchDatabaseAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "database.user=casuser",
    "database.name:cas-bindmode-authentications",
    "database.password=Mellon"
})
@Tag("JDBCAuthentication")
@Import(BindModeSearchDatabaseAuthenticationHandlerTests.DatabaseTestConfiguration.class)
class BindModeSearchDatabaseAuthenticationHandlerTests extends BaseDatabaseAuthenticationHandlerTests {
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Test
    void verifyAction() throws Throwable {
        val h = new BindModeSearchDatabaseAuthenticationHandler(new BindJdbcAuthenticationProperties(),
            PrincipalFactoryUtils.newPrincipalFactory(), this.dataSource);
        assertNotNull(h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"), mock(Service.class)));
    }

    @Test
    void verifyInvalidAction() {
        val h = new BindModeSearchDatabaseAuthenticationHandler(new BindJdbcAuthenticationProperties(),
            PrincipalFactoryUtils.newPrincipalFactory(), this.dataSource);
        assertThrows(FailedLoginException.class,
            () -> h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("unknown", "Mellon"), mock(Service.class)));
    }

    @TestConfiguration(value = "TestConfiguration", proxyBeanMethods = false)
    static class DatabaseTestConfiguration {
        @Bean
        public JpaPersistenceProviderContext persistenceProviderContext() {
            return new JpaPersistenceProviderContext();
        }
    }
}
