package org.apereo.cas.web.support.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.hz.HazelcastConfigurationFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Configuration(value = "casHazelcastThrottlingConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasHazelcastThrottlingConfiguration {

    private static final String MAP_KEY = "ipMap";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casTicketRegistryHazelcastInstance")
    private ObjectProvider<HazelcastInstance> casTicketRegistryHazelcastInstance;

    @Bean
    public IMap throttleSubmissionMap() {
        val hz = casProperties.getAuthn().getThrottle().getHazelcast();
        val timeout = Beans.newDuration(casProperties.getAuthn().getThrottle().getSchedule().getRepeatInterval()).getSeconds();
        LOGGER.debug("Creating [{}] to record failed logins for throttling with timeout set to [{}]", MAP_KEY, timeout);
        val ipMapConfig = HazelcastConfigurationFactory.buildMapConfig(hz, MAP_KEY, timeout);
        val hazelcastInstance = this.casTicketRegistryHazelcastInstance.getObject();
        hazelcastInstance.getConfig().addMapConfig(ipMapConfig);
        return hazelcastInstance.getMap(MAP_KEY);
    }
}
