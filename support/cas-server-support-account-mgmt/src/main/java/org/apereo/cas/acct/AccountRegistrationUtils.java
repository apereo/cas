package org.apereo.cas.acct;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;

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
        requestContext.getConversationScope().put("accountRegistrationRequest", registrationRequest);
    }

    /**
     * Put account registration request username.
     *
     * @param requestContext the request context
     * @param username       the username
     */
    public static void putAccountRegistrationRequestUsername(final RequestContext requestContext, final String username) {
        requestContext.getConversationScope().put("accountRegistrationRequestUsername", username);
    }

    /**
     * Gets account registration request username.
     *
     * @param requestContext the request context
     * @return the account registration request username
     */
    public static String getAccountRegistrationRequestUsername(final RequestContext requestContext) {
        return requestContext.getConversationScope().get("accountRegistrationRequestUsername", String.class);
    }

    /**
     * Gets account registration request.
     *
     * @param requestContext the request context
     * @return the account registration request
     */
    public static AccountRegistrationRequest getAccountRegistrationRequest(final RequestContext requestContext) {
        return requestContext.getConversationScope().get("accountRegistrationRequest", AccountRegistrationRequest.class);
    }

    /**
     * Put account management registration security questions count.
     *
     * @param requestContext the request context
     * @param count          the count
     */
    public static void putAccountRegistrationSecurityQuestionsCount(final RequestContext requestContext, final int count) {
        requestContext.getFlowScope().put("accountRegistrationSecurityQuestionsCount", count);
    }

    /**
     * Gets account management registration security questions count.
     *
     * @param requestContext the request context
     * @return the account management registration security questions count
     */
    public static Integer getAccountRegistrationSecurityQuestionsCount(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("accountRegistrationSecurityQuestionsCount", Integer.class);
    }

    /**
     * Put account management sign up enabled.
     *
     * @param requestContext the request context
     * @param value          the value
     */
    public static void putAccountRegistrationEnabled(final RequestContext requestContext, final boolean value) {
        requestContext.getFlowScope().put("accountRegistrationEnabled", value);
    }

    /**
     * Is account management registration captcha enabled.
     *
     * @param requestContext the request context
     * @return the boolean
     */
    public static boolean isAccountRegistrationCaptchaEnabled(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("accountRegistrationCaptchaEnabled", Boolean.class);
    }

    /**
     * Put account management sign up captcha enabled.
     *
     * @param requestContext the request context
     * @param properties     the properties
     */
    public static void putAccountRegistrationCaptchaEnabled(final RequestContext requestContext,
                                                                      final GoogleRecaptchaProperties properties) {
        requestContext.getFlowScope().put("accountRegistrationCaptchaEnabled", properties.isEnabled());
    }
}
