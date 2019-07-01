package org.apereo.cas.webauthn.authentication;

import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link AssertionResponse}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
public class AssertionResponse {
    private ByteArray requestId;
    private PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential;
}
