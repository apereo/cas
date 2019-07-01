package org.apereo.cas.webauthn.registration;

import org.apereo.cas.webauthn.attestation.AttestationCertInfo;
import org.apereo.cas.webauthn.credential.WebAuthnCredentialRegistration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * This is {@link WebAuthnSuccessfulU2fRegistrationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@AllArgsConstructor
public class WebAuthnSuccessfulU2fRegistrationResult {
    private WebAuthnRegistrationRequest request;
    private WebAuthnCredentialRegistrationResponse response;
    private WebAuthnCredentialRegistration registration;
    private boolean attestationTrusted;
    private Optional<AttestationCertInfo> attestationCert;
}
