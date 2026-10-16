package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.ticket.InvalidTicketException;

/**
 * Thrown when a token request refers to an authorization request the end-user has not yet approved,
 * reported to the client as {@code authorization_pending}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class OAuth20AuthorizationPendingException extends InvalidTicketException {
    @Serial
    private static final long serialVersionUID = 5310467723148850231L;

    public OAuth20AuthorizationPendingException(final String ticketId) {
        super(ticketId);
    }
}
