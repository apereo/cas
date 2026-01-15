package com.yubico.data;
import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RegistrationRequest(String username, Optional<String> credentialNickname,
    ByteArray requestId, PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions,
    Optional<ByteArray> sessionToken) {

}
