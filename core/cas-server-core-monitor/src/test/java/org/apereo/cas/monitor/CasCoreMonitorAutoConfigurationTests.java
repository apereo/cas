package org.apereo.cas.monitor;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMonitorAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreMonitorAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(
    classes = CasCoreMonitorAutoConfigurationTests.SharedTestConfiguration.class,
    properties = {
        "management.metrics.export.simple.enabled=true",

        "management.endpoint.metrics.enabled=true",
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.health.enabled=true",

        "management.health.systemHealthIndicator.enabled=true",
        "management.health.memoryHealthIndicator.enabled=true",
        "management.health.sessionHealthIndicator.enabled=true"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Metrics")
@AutoConfigureObservability
class CasCoreMonitorAutoConfigurationTests {
    @Autowired
    @Qualifier("defaultExecutableObserver")
    private ExecutableObserver defaultExecutableObserver;

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
    void verifyOperation() throws Throwable {
        assertNotNull(memoryHealthIndicator);
        assertNotNull(sessionHealthIndicator);
        assertNotNull(systemHealthIndicator);
    }

    @Test
    void verifyObserabilitySupplier() throws Throwable {
        val result = defaultExecutableObserver.supply(new MonitorableTask("verifyObserabilitySupplier"), () -> "CAS");
        assertEquals("CAS", result);
    }

    @Test
    void verifyObservabilityRunner() throws Throwable {
        val result = new AtomicBoolean(false);
        defaultExecutableObserver.run(new MonitorableTask("verifyObservabilityRunner"), () -> result.set(true));
        assertTrue(result.get());
    }
    @ImportAutoConfiguration({
        MetricsAutoConfiguration.class,
        ObservationAutoConfiguration.class,
        SimpleMetricsExportAutoConfiguration.class,
        MetricsEndpointAutoConfiguration.class,
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreMonitorAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class
    })
    @SpringBootConfiguration
    public static class SharedTestConfiguration {
    }
}
