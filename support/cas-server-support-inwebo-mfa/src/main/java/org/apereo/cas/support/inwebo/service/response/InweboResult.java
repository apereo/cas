package org.apereo.cas.support.inwebo.service.response;

/**
 * The JSON response result.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
public enum InweboResult {
    /**
     * Success.
     */
    OK,
    /**
     * User’s mobile app does not support push.
     */
    NOPUSH,
    /**
     * User does not have the mobile app.
     */
    NOMA,
    /**
     * User does not exist, or is still pending activation.
     */
    NOLOGIN,
    /**
     * Syntax error in input parameters.
     */
    SN,
    /**
     * Unknown service.
     */
    UNKNOWN_SERVICE,
    /**
     * Error.
     */
    NOK,
    /**
     * Waiting for the push validation.
     */
    WAITING,
    /**
     * The authentication was refused.
     */
    REFUSED,
    /**
     * The authentication timed out.
     */
    TIMEOUT
}
