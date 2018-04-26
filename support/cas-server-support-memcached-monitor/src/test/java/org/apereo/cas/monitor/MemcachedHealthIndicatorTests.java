package org.apereo.cas.monitor;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.monitor.config.MemcachedMonitorConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link MemcachedHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    MemcachedMonitorConfiguration.class,
    CasCoreUtilSerializationConfiguration.class
})
@TestPropertySource(locations = {"classpath:/monitor.properties"})
@DirtiesContext
@Slf4j
public class MemcachedHealthIndicatorTests {

    @Autowired
    @Qualifier("memcachedHealthIndicator")
    private HealthIndicator monitor;

    @Test
    public void verifyMonitorNotRunning() {
        final Health health = monitor.health();
        assertEquals(Status.OUT_OF_SERVICE, health.getStatus());
    }
}
