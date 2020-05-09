package org.apereo.cas.configuration.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.CasConfigurationWatchService;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * This is {@link CasCoreBootstrapStandaloneWatchConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casCoreBootstrapStandaloneWatchConfiguration", proxyBeanMethods = false)
@Profile("standalone")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(value = "spring.cloud.config.enabled", havingValue = "false")
public class CasCoreBootstrapStandaloneWatchConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("configurationPropertiesEnvironmentManager")
    private ObjectProvider<CasConfigurationPropertiesEnvironmentManager> configurationPropertiesEnvironmentManager;

    @ConditionalOnProperty(value = "cas.events.track-configuration-modifications", havingValue = "true")
    @Bean
    public CasConfigurationWatchService casConfigurationWatchService() {
        return new CasConfigurationWatchService(configurationPropertiesEnvironmentManager.getObject(), applicationContext);
    }
}
