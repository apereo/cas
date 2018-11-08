package org.apereo.cas.configuration.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.CasConfigurationWatchService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * This is {@link CasCoreBootstrapStandaloneWatchConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreBootstrapStandaloneWatchConfiguration")
@Slf4j
@Profile("standalone")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(value = "spring.cloud.config.enabled", havingValue = "false")
public class CasCoreBootstrapStandaloneWatchConfiguration {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    @Qualifier("configurationPropertiesEnvironmentManager")
    private ObjectProvider<CasConfigurationPropertiesEnvironmentManager> configurationPropertiesEnvironmentManager;

    @ConditionalOnProperty(value = "cas.events.trackConfigurationModifications", havingValue = "true")
    @RefreshScope
    @Bean
    public CasConfigurationWatchService casConfigurationWatchService() {
        return new CasConfigurationWatchService(configurationPropertiesEnvironmentManager.getIfAvailable(), eventPublisher);
    }
}
