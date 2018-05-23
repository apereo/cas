package org.apereo.cas.web.report;

import org.apache.commons.io.input.TailerListener;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreViewsConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDiscoveryProfileConfiguration;
import org.apereo.cas.config.CasMetricsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.support.events.config.CasCoreEventsConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.report.config.CasReportsConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is {@link LoggingOutputTailingServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreValidationConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasValidationConfiguration.class,
    CasCoreViewsConfiguration.class,
    CasProtocolViewsConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class,
    ThymeleafAutoConfiguration.class,
    ServerPropertiesAutoConfiguration.class,
    EndpointAutoConfiguration.class,
    CasDiscoveryProfileConfiguration.class,
    CasCoreEventsConfiguration.class,
    MultifactorAuthnTrustConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasMetricsConfiguration.class,
    CasReportsConfiguration.class,
    RefreshAutoConfiguration.class
})
public class LoggingOutputTailingServiceTests {
    @Autowired
    @Qualifier("loggingOutputTailingService")
    private TailerListener loggingOutputTailingService;

    @Test
    public void verifyOperation() {
        try {
            loggingOutputTailingService.handle("This is a message");
            loggingOutputTailingService.handle(new RuntimeException("This is an error"));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
