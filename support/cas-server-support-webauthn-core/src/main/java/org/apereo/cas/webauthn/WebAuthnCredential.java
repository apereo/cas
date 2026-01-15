package org.apereo.cas.webauthn;

import module java.base;
import org.apereo.cas.authentication.credential.OneTimeTokenCredential;
import org.apereo.cas.util.function.FunctionUtils;
import com.yubico.webauthn.data.ByteArray;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    @Serial
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
    public static ByteArray from(final WebAuthnCredential webAuthnCredential) {
        return FunctionUtils.doUnchecked(() -> ByteArray.fromBase64Url(webAuthnCredential.getToken()));
    }
}
