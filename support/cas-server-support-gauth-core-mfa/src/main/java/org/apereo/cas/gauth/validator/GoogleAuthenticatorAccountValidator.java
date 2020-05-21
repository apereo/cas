package org.apereo.cas.gauth.validator;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.validator.OneTimeTokenAccountValidator;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link GoogleAuthenticatorAccountValidator}.
 *
 * Implements {@link OneTimeTokenAccountValidator} to
 * provide validation of Google Authenticator credentials.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthenticatorAccountValidator implements OneTimeTokenAccountValidator {
    private final IGoogleAuthenticator googleAuthenticatorInstance;

    @Override
    public boolean isValid(final OneTimeTokenAccount account,
                           final int token) {
        LOGGER.trace("Testing token [{}] against account [{}]", token, account);
        return this.googleAuthenticatorInstance.authorize(account.getSecretKey(), token);
    }
}
