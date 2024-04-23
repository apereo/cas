package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.YamlServiceRegistry;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
 * This is {@link CasYamlServiceRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "yaml")
@AutoConfiguration
public class CasYamlServiceRegistryAutoConfiguration {

    @Configuration(value = "YamlServiceRegistryCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class YamlServiceRegistryCoreConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
    static class YamlServiceRegistryPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "yamlServiceRegistryExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceRegistryExecutionPlanConfigurer yamlServiceRegistryExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier("yamlServiceRegistry")
            final ServiceRegistry yamlServiceRegistry) {
            val registry = casProperties.getServiceRegistry().getYaml();
            return plan -> FunctionUtils.doIfNotNull(registry.getLocation(),
                input -> plan.registerServiceRegistry(yamlServiceRegistry));
        }
    }
}
