package org.apereo.cas.config;

import org.apereo.cas.adaptors.fortress.config.FortressAuthenticationConfiguration;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FortressAuthenticationConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreConfiguration.class,
    FortressAuthenticationConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilConfiguration.class
})
@Tag("Simple")
public class FortressAuthenticationConfigurationTests {
    @Autowired
    @Qualifier("fortressAuthenticationHandler")
    private AuthenticationHandler fortressAuthenticationHandler;

    @Test
    public void verifyOperation() {
        assertNotNull(fortressAuthenticationHandler);
    }
}
