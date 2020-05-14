package org.apereo.cas;

import org.apereo.cas.config.CasMetricsConfiguration;
import org.apereo.cas.config.CasMetricsRepositoryConfiguration;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link CasMetricsConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    MetricsAutoConfiguration.class,
    SimpleMetricsExportAutoConfiguration.class,
    CasMetricsConfiguration.class,
    CasMetricsRepositoryConfiguration.class,
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class
},
    properties = "management.metrics.export.simple.enabled=true")
@Tag("Simple")
public class CasMetricsConfigurationTests {
}
