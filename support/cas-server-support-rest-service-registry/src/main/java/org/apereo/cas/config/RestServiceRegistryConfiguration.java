package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RestfulServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link RestServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(name = "cas.service-registry.rest.url")
@Slf4j
@Configuration(value = "restServiceRegistryConfiguration", proxyBeanMethods = false)
public class RestServiceRegistryConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "restfulServiceRegistry")
    @Autowired
    public ServiceRegistry restfulServiceRegistry(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
        final ConfigurableApplicationContext applicationContext) {
        val registry = casProperties.getServiceRegistry().getRest();
        LOGGER.debug("Creating REST-based service registry using endpoint [{}]", registry.getUrl());
        return new RestfulServiceRegistry(applicationContext,
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new), registry);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "restfulServiceRegistryExecutionPlanConfigurer")
    @Autowired
    public ServiceRegistryExecutionPlanConfigurer restfulServiceRegistryExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties,
        @Qualifier("restfulServiceRegistry")
        final ServiceRegistry restfulServiceRegistry) {
        return plan -> {
            val registry = casProperties.getServiceRegistry().getRest();
            if (StringUtils.isNotBlank(registry.getUrl())) {
                plan.registerServiceRegistry(restfulServiceRegistry);
            }
        };
    }
}
