package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@Tag("Hazelcast")
@ExtendWith(CasTestExtension.class)
@Slf4j
class DefaultHazelcastInstanceConfigurationTests extends BaseHazelcastTests {

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
