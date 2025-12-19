package com.yubico.data;
import module java.base;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;

public record RegistrationResponse(ByteArray requestId,
                                   PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential,
                                   Optional<ByteArray> sessionToken) {

    @JsonCreator
    public RegistrationResponse(
        @JsonProperty("requestId")
        final ByteArray requestId,
        @JsonProperty("credential")
        final PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential,
        @JsonProperty("sessionToken")
        final Optional<ByteArray> sessionToken) {
        this.requestId = requestId;
        this.credential = credential;
        this.sessionToken = sessionToken;
    }

}
