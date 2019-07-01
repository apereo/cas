package org.apereo.cas.webauthn.registration;

import org.apereo.cas.webauthn.credential.WebAuthnCredentialResponseHolder;

import com.yubico.webauthn.data.ByteArray;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is {@link WebAuthnCredentialRegistrationResponse}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class WebAuthnCredentialRegistrationResponse {
    private ByteArray requestId;
    private WebAuthnCredentialResponseHolder credential;
}
