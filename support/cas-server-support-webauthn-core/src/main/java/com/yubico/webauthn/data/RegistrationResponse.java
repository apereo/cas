package com.yubico.webauthn.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.Optional;

/**
 * This is {@link RegistrationResponse}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Value
public class RegistrationResponse {

    private final ByteArray requestId;

    private final PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential;

    private final Optional<ByteArray> sessionToken;

    @JsonCreator
    public RegistrationResponse(
        @JsonProperty("requestId") final ByteArray requestId,
        @JsonProperty("credential") final PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential,
        @JsonProperty("sessionToken") final Optional<ByteArray> sessionToken) {
        this.requestId = requestId;
        this.credential = credential;
        this.sessionToken = sessionToken;
    }
}
