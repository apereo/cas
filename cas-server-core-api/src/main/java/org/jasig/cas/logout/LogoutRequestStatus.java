package org.jasig.cas.logout;

/**
 * Define the status of a logout request.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public enum LogoutRequestStatus {
    /** the logout request has not been performed. */
    NOT_ATTEMPTED,
    /** the logout request has failed. */
    FAILURE,
    /** the logout request has successed. */
    SUCCESS
}
