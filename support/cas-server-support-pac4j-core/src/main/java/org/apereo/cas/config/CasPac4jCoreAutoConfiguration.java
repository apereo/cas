package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.SessionTerminationHandler;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.logout.DelegatedProfileTerminationHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasPac4jCoreAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication)
@AutoConfiguration
public class CasPac4jCoreAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedProfileTerminationHandler")
    public SessionTerminationHandler delegatedProfileTerminationHandler() {
        return new DelegatedProfileTerminationHandler();
    }
}
