package org.apereo.cas.monitor;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasHazelcastMonitorAutoConfiguration;
import org.apereo.cas.config.CasHazelcastTicketRegistryAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.hazelcast.internal.memory.MemoryStats;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
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
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasHazelcastTicketRegistryAutoConfiguration.class,
    CasHazelcastMonitorAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
},
    properties = "cas.ticket.registry.hazelcast.cluster.instance-name=testlocalmonitor")
@Tag("Hazelcast")
@ExtendWith(CasTestExtension.class)
class HazelcastHealthIndicatorTests {
    @Autowired
    @Qualifier("hazelcastHealthIndicator")
    private HealthIndicator hazelcastHealthIndicator;

    @Test
    void verifyMonitor() {
        val health = hazelcastHealthIndicator.health();
        val status = health.getStatus();
        assertTrue(Arrays.asList(Status.UP, Status.OUT_OF_SERVICE).contains(status),
            () -> "Status should be UP or OUT_OF_SERVICE but was%s".formatted(status));

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
    void verifyFreeHeapPercentageCalculation() {
        val memoryStats = mock(MemoryStats.class);
        when(memoryStats.getFreeHeap()).thenReturn(125_555_248L);
        when(memoryStats.getCommittedHeap()).thenReturn(251_658_240L);
        val statistics = new HazelcastHealthIndicator.HazelcastStatistics(null, 1, memoryStats);

        assertEquals(49, statistics.getPercentFree());
    }
}
