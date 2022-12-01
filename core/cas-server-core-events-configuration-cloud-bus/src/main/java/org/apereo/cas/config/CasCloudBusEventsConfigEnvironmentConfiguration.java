package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.events.listener.CasCloudBusConfigurationEventListener;
import org.apereo.cas.support.events.listener.DefaultCasCloudBusConfigurationEventListener;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCloudBusEventsConfigEnvironmentConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration)
@AutoConfiguration
public class CasCloudBusEventsConfigEnvironmentConfiguration {

    @ConditionalOnMissingBean(name = "casCloudBusConfigurationEventListener")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasCloudBusConfigurationEventListener casCloudBusConfigurationEventListener(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("configurationPropertiesEnvironmentManager") final CasConfigurationPropertiesEnvironmentManager manager) {
        return new DefaultCasCloudBusConfigurationEventListener(manager, applicationContext);
    }

}
