package org.apereo.cas.support.rest;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.support.rest.config.RestServicesConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestServicesConfigurationTests}.
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
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    RestServicesConfiguration.class
}, properties = {
    "cas.rest.attribute-name=attr-name",
    "cas.rest.attribute-value=attr-v"
})
@Tag("Simple")
public class RestServicesConfigurationTests {
    @Autowired
    @Qualifier("registeredServiceResourceRestController")
    private RegisteredServiceResource registeredServiceResourceRestController;

    @Test
    public void verifyOperation() {
        assertNotNull(registeredServiceResourceRestController);
    }
}
