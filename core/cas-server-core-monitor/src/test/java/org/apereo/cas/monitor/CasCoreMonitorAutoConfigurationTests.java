package org.apereo.cas.monitor;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMonitorAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
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

        "management.endpoint.metrics.access=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.health.access=UNRESTRICTED",

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
    void verifyOperation() {
        assertNotNull(memoryHealthIndicator);
        assertNotNull(sessionHealthIndicator);
        assertNotNull(systemHealthIndicator);
    }

    @Test
    void verifyObserabilitySupplier() {
        val result = defaultExecutableObserver.supply(new MonitorableTask("verifyObserabilitySupplier"), () -> "CAS");
        assertEquals("CAS", result);
    }

    @Test
    void verifyObservabilityRunner() {
        val result = new AtomicBoolean(false);
        defaultExecutableObserver.run(new MonitorableTask("verifyObservabilityRunner"), () -> result.set(true));
        assertTrue(result.get());
    }
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreTicketsAutoConfiguration.class,
        CasCoreMonitorAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    public static class SharedTestConfiguration {
    }
}
