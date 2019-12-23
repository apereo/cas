package org.apereo.cas.webauthn.attestation;

import com.google.common.collect.Multimap;
import com.yubico.webauthn.attestation.TrustResolver;
import com.yubico.webauthn.attestation.resolver.SimpleTrustResolver;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link DefaultAttestationCertificateTrustResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class DefaultAttestationCertificateTrustResolver implements TrustResolver {
    private final SimpleTrustResolver simpleResolver;

    private final Multimap<String, X509Certificate> trustedCerts;

    @Override
    public Optional<X509Certificate> resolveTrustAnchor(final X509Certificate attestation,
                                                        final List<X509Certificate> chain) {
        val subResult = simpleResolver.resolveTrustAnchor(attestation, chain);
        if (subResult.isPresent()) {
            return subResult;
        }
        val x509Certificates = trustedCerts.get(attestation.getSubjectDN().getName());
        if (x509Certificates.isEmpty()) {
            return Optional.empty();
        }
        return x509Certificates
            .stream()
            .filter(cert -> cert.equals(attestation))
            .findFirst();
    }
}
