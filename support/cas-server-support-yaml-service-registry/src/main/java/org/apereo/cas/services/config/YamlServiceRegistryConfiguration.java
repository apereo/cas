package org.apereo.cas.services.config;

import com.google.common.base.Throwables;
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
import org.springframework.core.io.ClassPathResource;

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
    public ServiceRegistryDao yamlServiceRegistryDao() {
        final ServiceRegistryProperties registry = casProperties.getServiceRegistry();
        if (registry.getConfig().getLocation() == null) {
            LOGGER.warn("The location of service definitions is undefined for the service registry");
            throw new IllegalArgumentException("Service configuration directory for registry must be defined");
        }


        try {
            if (registry.getConfig().getLocation() instanceof ClassPathResource) {
                LOGGER.warn("The location of service definitions [{}] is on the classpath. It is recommended that the location of service definitions "
                                + "be externalized to allow for easier modifications and better sharing of the configuration.",
                        registry.getConfig().getLocation());
            }
            return new YamlServiceRegistryDao(registry.getConfig().getLocation(), registry.isWatcherEnabled(), eventPublisher);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
