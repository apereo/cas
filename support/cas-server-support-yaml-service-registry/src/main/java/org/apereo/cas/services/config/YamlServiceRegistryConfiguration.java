package org.apereo.cas.services.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.YamlServiceRegistry;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.WatcherService;

import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link YamlServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "yamlServiceRegistryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class YamlServiceRegistryConfiguration {

    @Configuration(value = "YamlServiceRegistryCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class YamlServiceRegistryCoreConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        @ConditionalOnMissingBean(name = "yamlServiceRegistry")
        public ServiceRegistry yamlServiceRegistry(
            @Qualifier("registeredServiceResourceNamingStrategy")
            final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("registeredServiceReplicationStrategy")
            final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
            final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners) throws Exception {

            val registry = casProperties.getServiceRegistry();
            val yaml = new YamlServiceRegistry(registry.getYaml().getLocation(),
                WatcherService.noOp(), applicationContext, registeredServiceReplicationStrategy,
                resourceNamingStrategy,
                Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
            if (registry.getYaml().isWatcherEnabled()) {
                yaml.enableDefaultWatcherService();
            }
            return yaml;
        }

    }

    @Configuration(value = "YamlServiceRegistryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class YamlServiceRegistryPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "yamlServiceRegistryExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public ServiceRegistryExecutionPlanConfigurer yamlServiceRegistryExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier("yamlServiceRegistry")
            final ServiceRegistry yamlServiceRegistry) {
            val registry = casProperties.getServiceRegistry().getYaml();
            return plan -> FunctionUtils.doIfNotNull(registry.getLocation(),
                Unchecked.consumer(input -> plan.registerServiceRegistry(yamlServiceRegistry)));
        }
    }
}
