package org.apereo.cas.otp.repository.credentials;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.OneTimeTokenCredential;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;

import java.security.GeneralSecurityException;

/**
 * This is {@link OneTimeTokenCredentialValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface OneTimeTokenCredentialValidator<C extends OneTimeTokenCredential, T extends OneTimeToken> {
    /**
     * Validate credential.
     *
     * @param authentication  the authentication
     * @param tokenCredential the token credential
     * @return the token
     * @throws GeneralSecurityException the general security exception
     * @throws PreventedException       the prevented exception
     */
    T validate(Authentication authentication, C tokenCredential)
        throws GeneralSecurityException, PreventedException;

    /**
     * Store.
     *
     * @param validatedToken the validated token
     * @return the one time token credential validator
     */
    OneTimeTokenCredentialValidator store(T validatedToken);

    /**
     * Is token authorized for account?
     *
     * @param token   the token
     * @param account the account
     * @return true/false
     */
    boolean isTokenAuthorizedFor(int token, OneTimeTokenAccount account);

    /**
     * Gets token repository.
     *
     * @return the token repository
     */
    OneTimeTokenRepository getTokenRepository();

    /**
     * Gets credential repository.
     *
     * @return the credential repository
     */
    OneTimeTokenCredentialRepository getCredentialRepository();
}
