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
@ActiveProfiles("provided_hz_config")
public class ProvidedHazelcastInstanceConfigurationTests {

    @Autowired
    private HazelcastInstance hzInstance;

    public HazelcastInstance getHzInstance() {
        return hzInstance;
    }

    @Test
    public void hazelcastInstanceIsCreatedNormally() throws Exception {
        assertNotNull(this.hzInstance);
        final Config config = this.hzInstance.getConfig();
        assertTrue(config.getNetworkConfig().getJoin().getMulticastConfig().isEnabled());
        assertEquals(Arrays.asList("127.0.0.1"), config.getNetworkConfig().getJoin().getTcpIpConfig().getMembers());
        assertFalse(config.getNetworkConfig().isPortAutoIncrement());
        assertEquals(5801, config.getNetworkConfig().getPort());

        final MapConfig mapConfig = config.getMapConfig("tickets-from-external-config");
        assertNotNull(mapConfig);
        assertEquals(20000, mapConfig.getMaxIdleSeconds());
        assertEquals(EvictionPolicy.LFU, mapConfig.getEvictionPolicy());
        assertEquals(99, mapConfig.getEvictionPercentage());
    }

    @After
    public void shutdownHz() {
        this.hzInstance.shutdown();
    }
}
