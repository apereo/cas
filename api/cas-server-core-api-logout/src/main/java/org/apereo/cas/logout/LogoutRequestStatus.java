package org.apereo.cas.logout;

/**
 * Define the status of a logout request.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public enum LogoutRequestStatus {
    /** The logout request has not been performed. */
    NOT_ATTEMPTED,
    
    /** The logout request has failed. */
    FAILURE,
    
    /** The logout request is successful. */
    SUCCESS
}
