package com.yubico.data;
import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;

@JsonIgnoreProperties("sessionToken")
public record AssertionResponse(ByteArray requestId, PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential) {

    public AssertionResponse(
        @JsonProperty("requestId")
        final ByteArray requestId,
        @JsonProperty("credential")
        final PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential
    ) {
        this.requestId = requestId;
        this.credential = credential;
    }

}
