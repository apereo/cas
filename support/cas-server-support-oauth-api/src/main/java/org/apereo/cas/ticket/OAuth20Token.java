package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;

import java.util.Map;
import java.util.Set;

/**
 * OAuth tokens are mostly like service tickets: they deal with authentication and service.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface OAuth20Token extends ServiceTicket, AuthenticationAwareTicket {

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

    /**
     * Gets response type.
     *
     * @return the response type
     */
    OAuth20ResponseTypes getResponseType();

    /**
     * Gets grant type.
     *
     * @return the grant type
     */
    OAuth20GrantTypes getGrantType();

}
