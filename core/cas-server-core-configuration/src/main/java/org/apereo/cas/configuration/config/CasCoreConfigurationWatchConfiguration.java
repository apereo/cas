package org.apereo.cas.configuration.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.CasConfigurationWatchService;
import org.apereo.cas.util.spring.CasEventListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreConfigurationWatchConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Configuration(value = "CasCoreConfigurationWatchConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(value = "cas.events.core.track-configuration-modifications", havingValue = "true")
public class CasCoreConfigurationWatchConfiguration {
    @Autowired
    @Bean(initMethod = "initialize")
    public CasEventListener casConfigurationWatchService(
        @Qualifier("configurationPropertiesEnvironmentManager")
        final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager,
        final ConfigurableApplicationContext applicationContext) {
        return new CasConfigurationWatchService(configurationPropertiesEnvironmentManager, applicationContext);
    }
}
