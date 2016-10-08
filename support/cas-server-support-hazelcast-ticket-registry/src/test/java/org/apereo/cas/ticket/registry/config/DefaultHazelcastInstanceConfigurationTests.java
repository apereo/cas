package org.apereo.cas.ticket.registry.config;

import com.google.common.collect.Lists;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAttributeRepositoryConfiguration;
import org.apereo.cas.config.HazelcastInstanceConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        HazelcastInstanceConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasPersonDirectoryAttributeRepositoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreLogoutConfiguration.class})
@ContextConfiguration(locations="classpath:HazelcastInstanceConfigurationTests-config.xml")
@TestPropertySource(properties = {"cas.ticket.registry.hazelcast.configLocation="})
@DirtiesContext
public class DefaultHazelcastInstanceConfigurationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHazelcastInstanceConfigurationTests.class);

    @Autowired
    @Qualifier("hazelcast")
    private HazelcastInstance hzInstance;

    public HazelcastInstance getHzInstance() {
        return hzInstance;
    }

    @Test
    public void correctHazelcastInstanceIsCreated() throws Exception {
        assertNotNull(this.hzInstance);
        final Config config = this.hzInstance.getConfig();
        assertFalse(config.getNetworkConfig().getJoin().getMulticastConfig().isEnabled());
        assertEquals(Lists.newArrayList("localhost"), config.getNetworkConfig().getJoin().getTcpIpConfig().getMembers());
        assertTrue(config.getNetworkConfig().isPortAutoIncrement());
        assertEquals(5701, config.getNetworkConfig().getPort());

        final MapConfig mapConfig = config.getMapConfig("tickets");
        assertNotNull(mapConfig);
        assertEquals(28800, mapConfig.getMaxIdleSeconds());
        assertEquals(EvictionPolicy.LRU, mapConfig.getEvictionPolicy());
        assertEquals(10, mapConfig.getEvictionPercentage());
    }

    @After
    public void shutdownHz() {
        LOGGER.info("Shutting down hazelcast instance {}", this.hzInstance.getConfig().getInstanceName());
        this.hzInstance.shutdown();
        while (this.hzInstance.getLifecycleService().isRunning()) {
            LOGGER.info("Waiting for instances to shut down");
        }
    }
}
