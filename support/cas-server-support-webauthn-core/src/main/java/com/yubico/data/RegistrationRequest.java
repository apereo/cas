package com.yubico.data;
import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;

/**
 * Registration in progress, kept server-side between the start and the finish of the ceremony.
 *
 * @param username                           the principal id that will own the registration
 * @param credentialNickname                 the credential nickname
 * @param requestId                          the request id
 * @param publicKeyCredentialCreationOptions the creation options sent to the browser
 * @param sessionToken                       the session token
 * @param conditional                        whether the browser was asked for a conditional create
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RegistrationRequest(String username, Optional<String> credentialNickname,
    ByteArray requestId, PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions,
    Optional<ByteArray> sessionToken, boolean conditional) {

    public RegistrationRequest(final String username, final Optional<String> credentialNickname,
                               final ByteArray requestId, final PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions,
                               final Optional<ByteArray> sessionToken) {
        this(username, credentialNickname, requestId, publicKeyCredentialCreationOptions, sessionToken, false);
    }
}
