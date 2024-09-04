package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasHazelcastTicketRegistryAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasHazelcastTicketRegistryAutoConfiguration.class,
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
    properties = {
        "cas.ticket.registry.hazelcast.cluster.core.instance-name=samplelocalhostinstance",
        "cas.ticket.registry.hazelcast.cluster.network.port=5702"
    })
@Slf4j
@Tag("Hazelcast")
@ExtendWith(CasTestExtension.class)
class DefaultHazelcastInstanceConfigurationTests {
    @Autowired
    @Qualifier("casTicketRegistryHazelcastInstance")
    private HazelcastInstance hzInstance;

    @Test
    void correctHazelcastInstanceIsCreated() {
        assertNotNull(this.hzInstance);
        val config = this.hzInstance.getConfig();
        assertFalse(config.getNetworkConfig().getJoin().getMulticastConfig().isEnabled());
        assertEquals(List.of("localhost"), config.getNetworkConfig().getJoin().getTcpIpConfig().getMembers());
        assertTrue(config.getNetworkConfig().isPortAutoIncrement());
        assertTrue(config.getManagementCenterConfig().isScriptingEnabled());
        assertEquals(5702, config.getNetworkConfig().getPort());
        val mapConfigs = config.getMapConfigs();
        mapConfigs.forEach((key, value) -> LOGGER.info("Hazelcast map key [{}]", key));
        assertTrue(mapConfigs.containsKey(CasTicketCatalogConfigurationValuesProvider.STORAGE_NAME_PROXY_TICKET));
        assertTrue(mapConfigs.containsKey(CasTicketCatalogConfigurationValuesProvider.STORAGE_NAME_PROXY_GRANTING_TICKETS));
        assertTrue(mapConfigs.containsKey(CasTicketCatalogConfigurationValuesProvider.STORAGE_NAME_SERVICE_TICKETS));
        assertTrue(mapConfigs.containsKey(CasTicketCatalogConfigurationValuesProvider.STORAGE_NAME_TICKET_GRANTING_TICKETS));
        assertTrue(mapConfigs.containsKey(CasTicketCatalogConfigurationValuesProvider.STORAGE_NAME_TRANSIENT_SESSION_TICKETS));
    }

    @AfterEach
    public void shutdownHz() {
        LOGGER.info("Shutting down hazelcast instance [{}]", this.hzInstance.getConfig().getInstanceName());
        this.hzInstance.shutdown();
        while (this.hzInstance.getLifecycleService().isRunning()) {
            LOGGER.info("Waiting for instances to shut down");
        }
    }
}
