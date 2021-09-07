package org.apereo.cas.acct;

/**
 * This is {@link AccountRegistrationService}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface AccountRegistrationService {
    /**
     * Gets account mgmt registration property loader.
     *
     * @return the account mgmt registration property loader
     */
    AccountRegistrationPropertyLoader getAccountRegistrationPropertyLoader();

    /**
     * Gets account registration username builder.
     *
     * @return the account registration username builder
     */
    AccountRegistrationUsernameBuilder getAccountRegistrationUsernameBuilder();

    /**
     * Create token.
     *
     * @param registrationRequest the registration request
     * @return the string
     */
    String createToken(AccountRegistrationRequest registrationRequest);

    /**
     * Validate token.
     *
     * @param token the token
     * @return the account registration request
     * @throws Exception the exception
     */
    AccountRegistrationRequest validateToken(String token) throws Exception;
}
