package org.apereo.cas.webauthn.credential;

import com.yubico.webauthn.data.ByteArray;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link WebAuthnCredentialResponse}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@AllArgsConstructor
public class WebAuthnCredentialResponse {
    private ByteArray keyHandle;
    private ByteArray publicKey;
    private ByteArray attestationCertAndSignature;
    private ByteArray clientDataJson;
}
