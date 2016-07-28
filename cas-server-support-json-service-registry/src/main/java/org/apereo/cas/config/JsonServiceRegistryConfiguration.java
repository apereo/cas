package org.apereo.cas.config;

import com.google.common.base.Throwables;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.JsonServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link JsonServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("jsonServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JsonServiceRegistryConfiguration {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean(name = {"jsonServiceRegistryDao", "serviceRegistryDao"})
    public ServiceRegistryDao jsonServiceRegistryDao() {
        try {
            final JsonServiceRegistryDao dao =
                    new JsonServiceRegistryDao(
                            casProperties.getServiceRegistry().getConfig().getLocation(),
                            casProperties.getServiceRegistry().isWatcherEnabled(),
                            eventPublisher);
            return dao;
        } catch (final Throwable e) {
            throw Throwables.propagate(e);
        }
    }
}
