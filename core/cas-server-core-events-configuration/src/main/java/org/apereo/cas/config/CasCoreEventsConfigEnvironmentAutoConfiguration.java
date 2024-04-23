package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.events.listener.CasConfigurationEventListener;
import org.apereo.cas.support.events.listener.DefaultCasConfigurationEventListener;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreEventsConfigEnvironmentAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration)
@AutoConfiguration
public class CasCoreEventsConfigEnvironmentAutoConfiguration {

    @ConditionalOnMissingBean(name = "casConfigurationEventListener")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Lazy(false)
    public CasConfigurationEventListener casConfigurationEventListener(
        @Qualifier(CasConfigurationPropertiesEnvironmentManager.BEAN_NAME) final CasConfigurationPropertiesEnvironmentManager manager,
        final ConfigurationPropertiesBindingPostProcessor binder,
        final ContextRefresher contextRefresher,
        final ConfigurableApplicationContext applicationContext) {
        return new DefaultCasConfigurationEventListener(manager, binder, contextRefresher, applicationContext);
    }
}
