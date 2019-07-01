package org.apereo.cas.webauthn.credential;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.UserIdentity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Optional;

/**
 * This is {@link WebAuthnCredentialRegistration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class WebAuthnCredentialRegistration {
    private long signatureCount;

    private UserIdentity userIdentity;

    private Optional<String> credentialNickname;

    @JsonIgnore
    private Instant registrationTime;

    private RegisteredCredential credential;

    private Optional<Attestation> attestationMetadata;

    @JsonProperty("registrationTime")
    public String getRegistrationTimestamp() {
        return registrationTime.toString();
    }

    public String getUsername() {
        return userIdentity.getName();
    }
}
