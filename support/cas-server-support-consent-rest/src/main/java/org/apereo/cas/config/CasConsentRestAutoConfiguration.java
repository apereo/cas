package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.RestfulConsentRepository;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasConsentRestAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Consent, module = "rest")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
public class CasConsentRestAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ConsentRepository consentRepository(
        final CasConfigurationProperties casProperties) {
        return new RestfulConsentRepository(casProperties.getConsent().getRest());
    }
}
