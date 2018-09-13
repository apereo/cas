package org.apereo.cas.web.support.config;

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

@Configuration("casHazelcastThrottlingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasHazelcastThrottlingConfiguration {


    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Bean
    public IMap throttleSubmissionMap() {
        LOGGER.debug("################## Creating SubmissionMap Throttle Map ############");
        final HazelcastTicketRegistryProperties hz = casProperties.getTicket().getRegistry().getHazelcast();
        final HazelcastConfigurationFactory factory = new HazelcastConfigurationFactory();
        hazelcastInstance.getConfig().addMapConfig(factory.buildMapConfig(hz, "ipmap", 120));
        return hazelcastInstance.getMap("ipmap");
    }

    @Bean
    public IMap throttleUserMap() {
        LOGGER.debug("################## Creating UserMap Throttle Map ############");
        final HazelcastTicketRegistryProperties hz = casProperties.getTicket().getRegistry().getHazelcast();
        final HazelcastConfigurationFactory factory = new HazelcastConfigurationFactory();
        hazelcastInstance.getConfig().addMapConfig(factory.buildMapConfig(hz, "usermap", 120));
        return hazelcastInstance.getMap("usermap");
    }
}
