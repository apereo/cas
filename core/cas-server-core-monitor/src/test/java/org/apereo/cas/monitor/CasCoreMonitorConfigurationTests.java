package org.apereo.cas.monitor;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.config.CasCoreMonitorConfiguration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreMonitorConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreMonitorConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    MetricsAutoConfiguration.class,
    SimpleMetricsExportAutoConfiguration.class,
    MetricsEndpointAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class
}, properties = {
    "management.endpoint.metrics.enabled=true",
    "management.endpoints.web.exposure.include=*",
    "management.metrics.export.simple.enabled=true",
    "management.endpoint.health.enabled=true"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class CasCoreMonitorConfigurationTests {
    @Autowired
    @Qualifier("memoryHealthIndicator")
    private HealthIndicator memoryHealthIndicator;

    @Autowired
    @Qualifier("sessionHealthIndicator")
    private HealthIndicator sessionHealthIndicator;

    @Autowired
    @Qualifier("systemHealthIndicator")
    private HealthIndicator systemHealthIndicator;

    @Test
    public void verifyOperation() {
        assertNotNull(memoryHealthIndicator);
        assertNotNull(sessionHealthIndicator);
        assertNotNull(systemHealthIndicator);
    }
}
