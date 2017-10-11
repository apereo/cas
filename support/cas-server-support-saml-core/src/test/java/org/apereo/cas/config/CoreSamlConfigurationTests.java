package org.apereo.cas.config;

import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is {@link CoreSamlConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {RefreshAutoConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasCoreWebConfiguration.class,
                CasWebApplicationServiceFactoryConfiguration.class,
                CasCoreServicesAuthenticationConfiguration.class,
                CasCoreAuthenticationPrincipalConfiguration.class,
                CasCoreAuthenticationPolicyConfiguration.class,
                CasCoreAuthenticationMetadataConfiguration.class,
                CasCoreAuthenticationSupportConfiguration.class,
                CasCoreAuthenticationHandlersConfiguration.class,
                CasCoreHttpConfiguration.class,
                CasCoreUtilConfiguration.class,
                CoreSamlConfiguration.class,
                CasCoreTicketCatalogConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasPersonDirectoryConfiguration.class,
                CasCoreServicesConfiguration.class})
@EnableScheduling
public class CoreSamlConfigurationTests {

    @Test
    public void verify() {}
}
