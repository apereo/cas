package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.support.events.listener.CasCloudBusConfigurationEventListener;
import org.apereo.cas.util.spring.CasEventListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCloudBusEventsConfigEnvironmentConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casCloudBusEventsConfigEnvironmentConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCloudBusEventsConfigEnvironmentConfiguration {

    @ConditionalOnMissingBean(name = "casCloudBusConfigurationEventListener")
    @Bean
    @Autowired
    public CasEventListener casCloudBusConfigurationEventListener(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("configurationPropertiesEnvironmentManager") final CasConfigurationPropertiesEnvironmentManager manager) {
        return new CasCloudBusConfigurationEventListener(manager, applicationContext);
    }

}
