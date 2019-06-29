package org.apereo.cas.webauthn.credential;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.UserIdentity;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;

import java.time.Instant;

/**
 * This is {@link CredentialRegistrationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Builder
@Wither
public class CredentialRegistrationRequest {
    private long signatureCount;

    private UserIdentity userIdentity;

    private String credentialNickname;

    @JsonIgnore
    private Instant registrationTime;

    private RegisteredCredential credential;

    private Attestation attestationMetadata;

    @JsonProperty("registrationTime")
    public String getRegistrationTimestamp() {
        return registrationTime.toString();
    }

    public String getUsername() {
        return userIdentity.getName();
    }
}
