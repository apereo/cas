package org.apereo.cas.webauthn.credential;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This is {@link WebAuthnCredentialResponseHolder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@AllArgsConstructor
public class WebAuthnCredentialResponseHolder {
    private final WebAuthnCredentialResponse u2fResponse;
}
