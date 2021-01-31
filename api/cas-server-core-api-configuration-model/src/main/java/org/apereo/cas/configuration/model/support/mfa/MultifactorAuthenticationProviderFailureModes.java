package org.apereo.cas.configuration.model.support.mfa;

/**
 * This is {@link MultifactorAuthenticationProviderFailureModes}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public enum MultifactorAuthenticationProviderFailureModes {
    /**
     * Disallow MFA, proceed with authentication but don't communicate MFA to the RP.
     */
    OPEN,

    /**
     * Disallow MFA, block with authentication.
     */
    CLOSED,

    /**
     * Disallow MFA, proceed with authentication and communicate MFA to the RP.
     */
    PHANTOM,

    /**
     * Do not check for failure at all.
     */
    NONE,

    /**
     * The default one indicating that no failure mode is set at all.
     */
    UNDEFINED
}
