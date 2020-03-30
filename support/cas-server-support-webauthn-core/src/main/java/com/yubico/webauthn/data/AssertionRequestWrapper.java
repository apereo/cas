package com.yubico.webauthn.data;

import com.yubico.webauthn.AssertionRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;

/**
 * This is {@link AssertionRequestWrapper}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Value
public class AssertionRequestWrapper {

    @NonNull
    private ByteArray requestId;

    @NonNull
    private PublicKeyCredentialRequestOptions publicKeyCredentialRequestOptions;

    @NonNull
    private Optional<String> username;

    @NonNull
    @JsonIgnore
    private transient AssertionRequest request;

    public AssertionRequestWrapper(
        @NonNull final ByteArray requestId,
        @NonNull final AssertionRequest request) {
        this.requestId = requestId;
        this.publicKeyCredentialRequestOptions = request.getPublicKeyCredentialRequestOptions();
        this.username = request.getUsername();
        this.request = request;

    }

}
