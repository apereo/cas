package com.yubico.webauthn.data;

import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.attestation.Attestation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.Optional;

/**
 * This is {@link CredentialRegistration}.
 *
 * @author Misagh Moayyed
 * @since 6.2
 */
@Value
@Builder
@SuppressWarnings({"ReferenceEquality", "OptionalEquality"})
@With
public class CredentialRegistration {

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
