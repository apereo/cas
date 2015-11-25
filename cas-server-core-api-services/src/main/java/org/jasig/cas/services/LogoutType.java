package org.jasig.cas.services;

/**
 * Enumeration of the logout type.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public enum LogoutType {
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
