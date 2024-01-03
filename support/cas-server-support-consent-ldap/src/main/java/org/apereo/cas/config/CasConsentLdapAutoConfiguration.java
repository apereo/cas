package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.LdapConsentRepository;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasConsentLdapAutoConfiguration}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Consent, module = "ldap")
@AutoConfiguration
public class CasConsentLdapAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ConsentRepository consentRepository(
        final CasConfigurationProperties casProperties,
        @Qualifier("consentLdapConnectionFactory")
        final ConnectionFactory consentLdapConnectionFactory) {
        val ldap = casProperties.getConsent().getLdap();
        return new LdapConsentRepository(new LdapConnectionFactory(consentLdapConnectionFactory), ldap);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "consentLdapConnectionFactory")
    public ConnectionFactory consentLdapConnectionFactory(final CasConfigurationProperties casProperties) {
        val ldap = casProperties.getConsent().getLdap();
        return LdapUtils.newLdaptiveConnectionFactory(ldap);
    }
}
