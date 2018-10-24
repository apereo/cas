package org.apereo.cas.config;

import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

/**
 * This is {@link CoreSamlConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
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
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Test
    public void verify() {
    }
}
