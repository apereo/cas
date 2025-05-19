package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.code.OAuth20Code;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * OAuth tokens are mostly like service tickets: they deal with authentication and service.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface OAuth20Token extends TicketGrantingTicketAwareTicket {

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
    default Map<String, Map<String, Object>> getClaims() {
        return new HashMap<>();
    }

    /**
     * Client id for whom this token was issued.
     *
     * @return client id.
     */
    String getClientId();

    /**
     * Gets response type.
     *
     * @return the response type
     */
    default OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.NONE;
    }

    /**
     * Gets grant type.
     *
     * @return the grant type
     */
    default OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.NONE;
    }

    /**
     * Is this an oauth code?
     *
     * @return true/false
     */
    @JsonIgnore
    default boolean isCode() {
        return this instanceof OAuth20Code;
    }


    /**
     * Assign ticket granting ticket.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     */
    default void assignTicketGrantingTicket(final Ticket ticketGrantingTicket) {}
}
