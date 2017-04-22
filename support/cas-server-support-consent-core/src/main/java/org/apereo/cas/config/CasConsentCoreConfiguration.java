package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.DefaultConsentEngine;
import org.apereo.cas.consent.GuavaCachingConsentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasConsentCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casConsentCoreConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConsentCoreConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasConsentCoreConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "consentRepository")
    @RefreshScope
    @Bean
    public ConsentRepository consentRepository() {
        return new GuavaCachingConsentRepository();
    }

    @ConditionalOnMissingBean(name = "consentEngine")
    @Bean
    @RefreshScope
    public ConsentEngine consentEngine() {
        return new DefaultConsentEngine();
    }
}
