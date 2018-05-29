package org.apereo.cas.adaptors.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.ServicesManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BindModeSearchDatabaseAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    DatabaseAuthenticationTestConfiguration.class
})
@Slf4j
@TestPropertySource(properties = {
    "database.user=casuser",
    "database.name:cas-bindmode-authentications",
    "database.password=Mellon"})
@DirtiesContext
public class BindModeSearchDatabaseAuthenticationHandlerTests {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Test
    public void verifyAction() throws Exception {
        final var h = new BindModeSearchDatabaseAuthenticationHandler(null, mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, this.dataSource);
        assertNotNull(h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon")));
    }
}
