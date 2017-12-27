package org.apereo.cas;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasApplicationContextConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasFiltersConfiguration;
import org.apereo.cas.config.CasMetricsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPropertiesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasSecurityContextConfiguration;
import org.apereo.cas.config.CasWebAppConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logging.config.CasLoggingConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

/**
 * This is {@link WiringConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {CasApplicationContextConfiguration.class,
                CasFiltersConfiguration.class,
                CasMetricsConfiguration.class,
                CasPropertiesConfiguration.class,
                CasSecurityContextConfiguration.class,
                CasWebAppConfiguration.class,
                CasCoreWebflowConfiguration.class,
                CasWebflowContextConfiguration.class,
                CasCoreAuthenticationConfiguration.class, 
                CasCoreServicesAuthenticationConfiguration.class,
                CasCoreAuthenticationPrincipalConfiguration.class,
                CasCoreAuthenticationPolicyConfiguration.class,
                CasCoreAuthenticationMetadataConfiguration.class,
                CasCoreAuthenticationSupportConfiguration.class,
                CasCoreAuthenticationHandlersConfiguration.class,
                CasWebApplicationServiceFactoryConfiguration.class,
                CasCoreHttpConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreTicketCatalogConfiguration.class,
                CasThemesConfiguration.class,
                CasLoggingConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasCoreUtilConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasCookieConfiguration.class,
                CasCoreWebConfiguration.class,
                CasCoreValidationConfiguration.class,
                CasCoreConfiguration.class,
                CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
                CasCoreAuditConfiguration.class,
                CasPersonDirectoryConfiguration.class,
                ThymeleafAutoConfiguration.class,
                AopAutoConfiguration.class,
                RefreshAutoConfiguration.class
        })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@WebAppConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@TestPropertySource(properties = "spring.aop.proxy-target-class=true")
public class WiringConfigurationTests {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void verifyConfigurationClasses() {
        assertNotNull(this.applicationContext);
        assertTrue(this.applicationContext.getBeanDefinitionCount() > 0);
    }
}
