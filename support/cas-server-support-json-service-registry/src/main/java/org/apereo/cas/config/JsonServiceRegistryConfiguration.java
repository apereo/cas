package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.JsonServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.WatcherService;

import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link JsonServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "jsonServiceRegistryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 1)
public class JsonServiceRegistryConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jsonServiceRegistry")
    @Autowired
    public ServiceRegistry jsonServiceRegistry(
        @Qualifier("registeredServiceResourceNamingStrategy")
        final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("registeredServiceReplicationStrategy")
        final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
        final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners) throws Exception {

        val registry = casProperties.getServiceRegistry();
        val json = new JsonServiceRegistry(registry.getJson().getLocation(),
            WatcherService.noOp(),
            applicationContext,
            registeredServiceReplicationStrategy,
            resourceNamingStrategy,
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
        if (registry.getJson().isWatcherEnabled()) {
            json.enableDefaultWatcherService();
        }
        return json;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    @ConditionalOnMissingBean(name = "jsonServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer jsonServiceRegistryExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties,
        @Qualifier("jsonServiceRegistry")
        final ServiceRegistry jsonServiceRegistry) {
        val registry = casProperties.getServiceRegistry().getJson();
        return plan -> FunctionUtils.doIfNotNull(registry.getLocation(),
            Unchecked.consumer(input -> plan.registerServiceRegistry(jsonServiceRegistry)));
    }
}
