package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.support.events.listener.CasConfigurationEventListener;
import org.apereo.cas.support.events.listener.DefaultCasConfigurationEventListener;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreEventsConfigEnvironmentConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "CasCoreEventsConfigEnvironmentConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreEventsConfigEnvironmentConfiguration {

    @ConditionalOnMissingBean(name = "casConfigurationEventListener")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasConfigurationEventListener casConfigurationEventListener(
        @Qualifier("configurationPropertiesEnvironmentManager")
        final CasConfigurationPropertiesEnvironmentManager manager,
        final ConfigurationPropertiesBindingPostProcessor binder,
        final ContextRefresher contextRefresher,
        final ConfigurableApplicationContext applicationContext) {
        return new DefaultCasConfigurationEventListener(manager, binder, contextRefresher, applicationContext);
    }
}
