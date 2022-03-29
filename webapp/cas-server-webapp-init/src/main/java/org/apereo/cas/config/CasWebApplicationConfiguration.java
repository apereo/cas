package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;
import org.apereo.cas.web.CasWebApplicationReady;
import org.apereo.cas.web.CasWebApplicationReadyListener;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * This is {@link CasWebApplicationConfiguration}.
 *
 * @author Hal Deadman
 * @since 6.6.0
 */
@Configuration(value = "CasWebApplicationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableAsync
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.WebApplication)
public class CasWebApplicationConfiguration {

    @ConditionalOnMissingBean(name = "casWebApplicationReadyListener")
    @Bean
    public CasWebApplicationReadyListener casWebApplicationReadyListener() {
        return new CasWebApplicationReady();
    }

}
