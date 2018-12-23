package org.apereo.cas.adaptors.duo;

/**
 * This is {@link DuoUserAccountAuthStatus}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public enum DuoUserAccountAuthStatus {
    /**
     * The user is known and permitted to authenticate.
     */
    AUTH,
    /**
     * The user is configured to bypass secondary authentication.
     */
    ALLOW,
    /**
     * The user is not permitted to authenticate at this time.
     */
    DENY,
    /**
     * The user is not known to Duo and needs to enroll.
     */
    ENROLL,
    /**
     * Duo service was unavailable.
     */
    UNAVAILABLE
}
