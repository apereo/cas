package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.adaptors.jdbc.config.CasJdbcAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is {@link JdbcConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class, 
        CasCoreAuthenticationConfiguration.class,
        CasCoreWebConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreUtilConfiguration.class, 
        CasPersonDirectoryConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasJdbcAuthenticationConfiguration.class, 
        CasCoreServicesConfiguration.class})
@EnableScheduling
public class JdbcConfigurationTests {

    @Test
    public void verifyConfiguration() {
    }
}
