package org.apereo.cas.configuration.model.support.delegation;

/**
 * This is {@link DelegationAutoRedirectTypes}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public enum DelegationAutoRedirectTypes {
    /**
     * Redirect on the server side, typically making CAS invisible.
     */
    SERVER,
    /**
     * Redirect on the client side using browser redirects, etc.
     */
    CLIENT,
    /**
     * Do nothing and let the selection take place manually.
     */
    NONE
}
