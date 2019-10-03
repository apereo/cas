package org.apereo.cas.config;

import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is {@link CoreSamlConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class,
    CoreSamlConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreServicesConfiguration.class
})
@EnableScheduling
@Tag("SAML")
public class CoreSamlConfigurationTests {
    @Test
    public void verify() {
    }
}
