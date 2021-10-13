package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.webauthn.LdapWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;

/**
 * This is {@link LdapWebAuthnConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration(value = "LdapWebAuthnConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapWebAuthnConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public WebAuthnCredentialRepository webAuthnCredentialRepository(final CasConfigurationProperties casProperties,
                                                                     @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
                                                                     final CipherExecutor webAuthnCredentialRegistrationCipherExecutor) {
        val ldap = casProperties.getAuthn().getMfa().getWebAuthn().getLdap();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        return new LdapWebAuthnCredentialRepository(connectionFactory, casProperties, webAuthnCredentialRegistrationCipherExecutor);
    }
}
