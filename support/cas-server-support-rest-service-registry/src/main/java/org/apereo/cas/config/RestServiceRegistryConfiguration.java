package org.apereo.cas.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RestfulServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * This is {@link RestServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("restServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class RestServiceRegistryConfiguration implements ServiceRegistryExecutionPlanConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    @SneakyThrows
    @ConditionalOnProperty(name = "cas.serviceRegistry.rest.url")
    public ServiceRegistry restfulServiceRegistry() {
        final var registry = casProperties.getServiceRegistry().getRest();
        final var restTemplate = new RestTemplate();
        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        if (StringUtils.isNotBlank(registry.getBasicAuthUsername())
            && StringUtils.isNotBlank(registry.getBasicAuthPassword())) {
            headers.putAll(HttpUtils.createBasicAuthHeaders(registry.getBasicAuthUsername(), registry.getBasicAuthPassword()));
        }
        return new RestfulServiceRegistry(restTemplate, registry.getUrl(), headers);
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        final var registry = casProperties.getServiceRegistry().getRest();
        if (StringUtils.isNotBlank(registry.getUrl())) {
            plan.registerServiceRegistry(restfulServiceRegistry());
        }
    }
}
