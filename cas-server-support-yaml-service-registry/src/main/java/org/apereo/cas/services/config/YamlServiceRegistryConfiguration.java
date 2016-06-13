package org.apereo.cas.services.config;

import com.google.common.base.Throwables;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ReloadableServicesManager;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.YamlServiceRegistryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    @Qualifier("servicesManager")
    private ReloadableServicesManager servicesManager;
    
    @Bean
    @RefreshScope
    public ServiceRegistryDao yamlServiceRegistryDao() {
        try {
            final YamlServiceRegistryDao dao = new YamlServiceRegistryDao(
                    casProperties.getServiceRegistry().getConfig().getLocation(),
                    casProperties.getServiceRegistry().isWatcherEnabled());
            
            dao.setServicesManager(this.servicesManager);
            return dao;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
