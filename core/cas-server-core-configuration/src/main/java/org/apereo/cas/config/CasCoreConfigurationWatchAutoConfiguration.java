package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationWatchService;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

/**
 * This is {@link CasCoreConfigurationWatchAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration)
@AutoConfiguration
@Lazy(false)
public class CasCoreConfigurationWatchAutoConfiguration {
    @Bean
    public InitializingBean casConfigurationWatchService(
        final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(InitializingBean.class)
            .when(BeanCondition.on("cas.events.core.track-configuration-modifications").isTrue().given(applicationContext.getEnvironment()))
            .supply(() -> new CasConfigurationWatchService(applicationContext))
            .otherwiseProxy()
            .get();
    }
}
