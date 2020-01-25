// Copyright (c) 2018, Yubico AB
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.yubico.webauthn.attestation.resolver;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.yubico.webauthn.attestation.TrustResolver;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Resolves a metadata object whose associated certificate has signed the
 * argument certificate, or is equal to the argument certificate.
 */
public class SimpleTrustResolverWithEquality implements TrustResolver {

    private final SimpleTrustResolver subresolver;
    private final Multimap<String, X509Certificate> trustedCerts = ArrayListMultimap.create();

    public SimpleTrustResolverWithEquality(Collection<X509Certificate> trustedCertificates) {
        subresolver = new SimpleTrustResolver(trustedCertificates);

        for (X509Certificate cert : trustedCertificates) {
            trustedCerts.put(cert.getSubjectDN().getName(), cert);
        }
    }

    @Override
    public Optional<X509Certificate> resolveTrustAnchor(X509Certificate attestationCertificate, List<X509Certificate> caCertificateChain) {
        Optional<X509Certificate> subResult = subresolver.resolveTrustAnchor(attestationCertificate, caCertificateChain);

        if (subResult.isPresent()) {
            return subResult;
        } else {
            for (X509Certificate cert : trustedCerts.get(attestationCertificate.getSubjectDN().getName())) {
                if (cert.equals(attestationCertificate)) {
                    return Optional.of(cert);
                }
            }

            return Optional.empty();
        }
    }

}
