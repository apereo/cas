package org.apereo.cas.services;

/**
 * This is {@link RegisteredServiceLogoutType}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public enum RegisteredServiceLogoutType {
    /**
     * For no SLO.
     */
    NONE,
    /**
     * For back channel SLO.
     */
    BACK_CHANNEL,
    /**
     * For front channel SLO.
     */
    FRONT_CHANNEL
}
