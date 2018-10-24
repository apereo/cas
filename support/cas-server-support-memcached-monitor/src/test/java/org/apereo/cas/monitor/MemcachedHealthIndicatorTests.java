package org.apereo.cas.monitor;

import org.apereo.cas.category.MemcachedCategory;
import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.monitor.config.MemcachedMonitorConfiguration;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link MemcachedHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    MemcachedMonitorConfiguration.class,
    CasCoreUtilSerializationConfiguration.class
})
@TestPropertySource(properties = {
    "cas.monitor.memcached.servers=localhost:11212",
    "cas.monitor.memcached.failureMode=Redistribute",
    "cas.monitor.memcached.locatorType=ARRAY_MOD",
    "cas.monitor.memcached.hashAlgorithm=FNV1A_64_HASH"
})
@DirtiesContext
@Category(MemcachedCategory.class)
public class MemcachedHealthIndicatorTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("memcachedHealthIndicator")
    private HealthIndicator monitor;

    @Test
    public void verifyMonitorNotRunning() {
        val health = monitor.health();
        assertEquals(Status.OUT_OF_SERVICE, health.getStatus());
    }
}
