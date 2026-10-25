package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.code.OAuth20Code;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
     * Get authorized resource indicators associated with this token.
     *
     * @return resources
     */
    default Set<String> getResources() {
        return Set.of();
    }

    /**
     * Assign the authorized resource indicators of this token.
     * Tokens that do not keep resource indicators ignore the assignment.
     *
     * @param resources the resources
     */
    default void setResources(final Collection<String> resources) {
    }

    /**
     * Get the audience fixed when this token was issued, as by a token exchange.
     * When empty, the audience follows the service definition and the resources of the token.
     *
     * @return the audience
     */
    default Set<String> getAudience() {
        return Set.of();
    }

    /**
     * Fix the audience of this token.
     * Tokens that do not keep an audience ignore the assignment.
     *
     * @param audience the audience
     */
    default void setAudience(final Collection<String> audience) {
    }

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
     * Determines whether this token is an OAuth code.
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
