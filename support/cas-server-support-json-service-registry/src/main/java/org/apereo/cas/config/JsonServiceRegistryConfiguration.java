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

import lombok.SneakyThrows;
import lombok.val;
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
import org.springframework.core.Ordered;

import java.util.Collection;

/**
 * This is {@link JsonServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("jsonServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 1)
public class JsonServiceRegistryConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("registeredServiceReplicationStrategy")
    private ObjectProvider<RegisteredServiceReplicationStrategy> registeredServiceReplicationStrategy;

    @Autowired
    @Qualifier("registeredServiceResourceNamingStrategy")
    private ObjectProvider<RegisteredServiceResourceNamingStrategy> resourceNamingStrategy;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @Bean
    @SneakyThrows
    @RefreshScope
    @ConditionalOnMissingBean(name = "jsonServiceRegistry")
    public ServiceRegistry jsonServiceRegistry() {
        val registry = casProperties.getServiceRegistry();
        val json = new JsonServiceRegistry(registry.getJson().getLocation(),
            WatcherService.noOp(),
            applicationContext,
            registeredServiceReplicationStrategy.getObject(),
            resourceNamingStrategy.getObject(),
            serviceRegistryListeners.getObject());
        if (registry.getJson().isWatcherEnabled()) {
            json.enableDefaultWatcherService();
        }
        return json;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "jsonServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer jsonServiceRegistryExecutionPlanConfigurer() {
        val registry = casProperties.getServiceRegistry().getJson();
        return plan -> FunctionUtils.doIfNotNull(registry.getLocation(), input -> plan.registerServiceRegistry(jsonServiceRegistry()));
    }
}
