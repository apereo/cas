package org.apereo.cas.config;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates a singular Hazelcast Instance that other hazelcast modules add maps to
 * instead of creating thier own instance.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Configuration("casHazelcastConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasHazelcastConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "casHazelcastInstance")
    @Bean
    public HazelcastInstance casHazelcastInstance() {
        final BaseHazelcastProperties hz = casProperties.getTicket().getRegistry().getHazelcast();
        LOGGER.debug("Creating Hazelcast instance using properties [{}]", hz);
        final HazelcastConfigurationFactory factory = new HazelcastConfigurationFactory();
        return Hazelcast.newHazelcastInstance(factory.build(hz));
    }
}
