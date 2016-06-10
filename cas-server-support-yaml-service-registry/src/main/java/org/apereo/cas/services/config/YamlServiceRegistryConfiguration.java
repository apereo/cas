package org.apereo.cas.services.config;

import com.google.common.base.Throwables;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.YamlServiceRegistryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link YamlServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("yamlServiceRegistryConfiguration")
public class YamlServiceRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public ServiceRegistryDao yamlServiceRegistryDao() {
        try {
            return new YamlServiceRegistryDao(
                    casProperties.getServiceRegistryProperties().getConfig().getLocation(),
                    casProperties.getServiceRegistryProperties().isWatcherEnabled());
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
