package org.apereo.cas.services.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.services.ServiceRegistryProperties;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.YamlServiceRegistryDao;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link YamlServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("yamlServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class YamlServiceRegistryConfiguration {


    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    @Qualifier("registeredServiceReplicationStrategy")
    private RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy;

    @Bean
    @RefreshScope
    @SneakyThrows
    public ServiceRegistryDao serviceRegistryDao() {

        final ServiceRegistryProperties registry = casProperties.getServiceRegistry();
        return new YamlServiceRegistryDao(registry.getYaml().getLocation(),
            registry.isWatcherEnabled(), eventPublisher, registeredServiceReplicationStrategy);

    }
}
