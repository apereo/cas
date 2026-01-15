package com.yubico.webauthn.attestation;

import module java.base;
import java.security.cert.X509Certificate;

/**
 * This is {@link AttestationMetadataSource}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface AttestationMetadataSource extends AttestationTrustSource {
    Optional<Attestation> findMetadata(X509Certificate attestationCertificate);
}
