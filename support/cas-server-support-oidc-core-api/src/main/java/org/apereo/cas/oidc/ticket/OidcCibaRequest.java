package org.apereo.cas.oidc.ticket;

import org.apereo.cas.ticket.AuthenticationAwareTicket;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Set;

/**
 * This is {@link OidcCibaRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface OidcCibaRequest extends AuthenticationAwareTicket {
    /**
     * Ticket prefix.
     */
    String PREFIX = "CIBA";

    /**
     * Get requested scopes requested at the time of issuing this code.
     *
     * @return requested scopes.
     */
    Set<String> getScopes();

    /**
     * Gets client id.
     *
     * @return the client id
     */
    String getClientId();

    /**
     * Gets encoded id.
     *
     * @return the encoded id
     */
    String getEncodedId();

    /**
     * Mark ticket ready.
     */
    void markTicketReady();

    /**
     * Is this ticket and authentication request ready for CAS to
     * start issuing tokens? This is most relevant for poll and ping delivery modes.
     *
     * @return true/false
     */
    boolean isReady();
}
