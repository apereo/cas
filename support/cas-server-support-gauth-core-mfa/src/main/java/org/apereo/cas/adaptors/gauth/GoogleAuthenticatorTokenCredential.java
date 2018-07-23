package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.authentication.OneTimeTokenCredential;

import lombok.NoArgsConstructor;

/**
 * This is {@link GoogleAuthenticatorTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@NoArgsConstructor
public class GoogleAuthenticatorTokenCredential extends OneTimeTokenCredential {

    private static final long serialVersionUID = -7570600701132111037L;

    public GoogleAuthenticatorTokenCredential(final String token) {
        super(token);
    }
}
