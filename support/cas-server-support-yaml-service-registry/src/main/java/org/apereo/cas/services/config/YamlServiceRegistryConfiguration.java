package org.apereo.cas.services.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.YamlServiceRegistry;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = "cas.serviceRegistry.yaml", name = "location")
public class YamlServiceRegistryConfiguration implements ServiceRegistryExecutionPlanConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    @Qualifier("registeredServiceReplicationStrategy")
    private RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy;

    @Autowired
    @Qualifier("registeredServiceResourceNamingStrategy")
    private RegisteredServiceResourceNamingStrategy resourceNamingStrategy;

    @Bean
    @RefreshScope
    @SneakyThrows
    public ServiceRegistry yamlServiceRegistry() {
        final var registry = casProperties.getServiceRegistry();
        return new YamlServiceRegistry(registry.getYaml().getLocation(),
            registry.isWatcherEnabled(), eventPublisher,
            registeredServiceReplicationStrategy, resourceNamingStrategy);
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        plan.registerServiceRegistry(yamlServiceRegistry());
    }
}
