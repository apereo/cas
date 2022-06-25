package org.apereo.cas.configuration.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.CasConfigurationWatchService;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreConfigurationWatchConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration)
@AutoConfiguration
public class CasCoreConfigurationWatchConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public InitializingBean casConfigurationWatchService(
        @Qualifier("configurationPropertiesEnvironmentManager")
        final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager,
        final ConfigurableApplicationContext applicationContext) throws Exception {
        return BeanSupplier.of(InitializingBean.class)
            .when(BeanCondition.on("cas.events.core.track-configuration-modifications").isTrue().given(applicationContext.getEnvironment()))
            .supply(() -> new CasConfigurationWatchService(configurationPropertiesEnvironmentManager, applicationContext))
            .otherwiseProxy()
            .get();
    }
}
