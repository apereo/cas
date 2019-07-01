package org.apereo.cas.webauthn.registration;

import org.apereo.cas.webauthn.attestation.AttestationCertInfo;
import org.apereo.cas.webauthn.credential.WebAuthnCredentialRegistration;

import com.fasterxml.jackson.databind.JsonNode;
import com.yubico.webauthn.data.ByteArray;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

/**
 * This is {@link WebAuthnSuccessfulRegistrationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
@Setter
public class WebAuthnSuccessfulRegistrationResult {
    private boolean success = true;

    private WebAuthnRegistrationRequest request;
    private WebAuthnRegistrationResponse response;
    private WebAuthnCredentialRegistration registration;
    private boolean attestationTrusted;
    private Optional<AttestationCertInfo> attestationCert;

    public WebAuthnSuccessfulRegistrationResult(final WebAuthnRegistrationRequest request, final WebAuthnRegistrationResponse response,
                                                final WebAuthnCredentialRegistration registration, final boolean attestationTrusted) {
        this.request = request;
        this.response = response;
        this.registration = registration;
        this.attestationTrusted = attestationTrusted;
        attestationCert = Optional.ofNullable(
            response.getCredential().getResponse().getAttestation().getAttestationStatement().get("x5c")
        ).map(certs -> certs.get(0))
            .flatMap((JsonNode certDer) -> {
                try {
                    return Optional.of(new ByteArray(certDer.binaryValue()));
                } catch (final IOException e) {
                    LOGGER.error("Failed to get binary value from x5c element: {}", certDer, e);
                    return Optional.empty();
                }
            })
            .map(AttestationCertInfo::new);
    }
}
