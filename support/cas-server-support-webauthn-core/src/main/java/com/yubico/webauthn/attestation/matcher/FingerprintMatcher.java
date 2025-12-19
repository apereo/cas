package com.yubico.webauthn.attestation.matcher;

import module java.base;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.hash.Hashing;
import com.yubico.webauthn.attestation.DeviceMatcher;
import lombok.val;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 * This is {@link FingerprintMatcher}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public final class FingerprintMatcher implements DeviceMatcher {
    public static final String SELECTOR_TYPE = "fingerprint";

    private static final String FINGERPRINTS_KEY = "fingerprints";

    @Override
    public boolean matches(final X509Certificate attestationCertificate, final JsonNode parameters) {
        val fingerprints = parameters.get(FINGERPRINTS_KEY);
        if (fingerprints.isArray()) {
            try {
                val fingerprint = Hashing.sha1().hashBytes(attestationCertificate.getEncoded()).toString().toLowerCase(Locale.ENGLISH);
                for (val candidate : fingerprints) {
                    if (fingerprint.equals(candidate.asText().toLowerCase(Locale.ENGLISH))) {
                        return true;
                    }
                }
            } catch (final CertificateEncodingException e) {
                // Fall through to return false.
            }
        }
        return false;
    }
}

