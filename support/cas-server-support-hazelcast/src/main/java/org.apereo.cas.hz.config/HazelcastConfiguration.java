package org.apereo.cas.hz.config;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("hazelcastConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class HazelcastConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public HazelcastInstance hazelcastInstance() {
        val hz = casProperties.getTicket().getRegistry().getHazelcast();
        val factory = new HazelcastConfigurationFactory();
        return Hazelcast.newHazelcastInstance(factory.build(hz));
    }
}
