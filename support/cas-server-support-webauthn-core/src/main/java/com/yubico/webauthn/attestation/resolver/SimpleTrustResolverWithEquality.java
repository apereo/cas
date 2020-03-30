package com.yubico.webauthn.attestation.resolver;

import com.yubico.webauthn.attestation.TrustResolver;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.val;

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link SimpleTrustResolverWithEquality}.
 * Resolves a metadata object whose associated certificate has signed the
 * argument certificate, or is equal to the argument certificate.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class SimpleTrustResolverWithEquality implements TrustResolver {

    private final SimpleTrustResolver resolver;
    private final Multimap<String, X509Certificate> trustedCerts = ArrayListMultimap.create();

    public SimpleTrustResolverWithEquality(final Collection<X509Certificate> trustedCertificates) {
        resolver = new SimpleTrustResolver(trustedCertificates);
        for (val cert : trustedCertificates) {
            trustedCerts.put(cert.getSubjectDN().getName(), cert);
        }
    }

    @Override
    public Optional<X509Certificate> resolveTrustAnchor(final X509Certificate attestationCertificate,
                                                        final List<X509Certificate> caCertificateChain) {
        val subResult = resolver.resolveTrustAnchor(attestationCertificate, caCertificateChain);

        if (subResult.isPresent()) {
            return subResult;
        }
        for (val cert : trustedCerts.get(attestationCertificate.getSubjectDN().getName())) {
            if (cert.equals(attestationCertificate)) {
                return Optional.of(cert);
            }
        }

        return Optional.empty();
    }

}
