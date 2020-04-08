package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;

import java.util.Map;
import java.util.Set;

/**
 * OAuth tokens are mostly like service tickets: they deal with authentication and service.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface OAuth20Token extends ServiceTicket {

    /**
     * Get the current authentication.
     *
     * @return the current authentication.
     */
    Authentication getAuthentication();

    /**
     * Get requested scopes requested at the time of issuing this code.
     *
     * @return requested scopes.
     */
    Set<String> getScopes();

    /**
     * Collection of requested claims, if any.
     *
     * @return map of requested claims.
     */
    Map<String, Map<String, Object>> getClaims();

    /**
     * Client id for whom this token was issued.
     * @return client id.
     */
    String getClientId();
}
