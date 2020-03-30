package com.yubico.webauthn.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * This is {@link AssertionResponse}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Value
@JsonIgnoreProperties("sessionToken")
public class AssertionResponse {

    private ByteArray requestId;
    private PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential;

    public AssertionResponse(
        @JsonProperty("requestId") final ByteArray requestId,
        @JsonProperty("credential") final PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential
    ) {
        this.requestId = requestId;
        this.credential = credential;
    }

}
