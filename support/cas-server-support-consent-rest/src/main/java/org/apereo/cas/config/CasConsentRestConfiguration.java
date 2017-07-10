package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.RestConsentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * This is {@link CasConsentRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casConsentRestConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConsentRestConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ConsentRepository consentRepository() {
        return new RestConsentRepository(new RestTemplate(), casProperties.getConsent().getRest().getEndpoint());
    }
}
