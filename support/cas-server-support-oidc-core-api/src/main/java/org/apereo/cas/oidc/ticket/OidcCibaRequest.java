package org.apereo.cas.oidc.ticket;

import module java.base;
import org.apereo.cas.ticket.OAuth20Token;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link OidcCibaRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface OidcCibaRequest extends OAuth20Token {
    /**
     * Ticket prefix.
     */
    String PREFIX = "CIBA";
    /**
     * Gets encoded id.
     *
     * @return the encoded id
     */
    String getEncodedId();

    /**
     * Mark ticket ready.
     */
    OidcCibaRequest markTicketReady();

    /**
     * Is this ticket and authentication request ready for CAS to
     * start issuing tokens? This is most relevant for poll and ping delivery modes.
     *
     * @return true/false
     */
    boolean isReady();
}
