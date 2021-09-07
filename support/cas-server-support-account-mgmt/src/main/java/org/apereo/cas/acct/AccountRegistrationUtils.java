package org.apereo.cas.acct;

import lombok.experimental.UtilityClass;

/**
 * This is {@link AccountRegistrationUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@UtilityClass
public class AccountRegistrationUtils {
    /**
     * Request parameter to carry the account token.
     */
    public static final String REQUEST_PARAMETER_ACCOUNT_REGISTRATION_ACTIVATION_TOKEN = "acctregtoken";

    /**
     * Property name to track the registration token in tickets.
     */
    public static final String PROPERTY_ACCOUNT_REGISTRATION_ACTIVATION_TOKEN = "acctregtoken";
}
