package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.ServicesManager;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

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
    @SneakyThrows
    public void verifyAction() {
        val h = new BindModeSearchDatabaseAuthenticationHandler(null, mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, this.dataSource);
        assertNotNull(h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon")));
    }
}
