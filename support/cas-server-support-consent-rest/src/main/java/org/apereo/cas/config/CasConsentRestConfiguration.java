package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.RestfulConsentRepository;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasConsentRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Consent, module = "rest")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
public class CasConsentRestConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ConsentRepository consentRepository(final CasConfigurationProperties casProperties) {
        return new RestfulConsentRepository(casProperties.getConsent().getRest());
    }
}
