package org.apereo.cas.web.support.config;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastTicketRegistryProperties;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures a Hazelcast IMap that is used by the InMemory throttling interceptors to store
 * failed login attempts.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Configuration("casHazelcastThrottlingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasHazelcastThrottlingConfiguration {

    private static final String MAP_KEY = "ipMap";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Bean
    public IMap throttleSubmissionMap() {
        LOGGER.debug("Creating Throttle map in hazelcast");
        final HazelcastTicketRegistryProperties hz = casProperties.getTicket().getRegistry().getHazelcast();
        final HazelcastConfigurationFactory factory = new HazelcastConfigurationFactory();
        final MapConfig ipMapConfig = factory.buildMapConfig(hz, MAP_KEY, 120);
        hazelcastInstance.getConfig().addMapConfig(ipMapConfig);
        return hazelcastInstance.getMap(MAP_KEY);
    }
}
