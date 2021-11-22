package org.apereo.cas.web.support.config;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.HazelcastTicketRegistryConfiguration;
import org.apereo.cas.config.HazelcastTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasHazelcastThrottlingConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    HazelcastTicketRegistryConfiguration.class,
    HazelcastTicketRegistryTicketCatalogConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasHazelcastThrottlingConfiguration.class
})
@Tag("Hazelcast")
public class CasHazelcastThrottlingConfigurationTests {

    @Autowired
    @Qualifier("throttleSubmissionMap")
    private ThrottledSubmissionsStore throttleSubmissionMap;

    @Test
    public void verifyOperation() {
        assertNotNull(throttleSubmissionMap);
        var key = UUID.randomUUID().toString();
        throttleSubmissionMap.put(key, ZonedDateTime.now(Clock.systemUTC()));
        assertNotNull(throttleSubmissionMap.get(key));
        assertNotEquals(0, throttleSubmissionMap.entries().count());
        throttleSubmissionMap.removeIf(entry -> entry.getKey().equals(key));
        assertEquals(0, throttleSubmissionMap.entries().count());
    }
}
