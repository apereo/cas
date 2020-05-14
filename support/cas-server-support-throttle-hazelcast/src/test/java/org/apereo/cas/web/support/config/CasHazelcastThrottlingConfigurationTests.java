package org.apereo.cas.web.support.config;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.HazelcastTicketRegistryConfiguration;
import org.apereo.cas.config.HazelcastTicketRegistryTicketCatalogConfiguration;

import com.hazelcast.map.IMap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

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
    CasCoreUtilConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasHazelcastThrottlingConfiguration.class
})
@Tag("Simple")
public class CasHazelcastThrottlingConfigurationTests {

    @Autowired
    @Qualifier("throttleSubmissionMap")
    private IMap throttleSubmissionMap;

    @Test
    public void verifyOperation() {
        assertNotNull(throttleSubmissionMap);

    }
}
