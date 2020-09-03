package org.apereo.cas.webauthn;

import org.apereo.cas.authentication.credential.OneTimeTokenCredential;

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
    private static final long serialVersionUID = -571682410132111037L;

    public WebAuthnCredential(final String token) {
        super(token);
    }
}
