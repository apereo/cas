package org.apereo.cas.webauthn.registration;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * This is {@link WebAuthnRegistrationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class WebAuthnRegistrationRequest {

    private String username;
    private Optional<String> credentialNickname;
    private ByteArray requestId;
    private PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions;
}
