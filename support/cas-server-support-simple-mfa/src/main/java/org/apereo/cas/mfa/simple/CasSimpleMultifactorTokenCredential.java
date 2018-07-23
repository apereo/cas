package org.apereo.cas.mfa.simple;

import org.apereo.cas.authentication.OneTimeTokenCredential;

import lombok.NoArgsConstructor;

/**
 * This is {@link CasSimpleMultifactorTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@NoArgsConstructor
public class CasSimpleMultifactorTokenCredential extends OneTimeTokenCredential {
    private static final long serialVersionUID = -4245600701132111037L;

    public CasSimpleMultifactorTokenCredential(final String token) {
        super(token);
    }
}
