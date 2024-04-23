package org.apereo.cas;

import org.apereo.cas.config.CasMetricsAutoConfiguration;
import io.micrometer.core.aop.TimedAspect;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
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
    CasMetricsAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    AopAutoConfiguration.class
},
    properties = "management.metrics.export.simple.enabled=true")
@Tag("Metrics")
class CasMetricsConfigurationTests {
    @Autowired
    @Qualifier("timedAspect")
    private TimedAspect timedAspect;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(timedAspect);
    }
}
