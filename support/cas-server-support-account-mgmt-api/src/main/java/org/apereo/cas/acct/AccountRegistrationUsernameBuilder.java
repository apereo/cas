package org.apereo.cas.acct;

/**
 * This is {@link AccountRegistrationUsernameBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface AccountRegistrationUsernameBuilder {

    /**
     * As default.
     *
     * @return the account registration username builder
     */
    static AccountRegistrationUsernameBuilder asDefault() {
        return AccountRegistrationRequest::getUsername;
    }

    /**
     * Build.
     *
     * @param registrationRequest the registration request
     * @return the string
     */
    String build(AccountRegistrationRequest registrationRequest);
}
