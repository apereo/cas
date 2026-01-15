package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.webauthn.RestfulWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasRestfulWebAuthnAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebAuthn)
@AutoConfiguration
public class CasRestfulWebAuthnAutoConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public WebAuthnCredentialRepository webAuthnCredentialRepository(
        final CasConfigurationProperties casProperties,
        @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
        final CipherExecutor<String, String> webAuthnCredentialRegistrationCipherExecutor) {
        return new RestfulWebAuthnCredentialRepository(casProperties, webAuthnCredentialRegistrationCipherExecutor);
    }
}
