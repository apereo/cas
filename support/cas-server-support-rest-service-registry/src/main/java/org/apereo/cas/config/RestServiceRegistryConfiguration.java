package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RestfulServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.util.HttpUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
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
public class RestServiceRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @Bean
    @RefreshScope
    @SneakyThrows
    @ConditionalOnProperty(name = "cas.serviceRegistry.rest.url")
    public ServiceRegistry restfulServiceRegistry() {
        val registry = casProperties.getServiceRegistry().getRest();
        val restTemplate = new RestTemplate();
        val headers = new LinkedMultiValueMap<String, String>();

        if (StringUtils.isNotBlank(registry.getBasicAuthUsername())
            && StringUtils.isNotBlank(registry.getBasicAuthPassword())) {
            headers.putAll(HttpUtils.createBasicAuthHeaders(registry.getBasicAuthUsername(), registry.getBasicAuthPassword()));
        }

        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        return new RestfulServiceRegistry(eventPublisher, restTemplate, registry.getUrl(), headers, serviceRegistryListeners.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(name = "restfulServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer restfulServiceRegistryExecutionPlanConfigurer() {
        return new ServiceRegistryExecutionPlanConfigurer() {
            @Override
            public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
                val registry = casProperties.getServiceRegistry().getRest();
                if (StringUtils.isNotBlank(registry.getUrl())) {
                    plan.registerServiceRegistry(restfulServiceRegistry());
                }
            }
        };
    }

}
