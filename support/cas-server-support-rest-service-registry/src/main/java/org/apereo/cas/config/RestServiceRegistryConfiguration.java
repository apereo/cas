package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceEntityMapper;
import org.apereo.cas.services.RegisteredServiceEntityMapper;
import org.apereo.cas.services.RestfulServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.util.HttpUtils;

import lombok.SneakyThrows;
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
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

/**
 * This is {@link RestServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("restServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(name = "cas.service-registry.rest.url")
@Slf4j
public class RestServiceRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "registeredServiceEntityMapper")
    public RegisteredServiceEntityMapper registeredServiceEntityMapper() {
        return new DefaultRegisteredServiceEntityMapper();
    }

    @Bean
    @ConditionalOnMissingBean(name = "restfulServiceRegistryTemplate")
    public RestOperations restfulServiceRegistryTemplate() {
        return new RestTemplate();
    }

    @Bean
    @RefreshScope
    @SneakyThrows
    @ConditionalOnMissingBean(name = "restfulServiceRegistry")
    public ServiceRegistry restfulServiceRegistry() {
        val registry = casProperties.getServiceRegistry().getRest();
        LOGGER.debug("Creating REST-based service registry using endpoint [{}]", registry.getUrl());
        return new RestfulServiceRegistry(applicationContext,
            restfulServiceRegistryTemplate(),
            registry.getUrl(),
            restfulServiceRegistryHttpHeaders(),
            serviceRegistryListeners.getObject(),
            registeredServiceEntityMapper());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "restfulServiceRegistryHttpHeaders")
    public MultiValueMap<String, String> restfulServiceRegistryHttpHeaders() {
        val registry = casProperties.getServiceRegistry().getRest();
        val headers = new LinkedMultiValueMap<String, String>();
        if (StringUtils.isNotBlank(registry.getBasicAuthUsername())
            && StringUtils.isNotBlank(registry.getBasicAuthPassword())) {
            headers.putAll(HttpUtils.createBasicAuthHeaders(registry.getBasicAuthUsername(), registry.getBasicAuthPassword()));
        }
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "restfulServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer restfulServiceRegistryExecutionPlanConfigurer() {
        return plan -> {
            val registry = casProperties.getServiceRegistry().getRest();
            if (StringUtils.isNotBlank(registry.getUrl())) {
                plan.registerServiceRegistry(restfulServiceRegistry());
            }
        };
    }

}
