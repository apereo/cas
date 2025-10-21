package com.yubico.webauthn.attestation;

import com.fasterxml.jackson.databind.JsonNode;
import java.security.cert.X509Certificate;

/**
 * This is {@link DeviceMatcher}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface DeviceMatcher {
    boolean matches(X509Certificate attestationCertificate, JsonNode parameters);
}
