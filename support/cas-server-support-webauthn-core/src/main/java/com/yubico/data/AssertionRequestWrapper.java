package com.yubico.data;
import module java.base;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import lombok.Value;
import org.jspecify.annotations.NonNull;

@Value
@SuppressWarnings("NullableOnContainingClass")
public class AssertionRequestWrapper {

    @NonNull
    ByteArray requestId;

    @NonNull
    PublicKeyCredentialRequestOptions publicKeyCredentialRequestOptions;

    @NonNull
    Optional<String> username;

    @NonNull
    AssertionRequest request;

    @JsonCreator
    public AssertionRequestWrapper(
        @NonNull @JsonProperty("requestId") final ByteArray requestId,
        @NonNull @JsonProperty("request") final AssertionRequest request) {
        this.requestId = requestId;
        this.publicKeyCredentialRequestOptions = request.getPublicKeyCredentialRequestOptions();
        this.username = request.getUsername();
        this.request = request;
    }
}
