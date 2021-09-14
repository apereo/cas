package org.apereo.cas.acct;

import lombok.experimental.UtilityClass;
import org.springframework.webflow.execution.RequestContext;

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

    /**
     * Put account registration request.
     *
     * @param requestContext      the request context
     * @param registrationRequest the registration request
     */
    public static void putAccountRegistrationRequest(final RequestContext requestContext,
                                                     final AccountRegistrationRequest registrationRequest) {
        requestContext.getFlowScope().put("accountRegistrationRequest", registrationRequest);
    }

    /**
     * Put account registration request username.
     *
     * @param requestContext the request context
     * @param username       the username
     */
    public static void putAccountRegistrationRequestUsername(final RequestContext requestContext, final String username) {
        requestContext.getFlowScope().put("accountRegistrationRequestUsername", username);
    }

    /**
     * Gets account registration request username.
     *
     * @param requestContext the request context
     * @return the account registration request username
     */
    public static String getAccountRegistrationRequestUsername(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("accountRegistrationRequestUsername", String.class);
    }

    /**
     * Gets account registration request.
     *
     * @param requestContext the request context
     * @return the account registration request
     */
    public static AccountRegistrationRequest getAccountRegistrationRequest(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("accountRegistrationRequest", AccountRegistrationRequest.class);
    }
}
