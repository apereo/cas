package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.services.publisher.CasRegisteredServiceHazelcastStreamPublisher;
import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasServicesStreamingHazelcastConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casServicesStreamingHazelcastConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasServicesStreamingHazelcastConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public CasRegisteredServiceStreamPublisher casRegisteredServiceStreamPublisher() {
        final String name = CasRegisteredServiceHazelcastStreamPublisher.class.getSimpleName();
        final HazelcastConfigurationFactory factory = new HazelcastConfigurationFactory();
        
//        casProperties.getServiceRegistry().get
//        final MapConfig mapConfig = factory.buildMapConfig(hz, name, 30);
//        
//        factory.build()
        return new CasRegisteredServiceHazelcastStreamPublisher();
    }
}
