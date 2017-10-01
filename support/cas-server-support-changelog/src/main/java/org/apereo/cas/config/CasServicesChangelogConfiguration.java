package org.apereo.cas.config;

import org.apereo.cas.DefaultObjectChangelog;
import org.apereo.cas.ObjectChangelog;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.repository.api.JaversRepository;
import org.javers.repository.inmemory.InMemoryRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasServicesChangelogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casServicesChangelogConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasServicesChangelogConfiguration {
    
    @ConditionalOnMissingBean(name = "registeredServicesChangelogRepository")
    @Bean
    public JaversRepository registeredServicesChangelogRepository() {
        return new InMemoryRepository();
    }

    @Bean
    public Javers javersInstance() {
        JaversBuilder javers = JaversBuilder.javers();
        javers = javers.registerJaversRepository(registeredServicesChangelogRepository());
        return javers.build();
    }

    @Bean
    public ObjectChangelog<RegisteredService> registeredServicesObjectChangeLog() {
        return new DefaultObjectChangelog<>(javersInstance());
    }
}
