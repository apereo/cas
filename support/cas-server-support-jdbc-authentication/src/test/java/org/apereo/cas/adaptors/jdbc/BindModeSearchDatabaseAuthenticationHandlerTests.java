package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BindModeSearchDatabaseAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    DatabaseAuthenticationTestConfiguration.class
})
@TestPropertySource(properties = {
    "database.user=casuser",
    "database.name:cas-bindmode-authentications",
    "database.password=Mellon"})
@DirtiesContext
public class BindModeSearchDatabaseAuthenticationHandlerTests {
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Test
    public void verifyAction() throws Exception {
        val h = new BindModeSearchDatabaseAuthenticationHandler(null, mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, this.dataSource);
        assertNotNull(h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon")));
    }
}
