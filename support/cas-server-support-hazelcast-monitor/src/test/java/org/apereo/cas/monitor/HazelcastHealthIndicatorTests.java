package org.apereo.cas.monitor;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreLogoutConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPersonDirectoryStubConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.HazelcastMonitorConfiguration;
import org.apereo.cas.config.HazelcastTicketRegistryConfiguration;
import org.apereo.cas.config.HazelcastTicketRegistryTicketCatalogConfiguration;
import com.hazelcast.internal.memory.MemoryStats;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.util.Arrays;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link HazelcastHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    HazelcastTicketRegistryConfiguration.class,
    HazelcastTicketRegistryTicketCatalogConfiguration.class,
    HazelcastMonitorConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasPersonDirectoryStubConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class
},
    properties = "cas.ticket.registry.hazelcast.cluster.instance-name=testlocalmonitor")
@Tag("Hazelcast")
class HazelcastHealthIndicatorTests {
    @Autowired
    @Qualifier("hazelcastHealthIndicator")
    private HealthIndicator hazelcastHealthIndicator;

    @Test
    void verifyMonitor() throws Throwable {
        val health = hazelcastHealthIndicator.health();
        val status = health.getStatus();
        assertTrue(Arrays.asList(Status.UP, Status.OUT_OF_SERVICE).contains(status),
            () -> "Status should be UP or OUT_OF_SERVICE but was" + status);

        val details = health.getDetails();
        assertTrue(details.containsKey("name"));

        details.values().stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .forEach(map -> {
                assertTrue(map.containsKey("size"));
                assertTrue(map.containsKey("capacity"));
                assertTrue(map.containsKey("evictions"));
                assertTrue(map.containsKey("percentFree"));
            });
        assertNotNull(hazelcastHealthIndicator.toString());
    }

    @Test
    void verifyFreeHeapPercentageCalculation() throws Throwable {
        val memoryStats = mock(MemoryStats.class);
        when(memoryStats.getFreeHeap()).thenReturn(125_555_248L);
        when(memoryStats.getCommittedHeap()).thenReturn(251_658_240L);
        val statistics = new HazelcastHealthIndicator.HazelcastStatistics(null, 1, memoryStats);

        assertEquals(49, statistics.getPercentFree());
    }
}
