package org.apereo.cas.webauthn;

import org.apereo.cas.authentication.credential.OneTimeTokenCredential;

import com.yubico.webauthn.data.ByteArray;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;

/**
 * This is {@link WebAuthnCredential}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WebAuthnCredential extends OneTimeTokenCredential {
    private static final long serialVersionUID = -571682410132111037L;

    public WebAuthnCredential(final String token) {
        super(token);
    }

    /**
     * Convert credential to byte array.
     *
     * @param webAuthnCredential the web authn credential
     * @return the byte array
     */
    @SneakyThrows
    public static ByteArray from(final WebAuthnCredential webAuthnCredential) {
        return ByteArray.fromBase64Url(webAuthnCredential.getToken());
    }
}
