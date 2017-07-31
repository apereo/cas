package org.apereo.cas.services.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.services.ServiceRegistryProperties;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.YamlServiceRegistryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link YamlServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("yamlServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class YamlServiceRegistryConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlServiceRegistryConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Bean
    @RefreshScope
    public ServiceRegistryDao serviceRegistryDao() {
        try {
            final ServiceRegistryProperties registry = casProperties.getServiceRegistry();
            return new YamlServiceRegistryDao(registry.getYaml().getLocation(), registry.isWatcherEnabled(), eventPublisher);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
