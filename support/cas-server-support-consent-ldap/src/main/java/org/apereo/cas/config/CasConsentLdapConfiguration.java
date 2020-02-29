package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.LdapConsentRepository;
import org.apereo.cas.util.LdapUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasConsentLdapConfiguration}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@Configuration(value = "casConsentLdapConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConsentLdapConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ConsentRepository consentRepository() {
        val ldap = casProperties.getConsent().getLdap();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        return new LdapConsentRepository(connectionFactory, ldap);
    }
}
