package org.jasig.cas.ticket.registry.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:HazelcastInstanceConfigurationTests-config.xml")
@ActiveProfiles("default_hz_config")
public class DefaultHazelcastInstanceConfigurationTests {

    @Autowired
    private HazelcastInstance hzInstance;

    public HazelcastInstance getHzInstance() {
        return hzInstance;
    }

    @Test
    public void correctHazelcastInstanceIsCreated() throws Exception {
        assertNotNull(this.hzInstance);
        final Config config = this.hzInstance.getConfig();
        assertFalse(config.getNetworkConfig().getJoin().getMulticastConfig().isEnabled());
        assertEquals(Arrays.asList("localhost"), config.getNetworkConfig().getJoin().getTcpIpConfig().getMembers());
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
        this.hzInstance.shutdown();
    }
}
