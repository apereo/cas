package com.yubico.webauthn.data;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Optional;

/**
 * This is {@link RegistrationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Value
@EqualsAndHashCode
public class RegistrationRequest {

    private String username;

    private Optional<String> credentialNickname;

    private ByteArray requestId;

    private PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions;

    private Optional<ByteArray> sessionToken;
}
