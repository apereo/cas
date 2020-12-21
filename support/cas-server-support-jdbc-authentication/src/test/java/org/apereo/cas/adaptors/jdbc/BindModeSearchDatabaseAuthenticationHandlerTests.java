package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Tag("JDBC")
public class BindModeSearchDatabaseAuthenticationHandlerTests extends BaseDatabaseAuthenticationHandlerTests {
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Test
    public void verifyAction() throws Exception {
        val h = new BindModeSearchDatabaseAuthenticationHandler(null, mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, this.dataSource);
        assertNotNull(h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon")));
    }

    @Test
    public void verifyInvalidAction() {
        val h = new BindModeSearchDatabaseAuthenticationHandler(null, mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, this.dataSource);
        assertThrows(FailedLoginException.class,
            () -> h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("unknown", "Mellon")));
    }
}
