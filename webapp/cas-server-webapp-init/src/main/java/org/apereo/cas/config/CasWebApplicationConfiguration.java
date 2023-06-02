package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebApplicationReady;
import org.apereo.cas.web.CasWebApplicationReadyListener;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * This is {@link CasWebApplicationConfiguration}.
 *
 * @author Hal Deadman
 * @since 6.6.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableAsync(proxyTargetClass = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebApplication)
@AutoConfiguration
public class CasWebApplicationConfiguration {

    @ConditionalOnMissingBean(name = "casWebApplicationReadyListener")
    @Bean
    @Lazy(false)
    public CasWebApplicationReadyListener casWebApplicationReadyListener() {
        return new CasWebApplicationReady();
    }
}
