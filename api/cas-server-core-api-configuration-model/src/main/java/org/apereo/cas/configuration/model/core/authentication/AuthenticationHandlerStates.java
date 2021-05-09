package org.apereo.cas.configuration.model.core.authentication;

/**
 * This is {@link AuthenticationHandlerStates}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public enum AuthenticationHandlerStates {
    /**
     * Active authentication handler,
     * and is invoked by default automatically to verify credentials globally.
     */
    ACTIVE,
    /**
     * Authentication handler is in a semi-enabled state,
     * waiting to be called only on-demand when explicitly
     * asked for.
     */
    STANDBY
}
