package org.apereo.cas.services.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.YamlServiceRegistry;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.io.WatcherService;

import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link YamlServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("yamlServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.service-registry.yaml", name = "location")
public class YamlServiceRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

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
    @RefreshScope
    @SneakyThrows
    public ServiceRegistry yamlServiceRegistry() {
        val registry = casProperties.getServiceRegistry();
        val yaml = new YamlServiceRegistry(registry.getYaml().getLocation(),
            WatcherService.noOp(),
            applicationContext,
            registeredServiceReplicationStrategy.getObject(),
            resourceNamingStrategy.getObject(),
            serviceRegistryListeners.getObject());
        if (registry.isWatcherEnabled()) {
            yaml.enableDefaultWatcherService();
        }
        return yaml;
    }

    @Bean
    @ConditionalOnMissingBean(name = "yamlServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer yamlServiceRegistryExecutionPlanConfigurer() {
        return plan -> plan.registerServiceRegistry(yamlServiceRegistry());
    }
}
