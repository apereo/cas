package org.apereo.cas.acct;

/**
 * This is {@link AccountRegistrationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
public interface AccountRegistrationRequestValidator {
    /**
     * Validate.
     *
     * @param request the request
     */
    void validate(AccountRegistrationRequest request);

    /**
     * No op account registration request validator.
     *
     * @return the account registration request validator
     */
    static AccountRegistrationRequestValidator noOp() {
        return request -> {
        };
    }
}
