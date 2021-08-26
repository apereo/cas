package org.apereo.cas;

import org.apereo.cas.config.CasMetricsConfiguration;
import org.apereo.cas.config.CasMetricsRepositoryConfiguration;

import io.micrometer.core.aop.TimedAspect;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

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
@Tag("Metrics")
public class CasMetricsConfigurationTests {
    @Autowired
    @Qualifier("timedAspect")
    private TimedAspect timedAspect;

    @Test
    public void verifyOperation() {
        assertNotNull(timedAspect);
    }
}
