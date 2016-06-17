package org.apereo.cas.ticket.registry.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.HazelcastInstanceConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {
        HazelcastInstanceConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreLogoutConfiguration.class},
        locations="classpath:HazelcastInstanceConfigurationTests-config.xml",
        initializers = ConfigFileApplicationContextInitializer.class)
@DirtiesContext
public class ProvidedHazelcastInstanceConfigurationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProvidedHazelcastInstanceConfigurationTests.class);

    @Autowired
    @Qualifier("hazelcast")
    private HazelcastInstance hzInstance;

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
        LOGGER.info("Shutting down hazelcast instance {}", this.hzInstance.getConfig().getInstanceName());
        this.hzInstance.shutdown();
        while (this.hzInstance.getLifecycleService().isRunning()) {
            LOGGER.info("Waiting for instances to shut down");
        }
    }
}
