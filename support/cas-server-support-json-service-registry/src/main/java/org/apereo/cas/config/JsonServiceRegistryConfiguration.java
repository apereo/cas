package org.apereo.cas.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.JsonServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link JsonServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("jsonServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
@ConditionalOnProperty(prefix = "cas.serviceRegistry.json", name = "location")
public class JsonServiceRegistryConfiguration implements ServiceRegistryExecutionPlanConfigurer {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("registeredServiceReplicationStrategy")
    private RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy;

    @Autowired
    @Qualifier("registeredServiceResourceNamingStrategy")
    private RegisteredServiceResourceNamingStrategy resourceNamingStrategy;

    @Bean
    @SneakyThrows
    public ServiceRegistry jsonServiceRegistry() {
        final var registry = casProperties.getServiceRegistry();
        return new JsonServiceRegistry(registry.getJson().getLocation(),
            registry.isWatcherEnabled(), eventPublisher,
            registeredServiceReplicationStrategy, resourceNamingStrategy);
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        plan.registerServiceRegistry(jsonServiceRegistry());
    }
}
