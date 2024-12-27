package org.apereo.cas;

import org.apereo.cas.config.CasMetricsAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import io.micrometer.core.aop.TimedAspect;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasMetricsConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasMetricsAutoConfiguration.class, properties = "management.metrics.export.simple.enabled=true")
@Tag("Metrics")
@ExtendWith(CasTestExtension.class)
class CasMetricsConfigurationTests {
    @Autowired
    @Qualifier("timedAspect")
    private TimedAspect timedAspect;

    @Test
    void verifyOperation() {
        assertNotNull(timedAspect);
    }
}
