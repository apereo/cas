package org.apereo.cas.services.config;

import com.google.common.base.Throwables;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.YamlServiceRegistryDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * This is {@link YamlServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("yamlServiceRegistryConfiguration")
public class YamlServiceRegistryConfiguration {

    @Value("${service.registry.config.location:classpath:services}")
    private Resource configDirectory;

    @Value("${service.registry.watcher.enabled:true}")
    private boolean enableWatcher;

    @Bean
    @RefreshScope
    public ServiceRegistryDao yamlServiceRegistryDao() {
        try {
            return new YamlServiceRegistryDao(this.configDirectory, this.enableWatcher);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
