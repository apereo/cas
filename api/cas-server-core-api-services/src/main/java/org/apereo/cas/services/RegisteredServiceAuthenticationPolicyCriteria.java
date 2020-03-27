package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceAuthenticationPolicyCriteria}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceAuthenticationPolicyCriteria extends Serializable {
    /**
     * Gets type of the policy
     * that loosely should translate to {@link org.apereo.cas.authentication.AuthenticationPolicy}
     * implementations.
     *
     * @return the type
     */
    AuthenticationPolicyTypes getType();

    /**
     * Whether all handlers/credentials should be tried
     * depending on the specific policy type.
     *
     * @return the boolean
     */
    boolean isTryAll();

    /**
     * Authentication policy script definition.
     *
     * @return the script
     */
    String getScript();

    /**
     * The authentication policy types.
     */
    enum AuthenticationPolicyTypes {
        /**
         * Do not assign a specific authentication policy
         * to this service and use the default.
         */
        DEFAULT,
        /**
         * Allow any authentication handler to proceed.
         */
        ANY_AUTHENTICATION_HANDLER,
        /**
         * Force all authentication handler to proceed.
         */
        ALL_AUTHENTICATION_HANDLERS,
        /**
         * Ensure authentication failures do not contain
         * a reference to {@link org.apereo.cas.authentication.PreventedException}.
         */
        NOT_PREVENTED,

        /**
         * Script the authentication policy definition using a groovy script.
         */
        GROOVY
    }
}
