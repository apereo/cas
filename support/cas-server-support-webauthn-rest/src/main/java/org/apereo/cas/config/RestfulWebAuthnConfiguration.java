package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.webauthn.RestfulWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link RestfulWebAuthnConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "RestfulWebAuthnConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RestfulWebAuthnConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
    private ObjectProvider<CipherExecutor<String, String>> webAuthnCredentialRegistrationCipherExecutor;

    @RefreshScope
    @Bean
    public WebAuthnCredentialRepository webAuthnCredentialRepository() {
        return new RestfulWebAuthnCredentialRepository(
            casProperties, webAuthnCredentialRegistrationCipherExecutor.getObject());
    }
}
