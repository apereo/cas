package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.webauthn.attestation.DefaultAttestationCertificateTrustResolver;

import com.google.common.collect.ArrayListMultimap;
import com.yubico.webauthn.attestation.TrustResolver;
import com.yubico.webauthn.attestation.resolver.SimpleTrustResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.cert.X509Certificate;

/**
 * This is {@link WebAuthnConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("webAuthnConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class WebAuthnConfiguration {

//    @Bean
//    public TrustResolver defaultAttestationCertificateTrustResolver() {
//        val subresolver = new SimpleTrustResolver(trustedCertificates);
//
//        val trustedCerts = ArrayListMultimap.<String, X509Certificate>create();
//        for (X509Certificate cert : trustedCertificates) {
//            trustedCerts.put(cert.getSubjectDN().getName(), cert);
//        }
//        return new DefaultAttestationCertificateTrustResolver(subresolver, trustedCerts);
//    }
}
